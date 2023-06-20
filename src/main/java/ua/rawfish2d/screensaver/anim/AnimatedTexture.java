package ua.rawfish2d.screensaver.anim;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import ua.rawfish2d.screensaver.Config;
import ua.rawfish2d.screensaver.Context;
import ua.rawfish2d.screensaver.FrameBufferUtils;
import ua.rawfish2d.visuallib.framebuffer.FrameBuffer;
import ua.rawfish2d.visuallib.utils.GLSM;
import ua.rawfish2d.visuallib.utils.MathUtils;
import ua.rawfish2d.visuallib.utils.RenderBuffer;
import ua.rawfish2d.visuallib.utils.RenderContext;
import ua.rawfish2d.visuallib.vertexbuffer.ShaderProgram;
import ua.rawfish2d.visuallib.vertexbuffer.VertexBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.LinkedList;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.glUseProgram;

public class AnimatedTexture {
	private final int textureWidth;
	private final int textureHeight;
	private int spriteWidth;
	private int spriteHeight;
	private final int colCount;
	private final int rowCount;
	private int currentCol;
	private int currentRow;
	private final LinkedList<Sprite> sprites = new LinkedList<>();
	private final FrameBuffer frameBuffer;
	private final RenderContext renderContext;
	@Getter
	private final int spriteCount;

	public AnimatedTexture(int spriteCount, int w, int h) {
		this.spriteCount = spriteCount;
		this.spriteWidth = w;
		this.spriteHeight = h;

		final int maxSize = Config.instance.getMaxSpriteSize();
		int max = Math.max(spriteWidth, spriteHeight);
		if (max > maxSize) {
			float scaleW = (float) maxSize / spriteWidth;
			float scaleH = (float) maxSize / spriteHeight;
			spriteWidth = (int) (spriteWidth * scaleW);
			spriteHeight = (int) (spriteHeight * scaleH);
		}

		renderContext = Context.setupTextureContext(1, 400, 400);

		double sqrt = MathUtils.sqrt(spriteCount);
		double floor = MathUtils.floor(sqrt);
		double half = sqrt - floor;
		if (half == 0) {
			colCount = (int) floor;
			rowCount = (int) floor;
		} else if (half < 0.5) {
			colCount = (int) floor + 1;
			rowCount = (int) floor;
		} else {
			colCount = (int) floor + 1;
			rowCount = (int) floor + 1;
		}

		currentCol = 0;
		currentRow = 0;

		this.textureWidth = colCount * spriteWidth;
		this.textureHeight = rowCount * spriteHeight;
		this.frameBuffer = new FrameBuffer(textureWidth, textureHeight, true, true);
		frameBuffer.setClearColor(0xFF000000);
		frameBuffer.setViewport(0, 0, textureWidth, textureHeight);
	}

	public Sprite getSprite(int index) {
		return sprites.get(index);
	}

	public int getTextureID() {
		return frameBuffer.getTextureID();
	}

	public void addSprite(int spriteTextureID, int duration) {
		final ShaderProgram shader = renderContext.getShaderProgram();
		final RenderBuffer renderBuffer = renderContext.getRenderBuffer();
		final VertexBuffer vbo = renderBuffer.getVbo();

		final GLSM glsm = GLSM.instance;
		glsm.glDisableDepthTest();
		glsm.glEnableAlpha();
		glsm.glEnableBlend();
		glsm.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glsm.glEnableCullFace();

		final IntBuffer oldViewport = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
		glGetIntegerv(GL_VIEWPORT, oldViewport);
		final int viewPortX = oldViewport.get(0);
		final int viewPortY = oldViewport.get(1);
		final int viewPortW = oldViewport.get(2);
		final int viewPortH = oldViewport.get(3);

		frameBuffer.bindFramebuffer();

		renderBuffer.clear();

		float x = currentCol * spriteWidth;
		float y = currentRow * spriteHeight;
		sprites.add(new Sprite(x, textureHeight - y - spriteHeight, spriteWidth, spriteHeight, textureWidth, textureHeight, duration));
		final int color = 0xFFFFFFFF;
		renderBuffer.addVertex(x, y, color, 0f, 0f);
		renderBuffer.addVertex(x, y + spriteHeight, color, 0f, 1f);
		renderBuffer.addVertex(x + spriteWidth, y + spriteHeight, color, 1f, 1f);
		renderBuffer.addVertex(x + spriteWidth, y, color, 1f, 0f);
		renderBuffer.addIndexCount(6);

		renderBuffer.canUpload();
		renderBuffer.uploadBuffers();

		glUseProgram(shader.getProgram());
		shader.setUniform1f("u_scale", 1f);
		shader.setUniform2f("u_resolution", textureWidth, textureHeight);
		shader.setUniform2f("u_translate", 0f, 0f);
		glsm.glBindTexture(spriteTextureID);
		vbo.draw();
		glUseProgram(0);

		frameBuffer.unbindFramebuffer(viewPortX, viewPortY, viewPortW, viewPortH);

		currentCol++;
		if (currentCol >= colCount) {
			currentCol = 0;
			currentRow++;
		}
		oldViewport.clear();
		glDeleteTextures(spriteTextureID);
	}

	public void cleanUp() {
		frameBuffer.deleteFrameBuffer();
		renderContext.getRenderBuffer().deleteBuffers();
		renderContext.getShaderProgram().deleteShader();
	}

	public void delete() {
		cleanUp();
		frameBuffer.delete();
		sprites.clear();
		currentCol = 0;
		currentRow = 0;
	}

	public void saveDebugFramebuffer(String filename) {
		FrameBufferUtils.saveFrameBuffer(filename, frameBuffer);
	}

	public static class Sprite {
		@Getter
		private final float u0;
		@Getter
		private final float u1;
		@Getter
		private final float v0;
		@Getter
		private final float v1;
		@Getter
		private final int duration;
		@Getter
		private final float width;
		@Getter
		private final float height;

		public Sprite(float posX, float posY, float width, float height, float textureWidth, float textureHeight, int duration) {
			final float stepX = 1f / textureWidth;
			final float stepY = 1f / textureHeight;
			this.u0 = stepX * posX;
			this.u1 = stepX * (posX + width);
			this.v0 = stepY * (posY);
			this.v1 = stepY * (posY + height);
			this.duration = duration;
			this.width = width;
			this.height = height;
		}
	}

	@AllArgsConstructor
	@Data
	public static class AnimatedFrame {
		private final int textureID;
		private final int duration;
	}
}
