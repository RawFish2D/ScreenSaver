package ua.rawfish2d.screensaver;

import lombok.Getter;
import ua.rawfish2d.screensaver.anim.AnimatedTexture;

public class Mesh {
	@Getter
	private final AnimatedTexture animatedTexture;
	@Getter
	private final RangeBuffer rangeBuffer = new RangeBuffer();
	@Getter
	private final int animatedTextureIndex;

	public Mesh(AnimatedTexture animatedTexture, int pointersMax, int animatedTextureIndex) {
		this.animatedTexture = animatedTexture;
		this.rangeBuffer.allocate(pointersMax);
		this.animatedTextureIndex = animatedTextureIndex;
	}
}
