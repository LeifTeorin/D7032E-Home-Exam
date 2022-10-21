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
		try {
			Game testgame = new Game(3, 0);
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
		}catch (IOException e) {
			
		}
		
	}

}
