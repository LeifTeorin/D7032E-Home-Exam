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
	
	public Game(String params[]){ // creates an instance of the game if the player amount is between 2 and 5, it also creates the deck
		if(params.length == 2) {
			try {
				this.setUpGame(Integer.valueOf(params[0]).intValue(), Integer.valueOf(params[1]).intValue());
			} catch (IOException e) {
				e.getMessage();
				System.exit(0);
			}
		} else if(params.length == 1) {
			try {
				client(params[0]);
			}catch(Exception e) {
				e.getMessage();
			}
		}
	}
	
	public ArrayList<Card> addKittens(ArrayList<Card> inputDeck, int playercount) { // adds the right amount of kittens and shuffles the deck
		for(int i=0; i<playercount-1; i++) {
			deck.add(new Card(CardType.ExplodingKitten));
		}
		Collections.shuffle(deck);
		return inputDeck;
	}
	
	public void setUpGame(int numplayers, int numbots) throws IOException{  // we host our server and add all our players, give them cards and add exploding kittens
		if(numplayers+numbots > 5 || numplayers + numbots < 2) {
			throw new IOException("there can only be 2 - 5 players ya dinguz");
		}
		deck = createDeck(numplayers + numbots);
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
	
	public int checkNrNope(ArrayList<Card> discard) {
		int i=0;
		while(i<discard.size() && discard.get(i).type==CardType.Nope) {
			i++;	
		}
		return i;
	}
	
	public void addToDiscardPile(Player currentPlayer, String card, ArrayList<Card> discard, ArrayList<Card> deck){
		//After an interruptable card is played everyone has 5 seconds to play Nope
		int nopePlayed = checkNrNope(discard);
		ExecutorService threadpool = Executors.newFixedThreadPool(players.size());
		for(Player p : players) {
			p.sendMessage("Action: Player " + currentPlayer.playerID + " played " + card);
			if(p.hand.contains(CardType.Nope)) { //only give the option to interrupt to those who have a Nope card
				p.sendMessage("Press <Enter> to play Nope");
				Runnable task = new Runnable() {
		        	@Override
		        	public void run() {
	        			try {
			        		String nextMessage = p.readMessage(true); //Read that is interrupted after secondsToInterruptWithNope
			        		if(!nextMessage.equals(" ") && p.hand.contains(CardType.Nope)) {
		    	    			discard.add(0, p.hand.remove(CardType.Nope));
		    	    			for(Player notify: players)
		    	    				notify.sendMessage("Player " + p.playerID + " played Nope");
			        		}
	        			} catch(Exception e) {
	        				System.out.println("addToDiscardPile: " +e.getMessage());
	        			}
	        		}
	        	};
            	threadpool.execute(task);
			}
		}
		threadpool.awaitTermination((secondsToInterruptWithNope*1000)+500, TimeUnit.MILLISECONDS); //add an additional delay to avoid concurrancy problems with the ObjectInputStream
		for(Player notify: players)
			notify.sendMessage("The timewindow to play Nope passed");
		if(checkNrNope(discard)>nopePlayed) {
			for(Player notify: players)
				notify.sendMessage("Play another Nope? (alternate between Nope and Yup)");
			addToDiscardPile(currentPlayer, card);
		}
	}
	
	public void getHand(int playerId) {
		
	}
	
	public ArrayList<Player> playOrder(ArrayList<Player> players){ // this creates a shuffled copy of our list of players, we will use this to
		ArrayList<Player> playOrder = new ArrayList<Player>(players);
		Collections.shuffle(playOrder);
		return playOrder;
	}
	
	// jag tror alla funktioner ska ta kortleken som argument
	public int pass(Player currentPlayer, ArrayList<Card> deck, ArrayList<Card> discardPile, int playersleft) {
		Card drawn = deck.remove(0);
		if(drawn.type == CardType.ExplodingKitten) {
			for(int i=0; i<currentPlayer.hand.size(); i++) {
				if(currentPlayer.hand.get(i).type == CardType.Defuse) {
					currentPlayer.sendMessage("You defused the kitten. Where in the deck do you wish to place the ExplodingKitten? [0.." + (deck.size()-1) + "]");
					deck.add((Integer.valueOf(currentPlayer.readMessage(false))).intValue(), drawn);
					for(Player p : players) {
						p.sendMessage("Player " + currentPlayer.playerID + " successfully defused a kitten");
					}
					return playersleft;
				}
				
			}
			discardPile.add(drawn);
			discardPile.addAll(currentPlayer.hand);
			currentPlayer.hand.clear();
			currentPlayer.blowUp();
			for(Player p : players) {
				p.sendMessage("Player " + currentPlayer.playerID + " exploded");
			}
			playersleft --;
			return playersleft;
		}else {
			currentPlayer.hand.add(drawn);
			currentPlayer.sendMessage("You drew: " + drawn.type);
			return playersleft;
		}
	}
	
	public void two(Player currentPlayer, CardType played, CardType targetCard, Player target, ArrayList<Card> deck, ArrayList<Card> discardPile) {
		int removedFromHand = 0;
		for(int i=0; i<currentPlayer.hand.size(); i++) { // we scan the deck and remove two instances of the played card
			if(currentPlayer.hand.get(i).type == played) {
				discardPile.add(0, currentPlayer.hand.remove(i)); // the card is moved from the hand to the discard pile
				removedFromHand ++;
				if(removedFromHand >=2) {
					break;
				}
			}
		}
		addToDiscardPile(currentPlayer, "Two of a kind against player " + Integer.toString(target.playerID), discardPile, deck);
		
		if(checkNrNope() % 2 == 0) { // if an even amount of nopes have been played it means yea
	        Random rnd = new Random();
	        Card aCard = target.hand.remove(rnd.nextInt(target.hand.size()-1));
	        currentPlayer.hand.add(aCard);
	        target.sendMessage("You gave " + aCard + " to player " + currentPlayer.playerID);
	        currentPlayer.sendMessage("You received " + aCard + " from player " + target.playerID);								
		}
	}
	
	public void three(Player currentPlayer, CardType played, CardType targetCard, Player target, ArrayList<Card> deck, ArrayList<Card> discardPile) {
		int removedFromHand = 0;
		for(int i=0; i<currentPlayer.hand.size(); i++) { // we scan the deck and remove two instances of the played card
			if(currentPlayer.hand.get(i).type == played) {
				discardPile.add(0, currentPlayer.hand.remove(i)); // the card is moved from the hand to the discard pile
				removedFromHand ++;
				if(removedFromHand >=3) {
					break;
				}
			}
		}
		addToDiscardPile(currentPlayer, "Three of a kind against player " + Integer.toString(target.playerID), discardPile, deck);
		if(checkNrNope() % 2 == 0) {
			Card aCard = Card.valueOf(targetCard);
			if(target.hand.contains(aCard)) {
				target.hand.remove(aCard);
				currentPlayer.hand.add(aCard);
	        	target.sendMessage("Player " + currentPlayer.playerID + " stole " + aCard);
	        	currentPlayer.sendMessage("You received " + aCard + " from player " + target.playerID);										
			} else {
				currentPlayer.sendMessage("The player did not have any " + aCard);
			}								
		}
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
		ArrayList<Player> playOrder = playOrder(players);
		Player currentPlayer = playOrder.get(currentplayernr);
		
		while(playersleft > 1) { // as long as there are players left the game goes on
			for(Player p : players) {
				if(p == currentPlayer)
					p.sendMessage("It is your turn");
				else
					p.sendMessage("It is now the turn of player " + currentPlayer.playerID);
			}
			for(int i=0; i<currentPlayer.turns; i++) { // every player can take more than one turn depending on what happens in the game.
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
					if(i==(currentPlayer.turns-1)) {
						currentPlayer.turns=1; //We have served all of our turns, reset it for the next player
					}
				}
			}
			while(currentPlayer.exploded && playersleft>1){ //next player that is still in the game
				currentplayernr ++;
				try {
					currentPlayer = playOrder.get(currentplayernr);
				}catch(IndexOutOfBoundsException e) {
					currentplayernr = 0;
					currentPlayer = playOrder.get(currentplayernr);
				}
			}
		}
		Player winner = currentPlayer;
		for(Player notify: players) {
			winner = (!notify.exploded?notify:winner);
		}
		for(Player notify: players) {
			notify.sendMessage("Player " + winner.playerID + " has won the game");
		}
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