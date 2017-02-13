/*
1. Open socket on a port
2. Allocate an array to wait for packet from client
3. Allocate a buffer to store response
4. Get destination address and port from the received packet
5. Create a packet and send to the client
*/

import java.io.*;
import java.net.*;

public class UDPServer implements Runnable {
  public static void main(String[] args) {
    new Thread(new UDPServer()).start();
  }

  private DatagramSocket socket;      // socket of the server
  private byte[] buf = new byte[256]; // buffer for sending and receiving data

  public void run() {
    // Create the socket on the server side
    try {
      socket = new DatagramSocket(2017);
    } catch (SocketException e) {
      System.out.println("Server: SocketException!");
    }

    while (true) {
      // Wait for a request from the client
      DatagramPacket packet = new DatagramPacket(buf, buf.length);
      try {
        socket.receive(packet);   // Wait for the client request
      } catch (IOException e) {
        System.out.println("IOException to receive a request on server!");
      }
      // Record the received time of a request
      String tm = Long.toString(System.currentTimeMillis()) + ":";
      // Flush the buffer before being used as sending buffer
      for (int i = 0; i < buf.length; i++) {
        buf[i] = 0;
      }
      tm += Long.toString(System.currentTimeMillis());
      // Send the time of request received and reply sent on the server side
      buf = (tm.getBytes());
      // Send back the reply
      packet = new DatagramPacket(buf, buf.length, packet.getAddress(),
        packet.getPort());

      try {
        socket.send(packet);  // Send reply to client
      } catch(IOException e) {
        System.out.println("IOException to send a reply on server!");
      }
    }
  }
}
