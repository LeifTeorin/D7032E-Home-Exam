package Game;

import java.util.*; 
import java.io.*; 
import java.net.*;
import java.util.concurrent.*;

import Game.Cards.CardFunctions;
import Game.Cards.DeckFactory;
import Game.Players.Player;





public class Game {
	
	public ArrayList<Player> players = new ArrayList<Player>();
	public ServerSocket aSocket;
	public ArrayList<String> deck = new ArrayList<String>();
	public ArrayList<String> discard = new ArrayList<String>();
	public int secondsToInterruptWithNope = 5;
	private CardFunctions cards = new CardFunctions();
	private DeckFactory deckMaker = new DeckFactory();
	
	public Game(String params[]) throws IOException{ // creates an instance of the game if the player amount is between 2 and 5, it also creates the deck
		if(params.length == 2) {
		    try {
                server(Integer.valueOf(params[0]).intValue(), Integer.valueOf(params[1]).intValue());
            } catch (NumberFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
			this.setUpGame(Integer.valueOf(params[0]).intValue(), Integer.valueOf(params[1]).intValue(), players); //we then set up the game if the player wants to run a server
		} else if(params.length == 1) { // if they just gave us an ip as an argument we set up their client
			try {
				client(params[0]);
			}catch(Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	public void setUpGame(int numplayers, int numbots, ArrayList<Player> players) throws IOException{  // we add all our players, give them cards and add exploding kittens
		if(numplayers+numbots > 5 || numplayers + numbots < 2) {
			throw new IOException("there can only be 2 - 5 players ya dingus"); //if there are too many or too few players we throw an exception
		}
		deck = deckMaker.createDeck(numplayers + numbots);
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
	
	public int playerInput(Player currentPlayer, String response, String otherPlayerIDs, String yourOptions, ArrayList<Player> players, ArrayList<Player> playOrder, ArrayList<String> deck, ArrayList<String> discard, int playersleft) {
	    if(yourOptions.contains(response.replaceAll("\\d",""))) { //remove targetID to match vs yourOptions
            try {
                String[] input = response.split(" ");
                if(input[0].equals("Pass")) { //Draw a card and end turn
                    playersleft = cards.pass(currentPlayer, deck, discard, playersleft, players); // in case the player blows up the playercount will go down
                } else if(input[0].equals("Two") && Collections.frequency(currentPlayer.hand, input[1])>=2 && otherPlayerIDs.contains(input[2])) { //played 2 of a kind - steal random card from target player
                    cards.two(currentPlayer, input[1], players.get(Integer.valueOf(input[2])), deck, discard, players);
                } else if(input[0].equals("Three") && Collections.frequency(currentPlayer.hand, input[1])>=3 && otherPlayerIDs.contains(input[2])) { // played 3 of a kind - name a card and force target player to hand one over if they have it
                    // if the player misspells they will not receive anything, but that's on them for not spelling correctly
                    cards.three(currentPlayer, input[1], input[3], players.get(Integer.valueOf(input[2])), deck, discard, players);
                } else if(input[0].equals("Attack")) {
                    Player target;
                    int currentplayernr = playOrder.indexOf(currentPlayer);
                    try {
                        target = playOrder.get(currentplayernr+1);
                    }catch(IndexOutOfBoundsException e) {
                        target = playOrder.get(0);
                    }
                    cards.attack(currentPlayer, "Attack", target, deck, discard, players);
                } else if(input[0].equals("Favor") && otherPlayerIDs.contains(input[1])) {
                    cards.favor(currentPlayer, "Favor", players.get(Integer.parseInt(input[1])), deck, discard, players);
                } else if(input[0].equals("Shuffle")) {
                    cards.shuffle(currentPlayer, "Shuffle", deck, discard, players);
                } else if(input[0].equals("Skip")) {
                    cards.skip(currentPlayer, "Skip", deck, discard, players);
                } else if(input[0].equals("SeeTheFuture")) {
                    ArrayList<String> topCards = cards.seethefuture(currentPlayer, "SeeTheFuture", deck, discard, players);
                    if(topCards.size()>0) {
                        currentPlayer.sendMessage("The top 3 cards are: " + topCards.get(0) + " " + topCards.get(1) + " " + topCards.get(2));
                    }else{ //this case should never be able to happen because if the deck is empty all players should have drawn an exploding kitten
                        currentPlayer.sendMessage("The deck is empty :,(");
                    }
                } else {
                    currentPlayer.sendMessage("Incorrect arguments");
                }
            }catch(IndexOutOfBoundsException e) {
                currentPlayer.sendMessage("Incorrect amount of arguments");
            }
        } else {
            currentPlayer.sendMessage("Not a viable option, try again");
        }
	    return playersleft;
	}
	
	public String yourOptions(Player currentPlayer, String otherPlayerIDs) { // This string will contain all actions the player can do and will be shown to them
	    String yourOptions = "You have the following options:\n";
        HashSet<String> handSet = new HashSet<String>(currentPlayer.hand); // We use this hashset to count the cards on the players hand
        for(String card : handSet) {
            int count = Collections.frequency(currentPlayer.hand, card);
            if(count>=2) {
                yourOptions += "\tTwo " + card + " [target] (available targets: " + otherPlayerIDs + ") (Steal random card)\n";
            }
            if(count>=3) {
                yourOptions += "\tThree " + card + " [target] [Card Type] (available targets: " + otherPlayerIDs + ") (Name and pick a card)\n";
            }
            String available = "Attack Favor Shuffle Skip SeeTheFuture";
            if(available.contains(card)) {
                yourOptions += "\t"+card+"\n";
            }
        }  
        yourOptions += "\tPass\n";
        return yourOptions;
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
			//Here we can check if the current player is a bot or not
			//If it is a bot the code below should not run, instead that player will run a function calledchooseMoveForBot which makes the bot's move
			Collections.sort(currentPlayer.hand);
			while(currentPlayer.turns > 0) { // every player can take more than one turn depending on what happens in the game.
			    // as long as the player has turns left it's their turn
				String otherPlayerIDs = "PlayerID: ";
				for(Player p : players) {
					if(p.playerID != currentPlayer.playerID && !p.exploded) {
						otherPlayerIDs += p.playerID + " ";
					}
				}

				String response = "";
				
				int turnsLeft = currentPlayer.turns;
				currentPlayer.sendMessage("\nYou have " + turnsLeft + ((turnsLeft>1)?" turns":" turn") + " to take");
				currentPlayer.sendMessage("Your hand: " + currentPlayer.hand);
				
				String yourOptions = yourOptions(currentPlayer, otherPlayerIDs);
				currentPlayer.sendMessage(yourOptions);
				response = currentPlayer.readMessage(false);
				playersleft = playerInput(currentPlayer, response, otherPlayerIDs, yourOptions, players, playOrder, deck, discard, playersleft); // we use the players input in our function to determine what happens next
				
			}
			currentPlayer.setTurns(1); // we reset the current players turns
			currentplayernr ++;
            try {
                currentPlayer = playOrder.get(currentplayernr);
            }catch(IndexOutOfBoundsException e) {
                currentplayernr = 0;
                currentPlayer = playOrder.get(currentplayernr);
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
        Socket aSocket = new Socket(ipAddress, 2049);
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
            aSocket = new ServerSocket(2049);
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