import keybind.HotkeyBindFactory;
import keybind.HotkeyBindManager;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class BarcodeReaderEmulator {

  public static final String DEFAULT_BARCODE_VALUE = "test";
  public static final String DEFAULT_HOTKEY = "ctrl_alt_g";
  public static final String DEFAULT_KEY_DELAY = "20";

  public static void main(String[] args) throws AWTException, IOException {
    final String codeValueParam;
    final String hotkeyParam;
    final String keyDelay;

    if (args.length > 0) {
      codeValueParam = args[0];
      hotkeyParam = args.length > 1 ? args[1] : DEFAULT_HOTKEY;
      keyDelay = args.length > 2 ? args[2] : DEFAULT_KEY_DELAY;
    } else {
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

      System.out.print("Please type in the barcode value to emulate [Enter to use default]: ");
      String barCodeIn = reader.readLine();
      codeValueParam = barCodeIn.isBlank() ? DEFAULT_BARCODE_VALUE : barCodeIn;

      System.out.print("Please type in the hotkey [Enter to use default]: ");
      String formatIn = reader.readLine();
      hotkeyParam = formatIn.length() == 0 ? DEFAULT_HOTKEY : formatIn;

      System.out.print("Please type in the key delay [Enter to use default]: ");
      String delayIn = reader.readLine();
      keyDelay = delayIn.length() == 0 ? DEFAULT_KEY_DELAY : delayIn;
    }

    if (codeValueParam.isBlank()) {
      throw new IllegalArgumentException("Barcode value cannot be blank.");
    }

    if (!hotkeyParam.matches("^(ctrl_|shift_|alt_|win_)+[a-z]$")) {
      throw new IllegalArgumentException(
          "Hotkey definition " + hotkeyParam + " doesn't comply with format.");
    }

    int ms = Integer.parseInt(keyDelay);
    if (ms < 10 || ms > 100) {
      throw new IllegalArgumentException(
          "Key delay should be between 10 and 100 for sanity reasons.");
    }

    String[] hotkeyValues = hotkeyParam.toUpperCase().split("_");

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

    final Robot robot = new Robot();

    System.out.println("Listening for a hotkey input...");
    hotkeyBinder.listen(() -> emulateBarCodeRead(robot, codeValueParam));
  }

  private static void emulateBarCodeRead(Robot robot, String codeValue) {
    System.out.printf("Emulating that barcode scanner has read '%s' value\n", codeValue);
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
    Toolkit.getDefaultToolkit().beep();
  }
}
