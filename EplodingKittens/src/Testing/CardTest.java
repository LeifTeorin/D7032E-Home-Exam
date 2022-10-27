package Testing;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import Game.Cards.CardFunctions;
import Game.Players.Player;

class CardTest {

    @Test
    void passExplosionTest() {
        //Given: one player with an empty hand and a deck with only one card, an exploding kitten
        CardFunctions cards = new CardFunctions();
        Player player = new Player(0, false, null, null, null );
        ArrayList<String> deck = new ArrayList<String>();
        ArrayList<String> discard = new ArrayList<String>();
        ArrayList<Player> players = new ArrayList<Player>();
        players.add(player);
        player.hand.remove(0);
        deck.add("ExplodingKitten");
        
        //When the player passes their turn and draws a card
        int playersleft = cards.pass(player, deck, discard, 1, players);
        
        //Then there will be zero players left because our player blew up
        assertEquals(playersleft, 0);
        assertTrue(player.exploded);
    }
    
    @Test
    void passTest() {
        //Given
        CardFunctions cards = new CardFunctions();
        Player player = new Player(0, false, null, null, null );
        ArrayList<String> deck = new ArrayList<String>();
        ArrayList<String> discard = new ArrayList<String>();
        ArrayList<Player> players = new ArrayList<Player>();
        players.add(player);
        player.hand.remove(0);
        deck.add("Cattermelon");
        
        //When
        int playersleft = cards.pass(player, deck, discard, 1, players);
        
        //Then the player should be alive and
        assertEquals(playersleft, 1);
        assertEquals(player.turns, 0);
        assertFalse(player.exploded);
    }
    
    @Test
    void twoTest() {
        //Given
        CardFunctions cards = new CardFunctions();
        Player player1 = new Player(0, false, null, null, null );
        Player player2 = new Player(1, false, null, null, null );
        ArrayList<String> deck = new ArrayList<String>();
        ArrayList<String> discard = new ArrayList<String>();
        ArrayList<Player> players = new ArrayList<Player>();
        players.add(player1);
        players.add(player2);
        player1.hand.add("Cattermelon");
        player1.hand.add("Cattermelon");
        player2.hand.add("Skip");
        
        //When
        cards.two(player1, "Cattermelon", player2, deck, discard, players);
        
        //Then
        assertEquals(player1.hand.size(), 2);
        assertEquals(player2.hand.size(), 1);
    }
    
    @Test
    void threeTest() {
        //given
        CardFunctions cards = new CardFunctions();
        Player player1 = new Player(0, false, null, null, null );
        Player player2 = new Player(1, false, null, null, null );
        ArrayList<String> deck = new ArrayList<String>();
        ArrayList<String> discard = new ArrayList<String>();
        ArrayList<Player> players = new ArrayList<Player>();
        players.add(player1);
        players.add(player2);
        player1.hand.add("Cattermelon");
        player1.hand.add("Cattermelon");
        player1.hand.add("Cattermelon");
        player2.hand.add("Attack");
        
        //When
        cards.three(player1, "Cattermelon", "Attack", player2, deck, discard, players);
        
        //Then
        assertTrue(player1.hand.contains("Attack"));
        assertFalse(player2.hand.contains("Attack"));
    }
    
    @Test
    void attackTest() {
        //Given
        CardFunctions cards = new CardFunctions();
        Player player1 = new Player(0, false, null, null, null );
        Player player2 = new Player(1, false, null, null, null );
        ArrayList<String> deck = new ArrayList<String>();
        ArrayList<String> discard = new ArrayList<String>();
        ArrayList<Player> players = new ArrayList<Player>();
        players.add(player1);
        players.add(player2);
        player1.hand.add("Attack");
        
        //When
        cards.attack(player1, "Attack", player2, deck, discard, players);
        
        //Then
        assertEquals(player1.turns, 0);
        assertEquals(player2.turns, 2);
    }
    
    @Test
    void shuffleTest() {
        //Given
        CardFunctions cards = new CardFunctions();
        Player player = new Player(0, false, null, null, null );
        ArrayList<String> deck = new ArrayList<String>();
        ArrayList<String> discard = new ArrayList<String>();
        ArrayList<Player> players = new ArrayList<Player>();
        players.add(player);
        deck.add("ExplodingKitten");
        deck.add("Cattermelon");
        deck.add("Attack");
        ArrayList<String> originalDeck = new ArrayList<String>(deck);
        
        //When
        cards.shuffle(player, "Shuffle", deck, discard, players);
        
        //Then
        assertNotEquals(deck, originalDeck);
    }
    
    @Test
    void skipTest() {
        //Given
        CardFunctions cards = new CardFunctions();
        Player player = new Player(0, false, null, null, null );
        ArrayList<String> deck = new ArrayList<String>();
        ArrayList<String> discard = new ArrayList<String>();
        ArrayList<Player> players = new ArrayList<Player>();
        players.add(player);
        player.hand.remove("Defuse");
        player.hand.add("Skip");
        
        //When
        cards.skip(player, "Skip", deck, discard, players);
        
        //Then
        assertEquals(player.turns, 0);
    }

    @Test
    void seeTheFutureTest() {
        //Given
        CardFunctions cards = new CardFunctions();
        Player player = new Player(0, false, null, null, null );
        ArrayList<String> deck = new ArrayList<String>();
        ArrayList<String> discard = new ArrayList<String>();
        ArrayList<Player> players = new ArrayList<Player>();
        players.add(player);
        player.hand.add("SeeTheFuture");
        ArrayList<String> topCards = new ArrayList<String>();
        topCards.add("kitten");
        topCards.add("kitten2");
        topCards.add("kitten3");
        deck.add("kitten");
        deck.add("kitten2");
        deck.add("kitten3");
        
        //When
        ArrayList<String> future = cards.seethefuture(player, "SeeTheFuture", deck, discard, players);
        
        //Then
        assertEquals(future, topCards);
    }
}
