package oxcafedead.barcodereader.encode.windows;

import oxcafedead.barcodereader.encode.KeyEncoder;

import java.awt.Robot;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.awt.event.KeyEvent.*;

public class WindowsUnicodeKeyEncoder implements KeyEncoder {

  private static final char[] supportedSpecialChars = {
    '`', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '-', '=', ';', '\'', ',', '.', '/', '\\',
  };
  private static final char[] supportedSpecialChars_shift = {
    '~', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '+', ':', '"', '<', '>', '?', '|',
  };
  private static final char[] supportedSpecialChars_code = {
    VK_BACK_QUOTE,
    VK_1,
    VK_2,
    VK_3,
    VK_4,
    VK_5,
    VK_6,
    VK_7,
    VK_8,
    VK_9,
    VK_0,
    VK_MINUS,
    VK_EQUALS,
    VK_SEMICOLON,
    VK_QUOTE,
    VK_COMMA,
    VK_PERIOD,
    VK_SLASH,
    VK_BACK_SLASH
  };

  private final Robot robot;

  public WindowsUnicodeKeyEncoder(Robot robot) {
    this.robot = robot;
  }

  @Override
  public void encode(int keyCode) {
    if ('a' <= keyCode && 'z' >= keyCode) {
      var correctCode = VK_A + (keyCode - 'a');
      robot.keyPress(correctCode);
      robot.keyRelease(correctCode);
      return;
    }
    if ('A' <= keyCode && 'Z' >= keyCode) {
      var correctCode = VK_A + (Character.toLowerCase(keyCode) - 'a');
      robot.keyPress(VK_SHIFT);
      robot.keyPress(correctCode);
      robot.keyRelease(correctCode);
      robot.keyRelease(VK_SHIFT);
      return;
    }
    int vkCode = -1;
    for (var i = 0; i < supportedSpecialChars.length; i++) {
      if (keyCode == supportedSpecialChars[i]) {
        vkCode = supportedSpecialChars_code[i];
        break;
      }
    }
    if (vkCode != -1) {
      robot.keyPress(vkCode);
      robot.keyRelease(vkCode);
      return;
    }

    for (var i = 0; i < supportedSpecialChars_shift.length; i++) {
      if (keyCode == supportedSpecialChars_shift[i]) {
        vkCode = supportedSpecialChars_code[i];
      }
    }
    if (vkCode != -1) {
      robot.keyPress(VK_SHIFT);
      robot.keyPress(vkCode);
      robot.keyRelease(vkCode);
      robot.keyRelease(VK_SHIFT);
      return;
    }
    Logger.getAnonymousLogger()
        .log(Level.WARNING, () -> "Key code " + keyCode + " is not supported");
  }
}
