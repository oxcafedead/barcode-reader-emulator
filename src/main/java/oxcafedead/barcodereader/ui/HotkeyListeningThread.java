package oxcafedead.barcodereader.ui;

import oxcafedead.barcodereader.encode.KeyEncoder;
import oxcafedead.barcodereader.encode.KeyEncoderFactory;
import oxcafedead.barcodereader.keybind.HotkeyBindFactory;
import oxcafedead.barcodereader.keybind.HotkeyBindManager;

import javax.swing.JFrame;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.util.Optional;
import java.util.function.Supplier;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

class HotkeyListeningThread extends Thread {

  private static final String COULD_NOT_BIND_HOTKEY_MSG = "Could not bind hotkey.";

  private final HotKey hotKey;
  private final Supplier<String> barcodeValueSupplier;
  private final Supplier<Long> keyDelaySupplier;

  public HotkeyListeningThread(
      JFrame frame,
      HotKey hotKey,
      Supplier<String> barcodeValueSupplier,
      Supplier<Long> keyDelaySupplier) {
    this.hotKey = hotKey;
    this.barcodeValueSupplier = barcodeValueSupplier;
    this.keyDelaySupplier = keyDelaySupplier;
    this.setDaemon(true);
    this.setUncaughtExceptionHandler(
        (thread, throwable) -> {
          if (COULD_NOT_BIND_HOTKEY_MSG.equals(throwable.getMessage())) {
            showMessageDialog(
                frame,
                "Could not bind this hotkey."
                    + "\nIt may be already globally used by some app or just reserved by operating system."
                    + "\nPlease try another one.",
                "Error",
                ERROR_MESSAGE);
          } else {
            new BugReporter(frame).uncaughtException(thread, throwable);
          }
        });
  }

  @Override
  public void run() {
    HotkeyBindManager hotkeyBinder = HotkeyBindFactory.INSTANCE.getHotkeyBinder();
    Optional<Integer> bindId =
        hotkeyBinder.bindKey(hotKey.key, hotKey.specialKey, hotKey.additionalSpecialKeys);
    if (bindId.isEmpty()) {
      throw new IllegalStateException(COULD_NOT_BIND_HOTKEY_MSG);
    }

    final Robot robot;
    try {
      robot = new Robot();
    } catch (AWTException e) {
      throw new IllegalStateException(e);
    }
    KeyEncoder keyEncoder = KeyEncoderFactory.INSTANCE.getKeyEncoder(robot);

    hotkeyBinder.listen(() -> emulateBarCodeRead(keyEncoder));

    hotkeyBinder.unbindKey(bindId.get());
  }

  private void emulateBarCodeRead(KeyEncoder keyEncoder) {
    try {
      // wait initially for some time not to mess hotkey and barcode input
      Thread.sleep(150);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return;
    }

    barcodeValueSupplier
        .get()
        .chars()
        .forEachOrdered(
            keycode -> {
              try {
                Thread.sleep(keyDelaySupplier.get());
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
              }
              keyEncoder.encode(keycode);
            });
    Toolkit.getDefaultToolkit().beep();
  }
}
