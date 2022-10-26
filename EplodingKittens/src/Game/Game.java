package Game;

import java.util.*; 
import java.io.*; 
import java.net.*;
import java.util.concurrent.*;
import org.json.*;

import Game.CardFunctions;





public class Game {
	
	public ArrayList<Player> players = new ArrayList<Player>();
	public ServerSocket aSocket;
	public ArrayList<String> deck = new ArrayList<String>();
	public ArrayList<String> discard = new ArrayList<String>();
	public int secondsToInterruptWithNope = 5;
	private CardFunctions cards = new CardFunctions();
	private DeckFactory deckMaker = new DeckFactory();
	
	public Game(String params[]){ // creates an instance of the game if the player amount is between 2 and 5, it also creates the deck
		if(params.length == 2) {
			try {
				this.setUpGame(Integer.valueOf(params[0]).intValue(), Integer.valueOf(params[1]).intValue()); //we then set up the game if the player wants to run a server
			} catch (IOException e) {
				e.getMessage();
				System.exit(0);
			}
		} else if(params.length == 1) { // if they just gave us an ip as an argument we set up their client
			try {
				client(params[0]);
			}catch(Exception e) {
				e.getMessage();
			}
		}
	}
	
	public void setUpGame(int numplayers, int numbots) throws IOException{  // we host our server and add all our players, give them cards and add exploding kittens
		if(numplayers+numbots > 5 || numplayers + numbots < 2) {
			throw new IOException("there can only be 2 - 5 players ya dinguz"); //if there are too many or too few players we throw an exception
		}
		deck = deckMaker.createDeck(numplayers + numbots);
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
		
		deck = deckMaker.addKittens(deck, numplayers + numbots);
		Collections.shuffle(deck);
		
	}
	
	public ArrayList<Player> playOrder(ArrayList<Player> players){ // this creates a shuffled copy of our list of players, we will use this to decide which order the players will go
		ArrayList<Player> playOrder = new ArrayList<Player>(players);
		Collections.shuffle(playOrder);
		return playOrder;
	}
		
	public void runGame(ArrayList<Player> players, ArrayList<String> deck, ArrayList<String> discard) {
		int currentplayernr = 0;
		int playersleft = players.size();
		ArrayList<Player> playOrder = playOrder(players);
		Player currentPlayer = playOrder.get(currentplayernr);
		
		while(playersleft > 1) { // as long as there are players left the game goes on
			for(Player p : players) {
				if(p == currentPlayer) // we message all players, even the dead ones of what's going on
					p.sendMessage("It is your turn");
				else
					p.sendMessage("It is now the turn of player " + currentPlayer.playerID);
			}
			for(int i=0; i<currentPlayer.turns; i++) { // every player can take more than one turn depending on what happens in the game.
				String otherPlayerIDs = "PlayerID: ";
				for(Player p : players) {
					if(p.playerID != currentPlayer.playerID && !p.exploded) {
						otherPlayerIDs += p.playerID + " ";
					}
				}

				String response = "";
				while(true) { // this function only breaks when a player passes, skips or succeeds with an attack in these cases we break the loop
					int turnsLeft = currentPlayer.turns-i;
					currentPlayer.sendMessage("\nYou have " + turnsLeft + ((turnsLeft>1)?" turns":" turn") + " to take");
					currentPlayer.sendMessage("Your hand: " + currentPlayer.hand);
					String yourOptions = "You have the following options:\n";
					Set<String> handSet = new HashSet<String>(currentPlayer.hand);
					for(String card : handSet) {
						int count = Collections.frequency(currentPlayer.hand, card);
						if(count>=2)
							yourOptions += "\tTwo " + card + " [target] (available targets: " + otherPlayerIDs + ") (Steal random card)\n";
						if(count>=3)
							yourOptions += "\tThree " + card + " [target] [Card Type] (available targets: " + otherPlayerIDs + ") (Name and pick a card)\n";
						if(card == "Attack")
							yourOptions += "\tAttack\n";
						if(card == "Favor")
							yourOptions += "\tFavor [target] (available targets: " + otherPlayerIDs + ")\n";
						if(card == "Shuffle")
							yourOptions += "\tShuffle\n";
						if(card == "Skip")
							yourOptions += "\tSkip\n";
						if(card == "SeeTheFuture")
							yourOptions += "\tSeeTheFuture\n";
					}  
					yourOptions += "\tPass\n";
					//We don't need to offer Nope as an option - it's only viable 5 seconds after another card is played and handled elsewhere
					
					currentPlayer.sendMessage(yourOptions);
					response = currentPlayer.readMessage(false);
					if(yourOptions.contains(response.replaceAll("\\d",""))) { //remove targetID to match vs yourOptions
					    try {
    					    String[] input = response.split(" ");
    					    if(input[0] == "Pass") { //Draw a card and end turn
    					        playersleft = cards.pass(currentPlayer, deck, discard, playersleft, players); // in case the player blows up the playercount will go down
    					        break;
    					    } else if(input[0] == "Two" && Collections.frequency(currentPlayer.hand, input[1])>=2 && otherPlayerIDs.contains(input[2])) { //played 2 of a kind - steal random card from target player
    							cards.two(currentPlayer, input[1], players.get(Integer.valueOf(input[2])), deck, discard, players);
    					    } else if(input[0] == "Three" && Collections.frequency(currentPlayer.hand, input[1])>=3 && otherPlayerIDs.contains(input[2])) { // played 3 of a kind - name a card and force target player to hand one over if they have it
    							// if the player misspells they will not receive anything, currently
    					        cards.three(currentPlayer, input[1], input[3], players.get(Integer.valueOf(input[2])), deck, discard, players);
    					        
    					    } else if(input[0] == "Attack") {
    					        Player target;
    					        try {
    					            target = playOrder.get(currentplayernr+1);
    					        }catch(IndexOutOfBoundsException e) {
    					            target = playOrder.get(0);
    					        }
    							if(cards.attack(currentPlayer, "Attack", target, deck, discard, players)) {
    							    break;
    							}
    					    } else if(input[0] == "Favor" && otherPlayerIDs.contains(input[1])) {
    					        cards.favor(currentPlayer, "Favor", players.get(Integer.parseInt(input[1])), deck, discard, players);
    					    } else if(input[0] == "Shuffle") {
    					        cards.shuffle(currentPlayer, "Shuffle", deck, discard, players);
    					    } else if(input[0] == "Skip") {
    							if(cards.skip(currentPlayer, "Skip", deck, discard, players)) {
    							    break;
    							}
    					    } else if(input[0] == "SeeTheFuture") {
    							cards.seethefuture(currentPlayer, "SeeTheFuture", deck, discard, players);			
    					    } else {
    					        currentPlayer.sendMessage("Incorrect arguments");
    					    }
					    }catch(IndexOutOfBoundsException e) {
					        currentPlayer.sendMessage("Incorrect amount of arguments");
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