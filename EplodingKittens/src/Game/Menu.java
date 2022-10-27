package Game;

import java.util.*; 
import java.io.*; 
import java.net.*;
import java.util.concurrent.*;

public class Menu {
    public Menu() {
        
    }
    
    public void runMenu() throws NumberFormatException, Exception {
            String word = "";
            Scanner sc = new Scanner(System.in); //System.in is a standard input stream  
            System.out.println("What would you like to do?");
            System.out.println("Your options are:");
            System.out.println("Server");
            System.out.println("Client");
            System.out.println("Exit");
            word = sc.nextLine();              //reads string  
            switch(word) {
                case "Server":
                    System.out.println("How many players?");
                    String players = sc.nextLine();
                    System.out.println("How many bots?");
                    String bots = sc.nextLine();
                    String[] args = {players, bots};
                    try {
                        Game server = new Game(args);
                        server.runGame(server.players, server.deck, server.discard);
                    }catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case "Client":
                    System.out.println("What is the IP?");
                    String ip = sc.nextLine();
                    String[] params = {ip};
                    try {
                        Game client = new Game(params);
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case "Exit":
                    System.exit(0);
                    break;
            }
        
    }
    
}