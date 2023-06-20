package ua.rawfish2d.screensaver;

import lombok.Getter;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL30;
import ua.rawfish2d.screensaver.anim.AnimatedTexture;
import ua.rawfish2d.screensaver.anim.gif.GifLoader;
import ua.rawfish2d.screensaver.anim.webp.AnimationLoader;
import ua.rawfish2d.visuallib.font.FontRenderer;
import ua.rawfish2d.visuallib.utils.*;
import ua.rawfish2d.visuallib.vertexbuffer.ShaderProgram;
import ua.rawfish2d.visuallib.vertexbuffer.VertexBuffer;
import ua.rawfish2d.visuallib.window.GWindow;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20C.glUseProgram;

public class Render {
	private final GWindow window;
	private final TimeHelper keyPressTimer = new TimeHelper();
	private final TimeHelper uniform3dTimer = new TimeHelper();
	private final TimeHelper fontTimer = new TimeHelper();
	private final TimeHelper recreateTimer = new TimeHelper();
	private final RenderContext textureContext3D;
	private final RenderContext fontContext;
	private final List<Mesh> meshes = new ArrayList<>();
	private final GLSM glsm = GLSM.instance;
	private final int screenWidth;
	private final int screenHeight;
	private final Input input = new Input();
	private final Camera camera;
	private final List<List<Quad3D>> quads3D = new ArrayList<>();
	@Getter
	private float depth = -5f;
	@Getter
	private final float renderDepth = 10f;
	private final int objectCount3D = 2000;
	private final int arraysCount = 2;
	private final FPSCounter fpsCounter = new FPSCounter();
	@Getter
	private long delta = 0;
	private final boolean debugMode;
	private final float zSize = 25f;

	public Render(int width, int height, boolean transparentFrameBuffer, boolean debugMode) {
		this.debugMode = debugMode;
		this.screenWidth = width;
		this.screenHeight = height;
		System.out.println("Creating window with resolution " + width + "x" + height);
		window = new GWindow(width, height, false);
		window.setTransparentFramebuffer(transparentFrameBuffer);
		//window.setAlwaysOnTop(true);
		window.create("ScreenSaver");
		if (transparentFrameBuffer) {
			window.setPos(0, 0);
		}
		window.setVsync(true);

		window.setKeyCallback(new GLFWKeyCallback() {
			@Override
			public void invoke(long hwnd, int key, int scancode, int action, int mods) {
				input.keyUpdate(key, scancode, action);
			}
		});

		fontContext = Context.setupFontContext(1024, window.getDisplayWidth(), window.getDisplayHeight());
		loadAnimations();
		textureContext3D = Context.setupTexture3DContext(objectCount3D * arraysCount, window.getDisplayWidth(), window.getDisplayHeight());
		camera = new Camera(input, window);

		Config config = Config.instance;
		window.setFullscreen(config.isFullScreen(), !config.isFullScreen());
		if (config.isFullScreen()) {
			window.setPos(0, 0);
		}
		window.showWindow();
		createQuads();
		keyPressTimer.reset();

		fontContext.setViewport();
		RenderContext.Clearcolor clearcolor = new RenderContext.Clearcolor();
		if (!transparentFrameBuffer) {
			// XNA background color
//			float r = (1f / 256f) * 100f;
//			float g = (1f / 256f) * 150f;
//			float b = (1f / 256f) * 238f;
//			clearcolor.setR(r);
//			clearcolor.setG(g);
//			clearcolor.setB(b);
//			clearcolor.setA(1.0f);
			clearcolor.setClearColor(0xFF000000);
		} else {
			clearcolor.setClearColor(0x00000000);
		}
		clearcolor.setClearcolor();

		glsm.glEnableDepthTest();
		glsm.glDepthMask(true);
		glsm.glDepthFunc(GL_LEQUAL);
		glsm.glEnableBlend();
		glsm.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glsm.glEnableCullFace();
		glsm.glEnableAlpha();

		while (!window.shouldClose()) {
			long time = System.nanoTime();
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			window.pollEvents();
			doStuff();
			render();

			fpsCounter.onRender();
			window.swapBuffers();
			delta = System.nanoTime() - time;
		}
		shutdown();
	}

	private void loadAnimations() {
		final GifLoader gifLoader = new GifLoader();
		final AnimationLoader animationLoader = new AnimationLoader();
		final Set<String> fileNames = Utils.listFilesUsingJavaIO("anims");

		int index = 0;
		for (String fileName : fileNames) {
			System.out.println("Loading file: " + fileName);
			final boolean hasTransparency = fileName.startsWith("T");
			AnimatedTexture animatedTexture;
			if (fileName.endsWith(".gif")) {
				animatedTexture = gifLoader.loadGif("assets/" + fileName);
				// without this it bugs out for some reason
				GLSM.instance.glBindTexture(0);
			} else if (fileName.endsWith(".webp")) {
				animatedTexture = animationLoader.loadAnimation("assets/" + fileName, fontContext, hasTransparency);
				// without this it bugs out for some reason
				GLSM.instance.glBindTexture(0);
			} else {
				continue;
			}
			meshes.add(new Mesh(animatedTexture, 6000, index));
			if (debugMode) {
				File folder = new File("debug");
				if (!folder.exists()) {
					if (!folder.mkdir()) {
						throw new RuntimeException("Cannot create " + folder + " folder");
					}
				}
				animatedTexture.saveDebugFramebuffer(folder.getName() + "/stitched_debug_texture" + index + ".png");
			}
			animatedTexture.cleanUp();
			index++;
		}
	}

	private void doStuff() {
		if (input.isPressed(GLFW_KEY_ESCAPE)) {
			shutdown();
		}
		if (debugMode) {
			if (input.isPressed(GLFW_KEY_R) && keyPressTimer.hasReachedMilli(150)) {
				keyPressTimer.reset();
				createQuads();
			}
			if (input.isPressed(GLFW_KEY_S) && keyPressTimer.hasReachedMilli(150)) {
				keyPressTimer.reset();
				reloadShaders();
			}
			if (input.isPressed(GLFW_KEY_V) && keyPressTimer.hasReachedMilli(150)) {
				keyPressTimer.reset();
				window.setVsync(!window.isVsync());
			}
			if (input.isPressed(GLFW_KEY_SPACE)) {
				depth = -5f;
			}
			if (input.isPressed(GLFW_KEY_KP_SUBTRACT)) {
				depth -= 0.05f;
			}
			if (input.isPressed(GLFW_KEY_KP_ADD)) {
				depth += 0.05f;
			}

			if (input.isPressed(GLFW_KEY_LEFT_ALT) && (input.isPressed(GLFW_KEY_ENTER) || input.isPressed(GLFW_KEY_KP_ENTER) && keyPressTimer.hasReachedMilli(150))) {
				keyPressTimer.reset();
				window.setFullscreen(!window.isFullScreen(), false);
			}
		}
		// im bad at naming variables
		int value = ((int) (depth + renderDepth) / (int) zSize);
		if (shouldCreateNextQuads()) {
			int index = value % quads3D.size();
			int z = (int) (-zSize * value);
			System.out.println("Remaking quads in list: " + index + " at " + z + " depth");
			quads3D.get(index).clear();
			createQuads(quads3D.get(index), z);
		}
	}

	private void reloadShaders() {
		System.out.println("Reloading shaders");

		final ShaderProgram shader3d = textureContext3D.getShaderProgram();
		InputStream vertStream = Utils.getInputStream("shaders/basic/texture3d.vert");
		InputStream fragStream = Utils.getInputStream("shaders/basic/texture3d.frag");
		shader3d.deleteShader();
		shader3d.loadShaders(vertStream, fragStream);
		textureContext3D.getRenderBuffer().getVbo().setShader(shader3d);
	}

	private void createQuad(Quad3D quad, float z) {
		final Mesh mesh = meshes.get(MiscUtils.random(0, meshes.size() - 1));
		final float x = MiscUtils.random(-5f, 5f);
		final float y = MiscUtils.random(-4f, 4f);

		quad.setMesh(mesh);
		quad.setPos(x, y, z);
		quad.setSize(0.2f, 0.2f);
		quad.setRandomSpriteIndex();
	}

	private void createQuads(List<Quad3D> quadList, float zStart) {
		final float minZ = zStart - zSize;
		final float maxZ = zStart;
		int objectCount = objectCount3D;
		float stepZ = (Math.abs(maxZ - minZ)) / objectCount;
		float z = minZ;
		for (int a = 0; a < objectCount; ++a) {
			final Quad3D quad = new Quad3D();
			createQuad(quad, z);
			quadList.add(quad);
			z += stepZ;
		}
	}

	private void createQuads() {
		quads3D.clear();
		float z = 0f;
		for (int a = 0; a < arraysCount; ++a) {
			List<Quad3D> list = new ArrayList<>();
			quads3D.add(list);
			createQuads(list, z);
			z -= zSize;
		}
	}

	private void render() {
		render3d();
		if (debugMode) {
			renderFont();
		}
	}

	private void renderFont() {
		glsm.glDisableDepthTest();

		final ShaderProgram fontShader = fontContext.getShaderProgram();
		final VertexBuffer fontVbo = fontContext.getRenderBuffer().getVbo();
		final FontRenderer fontRenderer = fontContext.getFontRenderer();

		if (fontTimer.hasReachedMilli(1000 / 75)) {
			fontTimer.reset();
			fontVbo.clearBuffers();
			fontRenderer.setRenderContext(fontContext);
//		fontRenderer.drawStringWithShadow("§c§lR§a§lG§9§lB", 2, 2);
			float offset = 16f;
			float fontY = 2f - offset;
			float fDelta = ((delta / 1000f) / 1000f);
			fontRenderer.drawGeometry(0, 0, 300, 220, 1f, 0x7F000000);
			fontRenderer.drawStringWithShadow("§cFPS: " + fpsCounter.getFps(), 2, fontY += offset);
			fontRenderer.drawStringWithShadow(String.format(Locale.US, "§cDelta: %.3f", fDelta), 2, fontY += offset);
			fontRenderer.drawStringWithShadow("§adepth: §e" + depth, 2, fontY += offset);
			fontRenderer.drawStringWithShadow("§avsync: §e" + window.isVsync(), 2, fontY += offset);
			fontRenderer.drawStringWithShadow("Space - reset zoom", 2, fontY += offset);
			fontRenderer.drawStringWithShadow("R - randomize sprite timings", 2, fontY += offset);
			fontRenderer.drawStringWithShadow("S - reload shaders", 2, fontY += offset);
			fontRenderer.drawStringWithShadow("V - toggle vsync", 2, fontY += offset);
			fontRenderer.drawStringWithShadow("Keypad PLUS/MINUS - move camera", 2, fontY += offset);
			fontRenderer.drawStringWithShadow("ALT + ENTER - toggle full screen", 2, fontY += offset);
			fontRenderer.drawStringWithShadow("ESC - exit", 2, fontY += offset);

			fontVbo.canUpload();
			fontVbo.uploadBuffers();
		}

		glsm.glBindTexture(fontRenderer.getFontTextureID());
		glUseProgram(fontShader.getProgram());
		fontShader.setUniform1f("u_scale", 1f);
		fontShader.setUniform2f("u_resolution", screenWidth, screenHeight);
		fontVbo.draw();
		glUseProgram(0);

		glsm.glEnableDepthTest();
	}

	private void render3d() {
		final ShaderProgram shader = textureContext3D.getShaderProgram();
		final RenderBuffer renderBuffer = textureContext3D.getRenderBuffer();
		final VertexBuffer vbo = renderBuffer.getVbo();

		if (uniform3dTimer.hasReachedMilli(1000 / 75)) {
			if (!window.isVsync()) {
				uniform3dTimer.reset();
			}
			// I think it can break at very high numbers :Hmmm:
			depth += 0.02f * (float) Config.instance.getSpeed();

			vbo.clearBuffers();

			int index = 0;
			meshes.forEach(mesh -> mesh.getRangeBuffer().clear());
			for (List<Quad3D> list : quads3D) {
				for (Quad3D quad : list) {
					if (quad.draw(renderBuffer, index, this)) {
						index++;
					}
				}
			}
			meshes.forEach(mesh -> mesh.getRangeBuffer().rewind());

			vbo.canUpload();
			vbo.uploadBuffers();

			final float fov = 45f;
			final Matrix4f view = camera.getViewMatrix();
			final Matrix4f proj = new Matrix4f()
					.setPerspective(
							MathUtils.toRad(fov),
							(float) screenWidth / screenHeight, 0.1f, zSize);

			final Matrix4f model = new Matrix4f()
					.identity()
					.translate(0f, 0f, depth);

			glUseProgram(shader.getProgram());
			shader.setUniformMatrix4f("view", view);
			shader.setUniformMatrix4f("proj", proj);
			shader.setUniformMatrix4f("model", model);
			glUseProgram(0);
		}

		glUseProgram(shader.getProgram());
		GL30.glBindVertexArray(vbo.getVaoID());
		GL30.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo.getEboID());
		for (final Mesh mesh : meshes) {
			final AnimatedTexture animatedTexture = mesh.getAnimatedTexture();
			final int textureID = animatedTexture.getTextureID();
			final RangeBuffer rangeBuffer = mesh.getRangeBuffer();
			glsm.glBindTexture(textureID);
			//vbo.draw(rangeBuffer.counts, rangeBuffer.pointerBuffer);
			GL30.glMultiDrawElements(GL_TRIANGLES, rangeBuffer.counts, GL_UNSIGNED_INT, rangeBuffer.pointerBuffer);
		}
		glUseProgram(0);
	}

	private boolean shouldCreateNextQuads() {
		int threshold = ((int) (depth + renderDepth) % (int) zSize);
		if (threshold == 0 && recreateTimer.hasReachedMilli(2000)) {
			recreateTimer.reset();
			return true;
		}
		return false;
	}

	private void shutdown() {
		window.closeWindow();
		meshes.forEach(mesh -> {
			mesh.getAnimatedTexture().delete();
			mesh.getRangeBuffer().free();
		});

		fontContext.getFontRenderer().free();
		fontContext.getShaderProgram().deleteShader();
		fontContext.getRenderBuffer().deleteBuffers();

		textureContext3D.getShaderProgram().deleteShader();
		textureContext3D.getRenderBuffer().deleteBuffers();

		quads3D.clear();

		window.cleanUp();
		window.terminate();

		System.exit(0);
	}
}
