package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Application;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Person;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;

public class BibliotecaApp {

    LinkedList <Reader> listReader= new LinkedList<>();

    public void addReader(String name, String username, String password) {
        if (readerExists(username)) {
            throw new RuntimeException("The reader with username '" + username + "' already exists.");
        }

        Reader reader = new Reader();
        reader.setName(name);
        reader.setUsername(username);
        reader.setPassword(password);

        listReader.addEnd(reader);
    }

    private boolean readerExists(String username) {
        for (int i = 0; i < listReader.getSize(); i++) {
            Reader current = listReader.getAmountNodo(i);
            if (current.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }
}
