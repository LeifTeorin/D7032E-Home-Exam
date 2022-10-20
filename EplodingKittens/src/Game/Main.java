package Game;

import java.util.*; 
import java.io.*; 
import java.net.*;
import java.util.concurrent.*;


public class Main {
	
    public static void main(String[] args) { 
    	try {
    		Game test = new Game(6, 0);
    	} catch(Exception e) {
    		System.out.println(e.getMessage());
    	}
    	System.out.println("hmmm");
    }
}