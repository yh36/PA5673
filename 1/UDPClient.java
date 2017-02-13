/*
1. Create a socket
2. Create a datagram packet
3. Wait for reply
4. Set a timer in case reponse is lost
*/

import java.io.*;
import java.net.*;

public class UDPClient implements Runnable {

  public static void main(String[] args) {
    new Thread(new UDPClient()).start();
  }

  private int port;
  private InetAddress serverAddr;
  private DatagramSocket socket;
  private byte[] buf = new byte[256];

  private int count;
  private long req_tx, req_rx, rply_tx, rply_rx;

  public UDPClient() {
    port = 2017;
    try {
      serverAddr = InetAddress.getByName("localhost");
    } catch (UnknownHostException e) {
      System.out.println("Client: UnknownHostException!");
    }
    count = 5;
  }

  public UDPClient(int pt, InetAddress addr, int cnt) {
    port = pt;
    serverAddr = addr;
    count = cnt;
  }

  public void run() {
    // Create a socket on the client side
    try {
      socket = new DatagramSocket();
    } catch (SocketException e) {
      System.out.println("Client: SocketException!");
    }

    for (int i = 0; i < count; i++) {
      DatagramPacket packet = new DatagramPacket(buf, buf.length,
        serverAddr, port);
      // Record the time of request sent
      req_tx = System.currentTimeMillis();
      try {
        socket.send(packet);
      } catch (IOException e) {
        System.out.println("IOException to senda request on client!");
      }

      // Wait for a reply from server
      packet = new DatagramPacket(buf, buf.length);
      try {
        socket.receive(packet);
      } catch (SocketTimeoutException e) {
        System.out.println("TimeoutException to receive a reply on client!");
      } catch (IOException e) {
        System.out.println("IOException to receive a reply on client!");
      }
      // Record the received time
      rply_rx = System.currentTimeMillis();

      String reply = new String(packet.getData(), 0, packet.getLength());
      String[] tm = reply.split(":");
      req_rx = Long.parseLong(tm[0]);
      rply_tx = Long.parseLong(tm[1]);

      System.out.println("Run: " + i + "\nRequest sent : " + req_tx +
        "\nRequest received: " + req_rx + "\nReply sent: " + rply_tx +
        "\nReply received: " + rply_rx + "\n");
      try {
        Thread.sleep(1500);
      } catch(InterruptedException e) {
        System.out.println(e);
      }
    }
  }
}
