package ua.rawfish2d.screensaver.anim.gif;

import ua.rawfish2d.screensaver.Utils;
import ua.rawfish2d.screensaver.anim.AnimatedTexture;

public class GifLoader {
	public AnimatedTexture loadGif(final String filename) {
		final GifDecoder gifDecoder = new GifDecoder();
		final int status = gifDecoder.read(filename);
		if (status == 1) {
			throw new RuntimeException("File read status: Error decoding file (may be partially decoded)");
		} else if (status == 2) {
			throw new RuntimeException("File read status: Unable to open source.");
		}
		final AnimatedTexture animatedTexture = new AnimatedTexture(gifDecoder.getFrameCount(), gifDecoder.width, gifDecoder.height);

		for (int a = 0; a < gifDecoder.getFrameCount(); ++a) {
			final int textureID = Utils.createTexture(gifDecoder.getFrame(a), false);
			animatedTexture.addSprite(textureID, gifDecoder.getDelay(a));
		}

		return animatedTexture;
	}
}
