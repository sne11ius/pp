// Package cmd contains all subcommands. Because this is a TUI app, currently (and in the foreseeable future) there is
// only one "root" command that starts the TUI.
package cmd

import (
	"fmt"
	"os"
	"runtime/debug"

	"github.com/spf13/cobra"
)

// Please do not change the following marker comments as they are used by the "release-please" github action.
// See https://github.com/googleapis/release-please/blob/main/docs/customizing.md#updating-arbitrary-files
// x-release-please-start-version
var version = "0.0.1"

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
	Run: func(_ *cobra.Command, _ []string) {
		printHeader()
		if len(roomID) != 0 {
			fmt.Printf("Will join room with id: %s\n", roomID)
		} else {
			fmt.Println("Will join a random room")
		}
	},
}

var roomID string

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
	rootCmd.PersistentFlags().StringVarP(&roomID, "room", "r", "", "room id")
}
