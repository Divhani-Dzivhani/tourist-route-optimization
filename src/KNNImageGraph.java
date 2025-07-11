import javafx.scene.image.Image;
import java.util.*;

/**
 * Constructs a K-Nearest Neighbor (KNN) image graph and compares graphs based on image similarity.
 * Nodes represent images and edges represent similarity to K nearest neighbors.
 */
public class KNNImageGraph {
    private static final int K = 5; // Number of nearest neighbors
    private static final int FEATURE_SIZE = 128; // Size of the feature vector (placeholder)

    /**
     * Represents an image node with extracted features and connections to neighbors.
     */
    public static class ImageNode {
        public String name;
        public double[] features;
        public List<Edge> edges = new ArrayList<>();

        public ImageNode(String name, double[] features) {
            this.name = name;
            this.features = features;
        }
    }

    /**
     * Represents an edge from one image node to another with a weight (distance).
     */
    public static class Edge {
        public ImageNode target;
        public double weight;

        public Edge(ImageNode target, double weight) {
            this.target = target;
            this.weight = weight;
        }
    }

    /**
     * A graph structure containing all image nodes.
     */
    public static class ImageGraph {
        public List<ImageNode> nodes = new ArrayList<>();
    }

    /**
     * Extracts placeholder feature vectors from an image (to be replaced with real CNN features).
     */
    public static double[] extractFeatures(Image image) {
        double[] features = new double[FEATURE_SIZE];
        // TODO: Replace with actual feature extraction (CNN, etc.)
        return features;
    }

    /**
     * Builds a KNN graph where each image is connected to its K nearest neighbors.
     * @param images List of images
     * @param names  Corresponding names
     * @return       Graph of image nodes with KNN edges
     */
    public static ImageGraph buildKNNGraph(List<Image> images, List<String> names) {
        ImageGraph graph = new ImageGraph();

        // Create graph nodes with extracted features
        for (int i = 0; i < images.size(); i++) {
            double[] features = extractFeatures(images.get(i));
            graph.nodes.add(new ImageNode(names.get(i), features));
        }

        // Build KNN edges
        for (ImageNode node : graph.nodes) {
            PriorityQueue<Edge> nearestNeighbors = new PriorityQueue<>(Comparator.comparingDouble(e -> e.weight));

            for (ImageNode other : graph.nodes) {
                if (node == other) continue;
                double distance = euclideanDistance(node.features, other.features);
                nearestNeighbors.add(new Edge(other, distance));
            }

            for (int i = 0; i < K && !nearestNeighbors.isEmpty(); i++) {
                node.edges.add(nearestNeighbors.poll());
            }
        }

        return graph;
    }

    /**
     * Computes overall similarity between two image graphs based on node features and edge structure.
     */
    public static double graphSimilarity(ImageGraph graph1, ImageGraph graph2) {
        double totalSimilarity = 0;
        int comparisons = 0;

        for (ImageNode node1 : graph1.nodes) {
            for (ImageNode node2 : graph2.nodes) {
                double nodeSim = 1 / (1 + euclideanDistance(node1.features, node2.features));
                double structureSim = compareNeighborhoods(node1, node2);
                totalSimilarity += nodeSim * structureSim;
                comparisons++;
            }
        }

        return totalSimilarity / comparisons;
    }

    /**
     * Compares the neighborhood (edges) of two nodes using feature similarity and edge weight similarity.
     */
    private static double compareNeighborhoods(ImageNode node1, ImageNode node2) {
        double similarity = 0;

        for (Edge edge1 : node1.edges) {
            for (Edge edge2 : node2.edges) {
                double featureSim = 1 / (1 + euclideanDistance(edge1.target.features, edge2.target.features));
                similarity += featureSim * (1 - Math.abs(edge1.weight - edge2.weight));
            }
        }

        return similarity / (node1.edges.size() * node2.edges.size());
    }

    /**
     * Computes the Euclidean distance between two vectors.
     */
    private static double euclideanDistance(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(sum);
    }
}
