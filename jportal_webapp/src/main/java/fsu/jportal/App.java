package fsu.jportal;

/**
 * Created by chi on 15.01.19.
 * @author Huu Chi Vu
 */
public class App {
    public String getGreeting() {

        return "Hello world.";
    }

    public static void main(String[] args) {
        System.out.println(new App().getGreeting());
    }
}