import keybind.HotkeyBindFactory;
import keybind.HotkeyBindManager;

import java.awt.AWTException;
import java.awt.Robot;

import static java.awt.event.KeyEvent.VK_G;
import static keybind.HotkeyBindManager.SpecialKey.ALT;
import static keybind.HotkeyBindManager.SpecialKey.CTRL;

public class BarCodeEmulator {

  public static void main(String[] args) throws AWTException {
    Robot robot = new Robot();
    String codeValue = args[0];

    HotkeyBindManager hotkeyBinder = HotkeyBindFactory.INSTANCE.getHotkeyBinder();
    if (!hotkeyBinder.bindKey(VK_G, CTRL, ALT).isPresent()) {
      throw new IllegalStateException("Could not bind hotkey...");
    }

    hotkeyBinder.listen(() -> emulateBarCodeRead(robot, codeValue));
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
