// Package tui contains all ui related code.
package tui

import (
	"log"

	"github.com/rivo/tview"
	"github.com/sne11ius/pp/client/data"
)

// TUI contains all required hooks to connect websockets to ui updates
type TUI struct {
	OnUpdate func()
	App      *tview.Application
	Room     *data.Room
}

// New creates the UI of the app
func New() *TUI {
	tui := &TUI{
		Room: &data.Room{},
	}

	flex := tview.NewFlex()
	tui.App = tview.NewApplication().SetRoot(flex, true)
	tui.OnUpdate = func() {
		tui.App.QueueUpdateDraw(func() {
			flex.Clear()
			usernames := ""
			for _, user := range tui.Room.Users {
				usernames += user.Username + "\n"
			}
			text := tview.NewTextView().SetWordWrap(true)
			text.SetBorder(true).SetTitle("╡ Users ╞")
			_, err := text.Write([]byte(usernames))
			if err != nil {
				log.Fatalf("Could not write to text: %v", err)
			}
			flex.AddItem(text, 0, 1, false)
		})
	}
	return tui
}
