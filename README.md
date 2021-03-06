# [<img src="https://github.com/oxcafedead/barcode-reader-emulator/raw/main/docs/barcode-emulator-logo.png">](https://oxcafedead.github.io/barcode-reader-emulator/)

[![Build Status](https://travis-ci.org/oxcafedead/barcode-reader-emulator.svg?branch=main)](https://travis-ci.org/oxcafedead/barcode-reader-emulator)

A Java 11 Windows desktop app. Emulates barcode reader device (which behaves like a keyboard).
Triggers barcode 'reading' with hotkey event which can be configured.

## Usage

- Download the latest release and unzip the archive. 
- Run `emulator.exe`.
- Optionally, you can change settings.
    - To change hotkey combination, just click on the read-only hotkey text field and type in the new combination. The new combination should appear as the text field updated value. Then click on 'refresh' button to apply the change. Keep in mind that lots of combinations are already bind and cannot be used.
    - You can also parse an existing barcode on the screen by clicking on 'photo' button and capture a screen area with the barcode image. It will be automatically parsed and set to 'Value' field.
- Just go to the target place (standalone app, browser tab or any other) which you are going to test with barcode input.
- Press the hotkey.

## Screenshot
![Screenshot](docs/screenshot.png)
