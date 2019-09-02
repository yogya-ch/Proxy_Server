package ccbd.proxy.yogya;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

public class Pipe implements Runnable {

  private InputStream inputStream;
  private OutputStream outputStream;

  public Pipe(InputStream inputStream, OutputStream outputStream) {
    super();
    this.inputStream = inputStream;
    this.outputStream = outputStream;
  }

  public void run() {
    try {
      connect(inputStream, outputStream);
    } catch (SocketException se) {
      if (!se.getMessage().contains("Socket closed")) {
        se.printStackTrace();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void connect(InputStream inputStream, OutputStream outputStream) throws IOException {
    byte[] buf = new byte[4096];
    int len;
    while ((len = inputStream.read(buf)) != -1) {
      outputStream.write(buf, 0, len);
      outputStream.flush();
    }
  }

}
