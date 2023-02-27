import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class PathFinder extends Application {

	private Stage primaryStage;
	private ImageView imageView;
	private BorderPane root;
	private FlowPane flow;
	private Pane outputArea;
	private Place place1;
	private Place place2;
	private ListGraph<Place> graph;
	private Boolean changed = false;
	private ArrayList<Label> labels;

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;

		// New graph collection object
		graph = new ListGraph<Place>();
		labels = new ArrayList<Label>();

		root = new BorderPane();
		flow = new FlowPane();
		outputArea = new Pane();
		imageView = new ImageView();

		// Menu on top
		MenuBar menuBar = new MenuBar();
		Menu menuFile = new Menu("File");
		menuBar.getMenus().add(menuFile);
		menuBar.setId("menu");
		menuFile.setId("menuFile");

		MenuItem menuNewMap = new MenuItem("New Map");
		MenuItem menuOpenFile = new MenuItem("Open");
		MenuItem menuSaveFile = new MenuItem("Save");
		MenuItem menuSaveImage = new MenuItem("Save Image");
		MenuItem menuExit = new MenuItem("Exit");
		menuNewMap.setId("menuNewMap");
		menuOpenFile.setId("menuOpenFile");
		menuSaveImage.setId("menuSaveImage");
		menuSaveFile.setId("menuSaveFile");
		menuExit.setId("menuExit");

		menuFile.getItems().addAll(menuNewMap, menuOpenFile, menuSaveFile, menuSaveImage, menuExit);

		menuOpenFile.setOnAction(new NewOpenHandler());
		menuSaveFile.setOnAction(new SaveFileHandler());
		menuSaveImage.setOnAction(new SaveImageHandler());
		menuExit.setOnAction(new ExitHandler());

		// Buttons on top
		Button btnFindPath = new Button("Find Path");
		Button btnShowConnection = new Button("Show Connection");
		Button btnNewPlace = new Button("New Place");
		Button btnNewConnection = new Button("New Connection");
		Button btnChangeConnection = new Button("Change Connection");
		btnFindPath.setId("btnFindPath");
		btnShowConnection.setId("btnShowConnection");
		btnNewPlace.setId("btnNewPlace");
		btnNewConnection.setId("btnNewConnection");
		btnChangeConnection.setId("btnChangeConnection");
		flow.getChildren().addAll(btnFindPath, btnShowConnection, btnNewPlace, btnNewConnection, btnChangeConnection);
		flow.setHgap(20);
		flow.setPadding(new Insets(10));
		flow.setAlignment(Pos.TOP_CENTER);

		btnNewPlace.setOnAction(new NewButtonHandler());
		outputArea.getChildren().add(imageView);
		outputArea.setId("outputArea");

		btnNewConnection.setOnAction(new NewConnectionHandler());
		btnFindPath.setOnAction(new FindPathHandler());
		btnShowConnection.setOnAction(new ShowConnectionHandler());
		btnChangeConnection.setOnAction(new ChangeConnectionHandler());

		root.setTop(menuBar);
		root.setCenter(flow);
		root.setBottom(outputArea);

		// Map
		menuNewMap.setOnAction(new NewMapHandler());

		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setMinWidth(600);
		primaryStage.setMaxHeight(831);
		primaryStage.show();
		primaryStage.setTitle("PathFinder");
		primaryStage.setOnCloseRequest(new ExitManuallyHandler());
	}

	class ChangeConnectionHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			// Check that two places are marked
			if (place1 == null || place2 == null) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error!");
				alert.setHeaderText("Two places must be selected!");
				alert.showAndWait();
				return;
			}
			// Check connection
			if (graph.getEdgeBetween(place1, place2) == null) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error!");
				alert.setHeaderText("There is no connection between the places!");
				alert.showAndWait();
				return;
			}

			MyTwoFieldAlert alert = new MyTwoFieldAlert();
			Edge<Place> edge = graph.getEdgeBetween(place1, place2);
			alert.setName(edge.getName());
			alert.nameDisable();

			Optional<ButtonType> result = alert.showAndWait();
			int weight = alert.getWeight();
			if (result.isPresent() && result.get() != ButtonType.OK) {
				return;
			} else {
				graph.setConnectionWeight(place1, place2, weight);
				changed = true;
			}

		}
	}

	class ShowConnectionHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			// check if two places are marked
			if (place1 == null || place2 == null) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error!");
				alert.setHeaderText("Two places must be selected!");
				alert.showAndWait();
				return;
			}
			// Check connection
			if (graph.getEdgeBetween(place1, place2) == null) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error!");
				alert.setHeaderText("There is no connection between the places!");
				alert.showAndWait();
				return;
			}
			MyTwoFieldAlert alert = new MyTwoFieldAlert();
			Edge<Place> edge = graph.getEdgeBetween(place1, place2);
			alert.setName(edge.getName());
			alert.setWeight(String.valueOf(edge.getWeight()));
			alert.showAndWait();

		}
	}

	class FindPathHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			if (graph.pathExists(place1, place2)) {

				TextArea text = null;
				List<Edge<Place>> path = graph.getPath(place1, place2);
				text = new TextArea();
				// Weight/time variable
				int totalWeight = 0;

				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Message");
				alert.setHeaderText("The path from " + place1.getName() + " to " + place2.getName());
				for (int i = 0; path.size() > i; i++) {
					Edge<Place> edge = path.get(i);
					String destination = edge.getDestination().getName();
					String name = edge.getName();
					String weight = String.valueOf(edge.getWeight());
					int weightInt = edge.getWeight();
					text.appendText("to " + destination + " by " + name + " takes " + weight + "\n");

					totalWeight += weightInt;

				}
				text.appendText("Total " + String.valueOf(totalWeight));
				alert.getDialogPane().setContent(text);
				alert.showAndWait();
			}
		}
	}

	class NewMapHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			if (changed || imageView.getImage() != null) {
				// Save dialog, if okay code below runs
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Warning!");
				alert.setHeaderText("Unsaved changes, continue anyway?");
				Optional<ButtonType> result = alert.showAndWait();
				if (result.isPresent() && result.get() != ButtonType.OK) {
					return;
				}
			}

			outputArea.getChildren().clear();
			
			imageView = new ImageView();
			outputArea.getChildren().add(imageView);
			graph = new ListGraph<>();

			Image image = new Image("file:europa.gif");
			imageView.setImage(image);
			primaryStage.sizeToScene();
			primaryStage.centerOnScreen();
			changed = true;
			place1 = null;
			place2 = null;
		}

	}

	class SaveImageHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			try {
				WritableImage image = outputArea.snapshot(null, null);
				BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
				ImageIO.write(bufferedImage, "png", new File("capture.png"));
			} catch (IOException e) {
				Alert alert = new Alert(Alert.AlertType.ERROR, "IO-fel " + e.getMessage());
				alert.showAndWait();
			}
		}
	}

	class SaveFileHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {

			try {
				FileWriter fileWriter = new FileWriter("europa.graph");
				PrintWriter printWriter = new PrintWriter(fileWriter);

				String url = imageView.getImage().getUrl();

				// Writes url of image on row 1
				printWriter.println(url);

				// Write nodes on row 2 with ";" as separator

				Iterator<Place> iterator = graph.getNodes().iterator();
				while (iterator.hasNext()) {
					Place place = iterator.next();

					// Last place 
					if (!iterator.hasNext()) {
						printWriter.println(place.getName() + ";" + place.getX() + ";" + place.getY());
					} else {
						printWriter.print(place.getName() + ";" + place.getX() + ";" + place.getY() + ";");
					}
				}

				// Writes connections row for row
				for (Place place : graph.getNodes()) {
					Collection<Edge<Place>> edgeFrom = graph.getEdgesFrom(place);
					for (Edge<Place> edge : edgeFrom) {

						String name1 = place.getName();
						String name2 = edge.getDestination().getName();
						String conName = edge.getName();
						int weight = edge.getWeight();

						printWriter.println(name1 + ";" + name2 + ";" + conName + ";" + String.valueOf(weight));
					}
				}
				fileWriter.close();
				printWriter.close();

			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	class NewOpenHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			try {
				FileReader fileReader = new FileReader("europa.graph");
				Scanner scanner = new Scanner(fileReader);

				// Read map
				if (changed || imageView.getImage() != null) {
					// Continue dialog
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("Warning!");
					alert.setHeaderText("Unsaved changes, continue anyway?");
					Optional<ButtonType> result = alert.showAndWait();
					if (result.isPresent() && result.get() != ButtonType.OK) {
						scanner.close();
						return;
					}

				}

				// Remove places and connections
				outputArea.getChildren().clear();
				imageView = new ImageView();
				outputArea.getChildren().add(imageView);
				graph = new ListGraph<>();

				String url = scanner.nextLine();
				Image image = new Image(url);
				imageView.setImage(image);
				primaryStage.sizeToScene();
				primaryStage.centerOnScreen();

				String line = scanner.nextLine();
				String[] tokens = line.split(";");
				
				for (int i = 0; i < tokens.length; i += 3) {
					String name = tokens[i];
					String dx = tokens[i + 1];
					String dy = tokens[i + 2];
					double x = Double.valueOf(dx);
					double y = Double.valueOf(dy);

					// Add places
					Place place = new Place(name, x, y);
					place.setId(name);
					place.setOnMouseClicked(new MarkNodeHandler());
					Label placeName = new Label(name);
					placeName.setDisable(true);
					placeName.setLayoutX(x);
					placeName.setLayoutY(y);
					placeName.setDisable(true);
					placeName.setStyle("-fx-font-weight: bold; -fx-text-fill:BLACK; -fx-font-size: 18");
					graph.add(place);
					outputArea.getChildren().add(place);
					outputArea.getChildren().add(placeName);

				}

				scanner.useDelimiter(";|\\r\\n|\\s");
				// Read connections
				while (scanner.hasNext()) {

					String name1 = scanner.next();
					String name2 = scanner.next();
					String name = scanner.next();
					String weightString = scanner.next();

					int weight = Integer.parseInt(weightString);

					for (Place place : graph.getNodes()) {
						if (place.getName().equals(name1)) {
							place1 = place;
						}
						if (place.getName().equals(name2)) {
							place2 = place;
						}
					}

					if (graph.getEdgeBetween(place1, place2) == null) {

						graph.connect(place1, place2, name, weight);
						Line newLineConnection = new Line(place1.getX(), place1.getY(), place2.getX(), place2.getY());
						newLineConnection.setDisable(true);
						outputArea.getChildren().add(newLineConnection);
					}
				}

				changed = true;
				place1 = null;
				place2 = null;
				scanner.close();
				// Error message if file not exist
			} catch (FileNotFoundException e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error!");
				alert.setHeaderText("No such file!");
				alert.showAndWait();
				return;
			}

		}

	}

	class NewConnectionHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			// Check if two places marked
			if (place1 == null || place2 == null) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error!");
				alert.setHeaderText("Two places must be selected!");
				alert.showAndWait();
				return;
			}

			// Check if connection already exists
			if (graph.getEdgeBetween(place1, place2) != null) {
				Alert alert = new Alert(AlertType.ERROR, "There is already a connection between the nodes!");
				alert.showAndWait();
				return;
			}

			// Check that time is a number
			try {
				MyTwoFieldAlert dialog = new MyTwoFieldAlert();
				dialog.setTitle("Connection");
				dialog.setHeaderText("Connection from " + place1.getName() + " to " + place2.getName());
				Optional<ButtonType> result = dialog.showAndWait();
				if (result.isPresent() && result.get() != ButtonType.OK) {
					return;
				}

				String name = dialog.getName();
				int weight = dialog.getWeight();
				if (name.isEmpty()) {
					Alert alert = new Alert(AlertType.ERROR, "Name cant be empty!");
					alert.showAndWait();
					return;
				}
				// Creates connection
				graph.connect(place1, place2, name, weight);
				Line line = new Line(place1.getX(), place1.getY(), place2.getX(), place2.getY());
				line.setDisable(true);
				outputArea.getChildren().add(line);
				changed = true;
			} catch (NumberFormatException e) {
				Alert alert = new Alert(AlertType.ERROR, "Time must be a number!");
				alert.showAndWait();
				return;
			}
		}

	}

	class MarkNodeHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) {

			Place place = (Place) event.getSource();
			if (place1 == null && place != place2) {
				place1 = place;
				place1.setFill(Color.RED);
			} else if (place1 != null && place2 == null && place != place1) {
				place2 = place;
				place2.setFill(Color.RED);
			} else if (place1 != null && place1 == place) {
				place1.setFill(Color.BLUE);
				place1 = null;
			} else if (place2 != null && place == place2) {
				place2.setFill(Color.BLUE);
				place2 = null;
			}

		}
	}

	class NewButtonHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {

			imageView.setOnMouseClicked(new NewHandler());
			outputArea.setCursor(Cursor.CROSSHAIR);
			
		}

	}

	class NewHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) {
			double x = event.getSceneX();
			double y = event.getSceneY();

			MyAlert dialog = new MyAlert();
			Optional<ButtonType> result = dialog.showAndWait();
			if (result.isPresent() && result.get() != ButtonType.OK || dialog.getName().isEmpty())
				return;

			String name = dialog.getName();
			Place place = new Place(name, x, y - 70);
			place.setId(name);
			place.setOnMouseClicked(new MarkNodeHandler());
			//Names
			Label placeName = new Label(name);
			labels.add(placeName);
			placeName.setLayoutX(x);
			placeName.setLayoutY(y - 70);
			placeName.setDisable(true);
			placeName.setStyle("-fx-font-weight: bold; -fx-text-fill:BLACK; -fx-font-size: 18");
			// Add places and names
			graph.add(place);
			outputArea.getChildren().addAll(place, placeName);
			changed = true;
			imageView.setOnMouseClicked(null);
			outputArea.setCursor(Cursor.DEFAULT);
		}
	}

	class ExitHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			if (changed) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Warning!");
				alert.setHeaderText("");
				alert.setContentText("Unsaved changes, exit anyway?");
				Optional<ButtonType> result = alert.showAndWait();
				if (result.isPresent() && result.get().equals(ButtonType.OK))
					primaryStage.close();
			}

		}
	}

	class ExitManuallyHandler implements EventHandler<WindowEvent> {
		@Override
		public void handle(WindowEvent event) {
			if (changed) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Warning!");
				alert.setHeaderText("");
				alert.setContentText("Unsaved changes, exit anyway?");
				Optional<ButtonType> result = alert.showAndWait();
				if (result.isPresent() && result.get().equals(ButtonType.OK)) {
					primaryStage.close();
				} else
					event.consume();
			}
		}
	}

	class MyAlert extends Alert {
		private TextField nameField = new TextField();

		MyAlert() {
			super(AlertType.CONFIRMATION);
			GridPane grid = new GridPane();
			grid.setAlignment(Pos.CENTER);
			grid.setPadding(new Insets(10));
			grid.setHgap(5);
			grid.setVgap(10);
			grid.addRow(0, new Label("Name of place:"), nameField);
			setHeaderText(null);
			getDialogPane().setContent(grid);
			grid.requestFocus();
			setTitle("Name");
			nameField.requestFocus();

		}

		public String getName() {
			return nameField.getText();
		}

	}

	class MyTwoFieldAlert extends Alert {
		private TextField nameField = new TextField();
		private TextField weightField = new TextField();

		MyTwoFieldAlert() {
			super(AlertType.CONFIRMATION);
			GridPane grid = new GridPane();
			grid.setAlignment(Pos.CENTER);
			grid.setPadding(new Insets(10));
			grid.setHgap(5);
			grid.setVgap(10);
			grid.addRow(0, new Label("Name:"), nameField);
			grid.addRow(1, new Label("Time:"), weightField);
			setHeaderText(null);
			getDialogPane().setContent(grid);
			grid.requestFocus();
			setTitle("Connection");

		}

		public String getName() {
			return nameField.getText();
		}

		public int getWeight() {
			return Integer.parseInt(weightField.getText());
		}

		public void setName(String name) {
			nameField.appendText(name);
		}

		public void setWeight(String weight) {
			weightField.appendText(weight);
		}

		public void nameDisable() {
			nameField.setDisable(true);
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}