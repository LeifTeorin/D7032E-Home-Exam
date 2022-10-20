package Game;

import java.util.*; 
import java.io.*; 
import java.net.*;
import java.util.concurrent.*;

import Game.Player;


public class Card {
	public CardType type;
	
	
	public static enum CardType {
		ExplodingKitten,
		Defuse,
		Attack,
		Favor,
		Nope,
		Shuffle,
		Skip,
		SeeTheFuture,
		HairyPotatoCat,
		Cattermelon,
		RainbowRalphingCat,
		TacoCat,
		OverweightBikiniCat
	}
	
	public Card(CardType cardtype) {
		this.type = cardtype;
	}
	
	// Card ska inte ta hand om alla funktioner tror jag
}