package ua.rawfish2d.screensaver;

import org.lwjgl.PointerBuffer;

import java.util.Arrays;

public class RangeBuffer {
	public int[] counts;
	public PointerBuffer pointerBuffer;
	private int pointers = 0;

	public void allocate(int pointersMax) {
		pointers = 0;
		counts = new int[pointersMax];
		Arrays.fill(counts, 0);
		if (pointerBuffer != null) {
			pointerBuffer.free();
		}
		pointerBuffer = PointerBuffer.allocateDirect(pointersMax);
	}

	public void addPointer(int indicesCount, int indexPos) {
		counts[pointers] = indicesCount;
		pointerBuffer.put((long) indexPos * Integer.BYTES);
		pointers++;
	}

	public void rewind() {
		pointerBuffer.rewind();
	}

	public void clear() {
		pointerBuffer.clear();
		Arrays.fill(counts, 0);
		pointers = 0;
	}

	public void free() {
		pointers = 0;
		counts = null;
		pointerBuffer.free();
		pointerBuffer = null;
	}
}
