package ua.rawfish2d.screensaver;

import ua.rawfish2d.visuallib.font.FontRenderer;
import ua.rawfish2d.visuallib.utils.GLSM;
import ua.rawfish2d.visuallib.utils.RenderContext;
import ua.rawfish2d.visuallib.vertexbuffer.ShaderProgram;
import ua.rawfish2d.visuallib.vertexbuffer.VertexBuffer;
import ua.rawfish2d.visuallib.window.GWindow;

import java.nio.BufferOverflowException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUseProgram;

public class LoadingProgress {
	private final RenderContext fontContext;
	private final GWindow window;
	private final List<String> list = new ArrayList<>();
	private int objectCount = 2048;

	public LoadingProgress(GWindow window) {
		this.fontContext = Context.setupFontContext(objectCount, window.getDisplayWidth(), window.getDisplayHeight());
		this.window = window;
	}

	public void renderString(String text) {
		final GLSM glsm = GLSM.instance;
		final ShaderProgram fontShader = fontContext.getShaderProgram();
		final VertexBuffer fontVbo = fontContext.getRenderBuffer().getVbo();
		final FontRenderer fontRenderer = fontContext.getFontRenderer();

		list.add("§a" + text);

		fontRenderer.setRenderContext(fontContext);
		fontRenderer.setScale(1f);
		fontRenderer.setShadowOffset(1f);

		int screenWidth = window.getDisplayWidth();
		int screenHeight = window.getDisplayHeight();
		float x = 16f;
		float y = 16f;

		fontVbo.clearBuffers();
		for (int a = 0; a < list.size(); ++a) {
			String string = list.get(a);
			float h = fontRenderer.getStringHeight(string);
			float w = fontRenderer.getStringWidth(string);
			try {
				fontRenderer.drawGeometry(x - 4f, y, x + w + 4f, y + h, 1f, 0xAF000000);
				fontRenderer.drawStringWithShadow(string, x, y, 0xFFFFFFFF);
			} catch (BufferOverflowException ex) {
				resize(objectCount + 2048);
				list.add("§cFont buffer resized to: " + objectCount);
				y = 16f;
				a = -1;
				continue;
			}
			y += h;
		}

		fontVbo.canUpload();
		fontVbo.uploadBuffers();

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		window.pollEvents();

		glsm.glDisableDepthTest();
		glsm.glEnableBlend();
		glsm.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glsm.glDisableCullFace();

		glViewport(0, 0, screenWidth, screenHeight);
		glClearColor(0f, 0f, 0f, 0f);

		glsm.glBindTexture(fontRenderer.getFontTextureID());
		glUseProgram(fontShader.getProgram());
		fontShader.setUniform1f("u_scale", 1f);
		fontShader.setUniform2f("u_resolution", screenWidth, screenHeight);
		fontVbo.draw();
		glUseProgram(0);
		glsm.glBindTexture(0);

		window.swapBuffers();
	}

	private void resize(int newObjectCount) {
		final VertexBuffer fontVbo = fontContext.getRenderBuffer().getVbo();
		this.objectCount = newObjectCount;

		fontVbo.resizeBuffer(objectCount);
		fontContext.getRenderBuffer().setBuffers(fontVbo, 0, 1, 2);

		final IntBuffer indexBuffer = fontVbo.getIndexBuffer();
		indexBuffer.clear();
		for (int a = 0; a < objectCount * fontVbo.verticesPerObject; a += fontVbo.verticesPerObject) {
			indexBuffer.put(a + 0);
			indexBuffer.put(a + 1);
			indexBuffer.put(a + 2);

			indexBuffer.put(a + 2);
			indexBuffer.put(a + 3);
			indexBuffer.put(a + 0);
		}
		indexBuffer.flip();
		fontVbo.uploadIndexBuffer();
		fontVbo.clearBuffers();
	}

	public void delete() {
		fontContext.getShaderProgram().deleteShader();
		fontContext.getRenderBuffer().getVbo().deleteBuffers();
		fontContext.getFontRenderer().free();
	}
}
