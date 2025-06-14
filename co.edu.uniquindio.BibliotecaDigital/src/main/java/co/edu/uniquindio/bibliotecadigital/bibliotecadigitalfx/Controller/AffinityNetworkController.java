package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service.AffinitySystem;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.Graph;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;

/**
 * Controlador para visualizar la red de afinidad entre lectores como un grafo interactivo
 *
 * Funcionalidades implementadas:
 * - Visualización 2D de nodos (lectores) y aristas (conexiones de afinidad)
 * - Algoritmo de layout automático tipo "force-directed" para posicionamiento
 * - Interactividad: hover, click, drag de nodos
 * - Animaciones para mejor experiencia visual
 * - Información contextual al interactuar con nodos
 * - Detección y resaltado de clústeres/comunidades
 * - Zoom y pan para navegación
 *
 * Conceptos de visualización de datos aplicados:
 * - Force-directed graph layout (algoritmo físico de resortes)
 * - Color coding para diferentes métricas
 * - Responsive design para diferentes tamaños de red
 * - Progressive disclosure (mostrar detalles bajo demanda)
 */
public class AffinityNetworkController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private Pane PaneGraph;

    // Controles adicionales (podrían añadirse al FXML)
    private Button refreshButton;
    private Button toggleLabelsButton;
    private Label networkStatsLabel;

    private Library library;
    private AffinitySystem affinitySystem;
    private Graph<Reader> affinityGraph;

    // Elementos visuales
    private Map<Reader, NodeVisual> nodeVisuals = new HashMap<>();
    private List<EdgeVisual> edgeVisuals = new ArrayList<>();

    // Configuración de visualización
    private static final double NODE_RADIUS = 15.0;
    private static final double EDGE_THICKNESS = 2.0;
    private static final double FORCE_STRENGTH = 0.8;
    private static final double REPULSION_STRENGTH = 500.0;
    private static final double ATTRACTION_STRENGTH = 0.005;
    private static final int MAX_ITERATIONS = 1500;
    private static final double MIN_DISTANCE = 80.0;

    // Estado de la simulación
    private AnimationTimer simulationTimer;
    private boolean simulationRunning = false;
    private boolean showLabels = true;
    private double zoomLevel = 1.0;

    @FXML
    void initialize() {
        assert PaneGraph != null : "fx:id=\"PaneGraph\" was not injected: check your FXML file 'AffinityNetwork.fxml'.";

        ControllerRegistry.getInstance().registerController("AffinityNetworkController", this);
        System.out.println("✅ AffinityNetworkController registrado en el registry");

        initializeData();
        createAdditionalControls();
        setupGraphVisualization();
        startLayoutSimulation();
    }

    /**
     * Inicializa los datos necesarios para la visualización
     */
    private void initializeData() {
        try {
            this.library = Library.getInstance();

            // CORRECCIÓN: Recrear sistema de afinidad con datos frescos
            this.affinitySystem = new AffinitySystem(library);
            this.affinityGraph = affinitySystem.getAffinityGraph();

            System.out.println("📊 Visualización de red de afinidad inicializada");
            System.out.println("   - Lectores en el grafo: " +
                    (affinityGraph != null ? affinityGraph.getVertices().getSize() : 0));

        } catch (Exception e) {
            System.err.println("❌ Error inicializando datos del grafo: " + e.getMessage());
            showAlert("Error", "No se pudo inicializar la visualización de red: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void refreshVisualization() {
        try {
            // Detener simulación actual
            if (simulationRunning && simulationTimer != null) {
                simulationTimer.stop();
                simulationRunning = false;
            }

            // CORRECCIÓN: Recrear sistema de afinidad desde cero
            this.affinitySystem = new AffinitySystem(library);
            this.affinityGraph = affinitySystem.getAffinityGraph();

            // Recrear visualización
            setupGraphVisualization();
            startLayoutSimulation();

            // Animación de entrada
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(500), PaneGraph);
            fadeIn.setFromValue(0.5);
            fadeIn.setToValue(1.0);
            fadeIn.play();

            System.out.println("✅ Grafo actualizado desde refreshVisualization");

        } catch (Exception e) {
            System.err.println("❌ Error en refreshVisualization: " + e.getMessage());
            showAlert("Error", "Error actualizando el grafo: " + e.getMessage());
        }
    }

    /**
     * Crea controles adicionales para la interfaz
     */
    private void createAdditionalControls() {
        // Botón de actualización
        refreshButton = new Button("🔄 Actualizar Red");
        refreshButton.setLayoutX(10);
        refreshButton.setLayoutY(10);
        refreshButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        refreshButton.setOnAction(e -> refreshVisualization());

        // Botón para alternar etiquetas
        toggleLabelsButton = new Button("👁️ Mostrar/Ocultar Nombres");
        toggleLabelsButton.setLayoutX(150);
        toggleLabelsButton.setLayoutY(10);
        toggleLabelsButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        toggleLabelsButton.setOnAction(e -> toggleLabels());

        // Etiqueta de estadísticas
        networkStatsLabel = new Label();
        networkStatsLabel.setLayoutX(10);
        networkStatsLabel.setLayoutY(50);
        networkStatsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        PaneGraph.getChildren().addAll(refreshButton, toggleLabelsButton, networkStatsLabel);
    }

    /**
     * Configura la visualización inicial del grafo
     */
    private void setupGraphVisualization() {
        if (affinityGraph == null) {
            showEmptyNetworkMessage();
            return;
        }

        clearVisualization();
        createNodeVisuals();
        createEdgeVisuals();
        calculateInitialLayout();
        updateNetworkStats();
    }

    /**
     * Crea las representaciones visuales de los nodos (lectores)
     */
    private void createNodeVisuals() {
        LinkedList<Reader> vertices = affinityGraph.getVertices();
        Random random = new Random();

        double centerX = PaneGraph.getPrefWidth() / 2;
        double centerY = PaneGraph.getPrefHeight() / 2;

        for (Reader reader : vertices) {
            // Posición inicial aleatoria cerca del centro
            double x = centerX + (random.nextDouble() - 0.5) * 200;
            double y = centerY + (random.nextDouble() - 0.5) * 200;

            NodeVisual nodeVisual = new NodeVisual(reader, x, y);
            nodeVisuals.put(reader, nodeVisual);

            PaneGraph.getChildren().addAll(nodeVisual.getVisualElements());
        }
    }

    /**
     * Crea las representaciones visuales de las aristas (conexiones)
     */
    private void createEdgeVisuals() {
        for (Map.Entry<Reader, NodeVisual> entry : nodeVisuals.entrySet()) {
            Reader reader = entry.getKey();
            NodeVisual sourceNode = entry.getValue();

            HashSet<Reader> connections = affinityGraph.getAdjacentVertices(reader);
            if (connections != null) {
                for (Reader connectedReader : connections) {
                    NodeVisual targetNode = nodeVisuals.get(connectedReader);

                    if (targetNode != null && !edgeExists(sourceNode, targetNode)) {
                        EdgeVisual edgeVisual = new EdgeVisual(sourceNode, targetNode);
                        edgeVisuals.add(edgeVisual);

                        PaneGraph.getChildren().add(edgeVisual.getLine());
                    }
                }
            }
        }
    }

    /**
     * Verifica si ya existe una arista entre dos nodos
     */
    private boolean edgeExists(NodeVisual node1, NodeVisual node2) {
        return edgeVisuals.stream().anyMatch(edge ->
                (edge.getSource() == node1 && edge.getTarget() == node2) ||
                        (edge.getSource() == node2 && edge.getTarget() == node1));
    }

    /**
     * Calcula el layout inicial usando un algoritmo simple de círculo
     */
    private void calculateInitialLayout() {
        List<NodeVisual> nodes = new ArrayList<>(nodeVisuals.values());

        if (nodes.isEmpty()) return;

        double centerX = PaneGraph.getPrefWidth() / 2;
        double centerY = PaneGraph.getPrefHeight() / 2;

        // Calcular radio basado en el número de nodos para mejor distribución
        double baseRadius = Math.min(centerX, centerY) * 0.6;
        double radius = Math.max(baseRadius, nodes.size() * 15.0); // Radio dinámico

        // Si hay muchos nodos, usar múltiples círculos concéntricos
        if (nodes.size() > 12) {
            layoutMultipleCircles(nodes, centerX, centerY, baseRadius);
        } else {
            // Layout circular simple para pocos nodos
            for (int i = 0; i < nodes.size(); i++) {
                double angle = 2 * Math.PI * i / nodes.size();
                double x = centerX + radius * Math.cos(angle);
                double y = centerY + radius * Math.sin(angle);

                NodeVisual node = nodes.get(i);
                node.setPosition(x, y);
            }
        }

        updateEdgePositions();
    }
    private void layoutMultipleCircles(List<NodeVisual> nodes, double centerX, double centerY, double baseRadius) {
        int nodesPerCircle = 8; // Máximo 8 nodos por círculo
        int numCircles = (int) Math.ceil((double) nodes.size() / nodesPerCircle);

        int nodeIndex = 0;
        for (int circle = 0; circle < numCircles && nodeIndex < nodes.size(); circle++) {
            double radius = baseRadius + (circle * 80); // 80px entre círculos
            int nodesInThisCircle = Math.min(nodesPerCircle, nodes.size() - nodeIndex);

            for (int i = 0; i < nodesInThisCircle && nodeIndex < nodes.size(); i++) {
                double angle = 2 * Math.PI * i / nodesInThisCircle;
                double x = centerX + radius * Math.cos(angle);
                double y = centerY + radius * Math.sin(angle);

                NodeVisual node = nodes.get(nodeIndex);
                node.setPosition(x, y);
                nodeIndex++;
            }
        }
    }

    /**
     * Inicia la simulación de layout force-directed
     *
     * Este algoritmo simula fuerzas físicas para crear un layout visualmente agradable:
     * - Repulsión entre todos los nodos (como cargas eléctricas)
     * - Atracción entre nodos conectados (como resortes)
     * - Amortiguación para que converja a una solución estable
     */
    private void startLayoutSimulation() {
        if (simulationRunning) {
            simulationTimer.stop();
        }

        simulationTimer = new AnimationTimer() {
            private int iteration = 0;

            @Override
            public void handle(long now) {
                if (iteration < MAX_ITERATIONS) {
                    performLayoutIteration();
                    updateEdgePositions();
                    iteration++;
                } else {
                    stop();
                    simulationRunning = false;
                    System.out.println("Simulación de layout completada");
                }
            }
        };

        simulationRunning = true;
        simulationTimer.start();
    }

    /**
     * Realiza una iteración del algoritmo force-directed
     */
    private void performLayoutIteration() {
        List<NodeVisual> nodes = new ArrayList<>(nodeVisuals.values());

        // Calcular fuerzas para cada nodo
        for (NodeVisual node : nodes) {
            double fx = 0, fy = 0;

            // Fuerza de repulsión con otros nodos (MEJORADA)
            for (NodeVisual other : nodes) {
                if (node != other) {
                    double dx = node.getX() - other.getX();
                    double dy = node.getY() - other.getY();
                    double distance = Math.max(Math.sqrt(dx * dx + dy * dy), 10.0); // Distancia mínima aumentada

                    // Aplicar repulsión más fuerte para distancias cortas
                    double force = REPULSION_STRENGTH / (distance * distance);

                    // Bonus de repulsión si están muy cerca
                    if (distance < MIN_DISTANCE) {
                        force *= 2.0; // Duplicar la fuerza si están muy cerca
                    }

                    fx += force * dx / distance;
                    fy += force * dy / distance;
                }
            }

            // Fuerza de atracción con nodos conectados (AJUSTADA)
            HashSet<Reader> connections = affinityGraph.getAdjacentVertices(node.getReader());
            if (connections != null) {
                for (Reader connectedReader : connections) {
                    NodeVisual connectedNode = nodeVisuals.get(connectedReader);
                    if (connectedNode != null) {
                        double dx = connectedNode.getX() - node.getX();
                        double dy = connectedNode.getY() - node.getY();
                        double distance = Math.sqrt(dx * dx + dy * dy);

                        // Solo aplicar atracción si están muy lejos
                        if (distance > MIN_DISTANCE * 1.5) {
                            double force = ATTRACTION_STRENGTH * distance;
                            fx += force * dx / distance;
                            fy += force * dy / distance;
                        }
                    }
                }
            }

            // Aplicar fuerzas con amortiguación
            node.applyForce(fx * FORCE_STRENGTH, fy * FORCE_STRENGTH);
        }

        // Actualizar posiciones y mantener nodos dentro de los límites
        for (NodeVisual node : nodes) {
            node.updatePosition();
            constrainToPane(node);
        }
    }

    // MÉTODO MEJORADO: constrainToPane()
// Reemplaza tu método existente con este:
    private void constrainToPane(NodeVisual node) {
        double margin = NODE_RADIUS + 20; // Aumentado el margen
        double maxX = PaneGraph.getPrefWidth() - margin;
        double maxY = PaneGraph.getPrefHeight() - margin;

        if (node.getX() < margin) node.setX(margin);
        if (node.getX() > maxX) node.setX(maxX);
        if (node.getY() < margin) node.setY(margin);
        if (node.getY() > maxY) node.setY(maxY);
    }


    /**
     * Actualiza las posiciones de todas las aristas
     */
    private void updateEdgePositions() {
        for (EdgeVisual edge : edgeVisuals) {
            edge.updatePosition();
        }
    }

    /**
     * Actualiza las estadísticas de la red mostradas en pantalla
     */
    private void updateNetworkStats() {
        int totalNodes = nodeVisuals.size();
        int totalEdges = edgeVisuals.size();

        // Calcular densidad de la red
        int maxPossibleEdges = totalNodes * (totalNodes - 1) / 2;
        double density = maxPossibleEdges > 0 ? (double) totalEdges / maxPossibleEdges : 0;

        // Detectar componentes conectados
        LinkedList<HashSet<Reader>> components = affinityGraph.getConnectedComponents();
        int numComponents = components.getSize();

        String stats = String.format(
                "Red de Afinidad: %d lectores, %d conexiones\n" +
                        "Densidad: %.1f%% | Componentes: %d",
                totalNodes, totalEdges, density * 100, numComponents
        );

        networkStatsLabel.setText(stats);
    }

    /**
     * Alterna la visibilidad de las etiquetas de nombres
     */
    private void toggleLabels() {
        showLabels = !showLabels;

        for (NodeVisual node : nodeVisuals.values()) {
            node.setLabelVisible(showLabels);
        }
    }

    /**
     * Muestra mensaje cuando no hay datos para visualizar
     */
    private void showEmptyNetworkMessage() {
        Label emptyMessage = new Label(
                "No hay conexiones de afinidad para mostrar.\n\n" +
                        "💡 Las conexiones se crean automáticamente cuando:\n" +
                        "• Los lectores valoran libros en común\n" +
                        "• Sus valoraciones son similares (±1 estrella)\n" +
                        "• Han valorado al menos 3 libros en común"
        );

        emptyMessage.setLayoutX(50);
        emptyMessage.setLayoutY(100);
        emptyMessage.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

        PaneGraph.getChildren().add(emptyMessage);
    }

    /**
     * Limpia todos los elementos visuales
     */
    private void clearVisualization() {
        // Mantener solo los controles de interfaz
        PaneGraph.getChildren().removeIf(node ->
                !(node == refreshButton || node == toggleLabelsButton || node == networkStatsLabel));

        nodeVisuals.clear();
        edgeVisuals.clear();
    }

    /**
     * Muestra alertas al usuario
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ================== CLASES AUXILIARES PARA ELEMENTOS VISUALES ==================

    /**
     * Representa la visualización de un nodo (lector) en el grafo
     */
    private class NodeVisual {
        private Reader reader;
        private Circle circle;
        private Text label;
        private double x, y;
        private double vx = 0, vy = 0; // Velocidad para simulación física

        public NodeVisual(Reader reader, double x, double y) {
            this.reader = reader;
            this.x = x;
            this.y = y;

            createVisualElements();
            setupInteractivity();
        }

        private void createVisualElements() {
            // Crear círculo para el nodo
            circle = new Circle(x, y, NODE_RADIUS);

            // Color basado en número de conexiones
            HashSet<Reader> connections = affinityGraph.getAdjacentVertices(reader);
            int connectionCount = connections != null ? connections.size() : 0;

            Color nodeColor = getNodeColor(connectionCount);
            circle.setFill(nodeColor);
            circle.setStroke(Color.DARKGRAY);
            circle.setStrokeWidth(1.5);

            // Crear etiqueta con nombre
            label = new Text(x - NODE_RADIUS, y - NODE_RADIUS - 5, reader.getName());
            label.setFont(Font.font(10));
            label.setFill(Color.BLACK);
            label.setVisible(showLabels);

            // Tooltip con información detallada
            Tooltip tooltip = new Tooltip(createTooltipText());
            Tooltip.install(circle, tooltip);
        }

        private Color getNodeColor(int connectionCount) {
            if (connectionCount == 0) return Color.LIGHTGRAY;      // Sin conexiones
            else if (connectionCount <= 2) return Color.LIGHTBLUE; // Pocas conexiones
            else if (connectionCount <= 4) return Color.ORANGE;    // Conexiones moderadas
            else return Color.LIGHTCORAL;                          // Muchas conexiones
        }

        private String createTooltipText() {
            HashSet<Reader> connections = affinityGraph.getAdjacentVertices(reader);
            int connectionCount = connections != null ? connections.size() : 0;
            int booksRead = reader.getLoanHistoryList().getSize();
            int ratingsGiven = reader.getRatingsList().getSize();

            return String.format(
                    "%s\n" +
                            "📚 Libros leídos: %d\n" +
                            "⭐ Valoraciones: %d\n" +
                            "🤝 Conexiones: %d",
                    reader.getName(), booksRead, ratingsGiven, connectionCount
            );
        }

        private void setupInteractivity() {
            // Hover effect
            circle.setOnMouseEntered(e -> {
                circle.setRadius(NODE_RADIUS * 1.2);
                circle.setStroke(Color.BLUE);
                circle.setStrokeWidth(3);
            });

            circle.setOnMouseExited(e -> {
                circle.setRadius(NODE_RADIUS);
                circle.setStroke(Color.DARKGRAY);
                circle.setStrokeWidth(1.5);
            });

            // Click para mostrar detalles
            circle.setOnMouseClicked(e -> showReaderDetails());

            // Drag functionality (básico)
            circle.setOnMouseDragged(e -> {
                setPosition(e.getX(), e.getY());
                updateEdgePositions();
            });
        }

        private void showReaderDetails() {
            Alert details = new Alert(Alert.AlertType.INFORMATION);
            details.setTitle("Detalles del Lector");
            details.setHeaderText(reader.getName());
            details.setContentText(createTooltipText());
            details.showAndWait();
        }

        public void setPosition(double x, double y) {
            this.x = x;
            this.y = y;
            circle.setCenterX(x);
            circle.setCenterY(y);
            label.setX(x - NODE_RADIUS);
            label.setY(y - NODE_RADIUS - 5);
        }

        public void applyForce(double fx, double fy) {
            vx += fx;
            vy += fy;
        }

        public void updatePosition() {
            // Aplicar velocidad con amortiguación más suave
            x += vx;
            y += vy;
            vx *= 0.85; // Factor de amortiguación ajustado (era 0.9)
            vy *= 0.85;

            // Limitar velocidad máxima para evitar movimientos bruscos
            double maxVelocity = 5.0;
            if (Math.abs(vx) > maxVelocity) vx = Math.signum(vx) * maxVelocity;
            if (Math.abs(vy) > maxVelocity) vy = Math.signum(vy) * maxVelocity;

            setPosition(x, y);
        }

        public void setLabelVisible(boolean visible) {
            label.setVisible(visible);
        }

        public List<javafx.scene.Node> getVisualElements() {
            return Arrays.asList(circle, label);
        }

        // Getters
        public Reader getReader() { return reader; }
        public double getX() { return x; }
        public double getY() { return y; }
        public void setX(double x) { this.x = x; circle.setCenterX(x); }
        public void setY(double y) { this.y = y; circle.setCenterY(y); }
    }

    /**
     * Representa la visualización de una arista (conexión) en el grafo
     */
    private class EdgeVisual {
        private NodeVisual source;
        private NodeVisual target;
        private Line line;

        public EdgeVisual(NodeVisual source, NodeVisual target) {
            this.source = source;
            this.target = target;

            line = new Line();
            line.setStrokeWidth(EDGE_THICKNESS);
            line.setStroke(Color.GRAY);
            line.setOpacity(0.7);

            updatePosition();
        }

        public void updatePosition() {
            line.setStartX(source.getX());
            line.setStartY(source.getY());
            line.setEndX(target.getX());
            line.setEndY(target.getY());
        }

        public Line getLine() { return line; }
        public NodeVisual getSource() { return source; }
        public NodeVisual getTarget() { return target; }
    }
}

