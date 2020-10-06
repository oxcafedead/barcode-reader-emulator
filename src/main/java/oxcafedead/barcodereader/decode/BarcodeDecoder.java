package oxcafedead.barcodereader.decode;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;

public class BarcodeDecoder {
  public BarcodeInfo decodeImage(InputStream inputStream) throws BarcodeDecodingException {
    try {
      BinaryBitmap bitmap =
          new BinaryBitmap(
              new HybridBinarizer(new BufferedImageLuminanceSource(ImageIO.read(inputStream))));
      if (bitmap.getWidth() < bitmap.getHeight()) {
        if (bitmap.isRotateSupported()) {
          bitmap = bitmap.rotateCounterClockwise();
        }
      }
      return decode(bitmap);
    } catch (IOException e) {
      throw new BarcodeDecodingException(e);
    }
  }

  private BarcodeInfo decode(BinaryBitmap bitmap) throws BarcodeDecodingException {
    Reader reader = new MultiFormatReader();
    try {
      Result result = reader.decode(bitmap);
      return new BarcodeInfo(result.getText(), result.getBarcodeFormat().toString());
    } catch (Exception e) {
      throw new BarcodeDecodingException(e);
    }
  }

  public static class BarcodeInfo {
    private final String text;
    private final String format;

    BarcodeInfo(String text, String format) {
      this.text = text;
      this.format = format;
    }

    public String getText() {
      return text;
    }

    public String getFormat() {
      return format;
    }
  }

  public static class BarcodeDecodingException extends Exception {
    BarcodeDecodingException(Throwable cause) {
      super(cause);
    }
  }
}
