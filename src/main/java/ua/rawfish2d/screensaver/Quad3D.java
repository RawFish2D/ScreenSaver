package ua.rawfish2d.screensaver;

import lombok.Getter;
import lombok.Setter;
import ua.rawfish2d.screensaver.anim.AnimatedTexture;
import ua.rawfish2d.visuallib.utils.MathUtils;
import ua.rawfish2d.visuallib.utils.MiscUtils;
import ua.rawfish2d.visuallib.utils.RenderBuffer;
import ua.rawfish2d.visuallib.utils.TimeHelper;

public class Quad3D {
	@Getter
	private float x;
	@Getter
	private float y;
	@Getter
	private float z;
	@Getter
	private float w;
	@Getter
	private float h;
	@Getter
	private float alpha = 0f;
	private int spriteIndex = 0;
	@Getter
	@Setter
	private Mesh mesh;
	private final TimeHelper timeHelper = new TimeHelper();

	public boolean draw(RenderBuffer renderBuffer, int objectIndex, Render render) {
		final float renderDepth = render.getRenderDepth();
		final float depth = render.getDepth();
		final long delta = render.getDelta();
		final float limit = depth + renderDepth - Math.abs(z) - 1f;
		alpha = MathUtils.clamp(limit, 0f, 1f);
		final int color = 0xFFFFFF | ((int) (alpha * 255) << 24);

//		if (Math.abs(z) > depth + renderDepth - 1f) {
//			//color = 0xFF00FF00;
//			return false;
//		}
//		if (Math.abs(z) - depth < -6f) {
//			//color = 0xFF0000FF;
//			return false;
//		}

		if (spriteIndex >= mesh.getAnimatedTexture().getSpriteCount()) {
			spriteIndex = 0;
		}
		final AnimatedTexture.Sprite sprite = mesh.getAnimatedTexture().getSprite(spriteIndex);
		final float u0 = sprite.getU0();
		final float u1 = sprite.getU1();
		final float v0 = sprite.getV0();
		final float v1 = sprite.getV1();

		renderBuffer.addVertex(x, y, z, color, u0, v0);
		renderBuffer.addVertex(x + w, y, z, color, u1, v0);
		renderBuffer.addVertex(x + w, y + h, z, color, u1, v1);
		renderBuffer.addVertex(x, y + h, z, color, u0, v1);
		renderBuffer.addIndexCount(6);
		mesh.getRangeBuffer().addPointer(6, objectIndex * 6);

		long frameDurationMicro = sprite.getDuration() * 1000L;
		long microDelta = delta / 1000;
		if (timeHelper.hasReachedMicro(frameDurationMicro - microDelta)) {
			timeHelper.reset();
			spriteIndex += 1;
		}

		return true;
	}

	public void setPos(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void setSize(float w, float h) {
		this.w = w;
		this.h = h;
	}

	public void setRandomSpriteIndex() {
		final AnimatedTexture animatedTexture = mesh.getAnimatedTexture();
		this.spriteIndex = MiscUtils.random(0, animatedTexture.getSpriteCount() - 1);
	}
}
