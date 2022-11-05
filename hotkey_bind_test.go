package main

import (
	"testing"

	"golang.design/x/hotkey"
)

func Test_bindingToLabel(t *testing.T) {
	type args struct {
		mods []hotkey.Modifier
		key  hotkey.Key
	}
	tests := []struct {
		name string
		args args
		want string
	}{
		{name: "Ctrl + Shift + A", args: args{mods: []hotkey.Modifier{hotkey.ModCtrl, hotkey.ModShift}, key: hotkey.KeyA}, want: "Ctrl + Shift + A"},
		{name: "Ctrl + F", args: args{mods: []hotkey.Modifier{hotkey.ModCtrl}, key: hotkey.KeyF}, want: "Ctrl + F"},
		{name: "Ctrl + Alt + Shift + 5", args: args{mods: []hotkey.Modifier{hotkey.ModCtrl, hotkey.ModAlt, hotkey.ModShift}, key: hotkey.Key5}, want: "Ctrl + Alt + Shift + 5"},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			if got := bindingToLabel(tt.args.mods, tt.args.key); got != tt.want {
				t.Errorf("bindingToLabel() = %v, want %v", got, tt.want)
			}
		})
	}
}

func Test_hotkeyToString(t *testing.T) {
	type args struct {
		v hotkey.Key
	}
	tests := []struct {
		name string
		args args
		want string
	}{
		{name: "basic number", args: args{hotkey.Key1}, want: "1"},
		{name: "basic letter", args: args{hotkey.KeyX}, want: "X"},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			if got := hotkeyToString(tt.args.v); got != tt.want {
				t.Errorf("hotkeyToString() = %v, want %v", got, tt.want)
			}
		})
	}
}
