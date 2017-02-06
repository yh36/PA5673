/*
1. Create a socket
2. Create a datagram packet
3. Wait for reply
4. Set a timer in case reponse is lost
*/

import java.io.*;
import java.net.*;
import java.util.Date;

public class UDPClient implements Runnable {

  public static void main(String[] args) {
    UDPClient client = new UDPClient();
    client.run();
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
    count = 2;
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
      req_tx = new Date().getTime();
      try {
        socket.send(packet);
      } catch (IOException e) {
        System.out.println("Client: IOException!");
      }

      // Wait for a reply from server
      packet = new DatagramPacket(buf, buf.length);
      try {
        socket.receive(packet);
      } catch (IOException e) {
        System.out.println("IOException!");
      }
      // Record the received time
      rply_rx = new Date().getTime();

      String reply = new String(packet.getData(), 0, packet.getLength());
      String[] tm = reply.split(":");
      req_rx = Long.parseLong(tm[0]);
      rply_tx = Long.parseLong(tm[1]);

      System.out.println("Run: " + i + "\nRequest sent : " + req_tx +
        "\nRequest received: " + req_rx + "\nReply sent: " + rply_tx +
        "\nReply received: " + rply_rx + "\n");
    }
  }
}
