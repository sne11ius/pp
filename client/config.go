package main

import (
	"fmt"
	"os"
	"path/filepath"

	"github.com/fatih/structs"
	"github.com/spf13/viper"
)

// GlobalConfig contains the global configuration
var GlobalConfig *config

func readGlobalConfig() {
	// Priority of configuration options
	// 1: CLI Parameters
	// 2: environment
	// 3: config.yaml
	// 4: defaults
	config, err := readConfig()
	if err != nil { // ignore.coverage
		panic(err.Error()) // ignore.coverage
	}

	// Set config object for main package
	GlobalConfig = config
}

// configInit must be called from the packages' init() func
func configInit() error {
	cliFlags()
	return bindFlagsAndEnv()
}

type config struct {
	ServerURL string `mapstructure:"server" structs:"server" env:"SERVER"`
	RoomID    string `mapstructure:"room" structs:"room" env:"ROOM"`
	Name      string `mapstructure:"name" structs:"name" env:"NAME"`
}

var defaultConfig = config{
	ServerURL: "https://pp.discordia.network",
	RoomID:    "",
	Name:      "",
}

func cliFlags() {
	rootCmd.PersistentFlags().StringP("server", "s", defaultConfig.ServerURL, "server url")
	rootCmd.PersistentFlags().StringP("room", "r", defaultConfig.RoomID, "room id")
	rootCmd.PersistentFlags().StringP("name", "n", defaultConfig.Name, "username")
}

// bindFlagsAndEnv will assign the environment variables to the cli parameters
func bindFlagsAndEnv() (err error) {
	for _, field := range structs.Fields(&config{}) {
		// Get the struct tag values
		key := field.Tag("structs")
		env := field.Tag("env")

		// Bind cobra flags to viper
		err = viper.BindPFlag(key, rootCmd.PersistentFlags().Lookup(key))
		if err != nil {
			return err
		}
		err = viper.BindEnv(key, env)
		if err != nil {
			return err
		}
	}
	return nil
}

// readConfig a helper to read default from a default config object.
func readConfig() (*config, error) {
	// Create a map of the default config
	defaultsAsMap := structs.Map(defaultConfig)

	// Set defaults
	for key, value := range defaultsAsMap {
		viper.SetDefault(key, value)
	}

	// Read config from file
	viper.SetConfigName("pp.config")
	viper.SetConfigType("yaml")
	viper.AddConfigPath(".")
	home, err := os.UserHomeDir()
	if err != nil { // ignore.coverage
		return nil, err // ignore.coverage
	}
	viper.AddConfigPath(home)
	viper.AddConfigPath(filepath.Join(home, ".config"))
	if err := viper.ReadInConfig(); err == nil { // ignore.coverage
		fmt.Println("Using config file:", viper.ConfigFileUsed()) // ignore.coverage
	}

	// Unmarshal config into struct
	c := &config{}
	err = viper.Unmarshal(c)
	if err != nil { // ignore.coverage
		return nil, err // ignore.coverage
	}
	if c.Name == "" {
		if envValue, exists := os.LookupEnv("USER"); exists && c.Name == "" {
			c.Name = envValue
		}
	}
	return c, nil
}
