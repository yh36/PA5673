/*
1. Open socket on a port
2. Allocate an array to wait for packet from client
3. Allocat a buffer to store response
4. Get destination address and port from the received packet
5. Create a packet and send to the client
*/

import java.io.*;
import java.net.*;
import java.util.Date;

public class UDPServer implements Runnable {
  public static void main(String[] args) {
    UDPServer server = new UDPServer();
    server.run();
  }

  private DatagramSocket socket;          // socket of the server
  private byte[] txBuf = new byte[256]; // buffer for data to send
  private byte[] rxBuf = new byte[256];  // buffer for data to receive

  private long req_rx, rply_tx;

  public void run() {
    // Create the socket on the server side
    try {
      socket = new DatagramSocket(2017);
    } catch (SocketException e) {
      System.out.println("Server: SocketException!");
    }

while (true) {
      // Wait for a request from the client
      DatagramPacket packet = new DatagramPacket(rxBuf, rxBuf.length);
      try {
        socket.receive(packet);
      } catch (IOException e) {
        System.out.println("IOException to receive a request on server!");
      }
      // Record the received time of a request
      req_rx = new Date().getTime();
      String tm = Long.toString(req_rx) + ":";
      rply_tx = new Date().getTime();
      tm += Long.toString(rply_tx);

      // Send the time of request received and reply sent on the server side
      txBuf = (tm.getBytes());
      // Send back the reply
      packet = new DatagramPacket(txBuf, txBuf.length, packet.getAddress(),
        packet.getPort());
      try {
        socket.send(packet);
      } catch(IOException e) {
        System.out.println("IOException to send a reply on server!");
      }
    }
  }
}
