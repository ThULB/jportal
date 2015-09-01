package fsu.jportal.backend;

import java.util.HashMap;

public class GreetingsManager {
    private static HashMap<String, GreetingsFS> fsMap = new HashMap<>();
    
    public static  GreetingsFS createFS(String journalID){
        GreetingsFS greetingsFS = fsMap.get(journalID);
        if(greetingsFS == null){
            greetingsFS = new GreetingsFS(journalID);
            fsMap.put(journalID, greetingsFS);
        }
        
        return greetingsFS;
    }
}
