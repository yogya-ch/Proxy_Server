package ccbd.proxy.yogya;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Proxy Server main class
 *
 */
public class App {
	public static void main(String[] args) {
	  int port = 8080;
	  if (args.length == 1) {
	    try {
        port = Integer.parseInt(args[0]);
        System.out.println("Using configured port " + port);
      } catch (Exception e) {
        System.out.println("Invalid port, error parsing " + args[0]);
      }
	  }
	  ServerSocket serverSocket = null;
		try {
      // creating a server socket object at specified port (default is 8080)
      serverSocket = new ServerSocket(port);
      System.out.println("Proxy Server up and running at port " + port);
      while (true) { // run for ever
      	// clientSocket is an object of type socket which accepts connections from clients
      	Socket clientSocket = serverSocket.accept();

      	//an object of the ConnectionHandler is created and clientSocket is passed as an argument to it
      	ConnectionHandler handler = new ConnectionHandler(clientSocket);

      	//connection handling is done in a separate thread
      	new Thread(handler).start();
      }
    } catch (IOException e) {
      System.out.println("Error in main proxy " + e.getMessage());
    } finally {
      if (serverSocket != null) {
        try {
          serverSocket.close();
        } catch (IOException e) {
        }
      }
    }
		
	}
}
