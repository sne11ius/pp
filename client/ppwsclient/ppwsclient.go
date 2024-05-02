// Package ppwsclient contains the actual planning poker websocket client.
package ppwsclient

import (
	"log"
	"os"

	"github.com/gorilla/websocket"
)

// PpWsClient is the pp ws client.
type PpWsClient struct {
	wsURL      string
	room       interface{}
	onUpdate   func()
	connection *websocket.Conn
}

// New creates a new PpWsClient with a given ws URL.
func New(wsURL string, room interface{}, onUpdate func()) *PpWsClient {
	return &PpWsClient{wsURL, room, onUpdate, nil}
}

// Stop stops the pp client.
func (client *PpWsClient) Stop() error {
	err := client.connection.WriteMessage(
		websocket.CloseMessage, websocket.FormatCloseMessage(websocket.CloseNormalClosure, ""))
	if err != nil {
		return err
	}
	err = client.connection.Close()
	return err
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
	client.connection = c
	for {
		err = c.ReadJSON(client.room)
		client.onUpdate()
		if err != nil {
			log.Println("read error:", err)
			return err
		}
	}
}
