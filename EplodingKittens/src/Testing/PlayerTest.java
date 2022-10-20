package Testing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import Game.Player;
import Game.Card;
import Game.Card.CardType;

class PlayerTest {

	@Test
	void defuseTest() {
		Player tester = new Player(0, false, null, null, null);
		CardType testcard = tester.hand.get(0).type;
		CardType testcard2 = CardType.Defuse;
		assertEquals(testcard, testcard2);
	}

}
