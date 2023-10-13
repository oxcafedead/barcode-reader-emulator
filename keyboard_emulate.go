package main

import (
	"time"
	"unicode"

	"github.com/micmonay/keybd_event"
)

// Emulates keyboard signals for a given test string. Only basic symbols are supported,
// such as upper/lowercase English letters, numbers and basic special symbols such as !,@,# etc.
func EmulateTyping(text string, delayBetweenKeys int, sendEnter bool) {
	for _, v := range text {

		kb, err := keybd_event.NewKeyBonding()
		if err != nil {
			panic(err)
		}

		keycode, shifted := translateToKeybd(unicode.ToUpper(v))

		kb.HasSHIFT(unicode.IsUpper(v) || shifted)

		kb.SetKeys(keycode)
		err = kb.Press()
		if err != nil {
			panic(err)
		}
		err = kb.Release()
		if err != nil {
			panic(err)
		}

		kb.Clear()
		time.Sleep(time.Millisecond * time.Duration(delayBetweenKeys))
	}
	if sendEnter {
		kb, err := keybd_event.NewKeyBonding()
		if err != nil {
			panic(err)
		}
	
		kb.SetKeys(keybd_event.VK_ENTER)
		err = kb.Press()
		if err != nil {
			panic(err)
		}
		err = kb.Release()
		if err != nil {
			panic(err)
		}
	}
}

func translateToKeybd(s rune) (int, bool) {
	if s == '0' {
		return keybd_event.VK_0, false
	}
	if s > '0' && s <= '9' {
		return keybd_event.VK_1 + int(s-'1'), false
	}
	switch s {
	case 'A':
		return keybd_event.VK_A, false
	case 'B':
		return keybd_event.VK_B, false
	case 'C':
		return keybd_event.VK_C, false
	case 'D':
		return keybd_event.VK_D, false
	case 'E':
		return keybd_event.VK_E, false
	case 'F':
		return keybd_event.VK_F, false
	case 'G':
		return keybd_event.VK_G, false
	case 'H':
		return keybd_event.VK_H, false
	case 'I':
		return keybd_event.VK_I, false
	case 'K':
		return keybd_event.VK_K, false
	case 'L':
		return keybd_event.VK_L, false
	case 'M':
		return keybd_event.VK_M, false
	case 'N':
		return keybd_event.VK_N, false
	case 'O':
		return keybd_event.VK_O, false
	case 'P':
		return keybd_event.VK_P, false
	case 'Q':
		return keybd_event.VK_Q, false
	case 'R':
		return keybd_event.VK_R, false
	case 'S':
		return keybd_event.VK_S, false
	case 'T':
		return keybd_event.VK_T, false
	case 'U':
		return keybd_event.VK_U, false
	case 'V':
		return keybd_event.VK_V, false
	case 'W':
		return keybd_event.VK_W, false
	case 'X':
		return keybd_event.VK_X, false
	case 'Y':
		return keybd_event.VK_Y, false
	case 'Z':
		return keybd_event.VK_Z, false
	case '!':
		return keybd_event.VK_1, true
	case '@':
		return keybd_event.VK_2, true
	case '#':
		return keybd_event.VK_3, true
	case '$':
		return keybd_event.VK_4, true
	case '%':
		return keybd_event.VK_5, true
	case '^':
		return keybd_event.VK_6, true
	case '&':
		return keybd_event.VK_7, true
	case '*':
		return keybd_event.VK_8, true
	case '(':
		return keybd_event.VK_9, true
	case ')':
		return keybd_event.VK_0, true
	case '-':
		return keybd_event.VK_OEM_MINUS, false
	case '+':
		return keybd_event.VK_OEM_PLUS, false
	case '=':
		return keybd_event.VK_EQUAL, false
	case '_':
		return keybd_event.VK_MINUS, true
	case '`':
		return keybd_event.VK_OEM_3, false
	case '~':
		return keybd_event.VK_OEM_3, true
	case '\\':
		return keybd_event.VK_OEM_5, false
	case '|':
		return keybd_event.VK_OEM_5, true
	case '[':
		return keybd_event.VK_OEM_4, false
	case '{':
		return keybd_event.VK_OEM_4, true
	case ']':
		return keybd_event.VK_OEM_6, false
	case '}':
		return keybd_event.VK_OEM_6, true
	case ';':
		return keybd_event.VK_SEMICOLON, false
	case ':':
		return keybd_event.VK_SEMICOLON, true
	case '\'':
		return keybd_event.VK_OEM_7, false
	case '"':
		return keybd_event.VK_OEM_7, true
	case ',':
		return keybd_event.VK_COMMA, false
	case '<':
		return keybd_event.VK_COMMA, true
	case '.':
		return keybd_event.VK_DOT, false
	case '>':
		return keybd_event.VK_DOT, true
	case '/':
		return keybd_event.VK_OEM_2, false
	case '?':
		return keybd_event.VK_OEM_2, true
	default:
		return keybd_event.VK_OEM_2, true // ? symbol for any unsupported rune
	}
}
