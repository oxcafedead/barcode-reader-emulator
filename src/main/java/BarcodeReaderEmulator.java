import keybind.HotkeyBindFactory;
import keybind.HotkeyBindManager;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.TextAttribute;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.awt.event.KeyEvent.*;
import static keybind.HotkeyBindManager.SpecialKey.*;

public class BarcodeReaderEmulator {

  public static final String DEFAULT_BARCODE_VALUE = "12345";
  public static final String DEFAULT_HOTKEY = "ctrl_alt_g";
  public static final String DEFAULT_KEY_DELAY = "20";
  public static final int WIDTH = 320;
  public static final int HEIGHT = 150;
  public static final String REPORT_TO_GITHUB_URL =
      "https://github.com/oxcafedead/barcode-reader-emulator/issues/new";

  private static Thread hotkeyListener;
  private static JTextField barcodeField;
  private static JSpinner delaySpinner;

  private static List<Integer> keyCodeRecordings = new ArrayList<>();
  static HotkeyBindManager.SpecialKey specialKey;
  static List<HotkeyBindManager.SpecialKey> additionalSpecialKeys;
  static int hotkeyLetter;
  private static JTextField hkField;

  public static void main(String[] args) {

    GraphicsConfiguration graphicsConfiguration =
        GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDefaultConfiguration();
    var frame = new JFrame("Barcode Reader Emulator");
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException
        | InstantiationException
        | IllegalAccessException
        | UnsupportedLookAndFeelException e) {
      System.err.println("Could not set system look and feel");
      e.printStackTrace();
      System.exit(1);
    }
    frame.setSize(WIDTH, HEIGHT);
    frame.setResizable(false);
    findIcon().ifPresent(frame::setIconImage);
    var frameP = new JPanel(new GridLayout(3, 2, 10, 10));
    frame.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 10));
    frame.add(frameP);

    frameP.add(new Label("Value"));

    var columns = 10;
    barcodeField = new JTextField(DEFAULT_BARCODE_VALUE, columns);
    frameP.add(barcodeField);

    frameP.add(new Label("Hotkey"));
    var hotkeyGroup = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    hkField = new JTextField();
    ToolTipManager.sharedInstance().setInitialDelay(50);
    hkField.setToolTipText(
        "Click on this field and enter a new hotkey combination, "
            + "can be two keys or tree keys, e.g. Alt + Z or"
            + " Ctrl + Shift + Q");
    hkField.setEditable(false);
    hkField.setColumns(columns - 2);
    hotkeyGroup.add(hkField);
    var syncBtn = new JButton("\uD83D\uDD03");
    syncBtn.setToolTipText(
        "Some hotkeys may not work, for example ones already bind in the "
            + "OS or in the parent app like browser.");
    syncBtn.setPreferredSize(new Dimension(43, 22));
    hotkeyGroup.add(syncBtn);
    frameP.add(hotkeyGroup);

    hkField.addKeyListener(
        new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent e) {
            keyCodeRecordings.add(e.getExtendedKeyCode());
          }
        });

    Thread keyCodeChecker =
        Executors.defaultThreadFactory()
            .newThread(
                () -> {
                  while (true) {
                    try {
                      Thread.sleep(300);
                    } catch (InterruptedException e) {
                      Thread.currentThread().interrupt();
                      return;
                    }

                    setHotkeys(keyCodeRecordings.toArray(new Integer[0]), true);
                  }
                });
    keyCodeChecker.setUncaughtExceptionHandler(getUncaughtExceptionHandler(frame));
    keyCodeChecker.setDaemon(true);
    keyCodeChecker.start();

    frameP.add(new JLabel("Input Key Delay"));
    delaySpinner = new JSpinner();
    delaySpinner.setValue(20);
    SpinnerNumberModel numberEditor =
        new SpinnerNumberModel(
            (Integer) Integer.parseInt(DEFAULT_KEY_DELAY),
            (Integer) 10,
            (Integer) 100,
            (Integer) 1);
    delaySpinner.setModel(numberEditor);
    JFormattedTextField jftf = ((JSpinner.NumberEditor) delaySpinner.getEditor()).getTextField();
    jftf.setColumns(columns);
    frameP.add(delaySpinner);

    frame.setVisible(true);
    frame.setLocation(
        (graphicsConfiguration.getBounds().width - WIDTH) / 2,
        (graphicsConfiguration.getBounds().height - HEIGHT) / 2);
    frame.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            hotkeyListener.interrupt();
            frame.dispose();
            super.windowClosing(e);
          }
        });

    hotkeyLetter = 'G';
    specialKey = CTRL;
    additionalSpecialKeys = Collections.singletonList(ALT);
    setJFIeldHotkey(specialKey, additionalSpecialKeys, (char) hotkeyLetter);
    hotkeyListener = new HotkeyListeningThread(frame);
    hotkeyListener.start();

    syncBtn.addActionListener(
        e -> {
          hotkeyListener.interrupt();
          hotkeyListener = new HotkeyListeningThread(frame);
          hotkeyListener.start();
        });
  }

  private static class HotkeyListeningThread extends Thread {
    public HotkeyListeningThread(JFrame frame) {
      this.setDaemon(true);
      this.setUncaughtExceptionHandler(BarcodeReaderEmulator.getUncaughtExceptionHandler(frame));
    }

    @Override
    public void run() {
      HotkeyBindManager hotkeyBinder = HotkeyBindFactory.INSTANCE.getHotkeyBinder();
      Optional<Integer> bindId =
          hotkeyBinder.bindKey(
              hotkeyLetter,
              specialKey,
              additionalSpecialKeys.toArray(new HotkeyBindManager.SpecialKey[0]));
      if (bindId.isEmpty()) {
        throw new IllegalStateException("Could not bind hotkey.");
      }

      final Robot robot;
      try {
        robot = new Robot();
      } catch (AWTException e) {
        throw new IllegalStateException(e);
      }

      hotkeyBinder.listen(() -> emulateBarCodeRead(robot));

      hotkeyBinder.unbindKey(bindId.get());
    }
  }

  static void setHotkeys(Integer[] keyCodes, boolean updateUiField) {
    HotkeyBindManager.SpecialKey specialKey = null;
    List<HotkeyBindManager.SpecialKey> additionalSpecialKeys = new ArrayList<>();
    int keyCode = 0;
    List<Integer> normalized =
        Stream.of(keyCodes)
            .filter(
                k ->
                    k >= VK_A && k <= VK_Z
                        || Set.of(VK_CONTROL, VK_SHIFT, VK_WINDOWS, VK_ALT).contains(k))
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    for (int key : normalized) {
      boolean isLetter = key >= VK_A && key <= VK_Z;
      if (isLetter && keyCode != 0) {
        return;
      } else if (isLetter) {
        keyCode = key;
      } else if (specialKey == null) {
        specialKey = getKeyLabel(key);
      } else {
        additionalSpecialKeys.add(getKeyLabel(key));
      }
    }

    if (keyCode != 0 && specialKey != null) {
      BarcodeReaderEmulator.hotkeyLetter = keyCode;
      BarcodeReaderEmulator.specialKey = specialKey;
      BarcodeReaderEmulator.additionalSpecialKeys = additionalSpecialKeys;
      keyCodeRecordings.clear();
      if (updateUiField) {
        setJFIeldHotkey(specialKey, additionalSpecialKeys, (char) keyCode);
      }
    }
  }

  private static void setJFIeldHotkey(
      HotkeyBindManager.SpecialKey specialKey,
      List<HotkeyBindManager.SpecialKey> additionalSpecialKeys,
      char keyCode) {
    StringBuilder displayNameBuilder = new StringBuilder(getDisplayName(specialKey));
    for (HotkeyBindManager.SpecialKey additionalSpecialKey : additionalSpecialKeys) {
      displayNameBuilder.append(" + ").append(getDisplayName(additionalSpecialKey));
    }
    displayNameBuilder.append(" + ").append(keyCode);
    hkField.setText(displayNameBuilder.toString());
  }

  private static String getDisplayName(HotkeyBindManager.SpecialKey specialKey) {
    return specialKey.name().charAt(0) + specialKey.name().substring(1).toLowerCase();
  }

  private static Thread.UncaughtExceptionHandler getUncaughtExceptionHandler(JFrame frame) {
    return (thread, throwable) -> {
      var exceptionDlg = new JDialog(frame, true);
      exceptionDlg.setLocationRelativeTo(frame);
      exceptionDlg.setSize(500, 300);
      exceptionDlg.setLocation(
          frame.getLocation().x + (WIDTH - 400) / 2, frame.getLocation().y + (HEIGHT - 100) / 2);
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

      findIcon().ifPresent(exceptionDlg::setIconImage);
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
    };
  }

  private static HotkeyBindManager.SpecialKey getKeyLabel(Integer firstKeyCode) {
    switch (firstKeyCode) {
      case KeyEvent.VK_SHIFT:
        return SHIFT;
      case KeyEvent.VK_CONTROL:
        return CTRL;
      case KeyEvent.VK_WINDOWS:
        return WIN;
      case KeyEvent.VK_ALT:
        return ALT;
      default:
        return null;
    }
  }

  private static Optional<Image> findIcon() {
    return Optional.ofNullable(
        new ImageIcon(BarcodeReaderEmulator.class.getResource("icon.png"))
            .getImage()
            .getScaledInstance(32, 32, 0));
  }

  private static void emulateBarCodeRead(Robot robot) {
    barcodeField
        .getText()
        .chars()
        .forEachOrdered(
            keycode -> {
              try {
                Thread.sleep(Long.parseLong(delaySpinner.getValue().toString()));
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              robot.keyPress(keycode);
            });
    Toolkit.getDefaultToolkit().beep();
  }

  private static void openWebpage(URI uri) {
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
