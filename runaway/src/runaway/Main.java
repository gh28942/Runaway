package runaway;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;

/*
 * By GerH, September 2018
 */
public class Main extends Application {

	@Override
	public void start(Stage stage) {
		try {

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("GameView.fxml"));

			Parent root = loader.load();

			stage.initStyle(StageStyle.DECORATED);
			stage.setTitle("Run away!");
			stage.setResizable(false);

			Scene scene = new Scene(root);
			stage.setScene(scene);
			stage.getIcons().add(new Image(getClass().getResourceAsStream("/runaway_icon.png"))); //für die runnable exe war eine Änderung von "../" zu "/" überall notwendig!

			stage.show();
			root.requestFocus();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

}
