package org.example;

public class Detail {
    private static int idCounter = 0;
    private int id;

    public Detail() {
        this.id = idCounter++;
    }

    public int getId() {
        return id;
    }
}
