import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main class for the South Africa Tourism Route Optimizer application. This
 * application allows users to upload an image of a tourist destination, find
 * visually similar places in South Africa, and plan safe routes.
 */
public class Main extends Application {
	// === File paths ===
	private final String ATTRACTION_PATH = "data/images/attractions";
	private final String MAP_PATH = "data/images/map/Map.jpg";
	private final String COORDINATES_CSV = "data/images/map/expanded_south_africa_attractions.csv";

	// === State Variables ===
	private Image uploadedImage;
	private ImageView previewImageView = new ImageView();
	private TextArea resultsArea = new TextArea();

	private Map<String, Point> attractionCoordinates = new HashMap<>();
	private List<String> topMatches = new ArrayList<>();
	private Point selectedDestinationPoint = null;
	private Point selectedStartPoint = null;
	private String selectedDestinationName = null;
	private final List<Point> unsafeZones = generateUnsafeZones(26, 700, 500);
	private List<Point> calculatedPath = new ArrayList<>();

	@Override
	public void start(Stage primaryStage) {
		showIntroScreen(primaryStage);
		loadAttractionCoordinates();
	}

	/**
	 * Generates randomly spaced unsafe zones on the map.
	 */
	private List<Point> generateUnsafeZones(int count, int width, int height) {
		List<Point> zones = new ArrayList<>();
		Random random = new Random(50);

		while (zones.size() < count) {
			int x = 50 + random.nextInt(width - 100);
			int y = 50 + random.nextInt(height - 100);
			Point newPoint = new Point(x, y);

			boolean valid = true;
			for (Point p : zones) {
				if (distance(newPoint, p) < 50) {
					valid = false;
					break;
				}
			}

			if (valid) {
				zones.add(newPoint);
			}
		}
		return zones;
	}

	/**
	 * Displays the introduction screen of the application. This version only
	 * includes the title and welcome button.
	 */
	private void showIntroScreen(Stage stage) {
		Image bgImage = new Image("file:data/images/map/Intro.jpg");
		ImageView bgView = new ImageView(bgImage);
		bgView.setPreserveRatio(false);
		bgView.setFitWidth(800);
		bgView.setFitHeight(600);

		Rectangle overlay = new Rectangle(800, 600);
		overlay.setFill(Color.rgb(0, 0, 0, 0.4));

		Label title = new Label("Mini Project by Divhani");
		title.setStyle("-fx-font-size: 28px; -fx-text-fill: white; -fx-font-weight: bold;");

		Button continueBtn = new Button("Welcome");
		continueBtn.setStyle(
				"-fx-background-color: #2196f3; -fx-text-fill: white; -fx-background-radius: 12; -fx-font-size: 14px; -fx-padding: 8px 16px;");
		continueBtn.setOnAction(e -> showWelcomeScreen(stage));

		VBox content = new VBox(30, title, continueBtn);
		content.setAlignment(Pos.CENTER);

		StackPane root = new StackPane(bgView, overlay, content);
		Scene scene = new Scene(root, 800, 600);
		stage.setScene(scene);
		stage.setTitle("Mini Project 2025");
		stage.show();
	}

	public void showWelcomeScreen(Stage stage) {
		VBox root = new VBox(20);
		root.setAlignment(Pos.CENTER);
		root.setPadding(new Insets(40));

		// === Title ===
		Label title = new Label("South Africa Tourism Route Optimizer");
		title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

		// === Main Image ===
		Image image = new Image("file:data/images/map/Tourist-Places-in-South-Africa.jpg");
		ImageView imageView = new ImageView(image);
		imageView.setFitWidth(400);
		imageView.setPreserveRatio(true);
		imageView.setSmooth(true);
		imageView.setStyle("-fx-effect: dropshadow(gaussian, gray, 4, 0.3, 1, 1);");

		// === Buttons (Start Exploring first, then Back) ===
		Button startBtn = new Button("Start Exploring");
		startBtn.setStyle(
				"-fx-background-color: #007ACC; -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 10;");
		startBtn.setOnAction(e -> showUploadStage(stage));

		Button backBtn = new Button("Back");
		backBtn.setStyle(
				"-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-font-size: 13px; -fx-background-radius: 10;");
		backBtn.setOnAction(e -> showIntroScreen(stage));

		HBox buttonBox = new HBox(15, startBtn, backBtn);
		buttonBox.setAlignment(Pos.CENTER);

		// === Footer ===
		Label footer = new Label("CSC03A3 Mini Project 2025");
		footer.setStyle("-fx-font-size: 11px;");

		// === Final Layout ===
		root.getChildren().addAll(title, imageView, buttonBox, footer);

		Scene scene = new Scene(root, 800, 600);
		stage.setScene(scene);
		stage.setTitle("Tourism Route Optimizer");
		stage.show();
	}

	//// SHOW UPLOAD STAGE////
	private void showUploadStage(Stage stage) {
		// === Heading ===
		Label heading = new Label("Discover Your Next Adventure Visually");
		heading.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

		Label subheading = new Label("Upload an image to explore similar tourist destinations in South Africa.");
		subheading.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

		VBox headingBox = new VBox(5, heading, subheading);
		headingBox.setAlignment(Pos.CENTER);

		// === Preview Image Setup ===
		previewImageView.setFitWidth(400);
		previewImageView.setPreserveRatio(true);
		previewImageView.setSmooth(true);
		previewImageView.setStyle("-fx-effect: dropshadow(gaussian, gray, 6, 0.4, 2, 2);");

		Label imageCaption = new Label("Selected Image Preview");
		imageCaption.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

		VBox imageBox = new VBox(10, previewImageView, imageCaption);
		imageBox.setAlignment(Pos.CENTER);

		// === Buttons ===
		Button uploadBtn = new Button("Upload Image");
		Button searchBtn = new Button("Find Similar Places");
		Button resetBtn = new Button("Reset");
		Button backBtn = new Button(" Back");

		String unifiedButtonStyle = "-fx-background-color: #cccccc; -fx-font-size: 14px; -fx-pref-width: 160px;";
		uploadBtn.setStyle(unifiedButtonStyle);
		searchBtn.setStyle(unifiedButtonStyle);
		resetBtn.setStyle(unifiedButtonStyle.replace("#eeeeee", "#f2f2f2"));
		backBtn.setStyle("-fx-background-color: #eeeeee; -fx-font-size: 14px; -fx-pref-width: 160px;");

		// === Actions ===
		uploadBtn.setOnAction(e -> {
			FileChooser fileChooser = new FileChooser();
			File imageFile = fileChooser.showOpenDialog(stage);
			if (imageFile != null) {
				uploadedImage = new Image(imageFile.toURI().toString());
				previewImageView.setImage(uploadedImage);
			}
		});

		searchBtn.setOnAction(e -> {
			if (uploadedImage != null) {
				findSimilarAttractions();
				showResultsWithMap(stage);
			}
		});

		resetBtn.setOnAction(e -> {
			uploadedImage = null;
			previewImageView.setImage(null);
		});

		backBtn.setOnAction(e -> showWelcomeScreen(stage));

		// === Button VBox ===
		VBox buttonBox = new VBox(20, uploadBtn, searchBtn, resetBtn, backBtn);
		buttonBox.setAlignment(Pos.CENTER);

		// === Layout ===
		HBox contentBox = new HBox(50, buttonBox, imageBox);
		contentBox.setAlignment(Pos.CENTER);
		contentBox.setPadding(new Insets(30));

		VBox root = new VBox(30, headingBox, contentBox);
		root.setAlignment(Pos.TOP_CENTER);
		root.setPadding(new Insets(30));

		Scene scene = new Scene(root, 950, 600);
		stage.setScene(scene);
		stage.setTitle("Tourism Route Optimizer");
		stage.show();
	}

	private void findSimilarAttractions() {
		File folder = new File(ATTRACTION_PATH);
		List<ImageEntry> entries = new ArrayList<>();

		// Extract features from query image
		AdvancedImageComparator.ImageFeatures queryFeatures = AdvancedImageComparator.extractFeatures(uploadedImage);

		// Process all candidate images
		for (File file : Objects.requireNonNull(folder.listFiles())) {
			if (file.getName().endsWith(".jpg") || file.getName().endsWith(".png")) {
				Image candidate = new Image(file.toURI().toString());
				AdvancedImageComparator.ImageFeatures features = AdvancedImageComparator.extractFeatures(candidate);
				String name = file.getName().split("\\.")[0];

				ImageEntry entry = new ImageEntry(name, features);
				entry.similarity = AdvancedImageComparator.compareImages(queryFeatures, features);
				entries.add(entry);
			}
		}

		// Sort by similarity (descending)
		entries.sort((a, b) -> Double.compare(b.similarity, a.similarity));
		topMatches = entries.stream().limit(6).map(e -> e.name).collect(Collectors.toList());
	}

	private void showResultsWithMap(Stage stage) {
		VBox root = new VBox(20); // spacing between sections
		root.setPadding(new Insets(30));
		root.setAlignment(Pos.TOP_CENTER);

		// === Heading ===
		Label heading = new Label("Top similar places based on your image:");
		heading.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

		// === Grid of images ===
		GridPane imageGrid = new GridPane();
		imageGrid.setHgap(25);
		imageGrid.setVgap(20);
		imageGrid.setAlignment(Pos.CENTER);
		imageGrid.setPadding(new Insets(20));

		int col = 0;
		int row = 0;
		int columns = 5;

		for (String name : topMatches) {
			File file = new File(ATTRACTION_PATH + "/" + name + ".jpg");
			if (!file.exists()) {
				file = new File(ATTRACTION_PATH + "/" + name + ".png");
			}
			if (!file.exists())
				continue;

			ImageView thumbView = new ImageView(new Image(file.toURI().toString(), 100, 100, true, true));
			thumbView.setStyle("-fx-cursor: hand;");
			thumbView.setOnMouseEntered(e -> thumbView.setEffect(new DropShadow()));
			thumbView.setOnMouseExited(e -> thumbView.setEffect(null));

			final File fullImage = file;
			thumbView.setOnMouseClicked(e -> showFullImage(stage, fullImage, name));

			Label imageLabel = new Label(name);
			imageLabel.setAlignment(Pos.CENTER);
			VBox entry = new VBox(5, thumbView, imageLabel);
			entry.setAlignment(Pos.CENTER);

			imageGrid.add(entry, col, row);
			col++;
			if (col >= columns) {
				col = 0;
				row++;
			}
		}

		// === Instruction Below Images ===
		Label instruction = new Label("Click on the image to get a full view");
		instruction.setStyle("-fx-font-size: 14px; -fx-text-fill: #444;");

		// === Buttons ===
		Button showMapBtn = new Button("See on Map");
		showMapBtn.setStyle("-fx-background-color: #cccccc; -fx-font-size: 14px; -fx-padding: 6 18;");
		showMapBtn.setOnAction(e -> showInteractiveMap(stage));

		Button backBtn = new Button("Back");
		backBtn.setStyle("-fx-background-color: #eeeeee; -fx-font-size: 14px; -fx-padding: 6 18;");
		backBtn.setOnAction(e -> showUploadStage(stage));

		HBox buttonBox = new HBox(20, showMapBtn, backBtn);
		buttonBox.setAlignment(Pos.CENTER);

		// === Final layout assembly ===
		root.getChildren().addAll(heading, imageGrid, instruction, buttonBox);

		Scene scene = new Scene(root, 900, 600);
		stage.setScene(scene);
	}

	private void showFullImage(Stage parentStage, File imageFile, String title) {
		Stage imageStage = new Stage();
		imageStage.initOwner(parentStage);
		imageStage.setTitle(title);

		// Load and display the full image
		ImageView fullImageView = new ImageView(new Image(imageFile.toURI().toString()));
		fullImageView.setPreserveRatio(true);
		fullImageView.setFitWidth(600); // Limits width while maintaining ratio

		// Add simple back button
		Button backButton = new Button("Back to Results");
		backButton.setOnAction(e -> imageStage.close());
		backButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 15;");

		VBox layout = new VBox(10, backButton, fullImageView);
		layout.setPadding(new Insets(10));
		layout.setAlignment(Pos.CENTER);

		// Calculate window size based on image dimensions
		Scene scene = new Scene(layout);
		imageStage.setScene(scene);

		// Fit window to image after it loads
		fullImageView.imageProperty().addListener((obs, oldImg, newImg) -> {
			if (newImg != null) {
				double width = Math.min(newImg.getWidth(), 800) + 40; // Add padding
				double height = Math.min(newImg.getHeight(), 600) + 80; // Add button space
				imageStage.setWidth(width);
				imageStage.setHeight(height);
			}
		});

		imageStage.show();
	}

	private void showInteractiveMap(Stage previousStage) {
		Stage mapStage = new Stage();
		mapStage.setTitle("Interactive Tourist Map");

		// === Title ===
		Label heading = new Label("Tourist Safety & Route Planner Map");
		heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
		heading.setAlignment(Pos.CENTER);

		// === Map Canvas ===
		Canvas canvas = new Canvas(700, 500);
		MapRenderer.drawMap(canvas, MAP_PATH, unsafeZones, topMatches, attractionCoordinates, selectedStartPoint,
				selectedDestinationPoint, calculatedPath);

		// === Instruction Label ===
		Label instructionLabel = new Label(
				"Click on the map to set start point, then click on a green point to set destination");
		instructionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

		// === Result TextArea ===
		resultsArea = new TextArea();
		resultsArea.setEditable(false);
		resultsArea.setWrapText(false);
		resultsArea.setPrefHeight(100);
		resultsArea.setStyle("-fx-font-family: monospace;");

		// === Buttons ===
		Button generateRouteBtn = new Button("Generate Safest Route");
		generateRouteBtn.setPrefWidth(180);
		generateRouteBtn.setDisable(selectedStartPoint == null || selectedDestinationPoint == null);
		generateRouteBtn.setOnAction(e -> {
			calculatedPath = PathFinder.calculatePath(selectedStartPoint, selectedDestinationPoint, unsafeZones, 700,
					500);
			MapRenderer.drawMap(canvas, MAP_PATH, unsafeZones, topMatches, attractionCoordinates, selectedStartPoint,
					selectedDestinationPoint, calculatedPath);
			updateResultsArea(generateRouteDescription());
		});

		Button showGraphBtn = new Button("Show Graph Structure");
		showGraphBtn.setPrefWidth(180);
		showGraphBtn.setOnAction(e -> {
			if (selectedStartPoint != null && selectedDestinationPoint != null) {
				GraphVisualizer.showGraph(attractionCoordinates, topMatches, selectedStartPoint,
						selectedDestinationPoint);
				updateResultsArea("Graph visualization displayed");
			} else {
				showAlert("Please select both start and destination points first");
			}
		});

		Button resetBtn = new Button("Reset");
		resetBtn.setPrefWidth(180);
		resetBtn.setOnAction(e -> {
			resetSelections();
			MapRenderer.drawMap(canvas, MAP_PATH, unsafeZones, topMatches, attractionCoordinates, selectedStartPoint,
					selectedDestinationPoint, calculatedPath);
			updateResultsArea("=== SELECTIONS RESET ===");
		});

		Button backBtn = new Button("Back");
		backBtn.setPrefWidth(180);
		backBtn.setOnAction(e -> {
			mapStage.close();
			previousStage.show();
		});

		canvas.setOnMouseClicked(e -> {
			int x = (int) e.getX();
			int y = (int) e.getY();

			if (!calculatedPath.isEmpty()) {
				showAlert("Please reset the current path before selecting new points");
				return;
			}

			boolean clickedOnDestination = false;
			for (String match : topMatches) {
				Point p = attractionCoordinates.get(match);
				if (p != null && distance(x, y, p.x, p.y) < 10) {
					if (selectedStartPoint == null) {
						showAlert("Please select start point first");
						return;
					}
					selectedDestinationPoint = p;
					selectedDestinationName = match;
					clickedOnDestination = true;
					updateResultsArea("Destination set: " + match + "\nCoordinates: (" + p.x + ", " + p.y + ")");
					break;
				}
			}

			if (!clickedOnDestination && selectedStartPoint == null) {
				selectedStartPoint = new Point(x, y);
				updateResultsArea("Start point set at: (" + x + ", " + y + ")");
			}

			generateRouteBtn.setDisable(selectedStartPoint == null || selectedDestinationPoint == null);
			MapRenderer.drawMap(canvas, MAP_PATH, unsafeZones, topMatches, attractionCoordinates, selectedStartPoint,
					selectedDestinationPoint, calculatedPath);
		});

		VBox buttonBox = new VBox(15, generateRouteBtn, showGraphBtn, resetBtn, backBtn);
		buttonBox.setAlignment(Pos.TOP_LEFT);
		buttonBox.setPadding(new Insets(10));

		// === Canvas & Instruction in vertical layout ===
		VBox mapColumn = new VBox(5, canvas, instructionLabel);
		mapColumn.setAlignment(Pos.CENTER);

		// === HBox layout: buttons left, map right ===
		HBox topContent = new HBox(30, buttonBox, mapColumn);
		topContent.setAlignment(Pos.TOP_CENTER);

		// === Final layout ===
		VBox root = new VBox(20, heading, topContent, resultsArea);
		root.setAlignment(Pos.TOP_CENTER);
		root.setPadding(new Insets(30));

		ScrollPane scrollPane = new ScrollPane(root);
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);
		mapStage.setScene(new Scene(scrollPane, 1000, 700));
		mapStage.show();
		previousStage.hide();
	}

	private void updateResultsArea(String newInfo) {
		resultsArea.setText(resultsArea.getText() + "\n\n" + newInfo);
		resultsArea.positionCaret(resultsArea.getText().length());
	}

	private String generateRouteDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("=== ROUTE CALCULATION RESULTS ===\n");

		sb.append("From: (").append(selectedStartPoint.x).append(", ").append(selectedStartPoint.y).append(")\n");

		if (selectedDestinationName != null) {
			sb.append("To: ").append(selectedDestinationName).append("\n");
		}

		sb.append("Path segments: ").append(calculatedPath.size()).append("\n");
		sb.append("Total distance: ").append(String.format("%.1f pixels", calculateTotalDistance())).append("\n");
		sb.append("Unsafe zones avoided: ").append(countAvoidedZones()).append("\n");

		return sb.toString();
	}

	private double calculateTotalDistance() {
		double total = 0;
		for (int i = 0; i < calculatedPath.size() - 1; i++) {
			total += distance(calculatedPath.get(i), calculatedPath.get(i + 1));
		}
		return total;
	}

	private int countAvoidedZones() {
		int count = 0;
		for (Point zone : unsafeZones) {
			for (Point pathPoint : calculatedPath) {
				if (distance(zone, pathPoint) < 25) {
					count++;
					break;
				}
			}
		}
		return unsafeZones.size() - count;
	}

	private void resetSelections() {
		selectedStartPoint = null;
		selectedDestinationPoint = null;
		selectedDestinationName = null;
		calculatedPath.clear();
		updateResultsArea("Selections and path cleared");
	}

	private void showAlert(String message) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle("Warning");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private double distance(Point p1, Point p2) {
		return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
	}

	@SuppressWarnings("unused")
	private String getDijkstraDescription(PathFinder.DijkstraState state) {
		return String.format("Current: (%d,%d) | Frontier nodes: %d | Visited: %d", state.current.x, state.current.y,
				state.frontier.size(), state.distances.size());
	}

	private double distance(int x1, int y1, int x2, int y2) {
		return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}

	private void loadAttractionCoordinates() {
		try (BufferedReader reader = new BufferedReader(new FileReader(COORDINATES_CSV))) {
			String line;
			boolean isFirstLine = true;

			double minLat = -36.0;
			double maxLat = -21.5;
			double minLon = 15.5;
			double maxLon = 33.5;
			int mapWidth = 700;
			int mapHeight = 500;

			while ((line = reader.readLine()) != null) {
				if (isFirstLine) {
					isFirstLine = false;
					continue;
				}

				String[] parts = line.split(",");
				if (parts.length >= 5) {
					try {
						String name = parts[0].trim();
						double lat = Double.parseDouble(parts[3].trim());
						double lon = Double.parseDouble(parts[4].trim());

						double normX = (lon - minLon) / (maxLon - minLon);
						double normY = 1.0 - ((lat - minLat) / (maxLat - minLat));

						int x = (int) (normX * mapWidth);
						int y = (int) (normY * mapHeight);

						attractionCoordinates.put(name, new Point(x, y));
					} catch (NumberFormatException e) {
						System.err.println("Skipping invalid line: " + line);
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Failed to load coordinates: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		launch();
	}
}
