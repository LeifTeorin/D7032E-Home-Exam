package Game;

import java.util.*; 
import java.io.*; 
import java.net.*;
import java.util.concurrent.*;

public class Menu {
    public Menu() {
        
    }
    
    public void runMenu() {
        while(true) {
            String word = "";
            Scanner sc = new Scanner(System.in); //System.in is a standard input stream  
            System.out.print("What would you like to do?");  
            word = sc.nextLine();              //reads string  
        }
    }
    
    public void chooseAction(String input) {
        switch(input) {
            case "Server":
                break;
            case "Client":
                break;
        }
    }
}