package oxcafedead.barcodereader.encode.windows;

import org.junit.Before;
import org.junit.Test;

import java.awt.Robot;
import java.awt.event.KeyEvent;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class WindowsUnicodeKeyEncoderTest {

  WindowsUnicodeKeyEncoder encoder;

  Robot robot;

  @Before
  public void setUp() {
    robot = mock(Robot.class);
    encoder = new WindowsUnicodeKeyEncoder(robot);
  }

  @Test
  public void encode_smallChar() {
    encoder.encode('a');
    var inOrder = inOrder(robot);
    inOrder.verify(robot).keyPress(KeyEvent.VK_A);
  }

  @Test
  public void encode_smallChar2() {
    encoder.encode('l');
    var inOrder = inOrder(robot);
    inOrder.verify(robot).keyPress(KeyEvent.VK_L);
  }

  @Test
  public void encode_bigChar() {
    encoder.encode('A');
    var inOrder = inOrder(robot);
    inOrder.verify(robot).keyPress(KeyEvent.VK_SHIFT);
    inOrder.verify(robot).keyPress(KeyEvent.VK_A);
  }

  @Test
  public void encode_bigChar2() {
    encoder.encode('X');
    var inOrder = inOrder(robot);
    inOrder.verify(robot).keyPress(KeyEvent.VK_SHIFT);
    inOrder.verify(robot).keyPress(KeyEvent.VK_X);
  }

  @Test
  public void encode_specialChar() {
    encoder.encode('~');
    var inOrder = inOrder(robot);
    inOrder.verify(robot).keyPress(KeyEvent.VK_SHIFT);
    inOrder.verify(robot).keyPress(KeyEvent.VK_BACK_QUOTE);
  }

  @Test
  public void encode_specialChar2() {
    encoder.encode('&');
    var inOrder = inOrder(robot);
    inOrder.verify(robot).keyPress(KeyEvent.VK_SHIFT);
    inOrder.verify(robot).keyPress(KeyEvent.VK_7);
  }

  @Test
  public void encode_specialChar3() {
    encoder.encode('_');
    var inOrder = inOrder(robot);
    inOrder.verify(robot).keyPress(KeyEvent.VK_SHIFT);
    inOrder.verify(robot).keyPress(KeyEvent.VK_MINUS);
  }

  @Test
  public void encode_number() {
    encoder.encode('5');
    var inOrder = inOrder(robot);
    inOrder.verify(robot).keyPress(KeyEvent.VK_5);
  }

  @Test
  public void encode_number2() {
    encoder.encode('0');
    var inOrder = inOrder(robot);
    inOrder.verify(robot).keyPress(KeyEvent.VK_0);
  }

  @Test
  public void encode_number3() {
    encoder.encode('1');
    var inOrder = inOrder(robot);
    inOrder.verify(robot).keyPress(KeyEvent.VK_1);
  }
}
