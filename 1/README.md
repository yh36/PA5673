CSCI/ECEN 5673: Distributed Systems @ University of Colorado Boulder 2017 Spring 

Programming Assignment One: This is a Java program for UDP Client and Server.

Team members: Yi Hou and Yu-Chih Cho



##Implmentation
1. Client sends a request to the server and the server sends a reply to the client.
2. The client records the local clock times (client machine) of when the request is sent and when the reply is received. 
3. The server’s reply contains the local clock time (server machine) of when the request is received and when the reply is sent.



##How do we Implement it
In Java, we use DatagramPacket and DatagramSocket to create UDP protocol. 
+   Both Client and Server use DatagramSocket to create the function of send（）and receive（）.
+   Both Client and Server use DatagramPacket to pack the message that need to be sent and received. 

+   When sending the message, Java create a DatagramPacket which include the message that need to send, and pass it as an parameter to the send() function in DatagramSocket.
+   When receiving the message, Java create a DatagramPacket which would allocate some memory after receiving the message, and pass it as an parameter to the receive() function in DatagramSocket.



##Process of UDP Communication
###UDPClient
1. Create a DatagramSocket and set the local address. 
2. Use DatagramSocket to send a request and wait for the reply. 
3. After the communication, use close（）of DatagramSocket to close the socket. 

###UDPServer
1. Create a DatagramSocket and set the local address. Here, Server is ready for listening the request from Client. 
2. Use receive() in DatagramSocket and listen the package from DatagramPacket. When returning the receive(), the package would contain the address of client. 
3. Use send() in DatagramSocket to send the reply and return the package in DatagramPacket.


