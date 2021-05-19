package oxcafedead.barcodereader.encode;

import oxcafedead.barcodereader.encode.windows.WindowsUnicodeKeyEncoder;

import java.awt.Robot;

public enum KeyEncoderFactory {
  INSTANCE;

  public KeyEncoder getKeyEncoder(Robot robot) {
    String osName = System.getProperty("os.name");
    if (osName.startsWith("Windows")) {
      return new WindowsUnicodeKeyEncoder(robot);
    }
    throw new IllegalStateException("For now only Windows is supported.");
  }
}
