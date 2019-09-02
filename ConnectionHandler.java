package ccbd.proxy.yogya;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConnectionHandler implements Runnable {

  Socket clientSocket;
  static HashMap<String, File> cache;

  public ConnectionHandler(Socket clientSocket) {
    super();
    this.clientSocket = clientSocket;
  }

  // runnable implemented by the ConnectionHandler class
  public void run() {

    int port = 80;
    String host = null;
    Socket remoteSocket = null;
    InputStream clientInput = null;
    OutputStream clientOutput = null; // output to be given to the client
    OutputStream remoteOutput = null; // Data to be given to the remote server

    try {
      clientInput = clientSocket.getInputStream();
      clientOutput = clientSocket.getOutputStream();
      BufferedReader clientReader = new BufferedReader(new InputStreamReader(clientInput));// input is taken
                                                                                           // from client and
                                                                                           // is wrapped to
                                                                                           // access the bytes
                                                                                           // of data as
                                                                                           // string

      String requestLine = clientReader.readLine();
      String[] requestLineTokens = requestLine.split(" ");
      String httpMethod = requestLineTokens[0]; // first word is http method GET/POST/HEAD...
      String httpResource = requestLineTokens[1]; // second word is the requested resource url
      boolean https = httpMethod.equalsIgnoreCase("CONNECT"); // method CONNECT means https

      List<String> inputLines = new ArrayList<String>();

      while (requestLine.length() > 0) {
        inputLines.add(requestLine);
        if (requestLine.startsWith("Host: ")) { // To find the IP address of the remote host
          String[] arr = requestLine.split(":");
          host = arr[1].trim();
          try {
            if (arr.length == 3) {
              port = Integer.parseInt(arr[2]);
            } else if (https) {
              port = 443; // https
            } else {
              port = 80; // http
            }
          } catch (NumberFormatException e) {
            throw new IOException(e);
          }
        }
        requestLine = clientReader.readLine();// To read each line of data from the client
      }

      byte[] content = Cache.getCachedContent(httpResource);
      if (content != null) {
        System.out.println("Serving cached content (size = " + content.length + ") for " + httpResource);
        clientOutput.write(content);
      } else {
        if (host != null) { // If the IP address is found then
          remoteSocket = new Socket(host, port); // create a new connection to the remote server at port 80
          remoteOutput = remoteSocket.getOutputStream();
          PrintWriter remoteWriter = null;
          if (https) {
            clientOutput.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
            clientOutput.flush();
            forwardRequestHeaders(inputLines, remoteWriter);
          } else {
            remoteWriter = new PrintWriter(new OutputStreamWriter(remoteOutput));
            forwardRequestHeaders(inputLines, remoteWriter);
            remoteWriter.println();
            remoteWriter.flush();
          }
          Pipe client2remote = new Pipe(clientInput, remoteOutput);
          new Thread(client2remote).start();

          System.out.println("\nHttpResponse:");
          System.out.println("-------------");

          InputStream remoteInput = remoteSocket.getInputStream();// Receive data from the remote server
          ByteArrayOutputStream cacheableStream = new ByteArrayOutputStream();
          connectTee(remoteInput, clientOutput, cacheableStream);
          content = cacheableStream.toByteArray();
          Cache.cacheContent(httpResource, content);
        }
        System.out.println(new String(content));
        System.out.println("\nDone handling " + httpResource + " on " + host);
      }

    } catch (IOException e) {
      //e.printStackTrace();
      System.out.println("Error handling " + host + " " + e.getMessage());
    } finally {
      try {
        if (clientOutput != null) {
          clientOutput.close();
        }
        if (remoteSocket != null) {
          remoteSocket.close();
        }
      } catch (IOException e) {
        System.out.println("Error cleaning up " + e.getMessage());
      }
    }
  }

  private void forwardRequestHeaders(List<String> headerLines, PrintWriter remoteWriter) {
    System.out.println("HttpRequest:");
    System.out.println("------------");
    for (String headerLine : headerLines) {
      System.out.println(headerLine);
      if (remoteWriter != null) {
        remoteWriter.println(headerLine);
      }
    }
  }

  // one input -> sent to -> two output streams (hence Tee)
  private void connectTee(InputStream inputStream, OutputStream outputStream1, OutputStream outputStream2)
      throws IOException {
    byte[] buf = new byte[4096];
    int len;
    while ((len = inputStream.read(buf)) != -1) {
      outputStream1.write(buf, 0, len);
      outputStream1.flush();
      outputStream2.write(buf, 0, len);
      outputStream2.flush();
    }
  }

}
