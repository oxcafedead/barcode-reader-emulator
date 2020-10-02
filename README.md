# Barcode reader emulator

A Java 11 console app. Emulates barcode reader device (which behaves like a keyboard).
Triggers barcode 'reading' with hotkey event which can be configured.

## Usage

Runs with arguments:
- code value
- hotkey with format `^(ctrl_|shift_|alt_|win_)+[a-z]$`

**Example**
```
java -jar emulator.jar 123456 ctrl_alt_g
```