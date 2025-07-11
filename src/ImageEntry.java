public class ImageEntry {

	/**
	 * Represents a single image entry in the system. Can store either feature
	 * vectors or a graph node representation, along with an optional similarity
	 * score.
	 */

	public String name;
	public Object representation; 
	public AdvancedImageComparator.ImageFeatures features;
	public double similarity;

	// Constructor for feature array approach
	public ImageEntry(String name, double[] features) {
		this.name = name;
		this.representation = features;
		this.similarity = 0;
	}

	// Constructor for graph node approach
	public ImageEntry(String name, KNNImageGraph.ImageNode node) {
		this.name = name;
		this.representation = node;
		this.similarity = 0;
	}

	// Constructor with similarity initialization
	public ImageEntry(String name, Object representation, double similarity) {
		this.name = name;
		this.representation = representation;
		this.similarity = similarity;
	}

	public ImageEntry(String name, AdvancedImageComparator.ImageFeatures features) {
		this.name = name;
		this.features = features;
		this.similarity = 0;
	}
}