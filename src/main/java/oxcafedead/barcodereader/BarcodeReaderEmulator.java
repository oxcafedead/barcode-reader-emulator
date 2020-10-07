package oxcafedead.barcodereader;

import oxcafedead.barcodereader.ui.BugReporter;
import oxcafedead.barcodereader.ui.MainWindow;

public class BarcodeReaderEmulator {

  public static void main(String[] args) {
    try {
      Thread.setDefaultUncaughtExceptionHandler(new BugReporter(new MainWindow()));
    } catch (Exception anyException) {
      anyException.printStackTrace();
      new BugReporter().uncaughtException(Thread.currentThread(), anyException);
    }
  }
}
