package ua.rawfish2d.screensaver;

import ua.rawfish2d.visuallib.font.FontRenderer;
import ua.rawfish2d.visuallib.utils.RenderBuffer;
import ua.rawfish2d.visuallib.utils.RenderContext;
import ua.rawfish2d.visuallib.vertexbuffer.ShaderProgram;
import ua.rawfish2d.visuallib.vertexbuffer.VertexBuffer;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11C.*;

public class Context {
	public static RenderContext setupTextureContext(int objectCount, int screenWidth, int screenHeight) {
		final ShaderProgram shaderProgram = new ShaderProgram();
		final InputStream vertStream = Utils.getInputStream("shaders/basic/texture2d.vert");
		final InputStream fragStream = Utils.getInputStream("shaders/basic/texture2d.frag");
		shaderProgram.loadShaders(vertStream, fragStream);

		VertexBuffer vbo = new VertexBuffer(0);
		vbo.setShader(shaderProgram);
		vbo.setDrawType(GL_QUADS);
		vbo.setMaxObjectCount(objectCount);

		final VertexBuffer.VertexAttribute posAttrib = vbo.addVertexAttribute("aPos", 2, Float.BYTES, false, GL_FLOAT);
		final VertexBuffer.VertexAttribute colorAttrib = vbo.addVertexAttribute("aColor", 4, 1, true, GL_UNSIGNED_BYTE);
		final VertexBuffer.VertexAttribute texCoordAttrib = vbo.addVertexAttribute("aTexCoord", 2, Float.BYTES, false, GL_FLOAT);
		vbo.addBuffer(posAttrib);
		vbo.addBuffer(colorAttrib);
		vbo.addBuffer(texCoordAttrib);
		vbo.initBuffers();
		vbo.clearBuffers();

		final IntBuffer indexBuffer = vbo.getIndexBuffer();
		for (int a = 0; a < objectCount * vbo.verticesPerObject; a += vbo.verticesPerObject) {
			indexBuffer.put(a + 0);
			indexBuffer.put(a + 1);
			indexBuffer.put(a + 2);

			indexBuffer.put(a + 2);
			indexBuffer.put(a + 3);
			indexBuffer.put(a + 0);
		}
		indexBuffer.flip();
		vbo.uploadBuffers();
		vbo.uploadIndexBuffer();
		vbo.clearBuffers();

		final RenderBuffer renderBuffer = new RenderBuffer();
		renderBuffer.setBuffers(vbo, 0, 1, 2);
		final RenderContext renderContext = new RenderContext();
		renderContext.setRenderBuffer(renderBuffer);
		renderContext.setScreenSize(screenWidth, screenHeight);
		renderContext.setShaderProgram(shaderProgram);
		renderContext.setClearcolor(0xFF000000);
		renderContext.setViewport(0, 0, screenWidth, screenHeight);

		return renderContext;
	}

	public static RenderContext setupTexture3DContext(int objectCount, int screenWidth, int screenHeight) {
		final ShaderProgram shaderProgram = new ShaderProgram();
		final InputStream vertStream = Utils.getInputStream("shaders/basic/texture3d.vert");
		final InputStream fragStream = Utils.getInputStream("shaders/basic/texture3d.frag");
		shaderProgram.loadShaders(vertStream, fragStream);

		VertexBuffer vbo = new VertexBuffer(0);
		vbo.setShader(shaderProgram);
		vbo.setDrawType(GL_QUADS);
		vbo.setMaxObjectCount(objectCount);

		final VertexBuffer.VertexAttribute posAttrib = vbo.addVertexAttribute("aPos", 3, Float.BYTES, false, GL_FLOAT);
		final VertexBuffer.VertexAttribute colorAttrib = vbo.addVertexAttribute("aColor", 4, 1, true, GL_UNSIGNED_BYTE);
		final VertexBuffer.VertexAttribute texCoordAttrib = vbo.addVertexAttribute("aTexCoord", 2, Float.BYTES, false, GL_FLOAT);
		final VertexBuffer.VertexAttribute textureIndex = vbo.addVertexAttribute("aTextureIndex", 1, Float.BYTES, false, GL_FLOAT);
		vbo.addBuffer(posAttrib);
		vbo.addBuffer(colorAttrib);
		vbo.addBuffer(texCoordAttrib);
		vbo.addBuffer(textureIndex);
		vbo.initBuffers();
		vbo.clearBuffers();

		final IntBuffer indexBuffer = vbo.getIndexBuffer();
		for (int a = 0; a < objectCount * vbo.verticesPerObject; a += vbo.verticesPerObject) {
			indexBuffer.put(a + 0);
			indexBuffer.put(a + 1);
			indexBuffer.put(a + 2);

			indexBuffer.put(a + 2);
			indexBuffer.put(a + 3);
			indexBuffer.put(a + 0);
		}
		indexBuffer.flip();
		vbo.uploadBuffers();
		vbo.uploadIndexBuffer();
		vbo.clearBuffers();

		final RenderBuffer renderBuffer = new RenderBuffer();
		renderBuffer.setBuffers(vbo, 0, 1, 2);
		final RenderContext renderContext = new RenderContext();
		renderContext.setRenderBuffer(renderBuffer);
		renderContext.setScreenSize(screenWidth, screenHeight);
		renderContext.setShaderProgram(shaderProgram);
		renderContext.setClearcolor(0xFF000000);
		renderContext.setViewport(0, 0, screenWidth, screenHeight);

		return renderContext;
	}

	public static RenderContext setupFontContext(int objectCount, int screenWidth, int screenHeight) {
		final ShaderProgram shader = new ShaderProgram();
		final InputStream vertStream = Utils.getInputStream("shaders/font/font.vert");
		final InputStream fragStream = Utils.getInputStream("shaders/font/font.frag");
		shader.loadShaders(vertStream, fragStream);

		VertexBuffer vbo = new VertexBuffer(0);
		vbo.setShader(shader);
		vbo.setDrawType(GL_QUADS);
		vbo.setMaxObjectCount(objectCount);

		final VertexBuffer.VertexAttribute posAttrib = vbo.addVertexAttribute("aPos", 2, Float.BYTES, false, GL_FLOAT);
		final VertexBuffer.VertexAttribute colorAttrib = vbo.addVertexAttribute("aColor", 4, 1, true, GL_UNSIGNED_BYTE);
		final VertexBuffer.VertexAttribute texCoordAttrib = vbo.addVertexAttribute("aTexCoord", 2, Float.BYTES, false, GL_FLOAT);
		vbo.addBuffer(posAttrib);
		vbo.addBuffer(colorAttrib);
		vbo.addBuffer(texCoordAttrib);
		vbo.initBuffers();
		vbo.clearBuffers();

		final IntBuffer indexBuffer = vbo.getIndexBuffer();
		for (int a = 0; a < objectCount * vbo.verticesPerObject; a += vbo.verticesPerObject) {
			indexBuffer.put(a + 0);
			indexBuffer.put(a + 1);
			indexBuffer.put(a + 2);

			indexBuffer.put(a + 2);
			indexBuffer.put(a + 3);
			indexBuffer.put(a + 0);
		}
		indexBuffer.flip();
		vbo.uploadBuffers();
		vbo.uploadIndexBuffer();

		int fontSize = 16;
		final Font font = new Font("Dialog.plain", Font.PLAIN, fontSize);
		final String fontCachePng = "font" + fontSize + ".png";
		final String fontCacheTxt = "font" + fontSize + ".txt";
		FontRenderer fontRenderer = null;
		if (!new File(fontCachePng).exists() || !new File(fontCacheTxt).exists()) {
			System.out.println("Font cache not found.");
			fontRenderer = new FontRenderer(font, 256, 256, true, true, 2, 2, 256);
			fontRenderer.saveTexture(fontCachePng);
			fontRenderer.saveCharactersData(fontCacheTxt);
		} else {
			System.out.println("Loading font from cache.");
			fontRenderer = new FontRenderer(font, fontCachePng, fontCacheTxt);
		}

		vbo.clearBuffers();

		//vboFont = vbo;
		//fontShader = shader;

		final RenderBuffer renderBuffer = new RenderBuffer();
		renderBuffer.setBuffers(vbo, 0, 1, 2);
		final RenderContext renderContext = new RenderContext();
		renderContext.setFontRenderer(fontRenderer);
		renderContext.setRenderBuffer(renderBuffer);
		renderContext.setScreenSize(screenWidth, screenHeight);
		renderContext.setShaderProgram(shader);
		renderContext.setClearcolor(0xFF000000);
		renderContext.setViewport(0, 0, screenWidth, screenHeight);

		fontRenderer.setScale(1f);
		fontRenderer.setShadowOffset(1f);
		fontRenderer.setRenderContext(renderContext);

		return renderContext;
	}
}
