/**
 * Helper class for pathfinding algorithm, representing a node in the search space.
 */
public class Node implements Comparable<Node> {
    public Point point;
    public double fScore;

    /**
     * Creates a new Node.
     * @param point The point in 2D space
     * @param fScore The calculated fScore for pathfinding
     */
    public Node(Point point, double fScore) {
        this.point = point;
        this.fScore = fScore;
    }

    /**
     * Compares this node with another based on fScore.
     * @param other The other node to compare with
     * @return Comparison result for sorting
     */
    @Override
    public int compareTo(Node other) {
        return Double.compare(this.fScore, other.fScore);
    }
}