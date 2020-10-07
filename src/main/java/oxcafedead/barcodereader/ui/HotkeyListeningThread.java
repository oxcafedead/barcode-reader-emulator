package oxcafedead.barcodereader.ui;

import oxcafedead.barcodereader.keybind.HotkeyBindFactory;
import oxcafedead.barcodereader.keybind.HotkeyBindManager;

import javax.swing.JFrame;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.util.Optional;
import java.util.function.Supplier;

class HotkeyListeningThread extends Thread {

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
    this.setUncaughtExceptionHandler(new BugReporter(frame));
  }

  @Override
  public void run() {
    HotkeyBindManager hotkeyBinder = HotkeyBindFactory.INSTANCE.getHotkeyBinder();
    Optional<Integer> bindId =
        hotkeyBinder.bindKey(hotKey.key, hotKey.specialKey, hotKey.additionalSpecialKeys);
    if (bindId.isEmpty()) {
      throw new IllegalStateException("Could not bind hotkey.");
    }

    final Robot robot;
    try {
      robot = new Robot();
    } catch (AWTException e) {
      throw new IllegalStateException(e);
    }

    hotkeyBinder.listen(() -> emulateBarCodeRead(robot));

    hotkeyBinder.unbindKey(bindId.get());
  }

  private void emulateBarCodeRead(Robot robot) {
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
              robot.keyPress(keycode);
            });
    Toolkit.getDefaultToolkit().beep();
  }
}
