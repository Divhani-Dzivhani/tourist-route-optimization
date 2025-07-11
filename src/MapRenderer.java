import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Map;

/**
 * Handles rendering of the map and all its elements.
 */
public class MapRenderer {
    /**
     * Draws the map with all elements (attractions, unsafe zones, paths, etc.)
     * @param canvas The canvas to draw on
     * @param mapPath Path to the map image file
     * @param unsafeZones List of unsafe zones
     * @param topMatches List of top matching attraction names
     * @param attractionCoordinates Map of attraction names to their coordinates
     * @param startPoint Selected start point (can be null)
     * @param destinationPoint Selected destination point (can be null)
     * @param calculatedPath Calculated path (can be empty)
     */
    public static void drawMap(Canvas canvas, String mapPath, List<Point> unsafeZones, 
                             List<String> topMatches, Map<String, Point> attractionCoordinates,
                             Point startPoint, Point destinationPoint, List<Point> calculatedPath) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw base map
        Image mapImage = new Image("file:" + mapPath);
        gc.drawImage(mapImage, 0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw unsafe zones
        gc.setFill(Color.RED.deriveColor(0, 1, 1, 0.3));
        for (Point p : unsafeZones) {
            gc.fillOval(p.x - 20, p.y - 20, 40, 40);
        }

        // Draw matched attractions as green points with labels
        gc.setFill(Color.GREEN);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        for (String match : topMatches) {
            Point p = attractionCoordinates.get(match);
            if (p != null) {
                gc.fillOval(p.x - 5, p.y - 5, 10, 10);
                gc.strokeText(match, p.x - 20, p.y - 10);
            }
        }

        // Draw selected points if they exist
        if (startPoint != null) {
            gc.setFill(Color.BLUE);
            gc.fillOval(startPoint.x - 5, startPoint.y - 5, 10, 10);
        }

        if (destinationPoint != null) {
            gc.setFill(Color.GREEN);
            gc.fillOval(destinationPoint.x - 5, destinationPoint.y - 5, 10, 10);
        }

        // Draw calculated path if it exists
        if (!calculatedPath.isEmpty()) {
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(2);
            for (int i = 0; i < calculatedPath.size() - 1; i++) {
                Point p1 = calculatedPath.get(i);
                Point p2 = calculatedPath.get(i + 1);
                gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }
}