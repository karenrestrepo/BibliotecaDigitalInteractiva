package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;

public class Reader extends Person {
    private LinkedList<Book> loanHistory;
    private LinkedList<Rating> ratings;

    public Reader() {

    }

    public Reader(String name, String username, String password) {
        super(name, username, password);
        this.loanHistory = new LinkedList<>();
        this.ratings = new LinkedList<>();
    }
}
