import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import static windows.User32.*;

public class BarCodeEmulator {

  public static void main(String[] args) throws AWTException, InterruptedException {
    Robot robot = new Robot();
    String codeValue = args[0];

    boolean registered =
        RegisterHotKey(null, 1, MOD_CONTROL | MOD_ALT, KeyEvent.VK_G);
    if (!registered) {
      throw new IllegalStateException("Could not register hotkey!");
    }

    MSG msg = new MSG();
    System.out.println("Starting listening...");
    while (true) {
      while (PeekMessageA(msg, null, 0, 0, PM_REMOVE)) {
        System.out.println("Picked up a key! " + msg.message);
        if (msg.message == WM_HOTKEY) {
          System.out.println("Hotkey pressed");
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
      Thread.sleep(300);
    }
  }
}
