package ua.rawfish2d.screensaver;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Input {
	private final List<Key> keys = new ArrayList<>();

	public void keyUpdate(int key, int scancode, int action) {
		//System.out.printf("key %d scancode %d action %d\n", key, scancode, action);
		if (keys.stream().noneMatch(k -> k.getKey() == key)) {
			final Key keyBind = new Key(key);
			keyBind.onAction(action);
			keys.add(keyBind);
			//System.out.println("added new key " + (char) key);
		} else {
			Optional<Key> optionalKeyBind = keys.stream().filter(k -> k.getKey() == key).findFirst();
			if (optionalKeyBind.isPresent()) {
				final Key keyBind = optionalKeyBind.get();
				keyBind.onAction(action);
				//System.out.println("key pressed " + (char) key);
			}
		}
	}

	public boolean isPressed(int key) {
		Optional<Key> optionalKeyBind = keys.stream().filter(k -> k.getKey() == key).findFirst();
		if (optionalKeyBind.isPresent()) {
			final Key keyBind = optionalKeyBind.get();
			return keyBind.pressed;
		}
		return false;
	}

	private static final class Key {
		@Getter
		private final int key;
		private boolean pressed;

		public Key(int key) {
			this.key = key;
		}

		public void onAction(int action) {
			if (action >= 1) {
				pressed = true;
			} else if (action == 0) {
				pressed = false;
			}
		}
	}
}
