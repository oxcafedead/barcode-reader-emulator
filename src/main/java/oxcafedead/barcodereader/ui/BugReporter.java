package oxcafedead.barcodereader.ui;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Label;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.TextAttribute;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BugReporter implements Thread.UncaughtExceptionHandler {

  private static final int WIDTH = 500;
  private static final int HEIGHT = 300;

  private static final String REPORT_TO_GITHUB_URL =
      "https://github.com/oxcafedead/barcode-reader-emulator/issues/new";

  private final JFrame frame;

  public BugReporter() {
    this(new JFrame());
  }

  public BugReporter(JFrame frame) {
    this.frame = frame;
  }

  @Override
  public void uncaughtException(Thread thread, Throwable throwable) {
    var exceptionDlg = new JDialog(frame, true);
    exceptionDlg.setResizable(false);
    exceptionDlg.setLocationRelativeTo(frame);
    exceptionDlg.setSize(WIDTH, HEIGHT);
    Point locationPoint = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
    locationPoint.move(
        locationPoint.getLocation().x - WIDTH / 2, locationPoint.getLocation().y - HEIGHT / 2);
    exceptionDlg.setLocation(locationPoint);
    exceptionDlg.setTitle("Error");
    var gridLayout = new FlowLayout(FlowLayout.CENTER, 5, 5);
    exceptionDlg.setLayout(gridLayout);

    exceptionDlg.add(
        new Label("Error has occurred! You may submit an issue to github:", Label.CENTER));

    var linkBtn = new JLabel(REPORT_TO_GITHUB_URL, SwingConstants.CENTER);
    linkBtn.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            openWebpage(URI.create(REPORT_TO_GITHUB_URL));
          }
        });
    linkBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    Map<TextAttribute, Object> fontAttributes = new HashMap<>();
    fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
    fontAttributes.put(TextAttribute.FOREGROUND, Color.BLUE);
    var boldUnderline = new Font("Serif", Font.BOLD, 12).deriveFont(fontAttributes);
    linkBtn.setFont(boldUnderline);
    exceptionDlg.add(linkBtn);

    var stacktraceArea =
        new JTextArea(
            Stream.concat(
                    Stream.of(throwable.getClass() + ": " + throwable.getMessage()),
                    Stream.of(throwable.getStackTrace()).map(StackTraceElement::toString))
                .collect(Collectors.joining("\n")),
            10,
            65);
    stacktraceArea.setEditable(false);
    exceptionDlg.add(stacktraceArea);

    var okBtn = new JButton("OK");
    okBtn.addActionListener(e -> exceptionDlg.dispose());
    okBtn.setSize(100, 150);
    exceptionDlg.add(okBtn);

    Util.loadAppIcon().ifPresent(exceptionDlg::setIconImage);
    exceptionDlg.setAlwaysOnTop(true);
    exceptionDlg.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            exceptionDlg.dispose();
            super.windowClosing(e);
          }

          @Override
          public void windowClosed(WindowEvent e) {
            frame.dispose();
            super.windowClosed(e);
          }
        });
    exceptionDlg.setVisible(true);
  }

  private void openWebpage(URI uri) {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(uri);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
