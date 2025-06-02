# API

pp (Planning Poker) is a web application that allows teams to estimate work
items using the Planning Poker technique. It uses WebSockets for real-time
communication between clients and the server, with JSON as the message format.

## Path

You can open a ws connection at
ws(s)://$YOUR_HOST/rooms/{roomId}?user={your_username}&usertype={user_type}
where

- `{roomId}` is the (optional) id of the room a user wants to join. It can be
  any string. If the room does not exist, it will be created
- `user` is an optional parameter to choose an initial username. If no username
  is provided, the server will assign a randome one. The username can be changed
  any time by sending a `ChangeName` request (see below)
- `usertype` is an optional parameter to specify the type of user. Possible
   values are `PARTICIPANT` (can participate in voting) and `SPECTATOR` (cannot
  participate in voting). If not provided, it defaults to `SPECTATOR`

## Server sent message

All server state will be broadcast to the clients whenever it changes. The
server will send only one type of JSON message to clients. The message contains
all information the user is allowed to see.

### Error Handling

The server does not send specific error messages to clients. In case of errors
(such as invalid JSON), the server might disconnect the client's WebSocket
connection. Clients should be prepared to handle disconnections and implement
reconnection logic if needed.

### Schema for the server state message

<!-- markdownlint-disable -->
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
      "description": "List of available card values. Default values are: \"1\", \"2\", \"3\", \"5\", \"8\", \"13\", \"☕\"",
      "type": "array",
      "items": {
        "type": "string"
      }
    },
    "gamePhase": {
      "description": "Phase the game is currently in. Default is 'PLAYING'",
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
      "description": "Average card value played by the users. Shows '?' if not in gamePhase 'CARDS_REVEALED'. If all participants played the same card, that value is used. Otherwise, it's the numerical average of all card values that can be converted to integers, formatted with one decimal place. If some values can't be converted to integers (like '☕'), '(?)' is appended to the average.",
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
          "description": "Level of this message. CHAT is for user chat messages, INFO is for general information, ERROR is for server-side errors, and CLIENT_BROADCAST is for messages broadcast by clients",
          "type": "string",
          "enum": [
            "CHAT",
            "INFO",
            "ERROR",
            "CLIENT_BROADCAST"
          ]
        },
        "message": {
          "description": "Content of this log entry",
          "type": "string"
        }
      },
      "required": ["level", "message"]
    }
  }
}
```
<!-- markdownlint-restore -->

### Example

Here's an example of a server-sent message:

```json
{
  "roomId": "team-estimation",
  "version": 42,
  "deck": ["1", "2", "3", "5", "8", "13", "☕"],
  "gamePhase": "CARDS_REVEALED",
  "users": [
    {
      "username": "John",
      "userType": "PARTICIPANT",
      "yourUser": true,
      "cardValue": "5"
    },
    {
      "username": "Alice",
      "userType": "PARTICIPANT",
      "yourUser": false,
      "cardValue": "8"
    },
    {
      "username": "Bob",
      "userType": "SPECTATOR",
      "yourUser": false,
      "cardValue": null
    }
  ],
  "average": "6.5",
  "log": [
    {
      "level": "INFO",
      "message": "John joined"
    },
    {
      "level": "INFO",
      "message": "Alice joined"
    },
    {
      "level": "INFO",
      "message": "Bob joined"
    },
    {
      "level": "CHAT",
      "message": "[John]: Hello everyone!"
    },
    {
      "level": "INFO",
      "message": "John revealed the cards"
    }
  ]
}
```

## Client messages

The following schema describes all messages a client can send to the API:

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://pp.discordia.network/pp-client.schema.json",
  "title": "UserRequest",
  "description": "Base schema for all client requests",
  "type": "object",
  "required": ["requestType"],
  "properties": {
    "requestType": {
      "description": "Type of the request",
      "type": "string",
      "enum": ["PlayCard", "ChangeName", "ChatMessage", "RevealCards", "StartNewRound", "ClientBroadcast"]
    }
  },
  "allOf": [
    {
      "if": {
        "properties": { "requestType": { "const": "PlayCard" } }
      },
      "then": {
        "description": "Request to play a card",
        "properties": {
          "cardValue": {
            "description": "The card value to play, must be one of the values in the deck",
            "type": ["string", "null"]
          }
        }
      }
    },
    {
      "if": {
        "properties": { "requestType": { "const": "ChangeName" } }
      },
      "then": {
        "description": "Request to change the user's name",
        "required": ["name"],
        "properties": {
          "name": {
            "description": "The new name for the user",
            "type": "string"
          }
        }
      }
    },
    {
      "if": {
        "properties": { "requestType": { "const": "ChatMessage" } }
      },
      "then": {
        "description": "Request to send a chat message",
        "required": ["message"],
        "properties": {
          "message": {
            "description": "The chat message content",
            "type": "string"
          }
        }
      }
    },
    {
      "if": {
        "properties": { "requestType": { "const": "RevealCards" } }
      },
      "then": {
        "description": "Request to reveal all played cards"
      }
    },
    {
      "if": {
        "properties": { "requestType": { "const": "StartNewRound" } }
      },
      "then": {
        "description": "Request to start a new round"
      }
    },
    {
      "if": {
        "properties": { "requestType": { "const": "ClientBroadcast" } }
      },
      "then": {
        "description": "Request to broadcast a message to other clients",
        "required": ["payload"],
        "properties": {
          "payload": {
            "description": "The broadcast payload (max 10,000 characters)",
            "type": "string",
            "minLength": 1,
            "maxLength": 10000
          }
        }
      }
    }
  ]
}
```

### Examples

#### Play a card

```json
{
  "requestType": "PlayCard",
  "cardValue": "5"
}
```

#### Change name

```json
{
  "requestType": "ChangeName",
  "name": "New Username"
}
```

#### Send a chat message

```json
{
  "requestType": "ChatMessage",
  "message": "Hello everyone!"
}
```

#### Reveal cards

```json
{
  "requestType": "RevealCards"
}
```

#### Start a new round

```json
{
  "requestType": "StartNewRound"
}
```

#### Broadcast a message

```json
{
  "requestType": "ClientBroadcast",
  "payload": "Custom client data"
}
```
