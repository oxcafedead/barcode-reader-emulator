package main

import (
	"flag"
	"os"
	"time"

	"github.com/gen2brain/beeep"
	"github.com/lxn/walk"
	. "github.com/lxn/walk/declarative"
)

func main() {
	// Define command-line flags
	valueFlag := flag.String("value", "", "The barcode value to emulate typing")
	delayFlag := flag.Int("delay", 50, "Input key delay in milliseconds")
	sendEnterFlag := flag.Bool("sendEnter", false, "Send ENTER key at the end of the input")
	beepFlag := flag.Bool("beep", true, "Enable or disable beep sound")
	flag.Parse()

	// If command-line flags are provided, handle them and exit
	if *valueFlag != "" {
		if *beepFlag {
			beeep.Beep(beeep.DefaultFreq*2, beeep.DefaultDuration/4) // emulate scanner sound :)
		}
		EmulateTyping(*valueFlag, *delayFlag, *sendEnterFlag)
		os.Exit(0)
	}

	// GUI logic starts here
	var value *walk.TextEdit
	var hotkeyLabel *walk.Label
	var slider *walk.Slider
	var mainWnd *walk.MainWindow
	var sendEnter *walk.CheckBox

	defaultBinding, err := SetNewBinding([]int{0, 1}, 10)
	if err != nil {
		walk.MsgBox(nil, "Failed to bind a default hotkey", err.Error()+"\nTry to change the hotkey to something another", walk.MsgBoxApplModal)
	}

	go func() {
		ListenForHotkey(func() {
			time.Sleep(time.Millisecond * 150)

			valueToWrite := value.Text()
			if len(valueToWrite) == 0 {
				return
			}
			beeep.Beep(beeep.DefaultFreq*2, beeep.DefaultDuration/4) // emulate scanner sound :)

			EmulateTyping(valueToWrite, slider.Value(), sendEnter.CheckState() == walk.CheckChecked)
		})
	}() // listen in bg, non-blocking the main GUI thread

	ico, err := walk.NewIconFromResourceId(2) // icon id from manifest
	if err != nil {
		panic("could not load icon")
	}

	MainWindow{
		AssignTo: &mainWnd,
		Title:    "Barcode Reader Emulator",
		Size:     Size{Width: 350, Height: 135},
		Icon:     ico,
		Layout:   VBox{},
		Children: []Widget{
			HSplitter{
				Children: []Widget{
					Label{Text: "Value"},
					TextEdit{
						AssignTo:      &value,
						Text:          "test",
						MaxLength:     128,
						CompactHeight: true,
						VScroll:       true,
					},
					PushButton{
						Text:    "Scan from the screen",
						MaxSize: Size{Width: 40, Height: 20},
						OnClicked: func() {
							val, err := ScanScreenBarcode()
							if err != nil {
								walk.MsgBox(mainWnd, "Failed to scan", "Could not find or parse a barcode. Try to zoom in a bit or choose another one.\nParser error: "+err.Error(), walk.MsgBoxApplModal)
							} else {
								value.SetText(val)
							}
						},
					},
				},
			},
			HSplitter{
				Children: []Widget{
					Label{Text: "Hotkey"},
					Label{AssignTo: &hotkeyLabel, Text: defaultBinding},
					PushButton{
						Text:    "Change",
						MaxSize: Size{Width: 40, Height: 20},
						OnClicked: func() {
							var lb *walk.ListBox
							var cb *walk.ComboBox
							var dlg *walk.Dialog
							modLabels := make([]string, len(AllModifiers))
							for i := 0; i < len(AllModifiers); i++ {
								modLabels[i] = modifierToString(AllModifiers[i])
							}
							hotkeyLabels := make([]string, len(AllHotkeys))
							for i := 0; i < len(AllHotkeys); i++ {
								hotkeyLabels[i] = hotkeyToString(AllHotkeys[i])
							}

							Dialog{
								AssignTo: &dlg,
								Title:    "Select a new hotkey",
								Layout:   HBox{},
								MinSize:  Size{Width: 250, Height: 110},
								Children: []Widget{
									ListBox{
										AssignTo:       &lb,
										MultiSelection: true,
										Model:          modLabels,
									},
									ComboBox{
										Model:        hotkeyLabels,
										AssignTo:     &cb,
										CurrentIndex: 0,
									},
									PushButton{Text: "OK", OnClicked: func() {
										selModIdx := lb.SelectedIndexes()
										selHotkeyIdx := cb.CurrentIndex()
										bindingLabel, err := SetNewBinding(selModIdx, selHotkeyIdx)
										if err != nil {
											walk.MsgBox(mainWnd, "Failed", err.Error(), walk.MsgBoxApplModal)
										} else {
											hotkeyLabel.SetText(bindingLabel)
											dlg.Close(walk.DlgCmdOK)
										}
									}},
								},
							}.Run(mainWnd)
						},
					},
				},
			},
			HSplitter{
				Children: []Widget{
					Label{Text: "Input Key Delay"},
					Slider{AssignTo: &slider, MinValue: 10, MaxValue: 100},
					CheckBox{
						Text:     "Send ENTER at the end",
						AssignTo: &sendEnter,
						MinSize:  Size{Width: 40, Height: 20},
					},
				},
			},
		},
	}.Run()
}
