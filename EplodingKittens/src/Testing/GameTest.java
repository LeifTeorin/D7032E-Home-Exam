package Testing;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import Game.Game;

import Game.Player;

class GameTest {

	@Test
	void playOrderTest() throws IOException { 
		String params[] = {};
		Game testgame = new Game(params);
		Player player1 = new Player(0, false, null, null, null);
		Player player2 = new Player(1, false, null, null, null);
		ArrayList<Player> players = new ArrayList<Player>();
		players.add(player1);
		players.add(player2);
		ArrayList<Player> playOrder = testgame.playOrder(players);
		playOrder.get(0).playerID = 0;
		playOrder.get(1).playerID = 0;
		assertEquals(players.get(0).playerID, playOrder.get(0).playerID);
		assertEquals(players.get(1).playerID, playOrder.get(1).playerID);
		
	}
	
	@Test
	void listTest() {
//		ArrayList<String> testDeck = new ArrayList<String>();
//		String params[] = {};
//		Game testGame = new Game(params);
//		testDeck = testGame.createDeck(2);
//		testGame.skip(testDeck);
//		assertEquals("hejehj", testDeck.get(0));
	}
	
	@Test
	void deckTest() {
	    String params[] = {};
        Game testgame = new Game(params);
	    ArrayList<String> deck = new ArrayList<String>();
	    deck.add("Defuse");
    	for(int i=0; i<4; i++) {deck.add("Attack");}
        for(int i=0; i<4; i++) {deck.add("Favor");}
        for(int i=0; i<5; i++) {deck.add("Nope");}
        for(int i=0; i<4; i++) {deck.add("Shuffle");}
        for(int i=0; i<4; i++) {deck.add("Skip");}
        for(int i=0; i<5; i++) {deck.add("SeeTheFuture");}
        for(int i=0; i<4; i++) {deck.add("HairyPotatoCat");}
        for(int i=0; i<4; i++) {deck.add("RainbowRalphingCat");}
        for(int i=0; i<4; i++) {deck.add("Cattermelon");}
        for(int i=0; i<4; i++) {deck.add("TacoCat");}
        for(int i=0; i<4; i++) {deck.add("OverweightBikiniCat");}
        
        ArrayList<String> testDeck = testgame.createDeck(5);
        assertEquals(testDeck, deck);
	}
}
