package main

import (
	"fmt"
	"log"
	"os"
	"os/exec"
	"strings"
	"testing"
)

// This way of testing the main func heavily inspired by this stackoverflow answer:
// https://stackoverflow.com/a/67945462

const (
	testTmp = "tmp"
	// SubCmdFlags space separated list of command line flags.
	SubCmdFlags = "SUB_CMD_FLAGS"
)

func TestMain(m *testing.M) {
	err := os.RemoveAll(testTmp)
	if err != nil {
		log.Fatalf("Could not remove test tmp directory: %v", err)
	}
	// Set up a temporary dir for generated files
	err = os.Mkdir(testTmp, os.ModeDir)
	if err != nil {
		log.Fatalf("Could not create test tmp directory: %v", err)
	}
	// Run all tests
	exitCode := m.Run()
	// Clean up
	os.Exit(exitCode)
}

func TestCallingMain(tester *testing.T) {
	// This was adapted from https://golang.org/src/flag/flag_test.go; line 596-657 at the time.
	// This is called recursively, because we will have this test call itself
	// in a sub-command with the environment variable `GO_CHILD_FLAG` set.
	// Note that a call to `main()` MUST exit or you'll spin out of control.
	if os.Getenv(SubCmdFlags) != "" {
		// We're in the test binary, so test flags are set, lets reset it so that only the program is set
		//  and whatever flags we want.
		args := strings.Split(os.Getenv(SubCmdFlags), " ")
		os.Args = append([]string{os.Args[0]}, args...)

		// Anything you print here will be passed back to the cmd.Stderr and
		// cmd.Stdout below, for example:
		fmt.Printf("os args = %v\n", os.Args)

		main()
	}

	tests := []struct {
		name string
		want int
		args []string
	}{
		{"roomFlagShort", 0, []string{"-r", "my-shortflag-id"}},
		{"roomFlagLong", 0, []string{"--room", "my-id"}},
		{"helpFlag", 0, []string{"-h"}},
		{"unknownFlagShort", 1, []string{"-u"}},
		{"unknownFlagLong", 1, []string{"--unknown", "flag"}},
	}

	for _, test := range tests {
		tester.Run(test.name, func(t *testing.T) {
			cmd := runMain(tester.Name(), test.args)

			out, sce := cmd.CombinedOutput()

			// get exit code.
			got := cmd.ProcessState.ExitCode()

			if got != test.want {
				t.Errorf("got %q, want %q", got, test.want)
			}

			if sce != nil {
				fmt.Printf("\nBEGIN sub-command\nstdout:\n%v\n\n", string(out))
				fmt.Printf("stderr:\n%v\n", sce.Error())
				fmt.Print("\nEND sub-command\n\n")
			}
		})
	}
}

func runMain(testFunc string, args []string) *exec.Cmd {
	// Run the test binary and tell it to run just this test with environment set.
	cmd := exec.Command(os.Args[0], "-test.run", testFunc) // #nosec G204 - this is not actually user input

	subEnvVar := SubCmdFlags + "=" + strings.Join(args, " ")
	cmd.Env = append(os.Environ(), subEnvVar)

	return cmd
}
