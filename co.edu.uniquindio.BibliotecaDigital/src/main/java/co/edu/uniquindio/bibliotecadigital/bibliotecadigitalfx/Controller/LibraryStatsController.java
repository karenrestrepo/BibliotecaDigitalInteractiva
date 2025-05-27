package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Book;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service.AffinitySystem;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;

/**
 * Controlador para el panel de estadísticas del administrador
 *
 * Conceptos aplicados:
 * - Algoritmos de ordenamiento para rankings
 * - Análisis de grafos para componentes conectados
 * - Algoritmos de camino más corto (BFS)
 * - Agregación y análisis de datos
 */
public class LibraryStatsController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;

    // Elementos de la interfaz para préstamos por lector
    @FXML private TableView<LoanStatistic> tableLoans;
    @FXML private TableColumn<LoanStatistic, String> tcReaderLoans;
    @FXML private TableColumn<LoanStatistic, String> tcAmountLoans;
    @FXML private TextField txtFilterReader;

    // Elementos para libros más valorados
    @FXML private TableView<RatingStatistic> tableRating;
    @FXML private TableColumn<RatingStatistic, String> tcTitle;
    @FXML private TableColumn<RatingStatistic, String> tcRating;

    // Elementos para lectores con más conexiones
    @FXML private TableView<ConnectionStatistic> tableConnection;
    @FXML private TableColumn<ConnectionStatistic, String> tcReaderConnection;
    @FXML private TableColumn<ConnectionStatistic, String> tcAmountConnection;

    // Elementos para camino más corto
    @FXML private ComboBox<Reader> cbReaderA;
    @FXML private ComboBox<Reader> cbReaderB;
    @FXML private Button btnSearch;
    @FXML private TextArea lblCamino;

    // Elementos para clústeres
    @FXML private ListView<String> tvClusters;

    private Library library;
    private AffinitySystem affinitySystem;

    /// ayuda actulizar la stablas y los combo box sin tener que salir y volver a entrar

    @FXML
    void onSearch(ActionEvent event) {
        Reader readerA = cbReaderA.getValue();
        Reader readerB = cbReaderB.getValue();

        if (readerA == null || readerB == null) {
            lblCamino.setText("Selecciona dos lectores válidos.");
            return;
        }

        if (readerA.equals(readerB)) {
            lblCamino.setText("Los lectores seleccionados son el mismo.");
            return;
        }

        // Encontrar camino más corto usando BFS
        LinkedList<Reader> path = affinitySystem.getShortestPath(readerA, readerB);

        if (path.getSize() == 0) {
            lblCamino.setText("No existe conexión entre " + readerA.getName() +
                    " y " + readerB.getName() + ".\nEstán en diferentes clústeres de afinidad.");
        } else {
            StringBuilder pathDescription = new StringBuilder();
            pathDescription.append("Camino encontrado (").append(path.getSize()).append(" pasos):\n\n");

            for (int i = 0; i < path.getSize(); i++) {
                pathDescription.append((i + 1)).append(". ").append(path.getAmountNodo(i).getName());
                if (i < path.getSize() - 1) {
                    pathDescription.append("\n   ↓ (conectados por gustos similares)\n");
                }
            }

            lblCamino.setText(pathDescription.toString());
        }

    }
    @FXML
    void onActualizar(ActionEvent event) {
        // NUEVO: Primero forzar recarga de datos desde persistencia
        try {
            System.out.println("🔄 Actualizando datos desde persistencia...");

            // Recargar la biblioteca desde persistencia
            Library library = Library.getInstance();
            library.forceRefreshAllData();

            // Recrear el sistema de afinidad con datos frescos
            this.affinitySystem = new AffinitySystem(library);

            // Actualizar todas las interfaces
            setupComboBoxes();
            loadAllStatistics();

            System.out.println("✅ Datos actualizados correctamente");

        } catch (Exception e) {
            System.err.println("❌ Error actualizando datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void initialize() {
        library = Library.getInstance();
        affinitySystem = new AffinitySystem(library);

        setupTableColumns();
        loadAllStatistics();
        setupComboBoxes();
        setupFilterListener();

        // CORRECCIÓN: Registrar este controlador
        ControllerRegistry.getInstance().registerController("LibraryStatsController", this);
    }

    /**
     * Configura las columnas de las tablas con sus respectivos extractores de datos
     *
     * Lección: Este patrón se llama "data binding" y es fundamental en interfaces reactivas.
     * Cada columna sabe cómo extraer su valor del objeto correspondiente.
     */
    private void setupTableColumns() {
        // Configurar tabla de préstamos
        tcReaderLoans.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getReaderName()));
        tcAmountLoans.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getLoanCount())));

        // Configurar tabla de valoraciones
        tcTitle.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getBookTitle()));
        tcRating.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.2f ⭐", cellData.getValue().getAverageRating())));

        // Configurar tabla de conexiones
        tcReaderConnection.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getReaderName()));
        tcAmountConnection.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getConnectionCount())));
    }

    /**
     * Carga todas las estadísticas principales
     * Este método demuestra cómo procesar grandes volúmenes de datos de manera eficiente
     */
    public void loadAllStatistics() {
        try {
            loadLoanStatistics();
            loadRatingStatistics();
            loadConnectionStatistics();
            loadClusterStatistics();
            setupComboBoxes(); // Actualizar también los ComboBox
            System.out.println("📊 Estadísticas actualizadas correctamente");
        } catch (Exception e) {
            System.err.println("❌ Error actualizando estadísticas: " + e.getMessage());
        }
    }

    /**
     * Análisis de préstamos por lector
     *
     * Algoritmo aplicado: Agregación de datos con ordenamiento
     * Complejidad: O(n log n) donde n es el número de lectores
     */
    private void loadLoanStatistics() {
        String rutaArchivo = "src/main/resources/Archivos/Loans/Loans.txt";
        java.util.HashMap<String, Integer> conteoPrestamos = new java.util.HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.startsWith("#") || linea.trim().isEmpty()) continue;

                String[] partes = linea.split(",");
                if (partes.length >= 1) {
                    String correo = partes[0].trim();
                    conteoPrestamos.put(correo, conteoPrestamos.getOrDefault(correo, 0) + 1);
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Error leyendo préstamos: " + e.getMessage());
            return;
        }

        // Convertir a lista de LoanStatistic
        ObservableList<LoanStatistic> stats = FXCollections.observableArrayList();
        for (String correo : conteoPrestamos.keySet()) {
            stats.add(new LoanStatistic(correo, conteoPrestamos.get(correo)));
        }

        // Ordenar (opcional, mayor a menor)
        stats.sort((a, b) -> Integer.compare(b.getLoanCount(), a.getLoanCount()));

        // Mostrar en la tabla
        tableLoans.setItems(stats);
    }


    /**
     * Análisis de libros mejor valorados
     *
     * Conceptos: Manejo de promedios y ordenamiento por múltiples criterios
     */
    private void loadRatingStatistics() {
        String rutaArchivo = "src/main/resources/Archivos/Ratings/Ratings.txt";
        java.util.HashMap<String, java.util.List<Integer>> mapaValoraciones = new java.util.HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.startsWith("#") || linea.trim().isEmpty()) continue;

                String[] partes = linea.split(",");
                if (partes.length >= 3) {
                    String idLibro = partes[1].trim();
                    int estrellas = Integer.parseInt(partes[2].trim());

                    mapaValoraciones
                            .computeIfAbsent(idLibro, k -> new java.util.ArrayList<>())
                            .add(estrellas);
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Error leyendo valoraciones: " + e.getMessage());
            return;
        }

        // Obtener títulos desde la lista de libros actual (usamos el modelo para el título solamente)
        java.util.HashMap<String, String> mapaTitulos = new java.util.HashMap<>();
        for (Book libro : library.getBookssList()) {
            mapaTitulos.put(libro.getIdBook(), libro.getTitle());
        }

        // Convertir a lista de RatingStatistic con promedio
        ObservableList<RatingStatistic> stats = FXCollections.observableArrayList();

        for (String idLibro : mapaValoraciones.keySet()) {
            java.util.List<Integer> estrellas = mapaValoraciones.get(idLibro);
            double promedio = estrellas.stream().mapToInt(i -> i).average().orElse(0.0);
            String titulo = mapaTitulos.getOrDefault(idLibro, "Libro " + idLibro);
            stats.add(new RatingStatistic(titulo, promedio));
        }

        // Ordenar de mayor a menor
        stats.sort((a, b) -> Double.compare(b.getAverageRating(), a.getAverageRating()));

        tableRating.setItems(stats);
    }


    /**
     * Análisis de conexiones en el grafo de afinidad
     *
     * Aplicación directa de teoría de grafos: análisis de grado de vértices
     */
    private void loadConnectionStatistics(){
        LinkedList<Reader> readers = library.getReadersList();
        LinkedList<ConnectionStatistic> connectionStats = new LinkedList<>();

        for (Reader reader : readers) {
            // Obtener número de conexiones del grafo
            HashSet<Reader> connections = affinitySystem.getAffinityGraph().getAdjacentVertices(reader);
            int connectionCount = connections != null ? connections.size() : 0;

            connectionStats.add(new ConnectionStatistic(reader.getName(), connectionCount));
        }

        // Ordenar por número de conexiones
        ConnectionStatistic[] statsArray = convertConnectionToArray(connectionStats);
        insertionSortByConnections(statsArray);

        ObservableList<ConnectionStatistic> observableStats = FXCollections.observableArrayList();
        for (ConnectionStatistic stat : statsArray) {
            observableStats.add(stat);
        }

        tableConnection.setItems(observableStats);
    }

    /**
     * Detecta y muestra clústeres de afinidad
     *
     * Algoritmo aplicado: Búsqueda de componentes conectados usando DFS
     * Es uno de los algoritmos fundamentales en teoría de grafos
     */
    private void loadClusterStatistics() {
        LinkedList<HashSet<Reader>> clusters = affinitySystem.detectAffinityClusters();
        ObservableList<String> clusterDescriptions = FXCollections.observableArrayList();

        for (int i = 0; i < clusters.getSize(); i++) {
            HashSet<Reader> cluster = clusters.getAmountNodo(i);

            if (cluster.size() > 1) { // Solo mostrar clústeres con más de 1 miembro
                StringBuilder description = new StringBuilder();
                description.append("Clúster ").append(i + 1).append(" (").append(cluster.size()).append(" miembros): ");

                boolean first = true;
                for (Reader reader : cluster) {
                    if (!first) description.append(", ");
                    description.append(reader.getName());
                    first = false;
                }

                clusterDescriptions.add(description.toString());
            }
        }

        if (clusterDescriptions.isEmpty()) {
            clusterDescriptions.add("No se detectaron clústeres de afinidad significativos.");
        }

        tvClusters.setItems(clusterDescriptions);
    }

    /**
     * Configura los ComboBox para selección de lectores
     */
    private void setupComboBoxes() {
        LinkedList<Reader> readers = library.getReadersList();
        ObservableList<Reader> readerList = FXCollections.observableArrayList();

        for (Reader reader : readers) {
            readerList.add(reader);
        }

        cbReaderA.setItems(readerList);
        cbReaderB.setItems(readerList);

        // Configurar cómo se muestran los lectores en el ComboBox
        cbReaderA.setConverter(new javafx.util.StringConverter<Reader>() {
            @Override
            public String toString(Reader reader) {
                return reader != null ? reader.getName() : "";
            }

            @Override
            public Reader fromString(String string) {
                return null; // No necesitamos conversión inversa
            }
        });

        cbReaderB.setConverter(new javafx.util.StringConverter<Reader>() {
            @Override
            public String toString(Reader reader) {
                return reader != null ? reader.getName() : "";
            }

            @Override
            public Reader fromString(String string) {
                return null;
            }
        });
    }

    /**
     * Configura el filtro de búsqueda en tiempo real
     *
     * Patrón Observer: La tabla se actualiza automáticamente cuando cambia el filtro
     */
    private void setupFilterListener() {
        txtFilterReader.textProperty().addListener((observable, oldValue, newValue) -> {
            filterLoanStatistics(newValue);
        });
    }

    /**
     * Filtra las estadísticas de préstamos según el texto ingresado
     */
    private void filterLoanStatistics(String filterText) {
        if (filterText == null || filterText.trim().isEmpty()) {
            loadLoanStatistics(); // Recargar todas las estadísticas
            return;
        }

        ObservableList<LoanStatistic> allItems = tableLoans.getItems();
        ObservableList<LoanStatistic> filteredItems = FXCollections.observableArrayList();

        String filter = filterText.toLowerCase().trim();
        for (LoanStatistic item : allItems) {
            if (item.getReaderName().toLowerCase().contains(filter)) {
                filteredItems.add(item);
            }
        }

        tableLoans.setItems(filteredItems);
    }

    public void refreshAfterRatingsLoaded() {
        try {
            System.out.println("🔄 Refrescando estadísticas después de cargar valoraciones...");

            // Recrear sistema de afinidad porque las valoraciones pueden cambiar las conexiones
            this.affinitySystem = new AffinitySystem(library);

            // Recargar todas las estadísticas
            loadAllStatistics();
            setupComboBoxes();

            System.out.println("✅ Estadísticas actualizadas tras cargar valoraciones");

        } catch (Exception e) {
            System.err.println("❌ Error refrescando estadísticas: " + e.getMessage());
        }
    }

    // NUEVO: Método específico para actualizar después de cargar conexiones
    public void refreshAfterConnectionsLoaded() {
        try {
            System.out.println("🔄 Refrescando grafo después de cargar conexiones...");

            // Recrear sistema de afinidad
            this.affinitySystem = new AffinitySystem(library);

            // Recargar estadísticas que dependen del grafo
            loadConnectionStatistics();
            loadClusterStatistics();
            setupComboBoxes();

            System.out.println("✅ Grafo actualizado tras cargar conexiones");

        } catch (Exception e) {
            System.err.println("❌ Error refrescando grafo: " + e.getMessage());
        }
    }

    /**
     * Maneja la búsqueda de camino más corto entre dos lectores
     *
     * Aplicación práctica del algoritmo BFS (Breadth-First Search)
     */


    // ================== ALGORITMOS DE ORDENAMIENTO ==================
    // Nota pedagógica: Implementamos nuestros propios algoritmos para entender
    // cómo funcionan internamente, aunque en producción usaríamos Collections.sort()

    private void insertionSortByLoans(LoanStatistic[] array) {
        for (int i = 1; i < array.length; i++) {
            LoanStatistic key = array[i];
            int j = i - 1;

            // Ordenar de mayor a menor (más préstamos primero)
            while (j >= 0 && array[j].getLoanCount() < key.getLoanCount()) {
                array[j + 1] = array[j];
                j--;
            }
            array[j + 1] = key;
        }
    }

    private void insertionSortByRating(RatingStatistic[] array) {
        for (int i = 1; i < array.length; i++) {
            RatingStatistic key = array[i];
            int j = i - 1;

            // Ordenar de mayor a menor valoración
            while (j >= 0 && array[j].getAverageRating() < key.getAverageRating()) {
                array[j + 1] = array[j];
                j--;
            }
            array[j + 1] = key;
        }
    }

    private void insertionSortByConnections(ConnectionStatistic[] array) {
        for (int i = 1; i < array.length; i++) {
            ConnectionStatistic key = array[i];
            int j = i - 1;

            // Ordenar de mayor a menor conexiones
            while (j >= 0 && array[j].getConnectionCount() < key.getConnectionCount()) {
                array[j + 1] = array[j];
                j--;
            }
            array[j + 1] = key;
        }
    }

    // ================== MÉTODOS AUXILIARES PARA CONVERSIÓN ==================

    private LoanStatistic[] convertToArray(LinkedList<LoanStatistic> list) {
        LoanStatistic[] array = new LoanStatistic[list.getSize()];
        for (int i = 0; i < list.getSize(); i++) {
            array[i] = list.getAmountNodo(i);
        }
        return array;
    }

    private RatingStatistic[] convertRatingToArray(LinkedList<RatingStatistic> list) {
        RatingStatistic[] array = new RatingStatistic[list.getSize()];
        for (int i = 0; i < list.getSize(); i++) {
            array[i] = list.getAmountNodo(i);
        }
        return array;
    }

    private ConnectionStatistic[] convertConnectionToArray(LinkedList<ConnectionStatistic> list) {
        ConnectionStatistic[] array = new ConnectionStatistic[list.getSize()];
        for (int i = 0; i < list.getSize(); i++) {
            array[i] = list.getAmountNodo(i);
        }
        return array;
    }

    // ================== CLASES AUXILIARES PARA ESTADÍSTICAS ==================

    /**
     * Clase para encapsular estadísticas de préstamos
     * Patrón Value Object: inmutable y enfocado en datos
     */
    public static class LoanStatistic {
        private final String readerName;
        private final int loanCount;

        public LoanStatistic(String readerName, int loanCount) {
            this.readerName = readerName;
            this.loanCount = loanCount;
        }

        public String getReaderName() { return readerName; }
        public int getLoanCount() { return loanCount; }
    }

    public static class RatingStatistic {
        private final String bookTitle;
        private final double averageRating;

        public RatingStatistic(String bookTitle, double averageRating) {
            this.bookTitle = bookTitle;
            this.averageRating = averageRating;
        }

        public String getBookTitle() { return bookTitle; }
        public double getAverageRating() { return averageRating; }
    }

    public static class ConnectionStatistic {
        private final String readerName;
        private final int connectionCount;

        public ConnectionStatistic(String readerName, int connectionCount) {
            this.readerName = readerName;
            this.connectionCount = connectionCount;
        }

        public String getReaderName() { return readerName; }
        public int getConnectionCount() { return connectionCount; }
    }
}

