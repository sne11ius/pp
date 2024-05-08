// Package ppwsclient contains the actual planning poker websocket client.
package ppwsclient

import (
	"fmt"
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
	channel    chan interface{}
}

// New creates a new PpWsClient with a given ws URL.
func New(wsURL string, room interface{}, onUpdate func()) *PpWsClient {
	return &PpWsClient{wsURL, room, onUpdate, nil, nil}
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
	_, isTest := os.LookupEnv("SUB_CMD_FLAGS")
	if isTest {
		fmt.Println("Not actually running ws client in test mode")
		return nil
	}
	client.connection = c
	client.channel = make(chan interface{})
	go func() {
		defer close(client.channel)
		for msg := range client.channel {
			err = c.WriteJSON(msg)
			if err != nil {
				return
			}
		}
	}()
	for {
		err = c.ReadJSON(client.room)
		client.onUpdate()
		if err != nil {
			log.Println("read error:", err)
			return err
		}
	}
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

// SendMessage sends a message to the pp api
func (client *PpWsClient) SendMessage(msg interface{}) {
	client.channel <- msg
}
