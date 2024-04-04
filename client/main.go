/*
pp is a planning poker TUI client

Currently, the implementation does nothing since it's merely a tool to test our CI setup.
*/
package main

import (
	"fmt"
	"log"
	"path/filepath"

	"github.com/mitchellh/go-homedir"
	"github.com/spf13/viper"
)

// Does nothing productive but reads a configuration file `pp.yaml` and outputs a single setting to stdout.
func main() {
	viper.SetConfigName("pp")
	viper.SetConfigType("yaml")
	viper.AddConfigPath(".")
	viper.AddConfigPath("$HOME/.config/pp.yaml")
	viper.AutomaticEnv()
	if err := viper.ReadInConfig(); err != nil {
		home, err := homedir.Dir()
		if err != nil {
			log.Fatal("Error: cannot home directory")
		}
		fullConfigName := filepath.Join(home, ".config", "pp.yaml")
		localConfigName := "pp.yaml"
		log.Printf("No/invalid config found at %s or %s\n", localConfigName, fullConfigName)
	}
	if viper.IsSet("server.url") {
		fmt.Printf("Using server URL: %v\n", viper.GetString("server.url"))
	} else {
		fmt.Println("No server URL")
	}
}
