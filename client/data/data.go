// Package data contains structs that are make up the actual stuff we care about. Like a player or a room.
package data

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

// User is participant in a planning poker
type User struct {
	Username string   `json:"username"`
	UserType UserType `json:"userType"`
}

// Room is a planning poker room, including all participants and a state
type Room struct {
	RoomID string  `json:"roomId"`
	Users  []*User `json:"users"`
}
