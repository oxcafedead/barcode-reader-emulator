A standalone Windows desktop app. Emulates a barcode reader device (which behaves like a keyboard). Triggers barcode 'reading' with a hotkey event which can be configured.

## Usage

Just download the latest release ZIP and extract the archive anywhere you want. Then run `emulator.exe`.

### Configuration
Optionally, you can change some settings.
 - __Preferred hotkey__
To change hotkey combination, just click on the read-only hotkey text field and type in the new combination. The new combination should appear as the text field updated value. Then click on "🔃" button to apply the change. Keep in mind that lots of combinations are already bind and cannot be used.
 - __Take a snap of a real barcode to get actual value__
You can also take a snap of an existing barcode (QR or a plain old barcode) on your screen by clicking on "📸" button and capture a screen area with the barcode image. It will be automatically parsed and set as the 'Value'.

### Trigger a barcode "read" event
Go to the target place (a standalone app, browser tab or any other) which you are going to test with the barcode input. Then press the hotkey. Simple enough, isn't it?
