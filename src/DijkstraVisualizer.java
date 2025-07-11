import javafx.animation.*;
import javafx.scene.canvas.*;
import javafx.scene.paint.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import java.util.*;

/**
 * Visualizes Dijkstra's algorithm step-by-step using JavaFX.
 * Highlights start/end points, visited nodes, frontier nodes, obstacles, and the final path.
 */
public class DijkstraVisualizer {
    // === Visualization Colors ===
    private static final Color START_COLOR = Color.GREEN;
    private static final Color END_COLOR = Color.PURPLE;
    private static final Color OBSTACLE_COLOR = Color.RED.deriveColor(0, 1, 1, 0.3);
    private static final Color VISITED_COLOR = Color.ORANGE;
    private static final Color FRONTIER_COLOR = Color.YELLOW;
    private static final Color CURRENT_COLOR = Color.CYAN;
    private static final Color PATH_COLOR = Color.RED;
    private static final Color TEXT_COLOR = Color.BLACK;

    private static final int STEP_DELAY_MS = 300;

    /**
     * Launches a new JavaFX window showing the Dijkstra algorithm step-by-step.
     * @param start      Starting point
     * @param end        Destination point
     * @param obstacles  Points to be avoided
     * @param allNodes   All possible nodes in the graph
     */
    public static void showDijkstraSteps(Point start, Point end, 
                                         List<Point> obstacles, List<Point> allNodes) {
        Stage stage = new Stage();
        stage.setTitle("Dijkstra's Algorithm Visualization");

        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        Map<Point, Point> pathMap = new HashMap<>();
        List<DijkstraStep> steps = runDijkstraRealTime(start, end, allNodes, pathMap);

        Timeline timeline = new Timeline();
        for (int i = 0; i < steps.size(); i++) {
            final int stepNum = i;
            timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(stepNum * STEP_DELAY_MS), e -> {
                    drawStep(gc, steps.get(stepNum), start, end, obstacles, pathMap);
                })
            );
        }

        // Draw the final shortest path at the end
        timeline.getKeyFrames().add(
            new KeyFrame(Duration.millis(steps.size() * STEP_DELAY_MS), e -> {
                drawFinalPath(gc, pathMap);
            })
        );

        timeline.play();
    }

    /**
     * Executes Dijkstra's algorithm and records each step for visualization.
     */
    private static List<DijkstraStep> runDijkstraRealTime(Point start, Point end, List<Point> nodes,
                                                          Map<Point, Point> pathMapOut) {
        List<DijkstraStep> steps = new ArrayList<>();
        Map<Point, Double> distances = new HashMap<>();
        Map<Point, Point> prev = new HashMap<>();
        PriorityQueue<Point> queue = new PriorityQueue<>(Comparator.comparingDouble(distances::get));

        for (Point p : nodes) distances.put(p, Double.MAX_VALUE);
        distances.put(start, 0.0);
        queue.add(start);

        Set<Point> visited = new HashSet<>();

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            if (!visited.add(current)) continue;

            Set<Point> frontier = new HashSet<>(queue);
            steps.add(new DijkstraStep(
                "Visited: (" + current.x + "," + current.y + ")", current, frontier, new HashSet<>(visited)
            ));

            List<Point> neighbors = nodes.stream()
                .filter(p -> !p.equals(current))
                .sorted(Comparator.comparingDouble(p -> distance(current, p)))
                .limit(2)
                .toList();

            for (Point neighbor : neighbors) {
                double alt = distances.get(current) + distance(current, neighbor);
                if (alt < distances.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    distances.put(neighbor, alt);
                    prev.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        // Trace back the shortest path
        pathMapOut.clear();
        Point step = end;
        while (prev.containsKey(step)) {
            pathMapOut.put(step, prev.get(step));
            step = prev.get(step);
        }

        steps.add(new DijkstraStep("Path found", end, Collections.emptySet(), visited));
        return steps;
    }

    /**
     * Draws the current visualization step on the canvas.
     */
    private static void drawStep(GraphicsContext gc, DijkstraStep step,
                                 Point start, Point end,
                                 List<Point> obstacles, Map<Point, Point> pathMap) {
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

        gc.setFill(OBSTACLE_COLOR);
        for (Point obs : obstacles) {
            gc.fillOval(obs.x - 10, obs.y - 10, 20, 20);
        }

        gc.setFill(VISITED_COLOR);
        for (Point visited : step.visited) {
            gc.fillOval(visited.x - 6, visited.y - 6, 12, 12);
        }

        gc.setFill(FRONTIER_COLOR);
        for (Point frontier : step.frontier) {
            gc.fillOval(frontier.x - 8, frontier.y - 8, 16, 16);
        }

        gc.setFill(CURRENT_COLOR);
        gc.fillOval(step.current.x - 10, step.current.y - 10, 20, 20);

        gc.setFill(START_COLOR);
        gc.fillOval(start.x - 10, start.y - 10, 20, 20);

        gc.setFill(END_COLOR);
        gc.fillOval(end.x - 10, end.y - 10, 20, 20);

        gc.setFill(TEXT_COLOR);
        gc.fillText(step.description, 20, 20);
        gc.fillText("Current: (" + step.current.x + "," + step.current.y + ")", 20, 40);
    }

    /**
     * Draws the final path once the shortest route is determined.
     */
    private static void drawFinalPath(GraphicsContext gc, Map<Point, Point> pathMap) {
        gc.setStroke(PATH_COLOR);
        gc.setLineWidth(3);
        for (Map.Entry<Point, Point> entry : pathMap.entrySet()) {
            Point from = entry.getValue();
            Point to = entry.getKey();
            gc.strokeLine(from.x, from.y, to.x, to.y);
        }
    }

    /**
     * Represents a single step in Dijkstra's algorithm.
     */
    private static class DijkstraStep {
        String description;
        Point current;
        Set<Point> frontier;
        Set<Point> visited;

        public DijkstraStep(String description, Point current,
                            Set<Point> frontier, Set<Point> visited) {
            this.description = description;
            this.current = current;
            this.frontier = frontier;
            this.visited = visited;
        }
    }

    /**
     * Calculates Euclidean distance between two points.
     */
    private static double distance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    /**
     * Simple 2D point class.
     */
    public static class Point {
        public double x, y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
