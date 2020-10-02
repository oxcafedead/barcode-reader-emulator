import keybind.HotkeyBindFactory;
import keybind.HotkeyBindManager;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.Arrays;

public class BarCodeEmulator {

  public static void main(String[] args) throws AWTException {
    Robot robot = new Robot();

    validateParams(args);
    String codeValue = args[0];
    String[] hotkeyValues = args[1].toUpperCase().split("_");
    char keyCode = hotkeyValues[hotkeyValues.length - 1].charAt(0);
    HotkeyBindManager.SpecialKey specialKey = HotkeyBindManager.SpecialKey.valueOf(hotkeyValues[0]);
    HotkeyBindManager.SpecialKey[] additionalSpecialKeys =
        Arrays.stream(hotkeyValues)
            .skip(1)
            .limit(hotkeyValues.length - 2)
            .map(HotkeyBindManager.SpecialKey::valueOf)
            .toArray(HotkeyBindManager.SpecialKey[]::new);

    HotkeyBindManager hotkeyBinder = HotkeyBindFactory.INSTANCE.getHotkeyBinder();
    if (hotkeyBinder.bindKey(keyCode, specialKey, additionalSpecialKeys).isEmpty()) {
      throw new IllegalStateException("Could not bind hotkey.");
    }

    System.out.println("Listening for a hotkey input...");
    hotkeyBinder.listen(() -> emulateBarCodeRead(robot, codeValue));
  }

  private static void validateParams(String[] args) {
    if (args.length != 2) {
      throw new IllegalArgumentException(
          "App requires two parameters to run: code value and hotkey definition.");
    }

    if (args[0].isBlank()) {
      throw new IllegalArgumentException("Barcode value cannot be blank");
    }

    if (!args[1].matches("^(ctrl_|shift_|alt_|win_)+[a-z]$")) {
      throw new IllegalArgumentException("Hotkey definition doesn't comply with format");
    }
  }

  private static void emulateBarCodeRead(Robot robot, String codeValue) {
    codeValue
        .chars()
        .forEachOrdered(
            keycode -> {
              try {
                Thread.sleep(20);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              robot.keyPress(keycode);
            });
  }
}
