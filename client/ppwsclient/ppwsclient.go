// Package ppwsclient contains the actual planning poker websocket client.
package ppwsclient

import (
	"fmt"
	"log"
	"os"
	"os/signal"

	"github.com/gorilla/websocket"
)

// PpWsClient is the pp ws client.
type PpWsClient struct {
	wsURL string
}

// New creates a new PpWsClient with a given ws URL.
func New(wsURL string) *PpWsClient {
	return &PpWsClient{wsURL}
}

// Start runs the pp client.
func (client *PpWsClient) Start() error {
	c, _, err := websocket.DefaultDialer.Dial(client.wsURL, nil)
	if err != nil {
		return err
	}
	defer func(c *websocket.Conn) {
		err = c.Close()
	}(c)
	if os.Getenv("SUB_CMD_FLAGS") != "" {
		return nil
	}
	done := make(chan struct{})
	go func() {
		defer close(done)
		for {
			_, message, err := c.ReadMessage()
			if err != nil {
				log.Println("read error:", err)
				return
			}
			fmt.Printf("%s\n", message)
		}
	}()

	interrupt := make(chan os.Signal, 1)
	signal.Notify(interrupt, os.Interrupt)
	for {
		select {
		case <-done:
			return nil
		case <-interrupt:
			// Cleanly close the connection by sending a close message and then
			// waiting (with timeout) for the server to close the connection.
			err := c.WriteMessage(websocket.CloseMessage, websocket.FormatCloseMessage(websocket.CloseNormalClosure, ""))
			if err != nil {
				return err
			}
		}
	}
}
