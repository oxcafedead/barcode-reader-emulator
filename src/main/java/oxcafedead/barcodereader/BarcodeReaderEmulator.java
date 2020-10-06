package oxcafedead.barcodereader;

import oxcafedead.barcodereader.ui.BugReporter;
import oxcafedead.barcodereader.ui.MainWindow;

public class BarcodeReaderEmulator {

  public static void main(String[] args) {
    try {
      new MainWindow();
    } catch (Exception anyException) {
      anyException.printStackTrace();
      new BugReporter().uncaughtException(Thread.currentThread(), anyException);
    }
  }
}
