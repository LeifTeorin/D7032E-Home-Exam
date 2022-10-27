package Testing;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import Game.Game;
import Game.Players.Player;

class GameTest {

	@Test
	void playOrderTest() throws IOException { 
		String params[] = {};
		Game testgame = new Game(params);
		Player player1 = new Player(0, false, null, null, null);
		Player player2 = new Player(1, false, null, null, null);
		Player player3 = new Player(2, false, null, null, null);
		Player player4 = new Player(3, false, null, null, null);
		ArrayList<Player> players = new ArrayList<Player>();
		players.add(player1);
		players.add(player2);
		players.add(player3);
		players.add(player4);
		ArrayList<Player> playOrder = testgame.playOrder(players);
		assertNotEquals(players, playOrder);
		
	}
	
	@Test
	void handOutTest() {
	    //Given
	    String params[] = {};
	    ArrayList<Player> players = new ArrayList<Player>();
	    try {
	        Game testgame = new Game(params);
	        players.add(new Player(1, false, null, null, null));
	        players.add(new Player(2, false, null, null, null));
	        
	        //When
	        testgame.setUpGame(2, 0, players);
	        Player player1 = players.get(0);
	        Player player2 = players.get(1);
	        
	        //Then
	        assertEquals(player1.hand.size(), 8);
	        assertEquals(player2.hand.size(), 8);
	    }catch(IOException e) {
	        fail("Something is wrong with the number of players");
	    }
	}
	
	@Test
	void playerCountTest() {
	    String params[] = {};
	    try {
	        Game testgame = new Game(params);
	        testgame.setUpGame(7, 0, null); // we don't need to add a list of players because we are only testing for the number of players
	        fail("Expected an IOException to be thrown");
	    }catch(IOException e) {
	        
	    }
	}
	
	@Test
    void seeHand() {
        Player tester = new Player(0, false, null, null, null);
        tester.hand.add("katt");
        tester.hand.add("Attack");
        String params[] = {};
        try {
            Game testgame = new Game(params);
            testgame.players.add(tester);
            System.out.println(testgame.yourOptions(tester, "1"));
        }catch(IOException e) {
            
        }
    }
	
}
