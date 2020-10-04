# Barcode reader emulator
[![Build Status](https://travis-ci.org/oxcafedead/barcode-reader-emulator.svg?branch=main)](https://travis-ci.org/oxcafedead/barcode-reader-emulator)

A Java 11 console app. Emulates barcode reader device (which behaves like a keyboard).
Triggers barcode 'reading' with hotkey event which can be configured.

## Usage

You can just run `emulator.exe` and type in asked parameters.

You can also run the app with arguments:
- code value (`test` is default)
- hotkey with format `^(ctrl_|shift_|alt_|win_)+[a-z]$` (`ctrl_alt_g` is default)
- delay between barcode key inputs in milliseconds, should be between `10` and `100` (`20` is default)

**Example**
```
emulator.exe 123456 ctrl_alt_f 30
```