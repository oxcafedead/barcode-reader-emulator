package oxcafedead.barcodereader.ui;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import oxcafedead.barcodereader.keybind.HotkeyBindManager;

import java.awt.event.KeyEvent;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MainWindowTest {

  MainWindow mainWindow;

  @Before
  public void setUp() throws Exception {
    mainWindow = new MainWindow(false);
    // reset
    mainWindow.specialKey = null;
    mainWindow.additionalSpecialKeys = null;
    mainWindow.key = 0;
  }

  @Test
  public void setHotkeys_threeKeys_setCorrectly() {
    Integer[] inputKeys = {KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_B};

    mainWindow.setHotkeys(inputKeys, false);

    Assert.assertEquals(HotkeyBindManager.SpecialKey.SHIFT, mainWindow.specialKey);
    assertEquals(
        singletonList(HotkeyBindManager.SpecialKey.CTRL), mainWindow.additionalSpecialKeys);
    assertEquals((int) ('B'), mainWindow.key);
  }

  @Test
  public void setHotkeys_twoKeys_setCorrectly() {
    Integer[] inputKeys = {KeyEvent.VK_ALT, KeyEvent.VK_Z};

    mainWindow.setHotkeys(inputKeys, false);

    Assert.assertEquals(HotkeyBindManager.SpecialKey.ALT, mainWindow.specialKey);
    assertEquals(emptyList(), mainWindow.additionalSpecialKeys);
    assertEquals((int) ('Z'), mainWindow.key);
  }

  @Test
  public void setHotkeys_justSpecialKey_doNothing() {
    Integer[] inputKeys = {KeyEvent.VK_ALT};

    mainWindow.setHotkeys(inputKeys, false);

    assertNull(mainWindow.specialKey);
    assertNull(mainWindow.additionalSpecialKeys);
    assertEquals(0, mainWindow.key);
  }

  @Test
  public void setHotkeys_justKey_doNothing() {
    Integer[] inputKeys = {KeyEvent.VK_B};

    mainWindow.setHotkeys(inputKeys, false);

    assertNull(mainWindow.specialKey);
    assertNull(mainWindow.additionalSpecialKeys);
    assertEquals(0, mainWindow.key);
  }

  @Test
  public void setHotkeys_incorrectOrder1_normalize() {
    Integer[] inputKeys = {KeyEvent.VK_CONTROL, KeyEvent.VK_B, KeyEvent.VK_SHIFT};

    mainWindow.setHotkeys(inputKeys, false);

    Assert.assertEquals(HotkeyBindManager.SpecialKey.SHIFT, mainWindow.specialKey);
    assertEquals(
        singletonList(HotkeyBindManager.SpecialKey.CTRL), mainWindow.additionalSpecialKeys);
    assertEquals((int) ('B'), mainWindow.key);
  }

  @Test
  public void setHotkeys_incorrectOrder2_normalize() {
    Integer[] inputKeys = {KeyEvent.VK_B, KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT};

    mainWindow.setHotkeys(inputKeys, false);

    Assert.assertEquals(HotkeyBindManager.SpecialKey.SHIFT, mainWindow.specialKey);
    assertEquals(
        singletonList(HotkeyBindManager.SpecialKey.CTRL), mainWindow.additionalSpecialKeys);
    assertEquals((int) ('B'), mainWindow.key);
  }

  @Test
  public void setHotkeys_incorrectOrder3_normalize() {
    Integer[] inputKeys = {KeyEvent.VK_B, KeyEvent.VK_CONTROL};

    mainWindow.setHotkeys(inputKeys, false);

    Assert.assertEquals(HotkeyBindManager.SpecialKey.CTRL, mainWindow.specialKey);
    assertEquals(emptyList(), mainWindow.additionalSpecialKeys);
    assertEquals((int) ('B'), mainWindow.key);
  }

  @Test
  public void setHotkeys_fourKeys_setCorrectly() {
    Integer[] inputKeys = {KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_ALT, KeyEvent.VK_B};

    mainWindow.setHotkeys(inputKeys, false);

    Assert.assertEquals(HotkeyBindManager.SpecialKey.SHIFT, mainWindow.specialKey);
    assertEquals(
        asList(HotkeyBindManager.SpecialKey.CTRL, HotkeyBindManager.SpecialKey.ALT),
        mainWindow.additionalSpecialKeys);
    assertEquals((int) ('B'), mainWindow.key);
  }

  @Test
  public void setHotkeys_duplicates_removeDuplicates() {
    Integer[] inputKeys = {
      KeyEvent.VK_CONTROL, KeyEvent.VK_CONTROL, KeyEvent.VK_ALT, KeyEvent.VK_B
    };

    mainWindow.setHotkeys(inputKeys, false);

    Assert.assertEquals(HotkeyBindManager.SpecialKey.CTRL, mainWindow.specialKey);
    assertEquals(singletonList(HotkeyBindManager.SpecialKey.ALT), mainWindow.additionalSpecialKeys);
    assertEquals((int) ('B'), mainWindow.key);
  }

  @Test
  public void setHotkeys_twoChars_doNothing() {
    Integer[] inputKeys = {KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_Z, KeyEvent.VK_B};

    mainWindow.setHotkeys(inputKeys, false);

    assertNull(mainWindow.specialKey);
    assertNull(mainWindow.additionalSpecialKeys);
    assertEquals(0, mainWindow.key);
  }
}
