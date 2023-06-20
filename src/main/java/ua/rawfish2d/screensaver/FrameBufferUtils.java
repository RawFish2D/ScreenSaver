package ua.rawfish2d.screensaver;

import org.lwjgl.opengl.GL30;
import ua.rawfish2d.visuallib.framebuffer.FrameBuffer;
import ua.rawfish2d.visuallib.utils.GLSM;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11C.GL_RGBA;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11C.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11C.glGetIntegerv;
import static org.lwjgl.opengl.GL11C.glReadBuffer;
import static org.lwjgl.opengl.GL11C.glReadPixels;
import static org.lwjgl.opengl.GL11C.glViewport;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30C.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30C.GL_READ_FRAMEBUFFER;

public class FrameBufferUtils {
	public static void saveFrameBuffer(String fileName, FrameBuffer fbo) {
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		final GLSM glsm = GLSM.instance;

		final IntBuffer oldViewport = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
		glGetIntegerv(GL_VIEWPORT, oldViewport);
		final int viewPortX = oldViewport.get(0);
		final int viewPortY = oldViewport.get(1);
		final int viewPortW = oldViewport.get(2);
		final int viewPortH = oldViewport.get(3);
		glViewport(0, 0, fbo.getWidth(), fbo.getHeight());

		ByteBuffer buffer;
		BufferedImage image;

		final int bufferSize = (fbo.getWidth() * fbo.getHeight() * 4);
		buffer = ByteBuffer.allocateDirect(bufferSize);
		glBindFramebuffer(GL_READ_FRAMEBUFFER, fbo.getFramebufferID());
		glReadBuffer(GL_COLOR_ATTACHMENT0);
		glReadPixels(0, 0, fbo.getWidth(), fbo.getHeight(), GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);

		image = new BufferedImage(fbo.getWidth(), fbo.getHeight(), BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < fbo.getHeight(); ++y) {
			for (int x = 0; x < fbo.getWidth(); ++x) {
				int blue = (((int) buffer.get()) & 255) << 16;
				int green = (((int) buffer.get()) & 255) << 8;
				int red = (((int) buffer.get()) & 255) << 0;
				int alpha = (((int) buffer.get()) & 255) << 24;

				int newPixel = blue | green | red | alpha;
				image.setRGB(x, fbo.getHeight() - y - 1, newPixel);
			}
		}

		//ExecutorService es = Executors.newSingleThreadExecutor();
		//es.execute(() -> {
		File outputfile = new File(fileName);
		try {
			ImageIO.write(image, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//es.shutdown();
		//});
		buffer.clear();
		glsm.glBindTexture(0);

		// setting old viewport
		glViewport(viewPortX, viewPortY, viewPortW, viewPortH);
	}

	// not tested btw
	public static void saveTexture(String fileName, int texture, int width, int height) {
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		final GLSM glsm = GLSM.instance;

		final IntBuffer oldViewport = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
		glGetIntegerv(GL_VIEWPORT, oldViewport);
		final int viewPortX = oldViewport.get(0);
		final int viewPortY = oldViewport.get(1);
		final int viewPortW = oldViewport.get(2);
		final int viewPortH = oldViewport.get(3);
		glViewport(0, 0, width, height);

		ByteBuffer buffer;
		BufferedImage image;

		final int bufferSize = (width * height * 4);
		buffer = ByteBuffer.allocateDirect(bufferSize);

		// create framebuffer
		int frameBufferID = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, frameBufferID);

		glBindTexture(GL_TEXTURE_2D, texture);
		// attach texture to the fbo
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);
		glBindTexture(GL_TEXTURE_2D, 0);

		glReadBuffer(GL_COLOR_ATTACHMENT0);
		glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
		glDeleteFramebuffers(frameBufferID);

		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				int blue = (((int) buffer.get()) & 255) << 16;
				int green = (((int) buffer.get()) & 255) << 8;
				int red = (((int) buffer.get()) & 255) << 0;
				int alpha = (((int) buffer.get()) & 255) << 24;

				int newPixel = blue | green | red | alpha;
				image.setRGB(x, height - y - 1, newPixel);
			}
		}


		//ExecutorService es = Executors.newSingleThreadExecutor();
		//es.execute(() -> {
		File outputfile = new File(fileName);
		try {
			ImageIO.write(image, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//es.shutdown();
		//});
		buffer.clear();
		glsm.glBindTexture(0);

		// setting old viewport
		glViewport(viewPortX, viewPortY, viewPortW, viewPortH);
	}

//	public static void saveTexture(String fileName, FrameBuffer fbo) {
//		ByteBuffer buffer;
//		BufferedImage image;
//
//		final int bufferSize = (fbo.getWidth() * fbo.getHeight() * 4);
//		buffer = ByteBuffer.allocateDirect(bufferSize);
//		glBindFramebuffer(GL_READ_FRAMEBUFFER, fbo.getFramebufferID());
//		glReadBuffer(GL_COLOR_ATTACHMENT0);
//		glReadPixels(0, 0, fbo.getWidth(), fbo.getHeight(), GL_RGBA, GL_UNSIGNED_BYTE, buffer);
//		glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
//
//		image = new BufferedImage(fbo.getWidth(), fbo.getHeight(), BufferedImage.TYPE_INT_ARGB);
//
//		for (int y = 0; y < fbo.getHeight(); ++y) {
//			for (int x = 0; x < fbo.getWidth(); ++x) {
//				int blue = (((int) buffer.get()) & 255) << 16;
//				int green = (((int) buffer.get()) & 255) << 8;
//				int red = (((int) buffer.get()) & 255) << 0;
//				int alpha = (((int) buffer.get()) & 255) << 24;
//
//				int newPixel = blue | green | red | alpha;
//				image.setRGB(x, y, newPixel);
//			}
//		}
//
//		//ExecutorService es = Executors.newSingleThreadExecutor();
//		//es.execute(() -> {
//		File outputfile = new File(fileName);
//		try {
//			ImageIO.write(image, "png", outputfile);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		//es.shutdown();
//		//});
//		buffer.clear();
//	}
}
