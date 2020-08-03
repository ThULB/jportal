package spike;

import java.lang.reflect.TypeVariable;
import java.util.UUID;

import org.junit.Test;


public class GenericSpike {
    public static class GenericClass<T>{
        Class<T> declaredClass;
        
        public GenericClass() {
            TypeVariable<?>[] typeParameters = getClass().getTypeParameters();
            
            for (TypeVariable<?> typeVariable : typeParameters) {
                System.out.println(typeVariable.getBounds());
            }
        }
    }
    
    @Test
    public void createGenerics() throws Exception {
        
        long leastSignificantBits = UUID.randomUUID().getLeastSignificantBits();
        long mostSignificantBits = UUID.randomUUID().getMostSignificantBits();
        String least = Long.toHexString(leastSignificantBits);
        String most = Long.toHexString(2);
        String string = UUID.randomUUID().toString().replaceAll("-", "");
        System.out.println(string + " length: " + string.length());
        System.out.println(most + " length: " + most.length());
        System.out.println(least + " length: " + least.length());
        new GenericClass<String>();
    }
}
