package main

import (
	"strings"
	"time"

	"golang.design/x/hotkey"
)

var bindingChan = make(chan *hotkey.Hotkey, 1)

func InitDefaultBinding() (string, error) {
	const defBindKey = hotkey.KeyQ
	var defBindMods = []hotkey.Modifier{hotkey.ModCtrl, hotkey.ModAlt}
	initialBinding := hotkey.New(defBindMods, defBindKey)
	err := initialBinding.Register()
	if err != nil {
		return "", err
	}
	var defHotkeyLabel = "n/a"

	bindingChan <- initialBinding
	defHotkeyLabel = bindingToLabel(defBindMods, defBindKey)
	return defHotkeyLabel, nil
}

func ListenForHotkey(actionCallback func()) {
	binding := <-bindingChan
	for {
		select {
		case newBinding := <-bindingChan:
			if binding != newBinding {
				err := binding.Unregister()
				if err != nil {
					panic(err)
				}
				binding = newBinding
			}
			continue
		case _, ok := <-binding.Keyup():
			if !ok {
				continue
			}
		default:
			time.Sleep(time.Millisecond * 100)
			continue
		}

		actionCallback()
	}
}

func SetNewBinding(modifierIndexes []int, keyIndex int) (string, error) {
	var hkMods = make([]hotkey.Modifier, len(modifierIndexes))
	for i, selectedIdx := range modifierIndexes {
		hkMods[i] = AllModifiers[selectedIdx]
	}
	key := AllHotkeys[keyIndex]
	hkBind := hotkey.New(hkMods, key)
	err := hkBind.Register()
	if err != nil {
		return "", err
	}

	bindingChan <- hkBind
	return bindingToLabel(hkMods, key), nil
}

var AllModifiers = []hotkey.Modifier{hotkey.ModCtrl, hotkey.ModAlt, hotkey.ModWin, hotkey.ModShift}

var AllHotkeys = []hotkey.Key{
	hotkey.Key0,
	hotkey.Key1,
	hotkey.Key2,
	hotkey.Key3,
	hotkey.Key4,
	hotkey.Key5,
	hotkey.Key6,
	hotkey.Key7,
	hotkey.Key8,
	hotkey.Key9,
	hotkey.KeyA,
	hotkey.KeyB,
	hotkey.KeyC,
	hotkey.KeyD,
	hotkey.KeyE,
	hotkey.KeyF,
	hotkey.KeyG,
	hotkey.KeyH,
	hotkey.KeyI,
	hotkey.KeyJ,
	hotkey.KeyK,
	hotkey.KeyL,
	hotkey.KeyM,
	hotkey.KeyN,
	hotkey.KeyO,
	hotkey.KeyP,
	hotkey.KeyQ,
	hotkey.KeyR,
	hotkey.KeyS,
	hotkey.KeyT,
	hotkey.KeyU,
	hotkey.KeyV,
	hotkey.KeyW,
	hotkey.KeyX,
	hotkey.KeyY,
	hotkey.KeyZ,
}

// some utility methods...

func bindingToLabel(mods []hotkey.Modifier, key hotkey.Key) string {
	s := new(strings.Builder)
	for _, v := range mods {
		if v == 0x0 {
			continue
		}
		s.WriteString(modifierToString(v) + " + ")
	}
	s.WriteString(hotkeyToString(key))
	return s.String()
}

func modifierToString(md hotkey.Modifier) string {
	switch md {
	case hotkey.ModAlt:
		return "Alt"
	case hotkey.ModCtrl:
		return "Ctrl"
	case hotkey.ModShift:
		return "Shift"
	case hotkey.ModWin:
		return "Win"
	default:
		panic("unexpected modifier " + string(md))
	}
}

func hotkeyToString(v hotkey.Key) string {
	if v <= 0x39 {
		return string((v - 0x30) + '0')
	} else if v <= 0x5A {
		return string((v - 0x41) + 'A')
	} else {
		panic("unexpected key found: " + string(v))
	}
}
