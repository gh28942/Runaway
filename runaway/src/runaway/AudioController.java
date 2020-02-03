package runaway;

import java.net.URL;
import java.util.concurrent.locks.LockSupport;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Port;
import javax.sound.sampled.Port.Info;

import javazoom.jl.player.Player;

public class AudioController {

	Thread playerThread = new Thread();
	Thread musicThread = new Thread();

	float audioValue = 0.20F;
	FloatControl volumeControl;
	private boolean gameIsOn = true;

	public  void stopMusic(){
		gameIsOn = false;
	}

	public AudioController(){

		try {
			Info source = Port.Info.SPEAKER;
		Port outline = (Port) AudioSystem.getLine(source);
		outline.open();
		volumeControl = (FloatControl) outline.getControl(FloatControl.Type.VOLUME);
		volumeControl.setValue(audioValue);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	public void playSound(String soundName) {
		playerThread = new Thread() {
			public void run() {
				try {
					
					final URL url = AudioController.class.getResource("/"+soundName+".mp3");
					final Player player = new Player(url.openStream());

					// play, stop if closed
					player.play();
				} catch (Exception e) {
					DialogBoxes.showErrorBox("Sound Error", "\"Run away!\" has encountered a problem.", e.getMessage());
				}
			}
		};
		playerThread.setDaemon(true);
		playerThread.start();
	}


	public  void startMusic() {
		musicThread = new Thread() {
			public void run() {
				try {

					final URL url = AudioController.class.getResource("/Plurabelle_-_01_-_Lips.mp3");
					final Player player = new Player(url.openStream());

					// play, stop if game stopped
					while (player.play(1)) {
						if (!gameIsOn) {
							LockSupport.park();
						}
					}
					// play repeatedly
					if (!player.play(1)) {
						player.close();
						startMusic();
					}
				} catch (Exception e) {
					DialogBoxes.showErrorBox("Music Error", "\"Run away!\" has encountered a problem.", e.getMessage());
				}
			}
		};
		musicThread.setDaemon(true);
		musicThread.start();
	}

	public  void setAudio(){
		if(audioValue == 0.20F)
			audioValue = 0.0F;
		else
			audioValue = 0.2F;

		volumeControl.setValue(audioValue);
	}
}
