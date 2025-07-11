import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.*;

/**
 * Visualizes a simple graph of tourist attractions and their connections.
 * Animates nodes and edges, including highlighting start and end points.
 */

public class GraphVisualizer {
	private static final Color NODE_COLOR = Color.rgb(70, 130, 180);
	private static final Color DEFAULT_EDGE_COLOR = Color.rgb(150, 150, 150, 0.8);
	private static final Color START_EDGE_COLOR = Color.rgb(255, 100, 100, 0.8);

	/**
	 * Displays the animated graph of selected attractions and optional start/end
	 * points.
	 * 
	 * @param allAttractions             Map of all attraction names to coordinates
	 * @param recommendedAttractionNames Names of the top similar attractions
	 * @param startPoint                 Optional start point
	 * @param endPoint                   Optional end point
	 */

	public static void showGraph(Map<String, Point> allAttractions, List<String> recommendedAttractionNames,
			Point startPoint, Point endPoint) {

		Stage stage = new Stage();
		stage.setTitle("Tourist Attractions Graph");

		Canvas canvas = new Canvas(800, 600);
		GraphicsContext gc = canvas.getGraphicsContext2D();

		// prepare canvas
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

		StackPane root = new StackPane(canvas);
		stage.setScene(new Scene(root));
		stage.show();

		// Get points for recommended attractions only
		List<Point> recommendedPoints = new ArrayList<>();
		for (String name : recommendedAttractionNames) {
			Point p = allAttractions.get(name);
			if (p != null)
				recommendedPoints.add(p);
		}

		// Prepare animations
		Timeline timeline = new Timeline();
		int delay = 0;

		// Animate default connections
		for (Point p1 : recommendedPoints) {
			List<Point> nearest = recommendedPoints.stream().sorted(Comparator.comparingDouble(p2 -> distance(p1, p2)))
					.filter(p2 -> p1 != p2).limit(2).toList();

			for (Point p2 : nearest) {
				Point from = p1;
				Point to = p2;
				timeline.getKeyFrames()
						.add(new KeyFrame(Duration.millis(delay), e -> drawEdge(gc, from, to, DEFAULT_EDGE_COLOR, 3)));
				delay += 150;
			}
		}

		// Animate start connections
		if (startPoint != null) {
			for (Point attraction : recommendedPoints) {
				Point from = startPoint;
				Point to = attraction;
				timeline.getKeyFrames()
						.add(new KeyFrame(Duration.millis(delay), e -> drawEdge(gc, from, to, START_EDGE_COLOR, 2)));
				delay += 100;
			}
		}

		// Animate attraction nodes
		for (int i = 0; i < recommendedPoints.size(); i++) {
			Point p = recommendedPoints.get(i);
			String name = recommendedAttractionNames.get(i);
			int index = i;
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(delay), e -> drawAttractionNode(gc, p, name)));
			delay += 100;
		}

		// Draw start and end points last (no delay)
		if (startPoint != null) {
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(delay),
					e -> drawSpecialNode(gc, startPoint, "Start", Color.GREEN.darker())));
			delay += 100;
		}

		if (endPoint != null) {
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(delay),
					e -> drawSpecialNode(gc, endPoint, "End", Color.RED.darker())));
		}

		// Start the animation
		timeline.play();
	}

	private static void drawEdge(GraphicsContext gc, Point p1, Point p2, Color color, double width) {
		gc.setStroke(color);
		gc.setLineWidth(width);
		gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
	}

	private static void drawAttractionNode(GraphicsContext gc, Point p, String name) {
		gc.setFill(NODE_COLOR);
		gc.setStroke(Color.BLACK);
		gc.setLineWidth(2);
		gc.fillOval(p.x - 10, p.y - 10, 20, 20);
		gc.strokeOval(p.x - 10, p.y - 10, 20, 20);
		gc.strokeText(name, p.x - 15, p.y - 15);
	}

	private static void drawSpecialNode(GraphicsContext gc, Point p, String label, Color color) {
		gc.setFill(color);
		gc.setStroke(Color.BLACK);
		gc.setLineWidth(2);
		gc.fillOval(p.x - 12, p.y - 12, 24, 24);
		gc.strokeOval(p.x - 12, p.y - 12, 24, 24);
		gc.setLineWidth(1.5);
		gc.strokeText(label, p.x - 20, p.y - 25);
	}

	private static double distance(Point p1, Point p2) {
		return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
	}
}
