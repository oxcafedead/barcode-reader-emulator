package oxcafedead.barcodereader.ui;

import oxcafedead.barcodereader.BarcodeReaderEmulator;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.util.Objects;
import java.util.Optional;

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
}
