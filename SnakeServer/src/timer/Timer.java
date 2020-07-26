package timer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Timer {
	
	// List of all ongoing timers.
	private static List<Timer> timers = Collections.synchronizedList(new ArrayList<Timer>());

	private TimerInterface timerInterface;
	private int timerLengthMs;
	private String timerMessage;
	private int startTime;
	
	private Timer(TimerInterface timerInterface, int timerLengthMs, String timerMessage) {
		
		this.timerInterface = timerInterface;
		this.timerLengthMs = timerLengthMs;
		this.timerMessage = timerMessage;
		
		this.startTime = (int) System.currentTimeMillis();
	}
	
	// The timer cannot be restarted after this method has been called
	public void stop() {
		
		timers.remove(this);
	}
	
	// Returns how much time is left on the timer
	public int getRemainingTime() {
		
		return ((int) System.currentTimeMillis()) - startTime;
	}
	
	public String getMessage() {
		
		return timerMessage;
	}

	// Starts a timer that notifies the TimerInterface when it's done.
	public static Timer startTimer(TimerInterface _timerInterface, int _timerLengthMs, String _timerMessage) {
		
		Timer timer = new Timer(_timerInterface, _timerLengthMs, _timerMessage);
		timers.add(timer);
		return timer;
	}
	
	public static Timer startTimer(TimerInterface timerInterface, int _timerLengthMs) {
		
		return startTimer(timerInterface, _timerLengthMs, "");
	}
	
	// Updates the timers
	public static void updateTimers() {
		
		ArrayList<Timer> endedTimers = new ArrayList<Timer>();
		
		for(int i = 0; i < timers.size(); i++) {
			
			Timer timer = timers.get(i);
			
			if(((int) System.currentTimeMillis()) - timer.startTime > timer.timerLengthMs) {
				timer.timerInterface.timerEnded(timer);
				endedTimers.add(timer);
			}
		}
		for(Timer timer : endedTimers) {
			timers.remove(timer);
		}
	}
	
	// Stops all timers
	public static void stopAllTimers() {
		
		while(timers.size() > 0) {
			timers.get(0).stop();
		}
	}
}
