package oxcafedead.barcodereader.ui;

import oxcafedead.barcodereader.keybind.HotkeyBindManager;

public class HotKey {
  public final HotkeyBindManager.SpecialKey specialKey;
  public final HotkeyBindManager.SpecialKey[] additionalSpecialKeys;
  public final int key;

  public HotKey(
      HotkeyBindManager.SpecialKey specialKey,
      HotkeyBindManager.SpecialKey[] additionalSpecialKeys,
      int key) {
    this.specialKey = specialKey;
    this.additionalSpecialKeys = additionalSpecialKeys;
    this.key = key;
  }
}
