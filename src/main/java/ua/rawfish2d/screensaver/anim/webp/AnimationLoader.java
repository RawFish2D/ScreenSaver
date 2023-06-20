package ua.rawfish2d.screensaver.anim.webp;

import ua.rawfish2d.screensaver.Utils;
import ua.rawfish2d.screensaver.anim.AnimatedTexture;
import ua.rawfish2d.visuallib.utils.RenderContext;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

public class AnimationLoader {

	public AnimatedTexture loadAnimation(String filename, RenderContext fontContext, boolean redrawAll) {
		AnimatedTexture animatedTexture = null;
		final File file = new File(filename);

		try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
			final ImageReader reader = ImageIO.getImageReadersByFormatName("WEBP").next();
			reader.setInput(input);

			BufferedImage baseImage = null;
			Graphics2D graphics = null;
			int counter = 0;
			final int imagesCount = reader.getNumImages(true);
			//System.out.println("images count: " + imagesCount);

			Class<?> animationFrameClass = null;
			Class<?> VP8xChunkClass = null;
			Class<?> webpImageReaderClass = null;
			List<Object> frames = null;
			try {
				animationFrameClass = Class.forName("com.twelvemonkeys.imageio.plugins.webp.AnimationFrame");
				VP8xChunkClass = Class.forName("com.twelvemonkeys.imageio.plugins.webp.VP8xChunk");
				webpImageReaderClass = Class.forName("com.twelvemonkeys.imageio.plugins.webp.WebPImageReader");
				Field framesField = webpImageReaderClass.getDeclaredField("frames");
				framesField.setAccessible(true);
				frames = (List<Object>) framesField.get(reader);

//				for (int a = 0; a < frames.size(); ++a) {
//					Object animationFrame = frames.get(a);
//					Field durationField = animationFrameClass.getDeclaredField("duration");
//					durationField.setAccessible(true);
//					int duration = (int) durationField.get(animationFrame);
//					System.out.println("frame [" + a + "] duration: " + duration);
//				}
			} catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}

			// Read metadata
//			IIOMetadata metadata = reader.getImageMetadata(counter);
//			String[] metadataNames = metadata.getMetadataFormatNames();
//			Arrays.stream(metadataNames).forEach(System.out::println);
//			List<Node> allNodes = new ArrayList<>();
//
//			if (metadataNames.length > 0) {
//				allNodes.add(metadata.getAsTree(metadataNames[0]));
//				for (int a = 0; a < allNodes.size(); ++a) {
//					Node nodeList = allNodes.get(a);
//					String parentNodeName0 = nodeList.getNodeName();
//					for (int b = 0; b < nodeList.getChildNodes().getLength(); ++b) {
//						Node node = nodeList.getChildNodes().item(b);
//						allNodes.add(node);
//
//						System.out.println("node #" + a + "." + b + " " + node.getNodeName() + " " + node.getNodeValue() + " child " + node.getChildNodes().getLength());
//						NamedNodeMap namedNodeMap = node.getAttributes();
//						String parentNodeName1 = node.getNodeName();
//						for (int c = 0; c < namedNodeMap.getLength(); ++c) {
//							node = namedNodeMap.item(c);
//							System.out.println("node #" + a + "." + b + "." + c + " " + parentNodeName0 + "." + parentNodeName1 + " " + node.getNodeName() + " " + node.getNodeValue());
//						}
//					}
//				}
//			}
			// Read metadata

			int imageWidth = 0;
			int imageHeight = 0;
			try {
				Field headerField = webpImageReaderClass.getDeclaredField("header");
				headerField.setAccessible(true);
				Object header = headerField.get(reader);

				Field widthField = VP8xChunkClass.getDeclaredField("width");
				widthField.setAccessible(true);
				imageWidth = (int) widthField.get(header);

				Field heightField = VP8xChunkClass.getDeclaredField("height");
				heightField.setAccessible(true);
				imageHeight = (int) heightField.get(header);
				//System.out.println("canvas " + counter + " width: " + imageWidth + " height: " + imageHeight);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}

			while (counter < imagesCount) {
				try {
					final BufferedImage image = reader.read(counter, null);

					final Object animationFrame = frames.get(counter);
					final Field durationField = animationFrameClass.getDeclaredField("duration");
					durationField.setAccessible(true);
					final int duration = (int) durationField.get(animationFrame);
					final Field boundsField = animationFrameClass.getDeclaredField("bounds");
					boundsField.setAccessible(true);
					final Rectangle bounds = (Rectangle) boundsField.get(animationFrame);
					//System.out.println("image #" + counter + " bounds x: " + bounds.x + " y: " + bounds.y + " w: " + bounds.width + " h: " + bounds.height);
					//final int tileWidth = reader.getTileWidth(counter);
					//final int tileHeight = reader.getTileHeight(counter);
					//System.out.println("image #" + counter + " tileWidth: " + tileWidth + " tileHeight: " + tileHeight);
					//final int width = reader.getWidth(counter);
					//final int height = reader.getHeight(counter);
					//System.out.println("image #" + counter + " width: " + width + " height: " + height);

					final int textureID;
					if (redrawAll) {
						if (baseImage == null) {
							baseImage = new BufferedImage(imageWidth, imageHeight, image.getType());
							graphics = baseImage.createGraphics();
						}
						graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
						graphics.fillRect(0, 0, imageWidth, imageHeight);
						graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
						graphics.drawImage(image, bounds.x, bounds.y, null);
						textureID = Utils.createTexture(baseImage, false);
					} else {
						if (baseImage == null) {
							baseImage = new BufferedImage(imageWidth, imageHeight, image.getType());
							graphics = baseImage.createGraphics();
						}
						graphics.drawImage(image, 0, 0, null);
						textureID = Utils.createTexture(baseImage, false);
					}
					//System.out.println("image #" + counter + " width: " + imageWidth + " height: " + imageHeight);
					//System.out.println("frame [" + counter + "] duration: " + duration);

					if (animatedTexture == null) {
						animatedTexture = new AnimatedTexture(imagesCount, imageWidth, imageHeight);
					}
					animatedTexture.addSprite(textureID, duration);
				} catch (Exception ex) {
					ex.printStackTrace();
					break;
				}
				counter++;
			}
			if (graphics != null) {
				graphics.dispose();
			}
			reader.dispose();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return animatedTexture;
	}
}
