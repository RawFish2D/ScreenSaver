package ua.rawfish2d.screensaver;


import ua.rawfish2d.visuallib.utils.GLSM;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_UNSIGNED_INT_8_8_8_8_REV;

public class Utils {
	public static int createTexture(BufferedImage bufferedImage, boolean pixelated) {
		final ByteBuffer buffer = ByteBuffer.allocateDirect(bufferedImage.getWidth() * bufferedImage.getHeight() * 4);

		for (int y = 0; y < bufferedImage.getHeight(); ++y) {
			for (int x = 0; x < bufferedImage.getWidth(); ++x) {
				int pixel = bufferedImage.getRGB(x, y);

				buffer.put((byte) ((pixel >> 16) & 0xFF));
				buffer.put((byte) ((pixel >> 8) & 0xFF));
				buffer.put((byte) (pixel & 0xFF));
				buffer.put((byte) ((pixel >> 24) & 0xFF));
			}
		}

		buffer.flip();

		int textureID = GLSM.instance.glGenTextures();
		GLSM.instance.glBindTexture(textureID);

		//System.out.printf("texture created width %d height %d ID %d%n", bufferedImage.getWidth(), bufferedImage.getHeight(), textureID);

		if (pixelated) {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		} else {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		}

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

		// GL11.GL_RGBA8 sucks ass btw
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, bufferedImage.getWidth(), bufferedImage.getHeight(), 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, buffer);

		buffer.clear();

		return textureID;
	}

	public static Set<String> listFilesUsingJavaIO(String dir) {
		return Stream.of(new File(dir).listFiles())
				.filter(file -> !file.isDirectory())
				.map(File::getName)
				.collect(Collectors.toSet());
	}

	public static InputStream getInputStream(String fileName) {
		ClassLoader classLoader = Utils.class.getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(fileName);

		if (inputStream == null) {
			throw new IllegalArgumentException("File not found: " + fileName);
		} else {
			return inputStream;
		}
	}
}
