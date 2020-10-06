package oxcafedead.barcodereader.ui;

import oxcafedead.barcodereader.decode.BarcodeDecoder;
import oxcafedead.barcodereader.keybind.HotkeyBindManager;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Label;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
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

import static java.awt.event.KeyEvent.*;
import static java.util.Collections.singletonList;
import static oxcafedead.barcodereader.keybind.HotkeyBindManager.SpecialKey.ALT;
import static oxcafedead.barcodereader.keybind.HotkeyBindManager.SpecialKey.CTRL;

public class MainWindow extends JFrame {

  private static final String DEFAULT_BARCODE_VALUE = "12345";

  private static final int WIDTH = 320;
  private static final int HEIGHT = 150;

  private static final Integer DEFAULT_KEY_DELAY = 20;
  public static final Integer DELAY_MIN = 10;
  public static final Integer DELAY_MAX = 100;
  public static final Integer DELAY_INCREMENT = 1;

  private Thread hotkeyListener;
  private JTextField barcodeField;
  private JSpinner delaySpinner;
  private JTextField hkField;

  private final List<Integer> keyCodeRecordings = new ArrayList<>();

  // screenshot
  Point screenStart;
  Point screenEnd;

  // hotkey
  HotkeyBindManager.SpecialKey specialKey = CTRL;
  List<HotkeyBindManager.SpecialKey> additionalSpecialKeys = singletonList(ALT);
  int key = 'G';

  public MainWindow()
      throws HeadlessException, ClassNotFoundException, UnsupportedLookAndFeelException,
          InstantiationException, IllegalAccessException {
    GraphicsConfiguration graphicsConfiguration =
        GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDefaultConfiguration();
    var frame = new JFrame("Barcode Reader Emulator");

    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

    frame.setSize(WIDTH, HEIGHT);
    frame.setResizable(false);
    Util.loadAppIcon().ifPresent(frame::setIconImage);
    var frameP = new JPanel(new GridLayout(3, 2, 10, 10));
    frame.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 10));
    frame.add(frameP);

    frameP.add(new Label("Value"));
    var valueGroup = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));

    var columns = 10;
    barcodeField = new JTextField(DEFAULT_BARCODE_VALUE, columns - 2);
    valueGroup.add(barcodeField);

    frameP.add(valueGroup);

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
            if (keyCodeRecordings.size() > 3) {
              keyCodeRecordings.clear();
            }
            keyCodeRecordings.add(e.getExtendedKeyCode());
          }
        });
    setHotkeyUiField(specialKey, additionalSpecialKeys, (char) key);

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
                              setHotkeyUiField(specialKey, additionalSpecialKeys, (char) key);
                            });
                  }
                });
    keyCodeChecker.setUncaughtExceptionHandler(new BugReporter(frame));
    keyCodeChecker.setDaemon(true);
    keyCodeChecker.start();

    frameP.add(new JLabel("Input Key Delay"));
    delaySpinner = new JSpinner();
    delaySpinner.setValue(20);
    SpinnerNumberModel numberEditor =
        new SpinnerNumberModel(DEFAULT_KEY_DELAY, DELAY_MIN, DELAY_MAX, DELAY_INCREMENT);
    delaySpinner.setModel(numberEditor);
    JFormattedTextField jftf = ((JSpinner.NumberEditor) delaySpinner.getEditor()).getTextField();
    jftf.setColumns(columns);
    frameP.add(delaySpinner);

    var shot = new JButton("\uD83D\uDCF8");
    shot.setToolTipText(
        "Some hotkeys may not work, for example ones already bind in the "
            + "OS or in the parent app like browser.");
    shot.setPreferredSize(new Dimension(43, 22));
    final Robot robot;
    try {
      robot = new Robot();
    } catch (AWTException awtException) {
      new BugReporter().uncaughtException(Thread.currentThread(), awtException);
      return;
    }
    shot.addActionListener(
        e ->
            SwingUtilities.invokeLater(
                () -> {
                  BufferedImage screenShotImg;
                  Rectangle maximumWindowBounds =
                      GraphicsEnvironment.getLocalGraphicsEnvironment()
                          .getDefaultScreenDevice()
                          .getDefaultConfiguration()
                          .getBounds();
                  final var screenWidth = (int) maximumWindowBounds.getWidth();
                  final var screenHeight = (int) maximumWindowBounds.getHeight();
                  screenShotImg =
                      robot.createScreenCapture(new Rectangle(0, 0, screenWidth, screenHeight));

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

                          try {
                            final var cropped =
                                screenShotImg.getSubimage(
                                    Math.min(screenEnd.x, screenStart.x),
                                    Math.min(screenEnd.y, screenStart.y),
                                    Math.abs(screenEnd.x - screenStart.x),
                                    Math.abs(screenEnd.y - screenStart.y));
                            final var byteArrayOutputStream = new ByteArrayOutputStream();
                            ImageIO.write(cropped, "jpeg", byteArrayOutputStream);
                            final var barcodeInfo =
                                new BarcodeDecoder()
                                    .decodeImage(
                                        new ByteArrayInputStream(
                                            byteArrayOutputStream.toByteArray()));
                            barcodeField.setText(barcodeInfo.getText());
                          } catch (IOException ioe) {
                            new BugReporter(frame).uncaughtException(Thread.currentThread(), ioe);
                          } catch (BarcodeDecoder.BarcodeDecodingException barcodeError) {
                            final var jDialog = new JDialog(frame);
                            JOptionPane.showMessageDialog(
                                frame,
                                "Barcode could not be read! Please try again (maybe try to zoom in a bit).");
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
                }));

    valueGroup.add(shot);

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

  private void setHotkeyUiField(
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

  private String getDisplayName(HotkeyBindManager.SpecialKey specialKey) {
    return specialKey.name().charAt(0) + specialKey.name().substring(1).toLowerCase();
  }
}
