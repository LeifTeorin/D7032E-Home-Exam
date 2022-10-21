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
	
	public Game(int numplayers, int numbots) throws IOException{ // creates an instance of the game if the player amount is between 2 and 5, it also creates the deck
		if(numplayers+numbots < 2 || numplayers+numbots > 5){
			throw new IOException("There can only be 2 - 5 players, ya dingus");
		}
		deck = createDeck(numplayers + numbots);
	}
	
	public ArrayList<Card> addKittens(ArrayList<Card> inputDeck, int playercount) { // adds the right amount of kittens and shuffles the deck
		for(int i=0; i<playercount-1; i++) {
			deck.add(new Card(CardType.ExplodingKitten));
		}
		Collections.shuffle(deck);
		return inputDeck;
	}
	
	public void setUpGame(int numplayers, int numbots) { // we host our server and add all our players, give them cards and add exploding kittens
		
		try {
			server(numplayers, numbots);
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
		Collections.shuffle(deck);
		for(Player player : players) {
			for(int i=0; i<7; i++) {
				player.hand.add(deck.remove(0));
			}
		}
		
		deck = addKittens(deck, numplayers + numbots);
		
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
	
	public ArrayList<Player> playOrder(ArrayList<Player> players){ // this creates a shuffled copy of our list of players, we will use this to
		ArrayList<Player> playOrder = new ArrayList<Player>(players);
		Collections.shuffle(playOrder);
		return playOrder;
	}
	
	// jag tror alla funktioner ska ta kortleken som argument
	public void pass(Player currentplayer) {
		
	}
	
	public void two(Player currentplayer) {
		
	}
	
	public void three(Player currentplayer) {
		
	}
	
	public void attack(Player currentplayer) {
		
	}
	
	public void favor(Player currentplayer) {
		
	}
	
	public void shuffle(Player currentplayer) {
		
	}
	
	public void skip(Player currentplayer) {
		
	}
	
	public void seethefuture(Player currentplayer) {
		
	}
	
	public void runGame() {
		int currentplayernr = 0;
		int playersleft = players.size();
		Player currentPlayer = players.get(currentplayernr);
		ArrayList<Player> playOrder = playOrder(players);
		
		while(playersleft > 1) { // as long as there are players left the game goes on
			for(Player p : players) {
				if(p == currentPlayer)
					p.sendMessage("It is your turn");
				else
					p.sendMessage("It is now the turn of player " + currentPlayer.playerID);
			}
			for(int i=0; i<numberOfTurnsToTake; i++) { // every player can take more than one turn depending on what happens in the game.
				String otherPlayerIDs = "PlayerID: ";
				for(Player p : players) {
					if(p.playerID != currentPlayer.playerID)
						otherPlayerIDs += p.playerID + " ";
				}

				String response = "";
				while(!response.equalsIgnoreCase("pass")) {
					int turnsLeft = numberOfTurnsToTake-i;
					currentPlayer.sendMessage("\nYou have " + turnsLeft + ((turnsLeft>1)?" turns":" turn") + " to take");
					currentPlayer.sendMessage("Your hand: " + currentPlayer.hand);
					String yourOptions = "You have the following options:\n";
					Set<Card> handSet = new HashSet<Card>(currentPlayer.hand);
					for(Card card : handSet) {
						int count = Collections.frequency(currentPlayer.hand, card.type);
						if(count>=2)
							yourOptions += "\tTwo " + card.type + " [target] (available targets: " + otherPlayerIDs + ") (Steal random card)\n";
						if(count>=3)
							yourOptions += "\tThree " + card.type + " [target] [Card Type] (available targets: " + otherPlayerIDs + ") (Name and pick a card)\n";
						if(card.type == CardType.Attack)
							yourOptions += "\tAttack\n";
						if(card.type == CardType.Favor)
							yourOptions += "\tFavor [target] (available targets: " + otherPlayerIDs + ")\n";
						if(card.type == CardType.Shuffle)
							yourOptions += "\tShuffle\n";
						if(card.type == CardType.Skip)
							yourOptions += "\tSkip\n";
						if(card.type == CardType.SeeTheFuture)
							yourOptions += "\tSeeTheFuture\n";
					}  
					yourOptions += "\tPass\n";
					//We don't need to offer Nope as an option - it's only viable 5 seconds after another card is played and handled elsewhere
					currentPlayer.sendMessage(yourOptions);
					response = currentPlayer.readMessage(false);
					if(yourOptions.contains(response.replaceAll("\\d",""))) { //remove targetID to match vs yourOptions
						if(response.equals("Pass")) { //Draw a card and end turn
							
						} else if(response.contains("Two")) { //played 2 of a kind - steal random card from target player
							
						} else if(response.contains("Three")) { //played 3 of a kind - name a card and force target player to hand one over if they have it
							
						} else if(response.equals("Attack")) {
							
						} else if(response.contains("Favor")) {
							
						} else if(response.equals("Shuffle")) {
							
						} else if(response.equals("Skip")) {
							
						} else if(response.equals("SeeTheFuture")) {
														
						} 
					} else {
						currentPlayer.sendMessage("Not a viable option, try again");
					}
				}
			}
		}
		Player winner = currentPlayer;
		for(Player notify: players)
			winner = (!notify.exploded?notify:winner);
		for(Player notify: players)
			notify.sendMessage("Player " + winner.playerID + " has won the game");
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