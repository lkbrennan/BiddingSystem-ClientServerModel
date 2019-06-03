/*
Student Name : Lauren Keenan Brennan
Student Number : C15434102
Title : Distributed Systems Assignment - Server.java

Base Code taken from Week4/T4 - MultiEchoServer.java and adapted
*/

import java.io.*;
import java.net.*;
import java.util.*;

//Server class
public class Server
{
	//variables for the server socket, port number and client socket
	private static ServerSocket serverSocket;
	private static Socket client;
	private static final int PORT = 1234;
	
	//variables for file reading and writing
	private static PrintWriter outToFile = null;
	private static BufferedReader fromFile = null;
	
	//ArrayList to hold client Socket addresses and a client count
	public static ArrayList<Socket> clients;
	public static int client_count;
	
	//variable to hold the current time value
	//used for the auction timer
	public static long timeval;
	
	//string to hold the value of the current auction item
	public static String currentItem;

	public static void main(String[] args) throws IOException
	{
		//Initialise vaariables
		clients = new ArrayList<Socket>();
		client_count=0; 
		
		//get the first item up for auction
		currentItem = readItems();
		
		try{
			//set up the server socket on PORT 1234
			serverSocket = new ServerSocket(PORT);
			
			//add 0 to the bidding file 
			//this will create the file and set the current highest bid to $0 to begin with
			addToFile(0);
		} catch (IOException e) {
			System.out.println(e);
			System.exit(1);
		}

		//Instantiate the timer class and start it
		TimerCount timerval = new TimerCount();
		timerval.start();
		
		do
		{
			//Wait for client...
			clients.add(serverSocket.accept());

			System.out.println("\nNew client accepted : " + clients.get(client_count) + "\n");

			//Create a thread to handle communication with
			//this client and pass the constructor for this
			//thread a reference to the relevant socket...
			ClientHandler handler = new ClientHandler(clients.get(client_count));

			//begin the client handler
			handler.start();
			
			//Increment the client count
			client_count++;
			
		}while (true);
	}
	
	//function used to send a message to all connected clients
	//it takes in the client socket address, an integer value and a message
	public static void clientMessage(Socket s,int amount,String msg){
		//sets the clientsocket variable to Socket s address
		Socket clientsock = s;
		
		//set up a PrintWriter to write and send messages to clients
		PrintWriter output;
		
		try {
			//sets up PrintWriter on the clients Socket and sends them a message
			output = new PrintWriter(clientsock.getOutputStream(),true);
			output.println(msg + amount);
			output.flush();
			
		} catch(IOException e){
			System.out.println(e);
		}
	}
	
	//function used to add new highest bids to the bidding file
	public static void addToFile(int bid) {
		//base code taken from Week3/T3 - Writer()
		
		try{
			outToFile = new PrintWriter(new FileWriter("bid.txt"));
			outToFile.println(bid);
			outToFile.close();
		}catch(IOException e){
			System.out.println(e);
		}
	}
	
	//function to read the highest bid from the bidding file
	public static int readFromFile() {
		//base code taken from Week3/T3 - Reader()
		
		int bid = 0;
		
		try {
			fromFile = new BufferedReader(new FileReader("bid.txt"));
			
			// Read a single line from the file.
			String lineOfText;

			//sets the highest bid to the value on the last line of the file
			while ((lineOfText = fromFile.readLine()) != null) {
				bid = Integer.parseInt(lineOfText);
			}
			
			fromFile.close();
			
		} catch (IOException e) {
			System.out.println(e);
		}
		return(bid);
	}
	
	//function to read auction items from the items file
	public static String readItems() {
		//base code taken from Week3/T3 - Reader()
		
		String currentItem ="";
		
		try {
			fromFile = new BufferedReader(new FileReader("items.txt"));

			String lineOfText;

			//sets the current auction item to the value on the last line of the file
			//reading from the last line of the file means that if an item is not auctioned off,
			//the item will remain on the last line of the file so it will automatically be
			//auctioned off again
			while ((lineOfText = fromFile.readLine()) != null) {
				currentItem = lineOfText;
			}
			
			fromFile.close();
			
		} catch (IOException e) {
			System.out.println(e);
		}
		return(currentItem);
	}
	
	//function to remove an auction item from a file when it has been auctioned off
	public static void removeItems(String item) {
		//base code taken from https://www.rosettacode.org/wiki/Remove_lines_from_a_file?fbclid=IwAR1plxwSMrVPV_of68BR_dGDVT_63fPsukQWYLCbXchlfgsCNcvBLHVsTaQ#Java
		
		//temporary string used to hold item values
		String tempstr = "";
		
		try {
			fromFile = new BufferedReader(new FileReader("items.txt"));
			
			String lineOfText;

			//read lines from file until end of file is reached
			while ((lineOfText = fromFile.readLine()) != null) {
				//if the value of the current line is NOT the same as the current item up for action
				//add it to the temporary string with a line break
				if(!lineOfText.equals(item)){
					tempstr += lineOfText;
					tempstr += "\n";
				}
			}
			
			fromFile.close();
			
			//instantiate a new PrintWriter to write to the items file
			outToFile = new PrintWriter(new FileWriter("items.txt"));
			//copy the temporary string into the items file
			outToFile.write(tempstr);
			outToFile.close();
			
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}

//Timer class which will handle the auction timer
class TimerCount extends Thread {
	public static long currentTime;
	public static long currentbid;
	
	//set the surrent time to the current time in milliseconds
	public TimerCount(){
		currentTime = System.currentTimeMillis();
	}
	
	public void run(){
		do
		{
			//while the timer hasn't gone reached 60 seconds
			do {
				//update the current time value
				currentTime = System.currentTimeMillis();
				
				//when 30 seconds have passed, send a message to the clients letting them know
				if((currentTime - Server.timeval) == 30000){
					for(Socket i:Server.clients){
						Server.clientMessage(i,30,"Seconds left to Auction : ");
					}
				}
			} while((currentTime - Server.timeval) < 60000 );
			
			System.out.println("\n***TimeUp On Item " + Server.readItems() + "***\n");
			
			//if the item was not bidded on, begin the auction for that item again
			if(Server.readFromFile() == 0){
				System.out.println("\nItem was not sold. Will begin auction again.\n");
				for(Socket i:Server.clients){
					Server.clientMessage(i,0,Server.currentItem + " was not sold, auction for item will begin again at $");
				}
			}
			//if item was sold, remove the item from the items file and reset the currentItem to the new item
			else{
				Server.removeItems(Server.currentItem);
				Server.currentItem = Server.readItems();
				
				//let clients know that item was sold and what the new item up for auction is
				for(Socket i:Server.clients){
					Server.clientMessage(i,Server.readFromFile(),"Item was sold for highest bid of $");
					Server.clientMessage(i,0,"New Item " + Server.currentItem + " up for Auction. Bidding begins at $");
				}
			}
			
			//reset the server time value and reset the highest bid to 0
			Server.timeval = System.currentTimeMillis();
			Server.addToFile(0);
			
			
		}while (true);
		
	}
	
}

//class which will handle all the clients
class ClientHandler extends Thread
{
	private Socket client;
	private Scanner input;
	private PrintWriter output;

	public ClientHandler(Socket s)
	{
		//Set up reference to associated socket...
		client = s;

		try
		{
			input = new Scanner(client.getInputStream());
			output = new PrintWriter(client.getOutputStream(),true);
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
	}

	public void run()
	{
		String strreceived;
		int received;
		int currentbid;
		
		do{
			//let client know of the current item up for auction and ask how much they would like to bid
			output.println("Current item up for Auction : " + Server.readItems());
			output.println("How much would you like to bid?"); 

			//take in clients value and convert it from a String into an Integer
			strreceived = input.nextLine();
			received = Integer.parseInt(strreceived);
			
			//get the curreent highest bid value
			currentbid = Server.readFromFile();

			//if the clients bid wasn't higher than the current bid
			if(received <= currentbid){
				//inform them of the current highest bid
				output.println("NOTE: Amount must be greater than previous bid : $" + currentbid); 
			}
			//if client made the new highest bid
			else{
				output.println("Thank you for bidding $" + received);
				
				System.out.println("/nClient " + client + " bids $" + received);
				
				//add new highest bid to bidding file
				Server.addToFile(received);
				
				//reset timer value
				Server.timeval = System.currentTimeMillis();
				
				//inform all clients of new highest bid
				for(Socket i:Server.clients){
					Server.clientMessage(i,received,"New highest bid : $");
				}
			}
		
		}while (received != 0); //run while the client hasn't sent 0 to close the connection

		try
		{
			//close down the connection with the client
			System.out.println("Closing down connection...");
			//send the client the exit code so they know to close the connection
			output.println("0");
			//remove the client from the array list and decrease the client count
			Server.clients.remove(client);
			Server.client_count --;
			//close the connection
			client.close();
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
	}
}