package Game;

import java.util.*; 
import java.io.*; 
import java.net.*;
import java.util.concurrent.*;

import Game.Card;
import Game.Card.CardType;



public class Player {
	public int playerID;
	public int turns = 1;
    public boolean online;
    public boolean isBot;
    public Socket connection;
    public boolean exploded = false;
    public ObjectInputStream inFromClient;
    public ObjectOutputStream outToClient;
    public ArrayList<Card> hand = new ArrayList<Card>();
	public int secondsToInterruptWithNope = 5;
    
    Scanner in = new Scanner(System.in);

    public Player(int playerID, boolean isBot, Socket connection, ObjectInputStream inFromClient, ObjectOutputStream outToClient) {
    	this.playerID = playerID; 
    	this.connection = connection; 
    	this.inFromClient = inFromClient; 
    	this.outToClient = outToClient; 
    	this.isBot = isBot;
    	this.hand.add(new Card(CardType.Defuse));
    	if(connection == null)
    		this.online = false;
    	else
    		this.online = true;
	}
    
    public ArrayList<Card> getHand(){
    	return hand;
    }
    
    public void sendMessage(Object message) {
        if(online) {
            try {
            	outToClient.writeObject(message);
            } catch (Exception e) {
            	
            }
        } else if(!isBot){
            System.out.println(message);                
        }
    }
    
    public String readMessage(boolean interruptable) {
        String word = " "; 
        if(online)
            try{
            	word = (String) inFromClient.readObject();
            } catch (Exception e){
            	System.out.println("Reading from client failed: " + e.getMessage());
            }
        else
            try {
            	if(interruptable) {
				    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				    int millisecondsWaited = 0;
				    while(!br.ready() && millisecondsWaited<(secondsToInterruptWithNope*1000)) {
				    	Thread.sleep(200);
				    	millisecondsWaited += 200;
				    }
				    if(br.ready())
				    	return br.readLine();               		
            	} else {
                	in = new Scanner(System.in); 
                	word=in.nextLine();
            	}
            } catch(Exception e){System.out.println(e.getMessage());}
        return word;
    }
}