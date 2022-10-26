package Game;

import java.util.*; 
import java.io.*; 
import java.net.*;
import java.util.concurrent.*;

import org.json.JSONArray;
import org.json.JSONException;

public class DeckFactory {
    public DeckFactory() {
        
    }
    
    public JSONArray jsonArrayCreator(String filename) throws FileNotFoundException{
        try {
            String jsonString = readFile(filename);
            JSONArray jsonArray = new JSONArray(jsonString);
            return jsonArray;
        } catch (JSONException e) {
            throw new FileNotFoundException("inte bra");
        }
    }
    
    public String readFile(String resourceName) {
        String jsonData = "";
        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new FileReader(resourceName));
            while ((line = br.readLine()) != null) {
                jsonData += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return jsonData;
        
    }
    
    public ArrayList<String> createDeck(int numplayers){ //creates our deck for the game
        // TODO lägg till input för expansion, boolean
        
        ArrayList<String> deck = new ArrayList<String>();
        if(numplayers == 5) {
            deck.add("Defuse");
        }else {
            for(int i=0; i<2; i++) {deck.add("Defuse");}
        }
        try { // we read cardnames and amount of specific cards from our config file
            JSONArray cardlist = jsonArrayCreator("config.json");
            for (int i =0; i < cardlist.length(); i++){
                JSONArray newCard = cardlist.getJSONArray(i);
                for(int j=0; j < newCard.getInt(1); j++) {
                    deck.add(newCard.getString(0));
                }
            }
            
        }catch(FileNotFoundException e) {
            System.out.println("Something's wrong with your file structure");
            System.exit(0);
        }
        
        return deck;
    }
    
    public ArrayList<String> createDeckExpansion(int numplayers, String expansion){
        ArrayList<String> deck = new ArrayList<String>();
        if(numplayers == 5) {
            deck.add("Defuse");
        }else {
            for(int i=0; i<2; i++) {deck.add("Defuse");}
        }
        try { // we read cardnames and amount of specific cards from our config file
            JSONArray cardlist = jsonArrayCreator("config.json");
            for (int i =0; i < cardlist.length(); i++){
                JSONArray newCard = cardlist.getJSONArray(i);
                for(int j=0; j < newCard.getInt(1); j++) {
                    deck.add(newCard.getString(0));
                }
            }
            
        }catch(FileNotFoundException e) {
            System.out.println("Something's wrong with your file structure");
            System.exit(0);
        }
        
        try { //we do the same here but this time with our expansion
            JSONArray cardlist = jsonArrayCreator(expansion+".json");
            for (int i =0; i < cardlist.length(); i++){
                JSONArray newCard = cardlist.getJSONArray(i);
                for(int j=0; j < newCard.getInt(1); j++) {
                    deck.add(newCard.getString(0));
                }
            }
            
        }catch(FileNotFoundException e) {
            System.out.println("Something's wrong with your file structure");
            System.exit(0);
        }
        
        return deck;
    }
    
    public ArrayList<String> addKittens(ArrayList<String> inputDeck, int playercount) { // adds the right amount of kittens
        for(int i=0; i<playercount-1; i++) {
            inputDeck.add("ExplodingKitten");
        }
        return inputDeck;
    }
    
}