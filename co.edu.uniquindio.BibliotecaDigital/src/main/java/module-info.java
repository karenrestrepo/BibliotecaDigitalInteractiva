module co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx {
    requires javafx.controls;
    requires javafx.fxml;


    opens co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx to javafx.fxml;
    exports co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx;

    opens co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;
    exports co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

    opens co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.pruebas;
    exports co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.pruebas;
}