package oxcafedead.barcodereader.keybind.windows;

import oxcafedead.barcodereader.keybind.HotkeyBindManager;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import static oxcafedead.barcodereader.keybind.windows.User32.*;

public class HotkeyBindManagerWindowsAdaptor implements HotkeyBindManager {

  private static final Map<SpecialKey, Integer> SPECIAL_KEY_CODES =
      Map.of(
          SpecialKey.ALT, 0x0001,
          SpecialKey.CTRL, 0x0002,
          SpecialKey.SHIFT, 0x0004,
          SpecialKey.WIN, 0x0008);

  private static final Random RANDOM = new Random();

  @Override
  public Optional<Integer> bindKey(
      int keyCode, SpecialKey specialKey, SpecialKey... additionalSpecialKeys) {
    if (keyCode > KeyEvent.VK_Z || keyCode < KeyEvent.VK_A) {
      throw new IllegalArgumentException("Incorrect key code. Key code should be from 'A' to 'Z'.");
    }
    int bindId = RANDOM.nextInt();
    int fsModifiers =
        Stream.concat(Stream.of(specialKey), Arrays.stream(additionalSpecialKeys))
            .mapToInt(SPECIAL_KEY_CODES::get)
            .reduce((i1, i2) -> i1 | i2)
            .orElseThrow(IllegalArgumentException::new);
    boolean registered = RegisterHotKey(null, bindId, fsModifiers, keyCode);
    return registered ? Optional.of(bindId) : Optional.empty();
  }

  @Override
  public boolean unbindKey(int keyBindId) {
    return UnregisterHotKey(null, keyBindId);
  }

  @Override
  @SuppressWarnings("java:S2189") // exit is designed via interrupt
  public void listen(Runnable callback) {
    MSG msg = new MSG();
    while (true) {
      while (PeekMessageA(msg, null, 0, 0, PM_REMOVE)) {
        if (msg.message == WM_HOTKEY) {
          callback.run();
        }
      }
      try {
        Thread.sleep(300);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    }
  }
}
