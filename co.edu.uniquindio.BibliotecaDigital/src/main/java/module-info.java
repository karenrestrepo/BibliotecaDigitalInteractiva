module co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx {
    requires javafx.controls;
    requires javafx.fxml;


    opens co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx to javafx.fxml;
    exports co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx;
}