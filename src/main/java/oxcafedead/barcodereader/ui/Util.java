package oxcafedead.barcodereader.ui;

import oxcafedead.barcodereader.BarcodeReaderEmulator;
import oxcafedead.barcodereader.keybind.HotkeyBindManager;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.util.Objects;
import java.util.Optional;

import static oxcafedead.barcodereader.keybind.HotkeyBindManager.SpecialKey.*;
import static oxcafedead.barcodereader.keybind.HotkeyBindManager.SpecialKey.ALT;

public class Util {
  private Util() {}

  public static Optional<Image> loadAppIcon() {
    return Optional.ofNullable(
        new ImageIcon(
                Objects.requireNonNull(
                    BarcodeReaderEmulator.class.getClassLoader().getResource("icon.png")))
            .getImage()
            .getScaledInstance(32, 32, 0));
  }

  public static HotkeyBindManager.SpecialKey getKeyType(Integer firstKeyCode) {
    switch (firstKeyCode) {
      case KeyEvent.VK_SHIFT:
        return SHIFT;
      case KeyEvent.VK_CONTROL:
        return CTRL;
      case KeyEvent.VK_WINDOWS:
        return WIN;
      case KeyEvent.VK_ALT:
        return ALT;
      default:
        return null;
    }
  }
}
