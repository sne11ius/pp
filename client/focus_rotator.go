package main

import (
	"github.com/gdamore/tcell/v2"
	"github.com/rivo/tview"
)

type inputCapturer interface {
	tview.Primitive
	SetInputCapture(capture func(event *tcell.EventKey) *tcell.EventKey) *tview.Box
	GetInputCapture() func(event *tcell.EventKey) *tcell.EventKey
}

type focusRotator struct {
	app          *tview.Application
	currentIndex int
	input        []inputCapturer
}

func newFocusRotator(app *tview.Application) *focusRotator {
	return &focusRotator{
		app:          app,
		currentIndex: 0,
		input:        make([]inputCapturer, 0),
	}
}

func (ir *focusRotator) Clear() {
	ir.input = make([]inputCapturer, 0)
	ir.currentIndex = 0
}

func (ir *focusRotator) AddAll(items []inputCapturer) {
	ir.input = append(ir.input, items...)
	ir.setupInputCaptures()
}

func (ir *focusRotator) SetFocusIndex(index int) {
	ir.currentIndex = index % len(ir.input)
	ir.app.SetFocus(ir.input[ir.currentIndex])
}

func (ir *focusRotator) setupInputCaptures() {
	for index, input := range ir.input {
		size := len(ir.input)
		nextIndex := (index + 1) % size
		prevIndex := (index - 1 + size) % size
		originalHandler := input.GetInputCapture()
		input.SetInputCapture(func(event *tcell.EventKey) *tcell.EventKey {
			result := event
			if originalHandler != nil {
				result = originalHandler(event)
			}
			if event.Key() == tcell.KeyTab {
				ir.app.SetFocus(ir.input[nextIndex])
				ir.currentIndex = nextIndex
				return result
			}
			if event.Key() == tcell.KeyBacktab {
				ir.app.SetFocus(ir.input[prevIndex])
				ir.currentIndex = prevIndex
				return result
			}
			return event
		})
	}
}
