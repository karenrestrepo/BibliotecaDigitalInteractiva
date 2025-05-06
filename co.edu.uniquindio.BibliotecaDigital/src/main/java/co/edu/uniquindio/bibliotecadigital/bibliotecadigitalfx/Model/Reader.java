package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;

public class Reader extends Person {
    private LinkedList<Book> loanHistoryList;
    private LinkedList<Rating> ratingsList;

    public Reader() {

    }

    public Reader(String name, String username, String password) {
        super(name, username, password);
        this.loanHistoryList = new LinkedList<>();
        this.ratingsList = new LinkedList<>();
    }
}
