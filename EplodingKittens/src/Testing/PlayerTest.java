package Testing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import Game.Players.Player;

class PlayerTest {

	@Test
	void defuseTest() {
		Player tester = new Player(0, false, null, null, null);
		String testcard = tester.hand.get(0);
		String testcard2 = "Defuse";
		assertEquals(testcard, testcard2);
	}

	
}
