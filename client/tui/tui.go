// Package tui contains all ui related code.
package tui

import (
	"log"

	"github.com/gdamore/tcell/v2"

	"github.com/sne11ius/pp/client/ppwsclient"

	"github.com/rivo/tview"
	"github.com/sne11ius/pp/client/data"
)

// TUI contains all required hooks to connect websockets to ui updates
type TUI struct {
	OnUpdate func()
	App      *tview.Application
	Room     *data.Room
	WsClient *ppwsclient.PpWsClient
}

// New creates the UI of the app
func New() *TUI {
	tui := &TUI{
		Room: &data.Room{},
	}

	flex := tview.NewFlex().
		SetDirection(tview.FlexRow)
	tui.App = tview.NewApplication().
		SetRoot(flex, true).
		EnablePaste(true).
		EnableMouse(true).
		SetInputCapture(func(event *tcell.EventKey) *tcell.EventKey {
			if event.Key() == tcell.KeyRune && event.Rune() == 'q' && event.Modifiers() == tcell.ModAlt {
				// Stop the application when Alt+Q is pressed.
				tui.App.Stop()
				return nil
			}
			return event
		})

	tui.OnUpdate = func() {
		tui.App.QueueUpdateDraw(func() {
			flex.Clear()
			var yourUser *data.User
			for _, user := range tui.Room.Users {
				if user.YourUser {
					yourUser = user
				}
			}
			if yourUser == nil {
				tui.App.Stop()
				log.Fatalf("No self user found")
			}
			flex.AddItem(tui.createHeader(yourUser.Username), 3, 1, false)
			usersAndActions := tview.NewFlex().
				SetDirection(tview.FlexColumn)

			usersAndActions.AddItem(tui.createUsersTable(), 0, 100, false)
			usersAndActions.AddItem(tui.createActionsArea(), 0, 100, false)
			flex.AddItem(usersAndActions, 0, 1, false)
		})
	}
	return tui
}

func (tui *TUI) createHeader(username string) *tview.Flex {
	header := tview.NewFlex().
		SetDirection(tview.FlexColumn).
		AddItem(tui.createUsernameInput(username), 0, 10, true).
		AddItem(tui.createQuitButton(), 0, 1, false)
	return header
}

func (tui *TUI) createQuitButton() *tview.Button {
	quitButton := tview.NewButton("[::u]Q[::-]uit").SetSelectedFunc(func() {
		tui.App.Stop()
	})
	return quitButton
}

func (tui *TUI) createUsernameInput(username string) *tview.InputField {
	nameInput := tview.NewInputField().
		SetLabel("Your name: ").
		SetText(username)
	nameInput.SetBorderPadding(1, 0, 1, 1)
	nameInput.SetDoneFunc(func(_ tcell.Key) {
		newName := nameInput.GetText()
		tui.WsClient.SendMessage(data.ChangeName(newName))
	})
	return nameInput
}

func (tui *TUI) createUsersTable() *tview.Flex {
	usernames := ""
	cardValues := ""
	for _, user := range tui.Room.Users {
		usernames += user.Username + "\n"
		if user.CardValue == "" {
			cardValues += "?\n"
		} else {
			cardValues += user.CardValue + "\n"
		}
	}
	usersText := tview.NewTextView().
		SetWordWrap(false)
	usersText.
		SetBorder(true).
		SetTitle("User").
		SetTitleAlign(tview.AlignLeft)

	_, err := usersText.Write([]byte(usernames))
	if err != nil {
		log.Fatalf("Could not write to text: %v", err)
	}

	cardValuesText := tview.NewTextView().
		SetWordWrap(false)
	cardValuesText.
		SetBorder(true).
		SetTitle("Cards").
		SetTitleAlign(tview.AlignLeft)

	_, err = cardValuesText.Write([]byte(cardValues))
	if err != nil {
		log.Fatalf("Could not write to text: %v", err)
	}

	usersTable := tview.NewFlex().
		AddItem(usersText, 0, 3, false).
		AddItem(cardValuesText, 0, 1, false)

	averageText := tview.NewTextView().
		SetWordWrap(false)
	averageText.
		SetBorder(true).
		SetTitle("Average").
		SetTitleAlign(tview.AlignLeft)

	_, err = averageText.Write([]byte(tui.Room.Average))
	if err != nil {
		log.Fatalf("Could not write to text: %v", err)
	}

	tableWithAverage := tview.NewFlex().
		AddItem(nil, 0, 3, false).
		AddItem(averageText, 0, 1, false)

	result := tview.NewFlex().
		SetDirection(tview.FlexRow).
		AddItem(usersTable, 0, 3, false).
		AddItem(tableWithAverage, 3, 0, false)

	return result
}

func (tui *TUI) createActionsArea() *tview.Flex {
	rows := tview.NewFlex().
		SetDirection(tview.FlexRow)
	rows.SetBorder(true)
	rows.SetTitle("Your card")
	var currentRow *tview.Flex
	for i, card := range tui.Room.Deck {
		if i%4 == 0 {
			currentRow = tview.NewFlex().
				SetDirection(tview.FlexColumn)
			rows.AddItem(currentRow, 5, 0, false)
		}
		button := tview.NewButton(card).
			SetSelectedFunc(func() {
				tui.WsClient.SendMessage(data.PlayCard(card))
			})
		currentRow.AddItem(button, 0, 1, true)
	}
	// Fill with blanks so all buttons have the same size
	for i := 0; i < currentRow.GetItemCount()%4; i++ {
		currentRow.AddItem(nil, 0, 1, false)
	}
	revealButton := tview.NewButton("Reveal").SetSelectedFunc(func() {
		tui.WsClient.SendMessage(data.RevealCards())
	})
	rows.AddItem(revealButton, 3, 1, false)
	newRoundButton := tview.NewButton("New Round").SetSelectedFunc(func() {
		tui.WsClient.SendMessage(data.StartNewRound())
	})
	rows.AddItem(newRoundButton, 3, 1, false)
	rows.AddItem(nil, 0, 10, false)
	return rows
}
