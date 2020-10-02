package keybind;

import keybind.windows.HotkeyBindManagerWindowsAdaptor;

public enum HotkeyBindFactory {
  INSTANCE;

  public HotkeyBindManager getHotkeyBinder() {
    String osName = System.getProperty("os.name");
    if (osName.startsWith("Windows")) {
      return new HotkeyBindManagerWindowsAdaptor();
    }
    throw new IllegalStateException("For now only Windows is supported.");
  }
}
