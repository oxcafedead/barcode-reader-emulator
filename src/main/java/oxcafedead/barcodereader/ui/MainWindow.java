package oxcafedead.barcodereader.ui;

import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_ALT;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_SHIFT;
import static java.awt.event.KeyEvent.VK_WINDOWS;
import static java.awt.event.KeyEvent.VK_Z;
import static java.util.Collections.singletonList;
import static javax.swing.JOptionPane.showMessageDialog;
import static oxcafedead.barcodereader.keybind.HotkeyBindManager.SpecialKey.ALT;
import static oxcafedead.barcodereader.keybind.HotkeyBindManager.SpecialKey.CTRL;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.*;

import oxcafedead.barcodereader.decode.BarcodeDecoder;
import oxcafedead.barcodereader.keybind.HotkeyBindManager;

@SuppressWarnings("java:S1948")
public class MainWindow extends JFrame {

  private static final String DEFAULT_BARCODE_VALUE = "12345";

  private static final int WIDTH = 320;
  private static final int HEIGHT = 150;

  private static final Integer DEFAULT_KEY_DELAY = 20;
  public static final Integer DELAY_MIN = 10;
  public static final Integer DELAY_MAX = 100;
  public static final Integer DELAY_INCREMENT = 1;

  private GraphicsConfiguration graphicsConfiguration;

  private Thread hotkeyListener;
  private JTextField barcodeField;
  private JSpinner delaySpinner;

  private final List<Integer> keyCodeRecordings = new ArrayList<>();

  // screenshot
  private Point screenStart;
  private Point screenEnd;

  // hotkey
  private HotkeyBindManager.SpecialKey specialKey = CTRL;
  private List<HotkeyBindManager.SpecialKey> additionalSpecialKeys = singletonList(ALT);
  private int key = 'G';

  private Robot robot;

  public MainWindow()
      throws HeadlessException, ClassNotFoundException, UnsupportedLookAndFeelException,
          InstantiationException, IllegalAccessException {

    try {
      robot = new Robot();
    } catch (AWTException awtException) {
      new BugReporter().uncaughtException(Thread.currentThread(), awtException);
      return;
    }

    JFrame frame = prepareFrame();

    var frameP = new JPanel(new GridLayout(3, 2, 10, 10));
    frame.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 10));
    frame.add(frameP);

    var textWidth = 10;
    var valueGroup = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));

    addValueSection(frame, frameP, textWidth, valueGroup);

    JButton syncBtn = addHotkeySection(frame, frameP, textWidth);

    addInputDelaySection(frameP, textWidth);

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

    hotkeyListener = buildHotkeyListener(frame);
    hotkeyListener.start();

    syncBtn.addActionListener(
        e -> {
          hotkeyListener.interrupt();
          hotkeyListener = buildHotkeyListener(frame);
          hotkeyListener.start();
        });
  }

  private void addInputDelaySection(JPanel frameP, int textWidth) {
    frameP.add(new JLabel("Input Key Delay"));
    delaySpinner = new JSpinner();
    delaySpinner.setValue(20);
    SpinnerNumberModel numberEditor =
        new SpinnerNumberModel(DEFAULT_KEY_DELAY, DELAY_MIN, DELAY_MAX, DELAY_INCREMENT);
    delaySpinner.setModel(numberEditor);
    JFormattedTextField jftf = ((JSpinner.NumberEditor) delaySpinner.getEditor()).getTextField();
    jftf.setColumns(textWidth);
    frameP.add(delaySpinner);
  }

  private void addValueSection(JFrame frame, JPanel frameP, int textWidth, JPanel valueGroup) {
    frameP.add(new Label("Value"));

    barcodeField = new JTextField(DEFAULT_BARCODE_VALUE, textWidth - 2);
    valueGroup.add(barcodeField);

    frameP.add(valueGroup);
    var shot = new JButton("\uD83D\uDCF8");
    shot.setToolTipText(
        "Some hotkeys may not work, for example ones already bind in the "
            + "OS or in the parent app like browser.");
    shot.setPreferredSize(new Dimension(43, 22));

    shot.addActionListener(e -> SwingUtilities.invokeLater(() -> makeScreenShot(frame)));

    valueGroup.add(shot);
  }

  private void makeScreenShot(JFrame frame) {
    BufferedImage screenShotImg;
    Rectangle maximumWindowBounds =
        GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDefaultConfiguration()
            .getBounds();
    final var screenWidth = (int) maximumWindowBounds.getWidth();
    final var screenHeight = (int) maximumWindowBounds.getHeight();
    screenShotImg = robot.createScreenCapture(new Rectangle(0, 0, screenWidth, screenHeight));

    try {
      File temp = File.createTempFile("screenshot", ".png");
      ImageIO.write(screenShotImg, "png", temp);
      temp.deleteOnExit();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }

    JFrame screenshotFrame = new JFrame();
    screenshotFrame.setUndecorated(true);
    screenshotFrame.setBounds(0, 0, screenWidth, screenHeight);

    screenStart = null;
    screenEnd = null;

    final var shotPanel =
        new JPanel() {

          @Override
          public Dimension getPreferredSize() {
            return new Dimension(screenWidth, screenHeight);
          }

          @Override
          public void paint(Graphics g) {
            g.drawImage(screenShotImg, 0, 0, screenWidth, screenHeight, null);

            g.setColor(new Color(250, 250, 250, 50));
            g.fillRect(0, 0, screenWidth, screenHeight);
            if (screenEnd != null && screenStart != null) {
              g.setColor(new Color(255, 0, 0, 40));
              g.fillRect(
                  Math.min(screenEnd.x, screenStart.x),
                  Math.min(screenEnd.y, screenStart.y),
                  Math.abs(screenEnd.x - screenStart.x),
                  Math.abs(screenEnd.y - screenStart.y));
            }
          }
        };
    screenshotFrame.add(shotPanel);
    screenshotFrame.pack();
    screenshotFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    screenshotFrame.setVisible(true);
    screenshotFrame.setResizable(false);
    shotPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));

    shotPanel.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            screenStart = e.getLocationOnScreen();
            screenEnd = e.getLocationOnScreen();
          }

          @Override
          public void mouseReleased(MouseEvent e) {
            screenEnd = e.getLocationOnScreen();
            screenshotFrame.setVisible(false);
            screenshotFrame.dispose();

            if (screenEnd.x == screenStart.x || screenEnd.y == screenStart.y) {
              return;
            }

            try {
              final var cropped =
                  screenShotImg.getSubimage(
                      Math.min(screenEnd.x, screenStart.x),
                      Math.min(screenEnd.y, screenStart.y),
                      Math.abs(screenEnd.x - screenStart.x),
                      Math.abs(screenEnd.y - screenStart.y));
              final var byteArrayOutputStream = new ByteArrayOutputStream();
              ImageIO.write(cropped, "jpeg", byteArrayOutputStream);
              final var barcode =
                  new BarcodeDecoder()
                      .decodeImage(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
              barcodeField.setText(barcode);
            } catch (IOException ioe) {
              new BugReporter(frame).uncaughtException(Thread.currentThread(), ioe);
            } catch (BarcodeDecoder.BarcodeDecodingException barcodeError) {
              showMessageDialog(
                  frame,
                  "Cannot decode barcode." + "\nPlease try again (maybe try to zoom in a bit).");
            }
          }
        });
    shotPanel.addMouseMotionListener(
        new MouseMotionAdapter() {
          @Override
          public void mouseDragged(MouseEvent e) {
            screenEnd = e.getLocationOnScreen();
            screenshotFrame.repaint();
          }
        });
  }

  private JButton addHotkeySection(JFrame frame, JPanel frameP, int columns) {
    frameP.add(new Label("Hotkey"));
    var hotkeyGroup = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    var hkField = new JTextField();
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
            if (keyCodeRecordings.size() > 3) {
              keyCodeRecordings.clear();
            }
            keyCodeRecordings.add(e.getExtendedKeyCode());
          }
        });
    StringBuilder displayNameBuilder1 = new StringBuilder(getDisplayName(specialKey));
    for (HotkeyBindManager.SpecialKey additionalSpecialKey1 : additionalSpecialKeys) {
      displayNameBuilder1.append(" + ").append(getDisplayName(additionalSpecialKey1));
    }
    displayNameBuilder1.append(" + ").append((char) key);
    hkField.setText(displayNameBuilder1.toString());

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

                    buildHotkey(keyCodeRecordings.toArray(new Integer[0]))
                        .ifPresent(
                            hk -> {
                              this.key = hk.key;
                              this.specialKey = hk.specialKey;
                              this.additionalSpecialKeys =
                                  Arrays.asList(hk.additionalSpecialKeys.clone());
                              keyCodeRecordings.clear();
                              StringBuilder displayNameBuilder =
                                  new StringBuilder(getDisplayName(specialKey));
                              for (HotkeyBindManager.SpecialKey additionalSpecialKey :
                                  additionalSpecialKeys) {
                                displayNameBuilder
                                    .append(" + ")
                                    .append(getDisplayName(additionalSpecialKey));
                              }
                              displayNameBuilder.append(" + ").append((char) key);
                              hkField.setText(displayNameBuilder.toString());
                            });
                  }
                });
    keyCodeChecker.setUncaughtExceptionHandler(new BugReporter(frame));
    keyCodeChecker.setDaemon(true);
    keyCodeChecker.start();
    return syncBtn;
  }

  private JFrame prepareFrame()
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          UnsupportedLookAndFeelException {
    graphicsConfiguration =
        GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDefaultConfiguration();
    var frame = new JFrame("Barcode Reader Emulator");

    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

    frame.setSize(WIDTH, HEIGHT);
    frame.setResizable(false);
    Util.loadAppIcon().ifPresent(frame::setIconImage);
    return frame;
  }

  private HotkeyListeningThread buildHotkeyListener(JFrame frame) {
    return new HotkeyListeningThread(
        frame,
        new HotKey(
            specialKey, additionalSpecialKeys.toArray(new HotkeyBindManager.SpecialKey[0]), key),
        barcodeField::getText,
        () -> Long.parseLong(delaySpinner.getValue().toString()));
  }

  public static Optional<HotKey> buildHotkey(Integer[] keyCodes) {
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
        return Optional.empty();
      } else if (isLetter) {
        keyCode = key;
      } else if (specialKey == null) {
        specialKey = Util.getKeyType(key);
      } else {
        additionalSpecialKeys.add(Util.getKeyType(key));
      }
    }

    if (keyCode != 0 && specialKey != null) {
      return Optional.of(
          new HotKey(
              specialKey,
              additionalSpecialKeys.toArray(new HotkeyBindManager.SpecialKey[0]),
              keyCode));
    }

    return Optional.empty();
  }

  private String getDisplayName(HotkeyBindManager.SpecialKey specialKey) {
    return specialKey.name().charAt(0) + specialKey.name().substring(1).toLowerCase();
  }
}
