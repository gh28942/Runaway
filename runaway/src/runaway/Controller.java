package runaway;

import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import java.util.TimeZone;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Controller {

	@FXML
	GridPane GameGridPane;
	@FXML
	Label timeLabel;
	@FXML
	Label punchesLabel;
	@FXML
	ImageView audioButton;
	@FXML
	ChoiceBox<String> difficultyChoice;
	@FXML
	Button startButton;

	@FXML
	TextField nameField;
	@FXML
	Button nameButton;
	@FXML
	Label placeLabel;

	final String secretKey = "d~FycA*2Q$?|svf:^Db_fJ#GE";
	
	int punches = 3;

	int yourPosX=5;
	int yourPosY=5;
	int enemyPosX=10;
	int enemyPosY=10;
	int carrotPosX=1000;
	int carrotPosY=1000;

	ImageView playerSprite;
	ImageView enemySprite;
	ImageView carrotSprite;
	Image imgTree;
	Image imgTrees;
	Image imgBush;
	Image imgChrt;
	Image imgCarrot;

	boolean gameIsOn = false;

	Integer rows = 0;
	Integer columns = 0;

	boolean[][] trees;
	ImageView[][] treesImgViews;

	long frameTime=120;

	boolean easterEggEnabled = false;

	boolean carrotPlaced=false;

	int foxPunched = 0;

	int treesFox = 0;
	int foxPunches = 0;
	int treeCombo6 = 0;
	int treeCombo7 = 0;
	int treeCombo8 = 0;
	int treeCombo9 = 0;

	boolean touchComboCurr = false;
	int touchComboCurrNum = 0;
	int touchComboMaxNum = 0;

	boolean audioIsOn = true;

	long campingSecs = 0;
	boolean playerStands = true;

	long startStand = 1000000000;
	long hundrethseconds = 0;
	long hundrethsecondsMax = 0;

	AudioController mAudioController;

	String[] HSlines;
	int newPlace = 11;
	long score;
	String imgSource = "/rabbit.png";
	String scoreText = "";

	public Controller() {

		//load images only once
		Image imgRabbit = new Image(getClass().getResourceAsStream("/rabbit.png")); 
		Image imgFox = new Image(getClass().getResourceAsStream("/fox.png"));
		Image imgCarrot = new Image(getClass().getResourceAsStream("/carrot.png"));
		imgTree = new Image(getClass().getResourceAsStream("/tree.png"));
		imgTrees = new Image(getClass().getResourceAsStream("/trees.png"));
		imgBush = new Image(getClass().getResourceAsStream("/bush.png"));
		imgChrt = new Image(getClass().getResourceAsStream("/christmas-tree.png"));

		//Not technically a sprite, but you get the idea
		playerSprite = setGameSpriteVals(playerSprite, imgRabbit);
		enemySprite = setGameSpriteVals(enemySprite, imgFox);
		carrotSprite = setGameSpriteVals(enemySprite, imgCarrot);

		//enable easter egg around Christmas time (20th Dec - 31st Dec)
		//1 in 100 chance to get a Christmas tree
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		int month = calendar.get(Calendar.MONTH);
		int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		if(month==11 && dayOfMonth>19)
			easterEggEnabled = true;
	}

	@FXML
	public void initialize() {
		difficultyChoice.getItems().add("Very Easy");
		difficultyChoice.getItems().add("Easy");
		difficultyChoice.getItems().add("Normal");
		difficultyChoice.getItems().add("Hard");
		difficultyChoice.getItems().add("Very Hard");
		difficultyChoice.getItems().add("Impossible");

		difficultyChoice.setValue("Normal");
	}

	public ImageView setGameSpriteVals(ImageView gameSprite, Image img){

		gameSprite = new ImageView(img);
		gameSprite.setFitHeight(20.0);
		gameSprite.setFitWidth(20.0);
		gameSprite.setPreserveRatio(true);
		//evtl. noch centern

		return gameSprite;
	}

	@FXML
	public void keyInput(KeyEvent key){

		if(gameIsOn){

			String code = String.valueOf(key.getCode());

			switch (code) {
			case "W":
				if (checkNewPosition(yourPosX, yourPosY-1)){
					yourPosY--;
					moveToNewPosition();
				}
				break;
			case "A":
				if (checkNewPosition(yourPosX-1, yourPosY)){
					yourPosX--;
					moveToNewPosition();
				}
				break;
			case "S":
				if (checkNewPosition(yourPosX, yourPosY+1)){
					yourPosY++;
					moveToNewPosition();
				}
				break;
			case "D":
				if (checkNewPosition(yourPosX+1, yourPosY)){
					yourPosX++;
					moveToNewPosition();
				}
				break;
			case "K":
				//remove plants around the rabbit
				if(punches>0){
					mAudioController.playSound("punch");
					int numOfTreesCut = 0;
					for(int a=-1; a<2; a++){
						for(int b=-1; b<2; b++){
							if(yourPosX+a > -1 && yourPosY+b > -1 &&
									yourPosX+a < columns && yourPosY+b < rows){
								trees[yourPosX+a][yourPosY+b] = false;
								if(treesImgViews[yourPosX+a][yourPosY+b] != null){
									GameGridPane.getChildren().remove(treesImgViews[yourPosX+a][yourPosY+b]);
									numOfTreesCut++;
								}

								//fox punch
								if(yourPosX+a == enemyPosX && yourPosY+b == enemyPosY){
									foxPunched += 10;
									foxPunches++;
								}
							}
						}
					}
					punches--;
					Platform.runLater(() -> punchesLabel.setText(String.valueOf(punches)));
					//bonus if player cuts a lot of trees at the same time
					if(numOfTreesCut>9)
						treeCombo9++;
					else if(numOfTreesCut>8)
						treeCombo8++;
					else if(numOfTreesCut>7)
						treeCombo7++;
					else if(numOfTreesCut>6)
						treeCombo6++;
				}
				break;
			default:
				//do nothing
			}
			key.consume();

		}
	}

	public boolean checkNewPosition(int posX, int posY){

		//border of the map
		if(posX<0 || posY<0 || posX>=rows || posY>=columns)
			return false;

		//trees blocking the way
		if (trees[posX][posY] == true)
			return false;

		return true;
	}

	public void moveToNewPosition(){

		Platform.runLater(new Runnable() {
			@Override public void run() {
				GameGridPane.getChildren().remove(playerSprite);
				GameGridPane.add(playerSprite, yourPosX, yourPosY);
			}
		});

		//manage Camper Bonus
		long elapsedTimeStand = System.nanoTime() - startStand;
		hundrethseconds = elapsedTimeStand / 6000000;
		if(hundrethseconds > hundrethsecondsMax)
			hundrethsecondsMax = hundrethseconds;
		startStand = System.nanoTime();
	}

	@FXML
	public void startGame(){

		startButton.setDisable(true);

		mAudioController = new AudioController();
		if(!audioIsOn)
			mAudioController.setAudio();

		gameIsOn = true;
		foxPunched = 0;
		punches = 3;
		treesFox = 0;
		foxPunches = 0;
		treeCombo6 = 0;
		treeCombo7 = 0;
		treeCombo8 = 0;
		treeCombo9 = 0;
		touchComboCurr = false;
		touchComboCurrNum = 0;
		touchComboMaxNum = 0;
		startStand = 1000000000;
		hundrethseconds = 0;
		hundrethsecondsMax = 0;
		newPlace = 11;
		score = 0;
		imgSource = "/rabbit.png";

		String diffStr = difficultyChoice.getValue();
		frameTime = 120;
		switch(diffStr) {
		case "Very Easy" :
			frameTime *= 3;
			break; // optional
		case "Easy" :
			frameTime *= 2;
			break;
		case "Hard" :
			frameTime *= 0.75;
			break;
		case "Very Hard" :
			frameTime *= 0.5;
			break;
		case "Impossible" :
			frameTime *= 0.25;
			break;
		default :
			// Normal
		}

		//Remove the GaussianBlur effect
		GaussianBlur gaussianBlur = new GaussianBlur();
		gaussianBlur.setRadius(0.0);
		GameGridPane.setEffect(gaussianBlur);
		nameField.setVisible(false);
		nameButton.setVisible(false);
		placeLabel.setVisible(false);

		//Count number of rows/columns
		Method method;
		try {
			method = GameGridPane.getClass().getDeclaredMethod("getNumberOfRows");
			method.setAccessible(true);
			rows = (Integer) method.invoke(GameGridPane);

			method = GameGridPane.getClass().getDeclaredMethod("getNumberOfColumns");
			method.setAccessible(true);
			columns = (Integer) method.invoke(GameGridPane);
		} catch (NoSuchMethodException e) {
			DialogBoxes.showErrorBox("Error", "\"Run away!\" has encountered a problem.", e.getMessage());
		} catch (SecurityException e) {
			DialogBoxes.showErrorBox("Error", "\"Run away!\" has encountered a problem.", e.getMessage());
		} catch (IllegalAccessException e) {
			DialogBoxes.showErrorBox("Error", "\"Run away!\" has encountered a problem.", e.getMessage());
		} catch (IllegalArgumentException e) {
			DialogBoxes.showErrorBox("Error", "\"Run away!\" has encountered a problem.", e.getMessage());
		} catch (InvocationTargetException e) {
			DialogBoxes.showErrorBox("Error", "\"Run away!\" has encountered a problem.", e.getMessage());
		}

		//remove Trees
		if(treesImgViews != null)
			for(int a=0; a<columns; a++)
				for(int b=0; b<rows; b++)
					if(treesImgViews[a][b] != null)
						GameGridPane.getChildren().remove(treesImgViews[a][b]);
		treesImgViews = new ImageView[columns][rows];

		//remove carrot of previous game
		carrotPosX=1000;
		carrotPosY=1000;

		//add player
		yourPosX = new Random().nextInt(rows);
		yourPosY = new Random().nextInt(columns);

		//add enemy (with some distance to the player)
		int difference = 0;
		while(difference>-5 && difference<5){
			enemyPosX = new Random().nextInt(rows);
			enemyPosY = new Random().nextInt(columns);
			difference = enemyPosX + enemyPosY - yourPosX - yourPosY;
		}

		//Perform these actions on the UI thread:
		Platform.runLater(new Runnable() {
			@Override public void run() {
				GameGridPane.getChildren().remove(carrotSprite);
				punchesLabel.setText(String.valueOf(punches));
				GameGridPane.getChildren().remove(playerSprite);
				GameGridPane.add(playerSprite, yourPosX, yourPosY);
				GameGridPane.getChildren().remove(enemySprite);
				GameGridPane.add(enemySprite, enemyPosX, enemyPosY);
			}
		});

		trees = new boolean[columns][rows];

		addPlants();
		startEnemy();
		startTime();
		mAudioController.startMusic();

		//For the camper bonus
		startStand = System.nanoTime();
	};

	public void addPlants(){

		Thread treeThread = new Thread() {
			public void run() {
				while(gameIsOn){
					ImageView newPlant = getRandomPlant();

					//check if position is taken
					int randPosX;
					int randPosY;

					randPosX = new Random().nextInt(rows);
					randPosY = new Random().nextInt(columns);

					while (trees[randPosX][randPosY] == true || carrotPosX==randPosX || carrotPosY==randPosY){

						randPosX = new Random().nextInt(rows);
						randPosY = new Random().nextInt(columns);
					}
					trees[randPosX][randPosY] = true;

					int finalRandPosX = randPosX;
					int finalRandPosY = randPosY;

					Platform.runLater(() -> GameGridPane.add(newPlant, finalRandPosX, finalRandPosY));
					treesImgViews[finalRandPosX][finalRandPosY] = newPlant;

					try {
						Thread.sleep(frameTime*2);
					} catch (InterruptedException e) {
						DialogBoxes.showErrorBox("Error", "\"Run away!\" has encountered a problem.", e.getMessage());
					}
				}
			}
		};
		treeThread.setDaemon(true);
		treeThread.start();
	}

	public ImageView getRandomPlant(){

		ImageView plantSprite = new ImageView();

		//check date 24-31 dec
		//check if 1 in 100
		//return chrt
		if(easterEggEnabled){
			if((new Random().nextInt(100)) == 0)
				return setGameSpriteVals(plantSprite, imgChrt);
		}

		int randPlantInt = new Random().nextInt(5);

		if(randPlantInt == 0)//0
			return setGameSpriteVals(plantSprite, imgBush);
		else if(randPlantInt < 3)//1,2
			return setGameSpriteVals(plantSprite, imgTree);
		else //3,4
			return setGameSpriteVals(plantSprite, imgTrees);
	}

	public void startEnemy(){
		Thread enemyThread = new Thread() {
			public void run() {
				while(gameIsOn){

					if(enemyPosX==yourPosX && enemyPosY==yourPosY){
						gameIsOn=false;
						mAudioController.playSound("eat_rabbit");
						Platform.runLater(() -> GameGridPane.getChildren().remove(playerSprite));
						mAudioController.stopMusic();
						startButton.setDisable(false);
					}

					if(foxPunched > 0){
						if(foxPunched > 2){
							if(enemyPosX>yourPosX)
								enemyPosX++;
							else if(enemyPosX<yourPosX)
								enemyPosX--;

							if(enemyPosY>yourPosY)
								enemyPosY++;
							else if(enemyPosY<yourPosY)
								enemyPosY--;
						}
						foxPunched--;
					}
					else{
						if(enemyPosX>yourPosX)
							enemyPosX--;
						else if(enemyPosX<yourPosX)
							enemyPosX++;

						if(enemyPosY>yourPosY)
							enemyPosY--;
						else if(enemyPosY<yourPosY)
							enemyPosY++;
					}

					if(enemyPosX>-1 && enemyPosX<columns && enemyPosY>-1 && enemyPosY<rows){

						Platform.runLater(new Runnable() {
							@Override public void run() {
								GameGridPane.getChildren().remove(enemySprite);
								GameGridPane.add(enemySprite, enemyPosX, enemyPosY);
							}
						});

						if(treesImgViews[enemyPosX][enemyPosY] != null){
							int bushSound = new Random().nextInt(3);
							if(bushSound == 0)
								mAudioController.playSound("bush1");
							else if(bushSound == 0)
								mAudioController.playSound("bush2");
							else
								mAudioController.playSound("bush3");

							Platform.runLater(() -> GameGridPane.getChildren().remove(treesImgViews[enemyPosX][enemyPosY]));
							trees[enemyPosX][enemyPosY] = false;
							treesFox++;
						}
					}

					try {
						Thread.sleep(frameTime*3);
					} catch (InterruptedException e) {
						DialogBoxes.showErrorBox("Error", "\"Run away!\" has encountered a problem.", e.getMessage());
					}
				}
			}
		};
		enemyThread.setDaemon(true);
		enemyThread.start();
	}

	public void startTime(){

		long start = System.nanoTime();
		DecimalFormat timeFormat = new DecimalFormat("##.000");

		Thread timeThread = new Thread() {
			public void run() {
				long elapsedTime = 0;

				while(gameIsOn){

					//Detect Touch Combo
					if(enemyPosX==yourPosX && enemyPosY==yourPosY){
						if(touchComboCurr){
							touchComboCurrNum++;
							if(touchComboCurrNum > touchComboMaxNum)
								touchComboMaxNum = touchComboCurrNum;
						}
						touchComboCurr = true;
					}
					else {
						touchComboCurr = false;
						touchComboCurrNum = 0;
					}

					elapsedTime = System.nanoTime() - start;
					double time = elapsedTime / 600000000.0;
					int seconds = (int)time;

					if(seconds % 30 == 0 && !carrotPlaced && seconds>0)
						placeCarrot();
					if(seconds % 30 == 1)
						carrotPlaced = false;
					if(carrotPosX == yourPosX && carrotPosY == yourPosY){
						mAudioController.playSound("eat_carrot");
						punches++;
						carrotPosX = 1000;
						carrotPosY = 1000;
						Platform.runLater(new Runnable() {
							@Override public void run() {
								GameGridPane.getChildren().remove(carrotSprite);
								punchesLabel.setText(String.valueOf(punches));
							}
						});
					}

					Platform.runLater(() -> timeLabel.setText(timeFormat.format(time)));

					try {
						Thread.sleep(23);
					} catch (InterruptedException e) {
						DialogBoxes.showErrorBox("Error", "\"Run away!\" has encountered a problem.", e.getMessage());
					}
				}
				calculateHighScore(timeFormat, elapsedTime);
			}
		};
		timeThread.setDaemon(true);
		timeThread.start();
	}

	public void placeCarrot(){

		carrotPlaced = true;
		carrotPosX = new Random().nextInt(rows);
		carrotPosY = new Random().nextInt(columns);

		int difference = 0;

		//place carrot on field without tree and with a distance to the player
		while (trees[carrotPosX][carrotPosY] == true || difference>-5 && difference<5){
			carrotPosX = new Random().nextInt(rows);
			carrotPosY = new Random().nextInt(columns);
			difference = carrotPosX + carrotPosY - yourPosX - yourPosY;
		}

		int finalRandPosX = carrotPosX;
		int finalRandPosY = carrotPosY;

		Platform.runLater(new Runnable() {
			@Override public void run() {
				GameGridPane.getChildren().remove(carrotSprite);
				GameGridPane.add(carrotSprite, finalRandPosX, finalRandPosY);
			}
		});
	}

	public void calculateHighScore(DecimalFormat timeFormat, long elapsedTime){

		score = 0;

		//add 10ths of a second to the score
		int timeScore = (int)(elapsedTime / 60000000.0);
		int minutes = timeScore/600;

		//Account for difficulty
		switch((int)frameTime) {
		case 360 :
			timeScore /= 9;
			break;
		case 240 :
			timeScore /= 4;
			break;
		case 90 :
			timeScore /= 0.5;
			break;
		case 60 :
			timeScore /= 0.25;
			break;
		case 30 :
			timeScore /= 0.1;
			break;
		default :
			// Normal (120)
		}

		score += timeScore;
		score += (minutes * 10);
		score += (punches * 50);
		score += treesFox;
		score += (foxPunches * 30);

		score += (treeCombo6 * 20);
		score += (treeCombo7 * 50);
		score += (treeCombo8 * 100);
		score += (treeCombo9 * 200);

		double seconds = (elapsedTime / 600000000.0);
		scoreText = "Time:  " + timeFormat.format(seconds) + " seconds   ("+timeScore+")";
		if(minutes > 0)
			scoreText += "\nMinutes:  " + minutes + "   (+" + minutes + " x 10)";
		else if(minutes == 0){
			score=score-500;
			scoreText += "\n<1 minute:     (-500)";
		}
		scoreText += "\n\n";

		//If player is close to/on the same spot as the fox for a long amount of time
		//Only combos bigger than 15 count
		//e.g. combo=7: 7+6+5+4+3+2+1 score
		if(touchComboMaxNum > 15){
			long touchComboScore = comboCalculation (touchComboMaxNum);
			score += touchComboScore;

			scoreText += "Touch Combo:  " + touchComboMaxNum + "   (+" + touchComboScore + ")\n";
		}

		//Camper Bunus
		//Give score points if the player stands for at least 4 seconds (and survives)
		if(hundrethsecondsMax > 400){
			double cSeconds = hundrethsecondsMax / 100.0;
			long camperBonus = (long)(1.5 * (comboCalculation ((int)cSeconds)));
			scoreText += "Camper:  " + timeFormat.format(cSeconds) + " seconds   (+" + camperBonus + ")\n";
			score += camperBonus;
		}

		if(punches > 0)
			scoreText += "Punches left:  " + punches + "   (+" + punches + " x 50)\n";

		//Bonus if you cut down at least a tree per second (fox)
		if(treesFox > seconds){
			scoreText += "Lumberjack   (+ 120)\n";
			score += 120;
		}
		//Bonus if the player cuts down less than one tree per 30 seconds
		if(treesFox < (seconds/30) && seconds>60){
			scoreText += "Environmentalist   (+ 120)\n";
			score += 120;
		}

		//au�: 1 point/ tree cut by fox
		if(treesFox > 0)
			scoreText += "Trees cut down (fox):  " + treesFox + "   (+" + treesFox + " x 1)\n";

		if(foxPunches > 0)
			scoreText += "Fox punches:  " + foxPunches + "   (+" + foxPunches + " x 30)\n";

		//How many trees were cut down at the same time (by rabbit)
		if(treeCombo6 > 0)
			scoreText += "Tree combo (6):  " + treeCombo6 + "   (+" + treeCombo6 + " x 20)\n";
		if(treeCombo7 > 0)
			scoreText += "Tree combo (7):  " + treeCombo7 + "   (+" + treeCombo7 + " x 50)\n";
		if(treeCombo8 > 0)
			scoreText += "Tree combo (8):  " + treeCombo8 + "   (+" + treeCombo8 + " x 100)\n";
		if(treeCombo9 > 0)
			scoreText += "Tree combo (9):  " + treeCombo9 + "   (+" + treeCombo9 + " x 200)\n";

		if(score<0)
			score=0;

		scoreText += "\n\n";

		imgSource = "/rabbit.png";
		if(score < 2000)
			imgSource = "/fox.png";

		manageHighscore(score, seconds, punches, imgSource);
	}

	public void manageHighscore(long score, double time, int punchesLeft, String imgSource){

		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		Date datetext = calendar.getTime();

		Scanner in;
		try {

			//Only compare with other scores at 'normal' or higher level, since the easier ones aren't really comparable
			if(frameTime <= 120){

				in = new Scanner(new FileReader("score/highscore.csv"));
				StringBuilder sb = new StringBuilder();
				while(in.hasNext()) {
					sb.append(in.next());
				}
				in.close();
				String highscoreString = sb.toString();
				String outString = AES.decrypt(highscoreString, secretKey) ;

				//read & check HS file
				int newPlaceAchieved=0;
				newPlace=11;
				HSlines = outString.split("\\|");
				for(int i=1; i<11; i++){
					String[] HSentries = HSlines[i].split(";");
					for(int j=0; j<5; j++){
						if(j==1){
							//higher score achieved:
							if(Long.parseLong(HSentries[j]) < score && newPlaceAchieved==0){
								HSlines[i-1] = "username"+";"+score+";"+time+";"+punchesLeft+";"+datetext+";"+scoreText;
								newPlaceAchieved=1;
								newPlace=i;
							}
							else{
								HSlines[i-1] = HSlines[i-newPlaceAchieved];
							}
						}
					}
				}

				//Add the GaussianBlur Effect
				GaussianBlur gaussianBlur = new GaussianBlur();
				gaussianBlur.setRadius(7.5);
				GameGridPane.setEffect(gaussianBlur);

				//write to HS file
				if(newPlaceAchieved==1){
					nameField.setVisible(true);
					nameButton.setVisible(true);
					Platform.runLater(() -> placeLabel.setText("Place "+newPlace+" achieved!"));
					placeLabel.setVisible(true);
					//continues in 'enterName()' (FXML onclick) and 'processName()'
				}
				else //No new place:
					DialogBoxes.showMessageBox("Game Over", "", scoreText + "Score: " + score, imgSource);
			}

		} catch (FileNotFoundException e) {
			DialogBoxes.showErrorBox("Error", "\"Run away!\" has encountered a problem.", e.getMessage());
		}
	}

	@FXML
	public void showHighscore(){
		HighscoreManager mHighscoreManager = new HighscoreManager();
		mHighscoreManager.openScoreview();
	}

	public static long comboCalculation (int n)
	{
		return n == 0 ? 1 : n + comboCalculation (n-1);
	}

	@FXML
	public void setAudio(){
		mAudioController.setAudio();
		Image audioImg;
		if(audioIsOn){
			audioImg = new Image(getClass().getResourceAsStream("/mute.png"));
		}
		else
			audioImg = new Image(getClass().getResourceAsStream("/speaker.png"));

		audioIsOn = !audioIsOn;

		audioButton.setImage(audioImg);
	}

	@FXML
	public void instructions(){
		DialogBoxes.showMessageBox("Instructions", "Start the game, run away from the fox and try to catch some carrots!\n\nPlay at least at the 'normal' difficulty level to compete for a new high score!", "\nControls:\n\u2192 w,a,s,d - move\n\u2192 k - kick", "none");
	}

	@FXML
	public void about(){
		String MITlicense = "Copyright 2018 GerH.\nPermission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation\n files (the \"Software\"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, \nmerge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom\n the Software is furnished to do so, subject to the following conditions:\nThe above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.\nTHE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT\n LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. \nIN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, \nDAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR \nIN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.";

		DialogBoxes.showMessageBox("About", MITlicense, "Developer Github: gh28942\nSound effects obtained from www.zapsplat.com\nMusic: \"Lips\" by Plurabelle (2012, freemusicarchive.org), http://freemusicarchive.org/music/Plurabelle/Money_Blood_and_Light/Lips\nIcons made by Freepik, Nikita Golubev (Bush), Twitter (Christmas tree) and Smashicons (mute/speaker) from www.flaticon.com", "none");
	}

	@FXML
	public void enterName(){
		String name = nameField.getText();

		if(name.equals("username") || name.equals("") || name.contains(";") || name.contains("|")) {
			nameField.setText("username");
			DialogBoxes.showMessageBox("Name required", "Please enter a valid username!", "", "none");
		}else{
			nameField.setVisible(false);
			nameButton.setVisible(false);
			processName(name);
		}
	}

	public void processName(String scoreName){
		try {
			scoreText = scoreText+"Place "+newPlace+" achieved!\n\n";

			//insert name into score data
			int currentPlace=newPlace-1;
			long localScore = Long.parseLong((HSlines[currentPlace].split(";"))[1]);
			HSlines[currentPlace] = HSlines[currentPlace].replace("username", scoreName);

			String highscoreString = "a;1;1.0;0;a|";
			for(int i=0; i<10; i++){
				highscoreString+=(HSlines[i]+"|");
			}
			String highscoreStringEncr = AES.encrypt(highscoreString, secretKey);
			PrintWriter writer;
			writer = new PrintWriter("score/highscore.csv", "UTF-8");
			writer.println(highscoreStringEncr);
			writer.close();

			//show Highscore
			HighscoreManager mHighscoreManager = new HighscoreManager();
			mHighscoreManager.openScoreview();

			DialogBoxes.showMessageBox("Game Over", "", scoreText + "Score: " + localScore, imgSource);

		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
			DialogBoxes.showErrorBox("Error", "\"Run away!\" has encountered a problem.", e.getMessage());
		}
	}
}
