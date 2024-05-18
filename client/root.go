package main

import (
	"fmt"
	"io"
	"log"
	"net/http"
	"net/url"
	"os"
	"os/signal"
	"runtime/debug"
	"strings"
	"syscall"

	"github.com/spf13/cobra"
)

// Please do not change the following marker comments as they are used by the "release-please" github action.
// See https://github.com/googleapis/release-please/blob/main/docs/customizing.md#updating-arbitrary-files
// x-release-please-start-version
var version = "0.0.0"

// x-release-please-end

var commit = func() string {
	if info, ok := debug.ReadBuildInfo(); ok {
		for _, setting := range info.Settings {
			if setting.Key == "vcs.revision" {
				return setting.Value[0:7]
			}
		}
	}
	return ""
}()

// Print version and commit hash - including link to github
func printHeader() {
	fmt.Printf("pp version: %s\n", version)
	fmt.Printf("Built from commit %s - see https://github.com/sne11ius/pp/commit/%s\n", commit, commit)
}

// rootCmd represents the base command when called without any subcommands
var rootCmd = &cobra.Command{
	Use:   "pp",
	Short: "A simple planning poker client",
	Long: `pp is a simple TUI planning poker client.
Simply running pp will connect to the default server
and create a new random room.
Running pp --room "my room id" will join the room
with the given id.` +
		"\n\nYour are running version " + version +
		"\nBuilt from commit " + commit + " - see https://github.com/sne11ius/pp/commit/" + commit,
	PersistentPreRun: func(_ *cobra.Command, _ []string) {
		readGlobalConfig()
	},
	Run: func(_ *cobra.Command, _ []string) {
		printHeader()
		roomWebsocketURL := getWsURL()
		ui := NewTUI()
		client := New(roomWebsocketURL, ui.Room, ui.OnUpdate)
		ui.WsClient = client
		// Having an actual ui and websocket client run doesn't work in tests since there is no one to stop the app
		_, isTest := os.LookupEnv("SUB_CMD_FLAGS")
		if !isTest {
			c := make(chan os.Signal, 1)
			signal.Notify(c, os.Interrupt, syscall.SIGINT)
			var err error
			go func() {
				<-c
				err := client.Stop()
				if err != nil { // ignore.coverage
					return // ignore.coverage
				}
				ui.App.Stop() // ignore.coverage
			}()
			go func() {
				err = client.Start()
			}()
			err = ui.App.Run()
			if err != nil { // ignore.coverage
				log.Fatalf("Could not do stuff: %v", err) // ignore.coverage
			}
		} else {
			fmt.Println("Running in test mode - no tui, no ws")
		}
	},
}

func getWsURL() string {
	var roomURL string
	if len(GlobalConfig.RoomID) != 0 {
		roomURL = GlobalConfig.ServerURL + "/rooms/" + url.QueryEscape(GlobalConfig.RoomID)
		roomURL = strings.Replace(roomURL, "http", "ws", 1)
	} else {
		roomURL = GlobalConfig.ServerURL + "/rooms/new"
		client := &http.Client{
			CheckRedirect: func(_ *http.Request, _ []*http.Request) error {
				return http.ErrUseLastResponse
			},
		}
		res, err := client.Get(roomURL) //nolint:bodyclose // its in the defer below
		if err != nil {                 // ignore.coverage
			log.Fatalf("error making http request: %s\n", err) // ignore.coverage
		}
		defer func(Body io.ReadCloser) {
			err := Body.Close()
			if err != nil { // ignore.coverage
				log.Fatalf("error closing body: %s\n", err) // ignore.coverage
			}
		}(res.Body)
		var location string
		if res.StatusCode == http.StatusTemporaryRedirect {
			location = res.Header.Get("Location")
		}
		roomURL = location
	}
	if GlobalConfig.User != "" {
		roomURL = roomURL + "?user=" + url.QueryEscape(GlobalConfig.User)
	}
	return roomURL
}

// Execute runs the main program by invoking the rootCmd. Any error will result in an os.Exit(1)
func Execute() {
	err := rootCmd.Execute()
	if err != nil {
		os.Exit(1)
	}
	// We need to explicitly exit here to keep the tests for main() from spinning out of control
	os.Exit(0)
}

func init() {
	if err := configInit(); err != nil { // ignore.coverage
		panic(err.Error()) // ignore.coverage
	}
}
