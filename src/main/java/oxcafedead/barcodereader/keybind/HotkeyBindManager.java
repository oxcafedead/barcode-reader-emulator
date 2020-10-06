package oxcafedead.barcodereader.keybind;

import java.util.Optional;

public interface HotkeyBindManager {

  enum SpecialKey {
    WIN,
    CTRL,
    ALT,
    SHIFT
  }

  /**
   * @param keyCode ascii english letter
   * @return integer key ID if binding was successful
   */
  Optional<Integer> bindKey(
      int keyCode, SpecialKey specialKey, SpecialKey... additionalSpecialKeys);

  /** @return {@code true} if unbinding was successful */
  boolean unbindKey(int keyBindId);

  /**
   * Blocking method which should be called in the same thread which called {@link #bindKey} method.
   *
   * @param callback triggered each time hotkey pressed
   */
  void listen(Runnable callback);
}
