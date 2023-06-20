package ua.rawfish2d.screensaver;

import com.beust.jcommander.Parameter;
import lombok.Getter;
import lombok.Setter;

public class Config {
	@Getter
	@Setter
	@Parameter(names = "-width", description = "screen width")
	private int width = 800;
	@Getter
	@Setter
	@Parameter(names = "-height", description = "screen height")
	private int height = 600;
	@Getter
	@Setter
	@Parameter(names = "-fullWindowedScreen", description = "full windowed screen", arity = 1)
	private boolean fullWindowedScreen = true;
	@Getter
	@Setter
	@Parameter(names = "-fullScreen", description = "full screen", arity = 1)
	private boolean fullScreen = false;
	@Getter
	@Setter
	@Parameter(names = "-transparentFramebuffer", description = "enable transparent framebuffer", arity = 1)
	private boolean transparentFramebuffer = true;
	@Getter
	@Setter
	@Parameter(names = "-debug", description = "debug", arity = 1)
	private boolean debug = false;
	@Getter
	@Setter
	@Parameter(names = "-speed", description = "animation speed")
	private int speed = 1;
	@Getter
	@Setter
	@Parameter(names = "-maxSpriteSize", description = "maximum sprite size")
	private int maxSpriteSize = 256;

	public static Config instance = new Config();
}
