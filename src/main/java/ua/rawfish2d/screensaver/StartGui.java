package ua.rawfish2d.screensaver;

import com.beust.jcommander.JCommander;
import lombok.Getter;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.*;

public class StartGui {

	public static void main(String[] args) {
		if (args.length == 0) {
			StartGui gui = new StartGui();
			gui.run(args);
		} else {
			Config config = new Config();
			JCommander.newBuilder()
					.addObject(config)
					.build()
					.parse(args);

			start(config);
		}
	}

	private JPanel panel;
	private JButton start;
	private JCheckBox transparentBackground;
	private boolean transparentBackgroundValue = true;
	private JCheckBox debugFramebuffer;
	private boolean debugFramebufferValue = false;
	private JCheckBox fullscreenWindowed;
	private boolean fullscreenWindowedValue = true;
	private JCheckBox fullScreen;
	private boolean fullScreenValue = false;
	private JComboBox comboBox;
	private JFrame frame;

	public void run(String[] args) {
		frame = new JFrame();

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				frame.dispose();
			}
		});

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(250, 250);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);

		panel = new JPanel();
		start = new JButton("Start");
		transparentBackground = new JCheckBox("Transparent Background", transparentBackgroundValue);
		debugFramebuffer = new JCheckBox("Debug", false);
		fullscreenWindowed = new JCheckBox("Windowed full screen", fullscreenWindowedValue);
		fullScreen = new JCheckBox("FullScreen", fullScreenValue);
		Resolution[] array = Objects.requireNonNull(getSupportedResolutions()).toArray(new Resolution[0]);
		comboBox = new JComboBox(array);

		panel.setLayout(null);

		int y = 4;
		transparentBackground.setBounds(4, 4, 200, 20);
		debugFramebuffer.setBounds(4, y += 20, 200, 20);
		fullscreenWindowed.setBounds(4, y += 20, 200, 20);
		fullScreen.setBounds(4, y += 20, 200, 20);
		comboBox.setBounds(4, y += 20, 200, 20);

		start.setBounds(4, 170, 200, 30);

		start.addActionListener(e -> {
			frame.dispose();

			Config config = new Config();
			JCommander.newBuilder()
					.addObject(config)
					.build()
					.parse(args);

			Resolution resolution = (Resolution) comboBox.getSelectedItem();

			config.setTransparentFramebuffer(transparentBackgroundValue);
			config.setFullWindowedScreen(fullscreenWindowedValue);
			config.setFullScreen(fullScreenValue);
			config.setDebug(debugFramebufferValue);
			assert resolution != null;
			config.setWidth(resolution.getWidth());
			config.setHeight(resolution.getHeight());

			start(config);
		});
		transparentBackground.addItemListener(e -> transparentBackgroundValue = e.getStateChange() == 1);
		fullscreenWindowed.addItemListener(e -> fullscreenWindowedValue = e.getStateChange() == 1);
		debugFramebuffer.addItemListener(e -> debugFramebufferValue = e.getStateChange() == 1);
		fullScreen.addItemListener(e -> fullScreenValue = e.getStateChange() == 1);

		panel.add(start);
		panel.add(transparentBackground);
		panel.add(fullscreenWindowed);
		panel.add(fullScreen);
		panel.add(debugFramebuffer);
		panel.add(comboBox);

		frame.getContentPane().add(panel);
		frame.setVisible(true);
	}

	public static List<Resolution> getSupportedResolutions() {
		if (!glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		PointerBuffer monitors = GLFW.glfwGetMonitors();

		if (monitors == null) {
			glfwTerminate();
			return null;
		}

		LinkedList<Resolution> resolutionList = new LinkedList<>();

		for (int i = 0; i < monitors.limit(); i++) {
			long monitor = monitors.get(i);

			GLFWVidMode.Buffer modes = GLFW.glfwGetVideoModes(monitor);
			if (modes == null) {
				System.out.println("modes == null");
				continue;
			}

			int modeCount = modes.limit();

			for (int j = 0; j < modeCount; j++) {
				modes.position(j);

				int width = modes.width();
				int height = modes.height();
				int rate = modes.refreshRate();

				Set<Resolution> set = resolutionList.stream().filter(res -> res.width == width && res.height == height).collect(Collectors.toSet());
				if (!set.isEmpty()) {
					set.forEach(res -> res.refreshRates.add(rate));
				} else {
					resolutionList.add(new Resolution(width, height, rate));
				}
			}
		}

		glfwTerminate();
		List<Resolution> reversedCopy = resolutionList.subList(0, resolutionList.size());
		Collections.reverse(reversedCopy);
		//reversedCopy.forEach(res -> System.out.println("Resolution: " + res.width + " x " + res.height + " @ " + res.getMaxRate()));
		return reversedCopy;
	}

	public static void start(Config config) {
		Thread newThread = new Thread(() -> {
			getSupportedResolutions();
			Config.instance = config;

			int width = config.getWidth();
			int height = config.getHeight();
			boolean transparentFramebuffer = config.isTransparentFramebuffer();
			boolean debug = config.isDebug();
			if (config.isFullWindowedScreen()) {
				if (!glfwInit()) {
					throw new IllegalStateException("Unable to initialize GLFW");
				}
				long monitor = glfwGetPrimaryMonitor();
				GLFWVidMode mode = glfwGetVideoMode(monitor);
				if (mode != null) {
					width = mode.width();
					height = mode.height();
				} else {
					System.out.println("Unable to get video mode RIP");
				}
				glfwTerminate();
			}
			new Render(width, height, transparentFramebuffer, debug);
		}, "main thread");
		newThread.start();
	}

	public static class Resolution {
		@Getter
		int width;
		@Getter
		int height;
		@Getter
		List<Integer> refreshRates = new ArrayList<>();

		public Resolution(int width, int height, int rate) {
			this.width = width;
			this.height = height;
			this.refreshRates.add(rate);
		}

		public int getMaxRate() {
			OptionalInt optional = refreshRates.stream()
					.mapToInt(v -> v)
					.max();
			if (optional.isPresent()) {
				return optional.getAsInt();
			} else {
				return 60;
			}
		}

		@Override
		public String toString() {
			return width + " x " + height + " @ " + getMaxRate() + " HZ";
		}
	}
}