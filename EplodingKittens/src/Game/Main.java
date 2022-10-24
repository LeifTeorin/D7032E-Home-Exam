package Game;

import java.util.*; 
import java.io.*; 
import java.net.*;
import java.util.concurrent.*;


public class Main {
	
    public static void main(String[] args) { 
    	String params[] = {"3", "5"};
    	try {
    		Game test = new Game(params);
    	} catch(Exception e) {
    		System.out.println(e.getMessage());
    	}
    	System.out.println("hmmm");
    }
}