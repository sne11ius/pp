package main

import (
	"encoding/json"
	"fmt"
	"strings"
)

// UserType of a user can be either "Participant" for active players, or "Spectator" for ... spectators
type UserType uint8

// from https://rotational.io/blog/marshaling-go-enums-to-and-from-json/
const (
	Participant UserType = iota + 1
	Spectator
)

var usertypeValue = map[string]uint8{
	"participant": 1,
	"spectator":   2,
}

func parseUserType(s string) (UserType, error) {
	s = strings.TrimSpace(strings.ToLower(s))
	value, ok := usertypeValue[s]
	if !ok {
		return UserType(0), fmt.Errorf("%q is not a valid user type", s)
	}
	return UserType(value), nil
}

// UnmarshalJSON parses JSON into a UserType
func (s *UserType) UnmarshalJSON(data []byte) (err error) {
	var value string
	if err := json.Unmarshal(data, &value); err != nil {
		return err
	}
	if *s, err = parseUserType(value); err != nil {
		return err
	}
	return nil
}

// Card represents data to send to the api if a user wants to play a card
type Card struct {
	RequestType string `json:"requestType"`
	CardValue   string `json:"cardValue"`
}

// PlayCard is a helper function to create Card structs
func PlayCard(cardValue string) Card {
	return Card{
		RequestType: "PlayCard",
		CardValue:   cardValue,
	}
}

// Name represents data to send to the api if a user wants to change its name
type Name struct {
	RequestType string `json:"requestType"`
	Name        string `json:"name"`
}

// ChangeName is a helper function to create Name structs
func ChangeName(name string) Name {
	return Name{
		RequestType: "ChangeName",
		Name:        name,
	}
}

// NoDetailsAction represents data to send to the api if a user wants to take an action
// that doesn't require any parameters
type NoDetailsAction struct {
	RequestType string `json:"requestType"`
}

// RevealCards creates a 'reveal cards' message to send to the api
func RevealCards() NoDetailsAction {
	return NoDetailsAction{
		RequestType: "RevealCards",
	}
}

// StartNewRound creates a 'start new round' message to send to the api
func StartNewRound() NoDetailsAction {
	return NoDetailsAction{
		RequestType: "StartNewRound",
	}
}

// User is participant in a planning poker
type User struct {
	Username  string   `json:"username"`
	UserType  UserType `json:"userType"`
	YourUser  bool     `json:"yourUser"`
	CardValue string   `json:"cardValue"`
}

// Room is a planning poker room, including all participants and a state
type Room struct {
	RoomID  string   `json:"roomId"`
	Deck    []string `json:"deck"`
	Users   []*User  `json:"users"`
	Average string   `json:"average"`
}
