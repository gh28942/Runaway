package runaway;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Scanner;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class HighscoreManager {
	@FXML
	TableView<Entry> HStableView;
	final String secretKey = "d~FycA*2Q$?|svf:^Db_fJ#GE";

	public void openScoreview(){

		try {
			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(getClass().getResource("HighscoreView.fxml"));
			Scene scene = new Scene(fxmlLoader.load(), 650, 355);
			Platform.runLater(new Runnable() {
				@Override public void run() {
					Stage stage = new Stage();
					//stage.setResizable(false);
					stage.setTitle("High Score");
					stage.getIcons().add(new Image(getClass().getResourceAsStream("/runaway_icon.png")));
					stage.setScene(scene);
					stage.show();

				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@FXML
	public void initialize() {

		TableColumn<Entry, String> placeCol = new TableColumn<Entry, String>("Place");
		placeCol.setMinWidth(50);
		placeCol.setCellValueFactory(
                new PropertyValueFactory<Entry, String>("place"));

        TableColumn<Entry, String> nameCol = new TableColumn<Entry, String>("Name");
        nameCol.setMinWidth(140);
        nameCol.setCellValueFactory(
                new PropertyValueFactory<Entry, String>("name"));

        TableColumn<Entry, String> scoreCol = new TableColumn<Entry, String>("Score");
        scoreCol.setMinWidth(90);
        scoreCol.setCellValueFactory(
                new PropertyValueFactory<Entry, String>("score"));

        TableColumn<Entry, String> timeCol = new TableColumn<Entry, String>("Time");
        timeCol.setMinWidth(50);
        timeCol.setCellValueFactory(
                new PropertyValueFactory<Entry, String>("time"));

        TableColumn<Entry, String> punchesleftCol = new TableColumn<Entry, String>("Punches left");
        punchesleftCol.setMinWidth(20);
        punchesleftCol.setCellValueFactory(
                new PropertyValueFactory<Entry, String>("punchesleft"));

        TableColumn<Entry, String> dateCol = new TableColumn<Entry, String>("Date");
        dateCol.setMinWidth(50);
        dateCol.setCellValueFactory(
                new PropertyValueFactory<Entry, String>("date"));

        TableColumn<Entry, String> textCol = new TableColumn<Entry, String>("Details");
        textCol.setMinWidth(50);
        textCol.setCellValueFactory(
                new PropertyValueFactory<Entry, String>("text"));

        HStableView.getColumns().addAll(placeCol, nameCol, scoreCol, timeCol, punchesleftCol, dateCol, textCol);//, timeCol, punchesleftCol, dateCol, textCol);

        final ObservableList<Entry> winners = loadCsvData();
        HStableView.setItems(winners);
	}

	public static class Entry {
	    private final SimpleStringProperty place;
	    private final SimpleStringProperty name;
	    private final SimpleStringProperty score;
	    private final SimpleStringProperty time;
	    private final SimpleStringProperty punchesleft;
	    private final SimpleStringProperty date;
	    private final SimpleStringProperty text;

	    private Entry(String fPlace, String fName, String fScore, String fTime, String fPunchesLeft, String fDate, String fText) {
	        this.place = new SimpleStringProperty(fPlace);
	        this.name = new SimpleStringProperty(fName);
	        this.score = new SimpleStringProperty(fScore);
	        this.time = new SimpleStringProperty(fTime);
	        this.punchesleft = new SimpleStringProperty(fPunchesLeft);
	        this.date = new SimpleStringProperty(fDate);
	        this.text = new SimpleStringProperty(fText);
	    }

	    public String getPlace() {
	        return place.get();
	    }
	    public void setPlace(String fPlace) {
	    	place.set(fPlace);
	    }

	    public String getName() {
	        return name.get();
	    }
	    public void setName(String fName) {
	    	name.set(fName);
	    }

	    public String getScore() {
	        return score.get();
	    }
	    public void setScore(String fScore) {
	    	score.set(fScore);
	    }

	    public String getTime() {
	        return time.get();
	    }
	    public void setTime(String fTime) {
	    	time.set(fTime);
	    }

	    public String getPunchesleft() {
	        return punchesleft.get();
	    }
	    public void setPunchesleft(String fPunchesLeft) {
	    	punchesleft.set(fPunchesLeft);
	    }

	    public String getDate() {
	        return date.get();
	    }
	    public void setDate(String fDate) {
	    	date.set(fDate);
	    }

	    public String getText() {
	        return text.get();
	    }
	    public void setText(String fText) {
	    	text.set(fText);
	    }
	}

	public ObservableList<Entry> loadCsvData(){

		ObservableList<Entry> winners = FXCollections.observableArrayList();
		DecimalFormat timeFormat = new DecimalFormat("##.000");
		Scanner in;

		try {
			in = new Scanner(new FileReader("score/highscore.csv"));
			StringBuilder sb = new StringBuilder();
			while(in.hasNext()) {
				sb.append(in.next());
			}
			in.close();

			String highscoreString = sb.toString();
			String highscoreStringDecr = AES.decrypt(highscoreString, secretKey) ;
			
			//read & check HS file
			String[] HSlines = highscoreStringDecr.split("\\|");

			for(int i=1; i<11; i++){
				String[] HSentry = HSlines[i].split(";");
				Entry mEntry = new Entry(i+".", HSentry[0], HSentry[1], timeFormat.format(Double.parseDouble(HSentry[2])), HSentry[3], HSentry[4], HSentry[5]);
				winners.add(mEntry);
			}
			return winners;

		} catch (FileNotFoundException e) {
			DialogBoxes.showErrorBox("Error", "\"Run away!\" has encountered a problem.", e.getMessage());
			return winners;
		}
	}
}
