package Game;

import java.util.*; 
import java.io.*; 
import java.net.*;
import java.util.concurrent.*;

import Game.Player;
import Game.Card.CardType;
import Game.Card;


public class Game {
	
	public ArrayList<Player> players = new ArrayList<Player>();
	public ServerSocket aSocket;
	public static ArrayList<Card> deck = new ArrayList<Card>();
	public static ArrayList<Card> discard = new ArrayList<Card>();
	public static int numberOfTurnsToTake = 1; //attacked?
	public int secondsToInterruptWithNope = 5;
	
	public Game(int numplayers, int numbots) throws IOException{
		if(numplayers < 2 || numplayers > 5){
			throw new IOException("There can only be 2 - 5 players, ya dingus");
		}
	}
	
	public void initGame(int numplayers, int numbots) {
		
		try {
			server(numplayers, numbots);
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
		deck = createDeck(numplayers + numbots);
		
		for(int i=0; i<numplayers-1; i++) {deck.add(new Card(CardType.ExplodingKitten));}
	}
	
	public ArrayList<Card> createDeck(int numplayers){ //creates our deck for the game
		/*HashMap<CardType, Integer> maxCards = new HashMap<CardType, Integer>();
		maxCards.put(CardType.Attack, 4);
		maxCards.put(CardType.Favor, 4);
		maxCards.put(CardType.Nope, 5);
		maxCards.put(CardType.Shuffle, 4);
		maxCards.put(CardType.Skip, 4);
		maxCards.put(CardType.SeeTheFuture, 5);
		maxCards.put(CardType.HairyPotatoCat, 4);
		maxCards.put(CardType.Cattermelon, 4);
		maxCards.put(CardType.RainbowRalphingCat, 4);
		maxCards.put(CardType.TacoCat, 4);
		maxCards.put(CardType.OverweightBikiniCat, 4);*/
		
		// vi ska läsa in maxkort från en fil tänkte jag, måste fixa
		
		ArrayList<Card> deck = new ArrayList<Card>();
		if(numplayers == 5) {
			deck.add(new Card(CardType.Defuse));
		}else {
			for(int i=0; i<2; i++) {deck.add(new Card(CardType.Defuse));}
		}
		for(int i=0; i<4; i++) {deck.add(new Card(CardType.Attack));}
		for(int i=0; i<4; i++) {deck.add(new Card(CardType.Favor));}
		for(int i=0; i<5; i++) {deck.add(new Card(CardType.Nope));}
		for(int i=0; i<4; i++) {deck.add(new Card(CardType.Shuffle));}
		for(int i=0; i<4; i++) {deck.add(new Card(CardType.Skip));}
		for(int i=0; i<5; i++) {deck.add(new Card(CardType.SeeTheFuture));}
		for(int i=0; i<4; i++) {deck.add(new Card(CardType.HairyPotatoCat));}
		for(int i=0; i<4; i++) {deck.add(new Card(CardType.RainbowRalphingCat));}
		for(int i=0; i<4; i++) {deck.add(new Card(CardType.Cattermelon));}
		for(int i=0; i<4; i++) {deck.add(new Card(CardType.TacoCat));}
		for(int i=0; i<4; i++) {deck.add(new Card(CardType.OverweightBikiniCat));}
		
		return deck;
	}
	
	public void getHand(int playerId) {
		
	}
	
	public void client(String ipAddress) throws Exception {
        //Connect to server
        Socket aSocket = new Socket(ipAddress, 2048);
        ObjectOutputStream outToServer = new ObjectOutputStream(aSocket.getOutputStream());
        ObjectInputStream inFromServer = new ObjectInputStream(aSocket.getInputStream());
        //Get the hand of apples from server
        ExecutorService threadpool = Executors.newFixedThreadPool(1);
        Runnable receive = new Runnable() {
        	@Override
        	public void run() {
    			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));	
        		while(true) {
        			try {
		        		String nextMessage = (String) inFromServer.readObject();
	    	    		System.out.println(nextMessage);   		
	    	    		if(nextMessage.contains("options") || nextMessage.contains("Give") || nextMessage.contains("Where")){ //options (your turn), Give (Opponent played Favor), Where (You defused an exploding kitten)
	    	    			outToServer.writeObject(br.readLine());
	    	    		} else if(nextMessage.contains("<Enter>")) { //Press <Enter> to play Nope and Interrupt
	    				    int millisecondsWaited = 0;
	    				    while(!br.ready() && millisecondsWaited<(secondsToInterruptWithNope*1000)) {
	    				    	Thread.sleep(200);
	    				    	millisecondsWaited += 200;
	    				    }
	    				    if(br.ready()) {
	    				    	outToServer.writeObject(br.readLine());
	    				    }	    				    
	    				    else
	    				    	outToServer.writeObject(" ");
	    	    		}
        			} catch(Exception e) {
        				System.exit(0);
        			}
        		}
        	}
        };

        threadpool.execute(receive);
    }

    public void server(int numberPlayers, int numberOfBots) throws Exception {
        players.add(new Player(0, false, null, null, null)); //add this instance as a player
        //Open for connections if there are online players
        for(int i=0; i<numberOfBots; i++) {
            players.add(new Player(i+1, true, null, null, null)); //add a bot    
        }
        if(numberPlayers>1)
            aSocket = new ServerSocket(2048);
        for(int i=numberOfBots+1; i<numberPlayers+numberOfBots; i++) {
            Socket connectionSocket = aSocket.accept();
            ObjectInputStream inFromClient = new ObjectInputStream(connectionSocket.getInputStream());
            ObjectOutputStream outToClient = new ObjectOutputStream(connectionSocket.getOutputStream());
            players.add(new Player(i, false, connectionSocket, inFromClient, outToClient)); //add an online client
            System.out.println("Connected to player " + i);
            outToClient.writeObject("You connected to the server as player " + i + "\n");
        }    
    }
	
}