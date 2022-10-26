package Game;

import java.util.*; 
import java.io.*; 
import java.net.*;
import java.util.concurrent.*;
import Game.Player;


public class CardFunctions {
    
    public CardFunctions() {
        
    }
    
    private int checkNrNope(ArrayList<String> discard) {
        int i=0;
        while(i<discard.size() && discard.get(i)=="Nope") {
            i++;    
        }
        return i;
    }
    
    public void addToDiscardPile(Player currentPlayer, String card, ArrayList<String> discard, ArrayList<String> deck, ArrayList<Player> players){
        //After an interruptable card is played everyone has 5 seconds to play Nope
        int nopePlayed = checkNrNope(discard);
        ExecutorService threadpool = Executors.newFixedThreadPool(players.size());
        for(Player p : players) {
            p.sendMessage("Action: Player " + currentPlayer.playerID + " played " + card);
            if(p.hand.contains("Nope")) { //only give the option to interrupt to those who have a Nope card
                p.sendMessage("Press <Enter> to play Nope");
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String nextMessage = p.readMessage(true); //Read that is interrupted after secondsToInterruptWithNope
                            if(!nextMessage.equals(" ") && p.hand.contains("Nope")) {
                                p.hand.remove("Nope");
                                discard.add(0, "Nope");
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
        try {
            threadpool.awaitTermination((5*1000)+500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } //add an additional delay to avoid concurrancy problems with the ObjectInputStream
        for(Player notify: players)
            notify.sendMessage("The timewindow to play Nope passed");
        if(checkNrNope(discard)>nopePlayed) {
            for(Player notify: players)
                notify.sendMessage("Play another Nope? (alternate between Nope and Yup)");
            addToDiscardPile(currentPlayer, card, discard, deck, players);
        }
    }
    
    public int pass(Player currentPlayer, ArrayList<String> deck, ArrayList<String> discardPile, int playersleft, ArrayList<Player> players) {
        String drawn = deck.remove(0);
        if(drawn == "ExplodingKitten") {
            if(currentPlayer.hand.contains("Defuse")) {
                currentPlayer.sendMessage("You defused the kitten. Where in the deck do you wish to place the ExplodingKitten? [0.." + (deck.size()-1) + "]");
                try {
                    deck.add((Integer.valueOf(currentPlayer.readMessage(false))).intValue(), drawn);
                }catch(IndexOutOfBoundsException e) { // if the player decides to be a funnyman and write a number that's out of bounds we place it at the back of the deck
                    deck.add(drawn);
                }
                for(Player p : players) {
                    p.sendMessage("Player " + currentPlayer.playerID + " successfully defused a kitten");
                }
                currentPlayer.hand.remove("Defuse");
                discardPile.add("Defuse");
            }else {
                discardPile.add(drawn);
                discardPile.addAll(currentPlayer.hand);
                currentPlayer.hand.clear();
                currentPlayer.blowUp();
                for(Player p : players) {
                    p.sendMessage("Player " + currentPlayer.playerID + " exploded");
                }
                playersleft --;
            }
            
            return playersleft;
        }else {
            currentPlayer.hand.add(drawn);
            currentPlayer.sendMessage("You drew: " + drawn);
            return playersleft;
        }
    }
    
    public void two(Player currentPlayer, String played, Player target, ArrayList<String> deck, ArrayList<String> discardPile, ArrayList<Player> players) {
        currentPlayer.hand.remove(played); 
        currentPlayer.hand.remove(played);
        discardPile.add(0, played);
        discardPile.add(0, played);
        addToDiscardPile(currentPlayer, "Two of a kind against player " + Integer.toString(target.playerID), discardPile, deck, players);
        
        if(checkNrNope(discardPile) % 2 == 0) { // if an even amount of nopes have been played it means yea
            Random rnd = new Random();
            try {
                String aCard = target.hand.remove(rnd.nextInt(target.hand.size()-1));
                currentPlayer.hand.add(aCard);
                target.sendMessage("You gave " + aCard + " to player " + currentPlayer.playerID);
                currentPlayer.sendMessage("You received " + aCard + " from player " + target.playerID);
            }catch(IndexOutOfBoundsException e) {
                target.sendMessage("You have no cards to give");
                currentPlayer.sendMessage("Player " + target.playerID + " had no cards to give");
            }
        }
    }
    
    public void three(Player currentPlayer, String played, String targetCard, Player target, ArrayList<String> deck, ArrayList<String> discardPile, ArrayList<Player> players) {
        currentPlayer.hand.remove(played); 
        currentPlayer.hand.remove(played);
        currentPlayer.hand.remove(played);
        discardPile.add(0, played);
        discardPile.add(0, played);
        discardPile.add(0, played);
        addToDiscardPile(currentPlayer, "Three of a kind against player " + Integer.toString(target.playerID), discardPile, deck, players);
        if(checkNrNope(discardPile) % 2 == 0) {
            if(target.hand.contains(targetCard)) {
                target.hand.remove(targetCard);
                currentPlayer.hand.add(targetCard);
                target.sendMessage("Player " + currentPlayer.playerID + " stole " + targetCard);
                currentPlayer.sendMessage("You received " + targetCard + " from player " + target.playerID);                                        
            } else {
                currentPlayer.sendMessage("The player did not have any " + targetCard);
            }                               
        }
    }
    
    public boolean attack(Player currentplayer, String played, Player target, ArrayList<String> deck, ArrayList<String> discardPile, ArrayList<Player> players) { // attack returns true if the attack succeeds
        int turnsToTake = 0;
        if(discardPile.size()>0 && discardPile.get(0).equals("Attack")) { // if the last player played an attack card we add 2 more turns for the next poor bastard
            turnsToTake = currentplayer.turns + 2;  
        } else {
            turnsToTake = 2;
        }
        currentplayer.hand.remove("Attack");
        discardPile.add(0, "Attack");
        addToDiscardPile(currentplayer, "Attack", deck, discardPile, players);
        if(checkNrNope(discardPile) % 2 == 0) {
            target.turns = turnsToTake; 
            return true;
        }
        return false;
    }
    
    public void favor(Player currentplayer, String played, Player target, ArrayList<String> deck, ArrayList<String> discardPile, ArrayList<Player> players) {
        currentplayer.hand.remove("Favor");
        discardPile.add(0, "Favor");
        addToDiscardPile(currentplayer, "Favor player " + target.playerID, deck, discardPile, players);
        if(checkNrNope(discardPile) % 2 == 0) {
            boolean viableOption = false;
            if(target.hand.size()==0)
                viableOption=true; //special case - target has no cards to give
            while(!viableOption) {
                target.sendMessage("Your hand: " + target.hand);
                target.sendMessage("Give a card to Player " + currentplayer.playerID);
                String tres = target.readMessage(false);
                if(target.hand.contains(tres)) {
                    viableOption = true;
                    currentplayer.hand.add(tres);
                    target.hand.remove(tres);
                } else {
                    target.sendMessage("Not a viable option, try again");
                }
            }                               
        }
    }
    
    public void shuffle(Player currentplayer, String played, ArrayList<String> deck, ArrayList<String> discardPile, ArrayList<Player> players) {
        discardPile.add(0, "Shuffle");
        currentplayer.hand.remove("Shuffle");
        addToDiscardPile(currentplayer, "Shuffle", deck, discardPile, players);
        if(checkNrNope(discardPile) % 2 == 0) {
            Collections.shuffle(deck);
        }
    }
    
    public boolean skip(Player currentplayer, String played, ArrayList<String> deck, ArrayList<String> discardPile, ArrayList<Player> players) { //returns true if the player gets to skip their turn
        currentplayer.hand.remove("Skip");
        discardPile.add(0, "Skip");
        addToDiscardPile(currentplayer, "Skip", discardPile, deck, players);
        if(checkNrNope(discardPile) % 2 == 0) {
            
            return true; //Exit the while loop
        }
        return false;
    }
    
    public void seethefuture(Player currentplayer, String played, ArrayList<String> deck, ArrayList<String> discardPile, ArrayList<Player> players) {
        currentplayer.hand.remove("SeeTheFuture");
        discardPile.add(0, "SeeTheFuture");
        addToDiscardPile(currentplayer, "SeeTheFuture", discardPile, deck, players);
        if(checkNrNope(discardPile) % 2 == 0) {
            currentplayer.sendMessage("The top 3 cards are: " + deck.get(0) + " " + deck.get(1) + " " + deck.get(2));
        }
    }
}