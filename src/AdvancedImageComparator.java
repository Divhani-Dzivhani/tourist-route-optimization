import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import java.util.Arrays;

/**
 * AdvancedImageComparator is a utility class used to extract and compare
 * visual features from images based on color, edge, and spatial characteristics.
 * These features are then used to determine image similarity.
 */
public class AdvancedImageComparator {
    private static final int COLOR_BINS = 8;
    private static final int GRID_SIZE = 4;

    /**
     * Container class for extracted image features.
     */
    public static class ImageFeatures {
        public double[] colorHistogram;
        public double[] edgeHistogram;
        public double[] spatialColorFeatures;

        public ImageFeatures(double[] colorHist, double[] edgeHist, double[] spatialFeatures) {
            this.colorHistogram = colorHist;
            this.edgeHistogram = edgeHist;
            this.spatialColorFeatures = spatialFeatures;
        }
    }

    /**
     * Extracts all image features (color, edge, spatial) from the given image.
     */
    public static ImageFeatures extractFeatures(Image image) {
        double[] colorHist = extractColorHistogram(image);
        double[] edgeHist = extractEdgeHistogram(image);
        double[] spatialFeatures = extractSpatialFeatures(image);
        return new ImageFeatures(colorHist, edgeHist, spatialFeatures);
    }

    /**
     * Generates a normalized histogram of RGB values across the image.
     */
    private static double[] extractColorHistogram(Image image) {
        double[] hist = new double[COLOR_BINS * 3];
        PixelReader reader = image.getPixelReader();
        int step = Math.max(1, (int)(image.getWidth() * image.getHeight() / 1000));

        for (int y = 0; y < image.getHeight(); y += step) {
            for (int x = 0; x < image.getWidth(); x += step) {
                Color color = reader.getColor(x, y);
                hist[(int)(color.getRed() * (COLOR_BINS - 1))]++;
                hist[COLOR_BINS + (int)(color.getGreen() * (COLOR_BINS - 1))]++;
                hist[2 * COLOR_BINS + (int)(color.getBlue() * (COLOR_BINS - 1))]++;
            }
        }

        double sum = Arrays.stream(hist).sum();
        if (sum > 0) {
            for (int i = 0; i < hist.length; i++) {
                hist[i] /= sum;
            }
        }
        return hist;
    }

    /**
     * Generates a simple 8-bin edge histogram using brightness gradients.
     */
    private static double[] extractEdgeHistogram(Image image) {
        double[] edgeHist = new double[8];
        PixelReader reader = image.getPixelReader();
        int step = 5;

        for (int y = 1; y < image.getHeight() - 1; y += step) {
            for (int x = 1; x < image.getWidth() - 1; x += step) {
                Color c = reader.getColor(x, y);
                Color right = reader.getColor(x + 1, y);
                Color bottom = reader.getColor(x, y + 1);

                double dx = brightness(right) - brightness(c);
                double dy = brightness(bottom) - brightness(c);

                double magnitude = Math.sqrt(dx * dx + dy * dy);
                if (magnitude > 0.1) {
                    double angle = Math.atan2(dy, dx);
                    int bin = (int)((angle + Math.PI) / (Math.PI / 4)) % 8;
                    edgeHist[bin] += magnitude;
                }
            }
        }

        double sum = Arrays.stream(edgeHist).sum();
        if (sum > 0) {
            for (int i = 0; i < edgeHist.length; i++) {
                edgeHist[i] /= sum;
            }
        }
        return edgeHist;
    }

    /**
     * Extracts average RGB color values for each cell in a 4x4 grid.
     */
    private static double[] extractSpatialFeatures(Image image) {
        double[] features = new double[GRID_SIZE * GRID_SIZE * 3];
        PixelReader reader = image.getPixelReader();
        int cellWidth = (int)(image.getWidth() / GRID_SIZE);
        int cellHeight = (int)(image.getHeight() / GRID_SIZE);

        for (int gy = 0; gy < GRID_SIZE; gy++) {
            for (int gx = 0; gx < GRID_SIZE; gx++) {
                int startX = gx * cellWidth;
                int startY = gy * cellHeight;
                int endX = Math.min(startX + cellWidth, (int)image.getWidth());
                int endY = Math.min(startY + cellHeight, (int)image.getHeight());

                double r = 0, g = 0, b = 0;
                int count = 0;

                for (int y = startY; y < endY; y += 2) {
                    for (int x = startX; x < endX; x += 2) {
                        Color color = reader.getColor(x, y);
                        r += color.getRed();
                        g += color.getGreen();
                        b += color.getBlue();
                        count++;
                    }
                }

                if (count > 0) {
                    int base = (gy * GRID_SIZE + gx) * 3;
                    features[base] = r / count;
                    features[base + 1] = g / count;
                    features[base + 2] = b / count;
                }
            }
        }
        return features;
    }

    /**
     * Calculates brightness for a given color using luminance formula.
     */
    private static double brightness(Color c) {
        return 0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue();
    }

    /**
     * Combines all three feature similarities using weighted cosine similarity.
     */
    public static double compareImages(ImageFeatures f1, ImageFeatures f2) {
        double colorSim = cosineSimilarity(f1.colorHistogram, f2.colorHistogram);
        double edgeSim = cosineSimilarity(f1.edgeHistogram, f2.edgeHistogram);
        double spatialSim = cosineSimilarity(f1.spatialColorFeatures, f2.spatialColorFeatures);

        return 0.5 * colorSim + 0.3 * spatialSim + 0.2 * edgeSim;
    }

    /**
     * Computes cosine similarity between two feature vectors.
     */
    private static double cosineSimilarity(double[] v1, double[] v2) {
        double dot = 0, norm1 = 0, norm2 = 0;
        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2) + 1e-10);
    }
}
