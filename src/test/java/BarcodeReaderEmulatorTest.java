import org.junit.Before;
import org.junit.Test;

import java.awt.event.KeyEvent;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static keybind.HotkeyBindManager.SpecialKey.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BarcodeReaderEmulatorTest {
  @Before
  public void reset() {
    BarcodeReaderEmulator.specialKey = null;
    BarcodeReaderEmulator.additionalSpecialKeys = null;
    BarcodeReaderEmulator.hotkeyLetter = 0;
  }

  @Test
  public void setHotkeys_threeKeys_setCorrectly() {
    Integer[] inputKeys = {KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_B};

    BarcodeReaderEmulator.setHotkeys(inputKeys, false);

    assertEquals(SHIFT, BarcodeReaderEmulator.specialKey);
    assertEquals(singletonList(CTRL), BarcodeReaderEmulator.additionalSpecialKeys);
    assertEquals((int) ('B'), BarcodeReaderEmulator.hotkeyLetter);
  }

  @Test
  public void setHotkeys_twoKeys_setCorrectly() {
    Integer[] inputKeys = {KeyEvent.VK_ALT, KeyEvent.VK_Z};

    BarcodeReaderEmulator.setHotkeys(inputKeys, false);

    assertEquals(ALT, BarcodeReaderEmulator.specialKey);
    assertEquals(emptyList(), BarcodeReaderEmulator.additionalSpecialKeys);
    assertEquals((int) ('Z'), BarcodeReaderEmulator.hotkeyLetter);
  }

  @Test
  public void setHotkeys_justSpecialKey_doNothing() {
    Integer[] inputKeys = {KeyEvent.VK_ALT};

    BarcodeReaderEmulator.setHotkeys(inputKeys, false);

    assertNull(BarcodeReaderEmulator.specialKey);
    assertNull(BarcodeReaderEmulator.additionalSpecialKeys);
    assertEquals(0, BarcodeReaderEmulator.hotkeyLetter);
  }

  @Test
  public void setHotkeys_justKey_doNothing() {
    Integer[] inputKeys = {KeyEvent.VK_B};

    BarcodeReaderEmulator.setHotkeys(inputKeys, false);

    assertNull(BarcodeReaderEmulator.specialKey);
    assertNull(BarcodeReaderEmulator.additionalSpecialKeys);
    assertEquals(0, BarcodeReaderEmulator.hotkeyLetter);
  }

  @Test
  public void setHotkeys_incorrectOrder1_normalize() {
    Integer[] inputKeys = {KeyEvent.VK_CONTROL, KeyEvent.VK_B, KeyEvent.VK_SHIFT};

    BarcodeReaderEmulator.setHotkeys(inputKeys, false);

    assertEquals(SHIFT, BarcodeReaderEmulator.specialKey);
    assertEquals(singletonList(CTRL), BarcodeReaderEmulator.additionalSpecialKeys);
    assertEquals((int) ('B'), BarcodeReaderEmulator.hotkeyLetter);
  }

  @Test
  public void setHotkeys_incorrectOrder2_normalize() {
    Integer[] inputKeys = {KeyEvent.VK_B, KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT};

    BarcodeReaderEmulator.setHotkeys(inputKeys, false);

    assertEquals(SHIFT, BarcodeReaderEmulator.specialKey);
    assertEquals(singletonList(CTRL), BarcodeReaderEmulator.additionalSpecialKeys);
    assertEquals((int) ('B'), BarcodeReaderEmulator.hotkeyLetter);
  }

  @Test
  public void setHotkeys_incorrectOrder3_normalize() {
    Integer[] inputKeys = {KeyEvent.VK_B, KeyEvent.VK_CONTROL};

    BarcodeReaderEmulator.setHotkeys(inputKeys, false);

    assertEquals(CTRL, BarcodeReaderEmulator.specialKey);
    assertEquals(emptyList(), BarcodeReaderEmulator.additionalSpecialKeys);
    assertEquals((int) ('B'), BarcodeReaderEmulator.hotkeyLetter);
  }

  @Test
  public void setHotkeys_fourKeys_setCorrectly() {
    Integer[] inputKeys = {KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_ALT, KeyEvent.VK_B};

    BarcodeReaderEmulator.setHotkeys(inputKeys, false);

    assertEquals(SHIFT, BarcodeReaderEmulator.specialKey);
    assertEquals(asList(CTRL, ALT), BarcodeReaderEmulator.additionalSpecialKeys);
    assertEquals((int) ('B'), BarcodeReaderEmulator.hotkeyLetter);
  }

  @Test
  public void setHotkeys_duplicates_removeDuplicates() {
    Integer[] inputKeys = {
      KeyEvent.VK_CONTROL, KeyEvent.VK_CONTROL, KeyEvent.VK_ALT, KeyEvent.VK_B
    };

    BarcodeReaderEmulator.setHotkeys(inputKeys, false);

    assertEquals(CTRL, BarcodeReaderEmulator.specialKey);
    assertEquals(singletonList(ALT), BarcodeReaderEmulator.additionalSpecialKeys);
    assertEquals((int) ('B'), BarcodeReaderEmulator.hotkeyLetter);
  }

  @Test
  public void setHotkeys_twoChars_doNothing() {
    Integer[] inputKeys = {KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_Z, KeyEvent.VK_B};

    BarcodeReaderEmulator.setHotkeys(inputKeys, false);

    assertNull(BarcodeReaderEmulator.specialKey);
    assertNull(BarcodeReaderEmulator.additionalSpecialKeys);
    assertEquals(0, BarcodeReaderEmulator.hotkeyLetter);
  }
}
