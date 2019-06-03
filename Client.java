import java.io.*;
/*
Student Name : Lauren Keenan Brennan
Student Number : C15434102
Title : Distributed Systems Assignment - Client.java

Base Code taken from Week4/T4 - MultiEchoClient.java and adapted
*/

import java.net.*;
import java.util.*;

//Client class
public class Client
{
	//variables for the host adress, port number and socket
	private static InetAddress host;
	private static final int PORT = 1234;
	private static Socket s;

	public static void main(String[] args)
	{
		try
		{
			//set host address to localhost
			host = InetAddress.getLocalHost();
		}
		catch(UnknownHostException uhEx)
		{
			System.out.println("\nHost ID not found!\n");
			System.exit(1);
		}
		try{
			//create a new socket s with the given host address and socket number
			s = new Socket(host,PORT);
		}catch (IOException e){
			System.out.println(e);
		}

		//Instantiate Incoming class and Outgoing class and begin them both
		Outgoing message = new Outgoing(s);
		Incoming received = new Incoming(s);
		
		message.start();
		received.start();
	}
}

//Outgoing class handles all messages sent by the client to the server
class Outgoing extends Thread {
	private Socket serversock;
	public PrintWriter networkOutput;
	public Scanner userEntry;
	
	public Outgoing(Socket s){
		
		//server socket is set to the socket which was created earlier
		serversock = s;
		
		try {
			
			//set up PrintWriter which can write on an output stream to the Server
			networkOutput = new PrintWriter(serversock.getOutputStream(),true);

			//Set up stream for keyboard entry...
			userEntry = new Scanner(System.in);
		} catch (IOException e){
			System.out.println(e);
		}
	}
	
	public void run(){
		String tosend;
		int sending;
		do {
			//tell user to enter 0 to close connection
			System.out.println("\nEnter 0 to Exit\n");
			
			//tosend is a String variable but I need to send an Integer variable
			//tosend is turned into an integer and stored in sending to do this
			tosend =  userEntry.nextLine();
			sending = Integer.parseInt(tosend);
			
			//send sending message to the server
			networkOutput.println(sending);
		}while (sending != 0);//if sending is equal to 0, close the connection
		
		try{
			//close the connection to the server
			System.out.println("Closing this connection : " + serversock); 
			serversock.close(); 
			System.out.println("Connection closed"); 
		} catch (IOException e){
			System.out.println(e);
		}
	}
}

//Incoming class handles all messages sent from the server to the client
class Incoming extends Thread {
	public Scanner networkInput;
	private Socket serversock;
	
	public Incoming(Socket s){
		
		//server socket is set to the socket which was created earlier
		serversock = s;
		
		try {
			//set up Scanner which will read from an InputStream stream from the Server
			networkInput = new Scanner(serversock.getInputStream());
		} catch (IOException e){
			System.out.println(e);
		}
	}
	
	public void run(){
		String received;
		do {
			//read the messages sent from the server
			received = networkInput.nextLine();
			
			//print messages out with line break
			System.out.println(received + "\n");
		}while (!received.equals("0"));//if received is equal to 0, close the connection
		
		try{
			//close the connection to the server
			System.out.println("Closing this connection : " + serversock); 
			serversock.close(); 
			System.out.println("Connection closed"); 
		} catch (IOException e){
			System.out.println(e);
		}
	}
	
	
}