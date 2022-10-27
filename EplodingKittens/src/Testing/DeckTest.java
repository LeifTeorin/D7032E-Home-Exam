package Testing;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

import Game.Cards.DeckFactory;

public class DeckTest {

	@Test
	public void deckBuildTest() {
	    //Given
	    DeckFactory factory = new DeckFactory();
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
        
        //When
        ArrayList<String> testDeck = factory.createDeck(5);
        
        //Then
        assertEquals(testDeck, deck);
	}
	
	@Test
	public void addKittensTest() {
	    DeckFactory factory = new DeckFactory();
        ArrayList<String> deck = new ArrayList<String>();
        deck.add("ExplodingKitten");
        ArrayList<String> testDeck = new ArrayList<String>();
        testDeck = factory.addKittens(testDeck, 2);
        assertEquals(testDeck, deck);
	}
	
	@Test
	public void deckBuildWithExpansion() {
	    
	}
}
