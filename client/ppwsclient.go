package main

import (
	"fmt"
	"log"
	"os"

	"github.com/gorilla/websocket"
)

// PpWsClient is the pp ws client.
type PpWsClient struct {
	wsURL      string
	room       *Room
	onUpdate   func()
	connection *websocket.Conn
	channel    chan interface{}
}

// New creates a new PpWsClient with a given ws URL.
func New(wsURL string, room *Room, onUpdate func()) *PpWsClient {
	return &PpWsClient{wsURL, room, onUpdate, nil, nil}
}

// Start runs the pp client.
func (client *PpWsClient) Start() error {
	c, r, err := websocket.DefaultDialer.Dial(client.wsURL, nil)
	if err != nil { // ignore.coverage
		return err // ignore.coverage
	}
	defer c.Close()
	defer r.Body.Close()
	_, isTest := os.LookupEnv("SUB_CMD_FLAGS")
	if isTest { // ignore.coverage
		fmt.Println("Not actually running ws client in test mode") // ignore.coverage
		return nil                                                 // ignore.coverage
	}
	client.connection = c
	client.channel = make(chan interface{})
	go func() {
		defer close(client.channel)
		for msg := range client.channel {
			err = c.WriteJSON(msg)
			if err != nil { // ignore.coverage
				log.Fatal(err) // ignore.coverage
			}
		}
	}()
	for {
		err = c.ReadJSON(client.room)
		client.onUpdate()
		if err != nil { // ignore.coverage
			log.Panic(err) // ignore.coverage
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
