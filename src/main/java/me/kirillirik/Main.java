package me.kirillirik;

public class Main {

    public static void main(String[] args) {
        final Window window = new Window();
        window.init();
        window.run();
        window.destroy();

        System.exit(0);
    }
}