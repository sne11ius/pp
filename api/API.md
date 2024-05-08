# Server API

pp-API uses JSON messages exchanged with websockets

## Path

You can open a ws connection at
ws(s)://$YOUR_HOST/rooms/{roomId}?user={your_username} where

- `{roomId}` is the id of the room a user wants to join. It can by any string.
  If the room does not exist, it will becreated
- `user` is an optional parameter to choose an initial username. The username
  can be changed any time by sending a `ChangeName` request (see below)

## Server sent message

The server will send only one type of JSON message to clients. The message
contains all information the user is allowed to to see.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://pp.discordia.network/pp.schema.json",
  "title": "Room",
  "description": "A planning poker room",
  "type": "object",
  "properties": {
    "roomId": {
      "description": "The unique identifier for a room. Can be any string",
      "type": "string"
    },
    "deck": {
      "description": "List of available card values",
      "type": "array",
      "items": {
        "type": "string"
      }
    },
    "gamePhase": {
      "description": "Phase the game is currently in",
      "type": "string",
      "enum": [
        "PLAYING",
        "CARDS_REVEALED"
      ]
    },
    "users": {
      "description": "List of users in this room",
      "type": "array",
      "items": {
        "$ref": "#/$defs/user"
      }
    },
    "average": {
      "description": "Average card value played by the users. Will display dummy content if not in gamePhase 'CARDS_REVEALED'",
      "type": "string"
    },
    "log": {
      "description": "All log messages in this room",
      "type": "array",
      "items": {
        "$ref": "#/$defs/logEntry"
      }
    }
  },
  "$defs": {
    "user": {
      "type": "object",
      "properties": {
        "username": {
          "description": "Display name of the user",
          "type": "string"
        },
        "userType": {
          "description": "Type of this user",
          "type": "string",
          "enum": [
            "PARTICIPANT",
            "SPECTATOR"
          ]
        },
        "yourUser": {
          "description": "Indicates if this user is 'you'",
          "type": "boolean"
        },
        "cardValue": {
          "description": "Value of the card played by this user, will be redacted for other users if not in CARDS_REVEALED game phase",
          "type": "string"
        }
      }
    },
    "logEntry": {
      "type": "object",
      "properties": {
        "level": {
          "description": "Level of this message",
          "type": "string",
          "enum": [
            "CHAT",
            "INFO"
          ]
        },
        "message": {
          "description": "Content of this log entry",
          "type": "string"
        }
      }
    }
  }
}
```

## Client messages

The following schema describes all messages a client can send to the API

```json
{
  "cannot": "do this no, maybe later"
}
```
