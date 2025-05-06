package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;

public class ReaderController {
    private Reader reader;
    private Persistence persistence;
    public void setLector(Reader reader) {
        this.reader = reader;
    }

    public void setPersistence(Persistence persistence) {
        this.persistence = persistence;
    }
}
