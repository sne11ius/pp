package main

import (
	"log"

	"github.com/gdamore/tcell/v2"

	"github.com/rivo/tview"
)

// TUI contains all required hooks to connect websockets to ui updates
type TUI struct {
	OnUpdate     func()
	App          *tview.Application
	focusRotator *focusRotator
	Room         *Room
	WsClient     *PpWsClient
}

// NewTUI creates the UI of the app
func NewTUI() *TUI {
	tui := &TUI{
		Room: &Room{},
	}

	root := tview.NewFlex().
		SetDirection(tview.FlexRow)
	tui.App = tview.NewApplication().
		SetRoot(root, true).
		EnablePaste(true).
		EnableMouse(true)

	tui.focusRotator = newFocusRotator(tui.App)

	isFirstDraw := true

	tui.OnUpdate = func() {
		oldIndex := tui.focusRotator.currentIndex
		tui.focusRotator.Clear()
		tui.App.QueueUpdateDraw(func() {
			root.Clear()
			header, inputs := tui.createHeader()
			tui.focusRotator.AddAll(inputs)
			root.AddItem(header, 3, 1, true)
			usersAndActions := tview.NewFlex().
				SetDirection(tview.FlexColumn)

			usersAndActions.AddItem(tui.createUsersTable(), 0, 100, false)
			actionsArea, inputs := tui.createActionsArea()
			tui.focusRotator.AddAll(inputs)
			if isFirstDraw {
				tui.focusRotator.SetFocusIndex(1) // Should be the first vote button
				isFirstDraw = false
			} else {
				tui.focusRotator.SetFocusIndex(oldIndex)
			}
			usersAndActions.AddItem(actionsArea, 0, 100, false)
			root.AddItem(usersAndActions, 0, 1, false)
		})
	}
	return tui
}

func (tui *TUI) createHeader() (*tview.Flex, []inputCapturer) {
	quitButton := tui.createQuitButton()
	title := tview.NewTextView().
		SetDynamicColors(true)
	title.SetBorder(true)
	title.SetText(tui.Room.RoomID)

	header := tview.NewFlex().
		SetDirection(tview.FlexColumn).
		AddItem(title, 0, 10, true).
		AddItem(quitButton, 0, 1, false)
	return header, []inputCapturer{quitButton}
}

func (tui *TUI) createQuitButton() *tview.Button {
	stopFunc := func() {
		tui.App.Stop()
	}
	quitButton := tview.NewButton("Quit").SetSelectedFunc(stopFunc)
	quitButton.SetInputCapture(func(event *tcell.EventKey) *tcell.EventKey {
		if event.Rune() == ' ' {
			stopFunc()
			return nil
		}
		return event
	})
	return quitButton
}

func (tui *TUI) createUsersTable() *tview.Flex {
	usernames := ""
	cardValues := ""
	for _, user := range tui.Room.Users {
		usernames += user.Username
		if user.YourUser {
			usernames += " (*)"
		}
		usernames += "\n"
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
	if err != nil { // ignore.coverage
		log.Fatalf("Could not write to text: %v", err) // ignore.coverage
	}

	cardValuesText := tview.NewTextView().
		SetWordWrap(false)
	cardValuesText.
		SetBorder(true).
		SetTitle("Cards").
		SetTitleAlign(tview.AlignLeft)

	_, err = cardValuesText.Write([]byte(cardValues))
	if err != nil { // ignore.coverage
		log.Fatalf("Could not write to text: %v", err) // ignore.coverage
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
	if err != nil { // ignore.coverage
		log.Fatalf("Could not write to text: %v", err) // ignore.coverage
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

func (tui *TUI) createActionsArea() (*tview.Flex, []inputCapturer) {
	rows := tview.NewFlex().
		SetDirection(tview.FlexRow)
	rows.SetBorder(true)
	rows.SetTitle("Your card")
	var currentRow *tview.Flex
	inputs := make([]inputCapturer, 0)
	for i, card := range tui.Room.Deck {
		if i%4 == 0 {
			currentRow = tview.NewFlex().
				SetDirection(tview.FlexColumn)
			rows.AddItem(currentRow, 5, 0, false)
		}
		button := tview.NewButton(card).
			SetSelectedFunc(func() {
				tui.WsClient.SendMessage(PlayCard(card))
			})
		inputs = append(inputs, button)
		currentRow.AddItem(button, 0, 1, true)
	}
	// Fill with blanks so all buttons have the same size
	for i := 0; i < currentRow.GetItemCount()%4; i++ {
		currentRow.AddItem(nil, 0, 1, false)
	}
	revealButton := tview.NewButton("Reveal").SetSelectedFunc(func() {
		tui.WsClient.SendMessage(RevealCards())
	})
	inputs = append(inputs, revealButton)
	rows.AddItem(revealButton, 3, 1, false)
	newRoundButton := tview.NewButton("New Round").SetSelectedFunc(func() {
		tui.WsClient.SendMessage(StartNewRound())
	})
	inputs = append(inputs, newRoundButton)
	rows.AddItem(newRoundButton, 3, 1, false)
	rows.AddItem(nil, 0, 10, false)
	return rows, inputs
}
