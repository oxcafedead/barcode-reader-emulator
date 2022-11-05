package main

import (
	"testing"

	"github.com/micmonay/keybd_event"
)

func Test_translateToKeybd(t *testing.T) {
	type args struct {
		s rune
	}
	tests := []struct {
		name        string
		args        args
		wantKeyCode int
		wantShifted bool
	}{
		{name: "letter", args: args{'A'}, wantKeyCode: keybd_event.VK_A, wantShifted: false},
		{name: "letter", args: args{'W'}, wantKeyCode: keybd_event.VK_W, wantShifted: false},
		{name: "number", args: args{'5'}, wantKeyCode: keybd_event.VK_5, wantShifted: false},
		{name: "special symbol !", args: args{'!'}, wantKeyCode: keybd_event.VK_1, wantShifted: true},
		{name: "special symbol ?", args: args{'?'}, wantKeyCode: keybd_event.VK_OEM_2, wantShifted: true},
		{name: "unsupported symbol mapped to ?", args: args{'❤'}, wantKeyCode: keybd_event.VK_OEM_2, wantShifted: true},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			got, got1 := translateToKeybd(tt.args.s)
			if got != tt.wantKeyCode {
				t.Errorf("translateToKeybd() got = %v, want %v", got, tt.wantKeyCode)
			}
			if got1 != tt.wantShifted {
				t.Errorf("translateToKeybd() got1 = %v, want %v", got1, tt.wantShifted)
			}
		})
	}
}
