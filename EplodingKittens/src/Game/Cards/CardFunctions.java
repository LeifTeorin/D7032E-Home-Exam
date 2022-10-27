package Game.Cards;

import java.util.*; 
import java.io.*; 
import java.net.*;
import java.util.concurrent.*;

import Game.Players.Player;


public class CardFunctions {
    
    private int secondsToPlayNope = 5;
    // This file contains the functions for all our cards, more cards can be added to either this file or a file for just expansion cards
    public CardFunctions() {
        
    }
    
    private int checkNrNope(ArrayList<String> discard) {
        int i=0;
        while(i<discard.size() && discard.get(i).equals("Nope")) {
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
            threadpool.awaitTermination((secondsToPlayNope*1000)+500, TimeUnit.MILLISECONDS); //
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
    
    public int pass(Player currentPlayer, ArrayList<String> deck, ArrayList<String> discardPile, int playersleft, ArrayList<Player> players) { // this function draws a card for the player and potentially blows them up
        // We return the number of players left after so the game knows if this player is still alive
        String drawn = deck.remove(0); // We draw the first card
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
                currentPlayer.decreaseTurns(); // The player made it past their turn so the number decreases
            }else {
                discardPile.add(drawn);
                discardPile.addAll(currentPlayer.hand);
                currentPlayer.hand.clear();
                currentPlayer.blowUp();
                for(Player p : players) {
                    p.sendMessage("Player " + currentPlayer.playerID + " exploded");
                }
                currentPlayer.setTurns(0); // The player doesn't have any turns left because they blew up, very tragic
                playersleft --;
            }
            
            return playersleft;
        }else {
            currentPlayer.hand.add(drawn);
            currentPlayer.sendMessage("You drew: " + drawn);
            currentPlayer.decreaseTurns(); // the player has one less turn now
            return playersleft; // the player count has not changed
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
                String aCard = target.hand.remove(rnd.nextInt(target.hand.size()));
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
            if(target.hand.contains(targetCard)) { // if they have the card we take it
                target.hand.remove(targetCard);
                currentPlayer.hand.add(targetCard);
                target.sendMessage("Player " + currentPlayer.playerID + " stole " + targetCard);
                currentPlayer.sendMessage("You received " + targetCard + " from player " + target.playerID);                                        
            } else { // if not we let the player know of their foolish mistake
                currentPlayer.sendMessage("The player did not have any " + targetCard);
            }                               
        }
    }
    
    public void attack(Player currentplayer, String played, Player target, ArrayList<String> deck, ArrayList<String> discardPile, ArrayList<Player> players) { // attack increases target's turns and removes the players' turns
        int turnsToTake = 0;
        if(discardPile.size()>0 && discardPile.get(0).equals("Attack")) { // if the last player played an attack card we add 2 more turns for the next poor bastard
            turnsToTake = currentplayer.turns + 2;  
        } else {
            turnsToTake = 2;
        }
        currentplayer.hand.remove("Attack");
        discardPile.add(0, "Attack");
        addToDiscardPile(currentplayer, "Attack", deck, discardPile, players);
        if(checkNrNope(discardPile) % 2 == 0) { // all of this will only go through if it hasn't been noped, of course
            currentplayer.setTurns(0);
            target.setTurns(turnsToTake); 
        }
    }
    
    public void favor(Player currentplayer, String played, Player target, ArrayList<String> deck, ArrayList<String> discardPile, ArrayList<Player> players) { // The target has to give a card of their choosing to the player
        currentplayer.hand.remove("Favor");
        discardPile.add(0, "Favor");
        addToDiscardPile(currentplayer, "Favor player " + target.playerID, deck, discardPile, players);
        if(checkNrNope(discardPile) % 2 == 0) {
            boolean viableOption = false; // In the future we need a check here to see if the target is a bot, if it is a bot it needs to choose a card to give on its' own
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
                    target.sendMessage("Not a viable option, try again"); // The target can't escape by typing a card they don't have
                }
            }                               
        }
    }
    
    public void shuffle(Player currentplayer, String played, ArrayList<String> deck, ArrayList<String> discardPile, ArrayList<Player> players) { // this function just shuffles the deck
        discardPile.add(0, "Shuffle");
        currentplayer.hand.remove("Shuffle");
        addToDiscardPile(currentplayer, "Shuffle", deck, discardPile, players);
        if(checkNrNope(discardPile) % 2 == 0) {
            Collections.shuffle(deck);
        }
    }
    
    public void skip(Player currentplayer, String played, ArrayList<String> deck, ArrayList<String> discardPile, ArrayList<Player> players) { //reduces the players turns if the player gets to skip their turn
        currentplayer.hand.remove("Skip");
        discardPile.add(0, "Skip");
        addToDiscardPile(currentplayer, "Skip", discardPile, deck, players);
        if(checkNrNope(discardPile) % 2 == 0) {
            currentplayer.turns--; //Exit the while loop
        }
    }
    
    public ArrayList<String> seethefuture(Player currentplayer, String played, ArrayList<String> deck, ArrayList<String> discardPile, ArrayList<Player> players) { // Returns the top three cards
        currentplayer.hand.remove("SeeTheFuture");
        discardPile.add(0, "SeeTheFuture");
        addToDiscardPile(currentplayer, "SeeTheFuture", discardPile, deck, players);
        ArrayList<String> cards = new ArrayList<String>();
        if(checkNrNope(discardPile) % 2 == 0) {
            if(deck.size() < 3) { // this is a rare case where there are less than three cards in the deck
                for(int i=0;i<deck.size();i++) {cards.add(deck.get(i));}
            }else {
                cards.add(deck.get(0));
                cards.add(deck.get(1));
                cards.add(deck.get(2));
            }
//            currentplayer.sendMessage("The top 3 cards are: " + deck.get(0) + " " + deck.get(1) + " " + deck.get(2));
            return cards;
        }
        return cards;
    }
}