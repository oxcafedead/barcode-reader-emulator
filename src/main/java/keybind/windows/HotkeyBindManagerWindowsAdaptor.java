package keybind.windows;

import keybind.HotkeyBindManager;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import static keybind.windows.User32.*;

public class HotkeyBindManagerWindowsAdaptor implements HotkeyBindManager {

  private static final Map<SpecialKey, Integer> SPECIAL_KEY_CODES =
      Map.of(
          SpecialKey.ALT, 0x0001,
          SpecialKey.CTRL, 0x0002,
          SpecialKey.SHIFT, 0x0004,
          SpecialKey.WIN, MOD_WIN);

  @Override
  public Optional<Integer> bindKey(
      int keyCode, SpecialKey specialKey, SpecialKey... additionalSpecialKeys) {
    if (keyCode > KeyEvent.VK_Z || keyCode < KeyEvent.VK_A) {
      throw new IllegalArgumentException("Incorrect key code. Key code should be from 'A' to 'Z'.");
    }
    int bindId = Math.abs(new Random().nextInt());
    int fsModifiers =
        Stream.concat(Stream.of(specialKey), Arrays.stream(additionalSpecialKeys))
            .mapToInt(SPECIAL_KEY_CODES::get)
            .reduce((i1, i2) -> i1 | i2)
            .orElseThrow(IllegalArgumentException::new);
    boolean registered = RegisterHotKey(null, bindId, fsModifiers, keyCode);
    if (registered) {
      System.out.println("Bind the hotkey successfully.");
      return Optional.of(bindId);
    }
    System.out.println("Could not bind the hotkey!");
    return Optional.empty();
  }

  @Override
  public boolean unbindKey(int keyBindId) {
    return UnregisterHotKey(null, keyBindId);
  }

  @Override
  public void listen(Runnable callback) {
    System.out.println("Starting listening...");
    MSG msg = new MSG();
    while (true) {
      while (PeekMessageA(msg, null, 0, 0, PM_REMOVE)) {
        System.out.println("Picked up a key! " + msg.message);
        if (msg.message == WM_HOTKEY) {
          callback.run();
        }
      }
      try {
        Thread.sleep(300);
      } catch (InterruptedException e) {
        System.err.println("Error when trying to make thread sleep.");
        e.printStackTrace();
      }
    }
  }
}
