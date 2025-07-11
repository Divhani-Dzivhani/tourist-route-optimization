import java.util.*;

public class PathFinder {
    
    /**
     * Represents a node in the pathfinding algorithm with comparable fScore.
     */
    public static class Node implements Comparable<Node> {
        public Point point;
        public double fScore;

        public Node(Point point, double fScore) {
            this.point = point;
            this.fScore = fScore;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.fScore, other.fScore);
        }
    }

    /**
     * Records the state of Dijkstra's algorithm at each step for visualization.
     */
    public static class DijkstraState {
        public final Point current;
        public final Set<Point> frontier;
        public final Map<Point, Double> distances;
        public final String description;
        
        public DijkstraState(Point current, Set<Point> frontier, 
                           Map<Point, Double> distances, String description) {
            this.current = current;
            this.frontier = frontier;
            this.distances = distances;
            this.description = description;
        }
    }

    /**
     * Calculates the path using Dijkstra's algorithm and records each step.
     */
    public static List<DijkstraState> recordDijkstraSteps(Point start, Point destination,
                                                        List<Point> unsafeZones,
                                                        int width, int height) {
        List<DijkstraState> states = new ArrayList<>();
        int[][] grid = initializeGrid(width, height, unsafeZones);

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Map<Point, Double> gScore = new HashMap<>();
        Map<Point, Point> cameFrom = new HashMap<>();

        // Initialization
        gScore.put(start, 0.0);
        openSet.add(new Node(start, distance(start, destination)));
        states.add(new DijkstraState(start, getFrontierPoints(openSet), 
                  new HashMap<>(gScore), "Algorithm initialized"));

        while (!openSet.isEmpty()) {
            Point current = openSet.poll().point;
            
            String desc = String.format("Processing point (%d, %d) - Frontier: %d", 
                                      current.x, current.y, openSet.size());
            states.add(new DijkstraState(current, getFrontierPoints(openSet),
                      new HashMap<>(gScore), desc));

            // Check if destination reached
            if (distance(current, destination) < 10) {
                states.add(new DijkstraState(current, Collections.emptySet(),
                                          new HashMap<>(gScore), "Destination reached"));
                break;
            }

            // Explore all 8 possible directions
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue; // Skip current point

                    processNeighbor(current, dx, dy, grid, width, height,
                                  destination, gScore, cameFrom, openSet);
                }
            }
        }
        
        return states;
    }

    /**
     * Initializes the grid with obstacles based on unsafe zones.
     */
    private static int[][] initializeGrid(int width, int height, List<Point> unsafeZones) {
        int[][] grid = new int[width][height];
        for (Point p : unsafeZones) {
            for (int x = Math.max(0, p.x - 25); x < Math.min(width, p.x + 25); x++) {
                for (int y = Math.max(0, p.y - 25); y < Math.min(height, p.y + 25); y++) {
                    if (distance(x, y, p.x, p.y) <= 25) {
                        grid[x][y] = -1;
                    }
                }
            }
        }
        return grid;
    }

    /**
     * Processes a single neighbor point during pathfinding.
     */
    private static void processNeighbor(Point current, int dx, int dy,
                                      int[][] grid, int width, int height,
                                      Point destination,
                                      Map<Point, Double> gScore,
                                      Map<Point, Point> cameFrom,
                                      PriorityQueue<Node> openSet) {
        int nx = current.x + dx * 5; // 5 pixel step size
        int ny = current.y + dy * 5;

        // Check boundaries and obstacles
        if (nx < 0 || nx >= width || ny < 0 || ny >= height || grid[nx][ny] == -1) {
            return;
        }

        Point neighbor = new Point(nx, ny);
        double moveCost = (dx * dy == 0) ? 1 : Math.sqrt(2); // Straight vs diagonal
        double tentativeGScore = gScore.getOrDefault(current, Double.MAX_VALUE) + moveCost;

        // If we found a better path to this neighbor
        if (tentativeGScore < gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
            cameFrom.put(neighbor, current);
            gScore.put(neighbor, tentativeGScore);
            double fScore = tentativeGScore + distance(neighbor, destination);
            openSet.add(new Node(neighbor, fScore));
        }
    }

    /**
     * Extracts points from the priority queue for frontier visualization.
     */
    private static Set<Point> getFrontierPoints(PriorityQueue<Node> openSet) {
        Set<Point> frontier = new HashSet<>();
        for (Node node : openSet) {
            frontier.add(node.point);
        }
        return frontier;
    }

    /**
     * Calculates Euclidean distance between two points.
     */
    private static double distance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    /**
     * Calculates Euclidean distance between coordinates.
     */
    private static double distance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    /**
     * Calculates the shortest path using Dijkstra's algorithm.
     */
    public static List<Point> calculatePath(Point start, Point destination,
    		List<Point> unsafeZones,
    		int width, int height) {
    	// Initialize data structures
    	int[][] grid = initializeGrid(width, height, unsafeZones);
    	PriorityQueue<Node> openSet = new PriorityQueue<>();
    	Map<Point, Double> gScore = new HashMap<>();
    	Map<Point, Point> cameFrom = new HashMap<>();

    	// Initialization
    	gScore.put(start, 0.0);
    	openSet.add(new Node(start, distance(start, destination)));

    	while (!openSet.isEmpty()) {
    		Point current = openSet.poll().point;

    		// Check if we've reached the destination
    		if (distance(current, destination) < 10) {
    			// Reconstruct path
    			return reconstructPath(cameFrom, current, start);
    		}

    		// Explore neighbors
    		for (int dx = -1; dx <= 1; dx++) {
    			for (int dy = -1; dy <= 1; dy++) {
    				if (dx == 0 && dy == 0) continue;

    				int nx = current.x + dx * 5;
    				int ny = current.y + dy * 5;

    				// Check boundaries and obstacles
    				if (nx < 0 || nx >= width || ny < 0 || ny >= height || grid[nx][ny] == -1) {
    					continue;
    				}

    				Point neighbor = new Point(nx, ny);
    				double moveCost = (dx * dy == 0) ? 1 : Math.sqrt(2);
    				double tentativeGScore = gScore.getOrDefault(current, Double.MAX_VALUE) + moveCost;

    				if (tentativeGScore < gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
    					cameFrom.put(neighbor, current);
    					gScore.put(neighbor, tentativeGScore);
    					double fScore = tentativeGScore + distance(neighbor, destination);
    					openSet.add(new Node(neighbor, fScore));
    				}
    			}
    		}
    	}

    	// If no path found
    	return Collections.emptyList();
    }
    
    /**
     * Reconstructs the path from cameFrom map
     */
    private static List<Point> reconstructPath(Map<Point, Point> cameFrom, 
                                             Point current, Point start) {
        List<Point> path = new ArrayList<>();
        path.add(current);
        
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(0, current); // Add to beginning to maintain order
        }
        
        // Verify the path starts at the actual start point
        if (!path.isEmpty() && !path.get(0).equals(start)) {
            path.add(0, start);
        }
        
        return path;
    }
}