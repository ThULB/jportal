package fsu.jportal.backend;

import java.util.HashMap;

public class ImprintManager {
    private static HashMap<String, ImprintFS> fsMap = new HashMap<>();
    
    public static ImprintFS createFS(String fsType){
        ImprintFS imprintFS = fsMap.get(fsType);
        if(imprintFS == null){
            imprintFS = new ImprintFS(fsType);
            fsMap.put(fsType, imprintFS);
        }
        
        return imprintFS;
    }
}
