package spike;

import java.lang.reflect.TypeVariable;

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
        new GenericClass<String>();
    }
}
