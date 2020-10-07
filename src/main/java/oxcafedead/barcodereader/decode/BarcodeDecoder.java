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
  public String decodeImage(InputStream inputStream) throws BarcodeDecodingException {
    try {
      BinaryBitmap bitmap =
          new BinaryBitmap(
              new HybridBinarizer(new BufferedImageLuminanceSource(ImageIO.read(inputStream))));
      if (bitmap.getWidth() < bitmap.getHeight() && bitmap.isRotateSupported()) {
        bitmap = bitmap.rotateCounterClockwise();
      }
      return decode(bitmap);
    } catch (IOException e) {
      throw new BarcodeDecodingException(e);
    }
  }

  private String decode(BinaryBitmap bitmap) throws BarcodeDecodingException {
    Reader reader = new MultiFormatReader();
    try {
      Result result = reader.decode(bitmap);
      return result.getText();
    } catch (Exception e) {
      throw new BarcodeDecodingException(e);
    }
  }

  public static class BarcodeDecodingException extends Exception {
    BarcodeDecodingException(Throwable cause) {
      super(cause);
    }
  }
}
