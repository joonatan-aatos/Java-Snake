package visualizer;

import java.util.ArrayList;
import java.util.Arrays;

import timer.Timer;
import timer.TimerInterface;

public class AnimatedString implements TimerInterface {
	
	private ArrayList<String> strings;
	private boolean isOn = false;
	private int switchDelayMs;
	private String currentString;
	private int currentStringIndex;
	
	public AnimatedString(String[] strings, int delayMs) {
		
		this.strings = new ArrayList<String>(Arrays.asList(strings));
		currentString = this.strings.get(0);
		currentStringIndex = 0;
		
		this.switchDelayMs = delayMs;
	}
	
	public String getString() {
		return currentString;
	}
	
	public boolean isOn() {
		return isOn;
	}
	
	public void turnOn() {
		isOn = true;
		Timer.startTimer(this, switchDelayMs);
	}
	
	public void turnOff() {
		isOn = false;
	}

	@Override
	public void timerEnded(Timer t) {
		
		currentStringIndex++;
		
		if(currentStringIndex >= strings.size()) 
			currentStringIndex = 0;
		
		currentString = strings.get(currentStringIndex);
		
		if(this.isOn)
			Timer.startTimer(this, switchDelayMs);
		else {
			currentStringIndex = 0;
			currentString = strings.get(0);
		}
	}
	
}
