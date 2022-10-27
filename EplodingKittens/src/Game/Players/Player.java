package Game.Players;

import java.util.*; 
import java.io.*; 
import java.net.*;
import java.util.concurrent.*;


public class Player {
	public int playerID;
	public int turns = 1;
    public boolean online;
    public Socket connection;
    public boolean exploded = false;
    public boolean isBot = false;
    public ObjectInputStream inFromClient;
    public ObjectOutputStream outToClient;
    public ArrayList<String> hand = new ArrayList<String>();
	public int secondsToInterruptWithNope = 5;
    
    Scanner in = new Scanner(System.in);

    public Player(int playerID, boolean isBot, Socket connection, ObjectInputStream inFromClient, ObjectOutputStream outToClient) {
    	this.playerID = playerID; 
    	this.connection = connection; 
    	this.inFromClient = inFromClient; 
    	this.outToClient = outToClient; ;
    	this.hand.add("Defuse");
    	this.isBot = isBot;
    	if(connection == null)
    		this.online = false;
    	else
    		this.online = true;
	}
    
    public ArrayList<String> getHand(){
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
    
    public String chooseMoveForBot() {
        return "we choose a move";
    }
    
    public void blowUp() {
    	this.exploded = true;
    }
    
    public void setTurns(int turns) {
        this.turns = turns;
    }
    
    public void decreaseTurns() {
        this.turns--;
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