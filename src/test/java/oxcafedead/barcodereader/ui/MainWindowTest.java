package oxcafedead.barcodereader.ui;

import org.junit.Assert;
import org.junit.Test;
import oxcafedead.barcodereader.keybind.HotkeyBindManager;

import java.awt.event.KeyEvent;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MainWindowTest {

  @Test
  public void setHotkeys_threeKeys_setCorrectly() {
    Integer[] inputKeys = {KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_B};

    final var hotKey = MainWindow.buildHotkey(inputKeys).get();

    Assert.assertEquals(HotkeyBindManager.SpecialKey.SHIFT, hotKey.specialKey);
    assertEquals(
        singletonList(HotkeyBindManager.SpecialKey.CTRL), hotKey.getAdditionalSpecialKeysList());
    assertEquals((int) ('B'), hotKey.key);
  }

  @Test
  public void setHotkeys_twoKeys_setCorrectly() {
    Integer[] inputKeys = {KeyEvent.VK_ALT, KeyEvent.VK_Z};

    final var hotKey = MainWindow.buildHotkey(inputKeys).get();

    Assert.assertEquals(HotkeyBindManager.SpecialKey.ALT, hotKey.specialKey);
    assertEquals(emptyList(), hotKey.getAdditionalSpecialKeysList());
    assertEquals((int) ('Z'), hotKey.key);
  }

  @Test
  public void setHotkeys_justSpecialKey_doNothing() {
    Integer[] inputKeys = {KeyEvent.VK_ALT};

    final var hotKey = MainWindow.buildHotkey(inputKeys);

    assertTrue(hotKey.isEmpty());
  }

  @Test
  public void setHotkeys_justKey_doNothing() {
    Integer[] inputKeys = {KeyEvent.VK_B};

    final var hotKey = MainWindow.buildHotkey(inputKeys);

    assertTrue(hotKey.isEmpty());
  }

  @Test
  public void setHotkeys_incorrectOrder1_normalize() {
    Integer[] inputKeys = {KeyEvent.VK_CONTROL, KeyEvent.VK_B, KeyEvent.VK_SHIFT};

    final var hotKey = MainWindow.buildHotkey(inputKeys).get();

    Assert.assertEquals(HotkeyBindManager.SpecialKey.SHIFT, hotKey.specialKey);
    assertEquals(
        singletonList(HotkeyBindManager.SpecialKey.CTRL), hotKey.getAdditionalSpecialKeysList());
    assertEquals((int) ('B'), hotKey.key);
  }

  @Test
  public void setHotkeys_incorrectOrder2_normalize() {
    Integer[] inputKeys = {KeyEvent.VK_B, KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT};

    final var hotKey = MainWindow.buildHotkey(inputKeys).get();

    Assert.assertEquals(HotkeyBindManager.SpecialKey.SHIFT, hotKey.specialKey);
    assertEquals(
        singletonList(HotkeyBindManager.SpecialKey.CTRL), hotKey.getAdditionalSpecialKeysList());
    assertEquals((int) ('B'), hotKey.key);
  }

  @Test
  public void setHotkeys_incorrectOrder3_normalize() {
    Integer[] inputKeys = {KeyEvent.VK_B, KeyEvent.VK_CONTROL};

    final var hotKey = MainWindow.buildHotkey(inputKeys).get();

    Assert.assertEquals(HotkeyBindManager.SpecialKey.CTRL, hotKey.specialKey);
    assertEquals(emptyList(), hotKey.getAdditionalSpecialKeysList());
    assertEquals((int) ('B'), hotKey.key);
  }

  @Test
  public void setHotkeys_fourKeys_setCorrectly() {
    Integer[] inputKeys = {KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_ALT, KeyEvent.VK_B};

    final var hotKey = MainWindow.buildHotkey(inputKeys).get();

    Assert.assertEquals(HotkeyBindManager.SpecialKey.SHIFT, hotKey.specialKey);
    assertEquals(
        asList(HotkeyBindManager.SpecialKey.CTRL, HotkeyBindManager.SpecialKey.ALT),
        hotKey.getAdditionalSpecialKeysList());
    assertEquals((int) ('B'), hotKey.key);
  }

  @Test
  public void setHotkeys_duplicates_removeDuplicates() {
    Integer[] inputKeys = {
      KeyEvent.VK_CONTROL, KeyEvent.VK_CONTROL, KeyEvent.VK_ALT, KeyEvent.VK_B
    };

    final var hotKey = MainWindow.buildHotkey(inputKeys).get();

    Assert.assertEquals(HotkeyBindManager.SpecialKey.CTRL, hotKey.specialKey);
    assertEquals(
        singletonList(HotkeyBindManager.SpecialKey.ALT), hotKey.getAdditionalSpecialKeysList());
    assertEquals((int) ('B'), hotKey.key);
  }

  @Test
  public void setHotkeys_twoChars_doNothing() {
    Integer[] inputKeys = {KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_Z, KeyEvent.VK_B};

    final var hotKey = MainWindow.buildHotkey(inputKeys);

    assertTrue(hotKey.isEmpty());
  }
}
