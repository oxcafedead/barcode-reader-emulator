package oxcafedead.barcodereader.keybind.windows;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.win32.W32APIOptions;

import java.util.List;

/**
 * This is mostly taken from <a href="https://habr.com/ru/post/124567">one great article</a> and
 * adjusted according to MS documentation.
 *
 * @see <a href="https://docs.microsoft.com/en-us/windows/win32/api/winuser/ns-winuser-msg">MSG
 *     doc</a>
 * @see <a
 *     href="https://docs.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-registerhotkey">RegisterHotKey
 *     doc</a>
 */
public class User32 {
  static {
    Native.register(NativeLibrary.getInstance("user32", W32APIOptions.DEFAULT_OPTIONS));
  }

  public static final int WM_HOTKEY = 0x0312;
  public static final int PM_REMOVE = 0x0001;

  public static native boolean RegisterHotKey(Pointer hWnd, int id, int fsModifiers, int vk);

  public static native boolean UnregisterHotKey(Pointer hWnd, int id);

  public static native boolean PeekMessageA(
      MSG lpMsg, Pointer hWnd, int wMsgFilterMin, int wMsgFilterMax, int wRemoveMsg);

  public static class MSG extends Structure {
    public Pointer hWnd;
    public int message;
    public int wParam;
    public int lParam;
    public int time;
    public int pt;
    public int lPrivate;

    @Override
    protected List<String> getFieldOrder() {
      return List.of("hWnd", "message", "wParam", "lParam", "time", "pt", "lPrivate");
    }
  }
}
