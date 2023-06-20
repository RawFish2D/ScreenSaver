package ua.rawfish2d.screensaver;

import lombok.Getter;
import ua.rawfish2d.visuallib.utils.TimeHelper;

public class FPSCounter {
	private int counter = 0;
	@Getter
	private int fps = 0;
	private final TimeHelper timeHelper = new TimeHelper();

	public void onRender() {
		counter++;
		if (timeHelper.hasReachedMilli(1000)) {
			timeHelper.reset();
			fps = counter;
			counter = 0;
		}
	}
}
