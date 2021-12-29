package oxcafedead.barcodereader.ui;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class UtilTest {

  @Test
  public void loadAppIcon_notNullIcon() {
    assertTrue(Util.loadAppIcon().isPresent());
  }
}
