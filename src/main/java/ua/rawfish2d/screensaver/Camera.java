package ua.rawfish2d.screensaver;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import ua.rawfish2d.visuallib.utils.MathUtils;
import ua.rawfish2d.visuallib.window.GWindow;

import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {
	// camera Attributes
	private final Vector3f position;
	private Vector3f front;
	private Vector3f up = new Vector3f(0f);
	private Vector3f right = new Vector3f(0f);
	private final Vector3f worldUp;
	// euler Angles
	private float yaw = 270f;
	private float pitch = 0f;
	// camera options
	private float movementSpeed = 0.05f;
	private float mouseSensitivity = 0.2f;
	private boolean mouseLocked = false;
	private final double screenWidthHalf;
	private final double screenHeightHalf;
	private double newX;
	private double newY;
	private final long hwnd;
	private final Input input;

	public Camera(Input input, GWindow window) {
		this.input = input;
		screenWidthHalf = (double) window.getDisplayWidth() / 2d;
		screenHeightHalf = (double) window.getDisplayHeight() / 2d;
		newX = screenWidthHalf;
		newY = screenHeightHalf;
		hwnd = window.hwnd;

		position = new Vector3f(0f, 0f, 6f);
		worldUp = new Vector3f(0f, 1f, 0f);
		front = new Vector3f(0f, 0f, -1f);

		updateCameraVectors();
	}

	// returns the view matrix calculated using Euler Angles and the LookAt Matrix
	public Matrix4f getViewMatrix() {
		return new Matrix4f()
				.lookAt(position.x, position.y, position.z,
						position.x + front.x, position.y + front.y, position.z + front.z,
						up.x, up.y, up.z);
	}

	// processes input received from any keyboard-like input system. Accepts input parameter in the form of camera defined ENUM (to abstract it from windowing systems)
	public void processKeyboard() {
		final float velocity = movementSpeed;
		if (input.isPressed(GLFW_KEY_W)) {
			position.add(front.mul(velocity));
		} else if (input.isPressed(GLFW_KEY_S)) {
			position.sub(front.mul(velocity));
		}

		if (input.isPressed(GLFW_KEY_A)) {
			position.sub(right.mul(velocity));
		} else if (input.isPressed(GLFW_KEY_D)) {
			position.add(right.mul(velocity));
		}

		if (input.isPressed(GLFW_KEY_SPACE)) {
			position.add(up.mul(velocity));
		} else if (input.isPressed(GLFW_KEY_LEFT_SHIFT)) {
			position.sub(up.mul(velocity));
		}
	}

	// processes input received from a mouse input system. Expects the offset value in both the x and y direction.
	private void processMouseMovement(double xoffset, double yoffset) {
		yaw += xoffset * mouseSensitivity;
		pitch -= yoffset * mouseSensitivity;

		yaw = MathUtils.fmod(yaw, 360f); // normalize

		// make sure that when pitch is out of bounds, screen doesn't get flipped
		if (pitch > 89.0f)
			pitch = 89.0f;
		if (pitch < -89.0f)
			pitch = -89.0f;

		// update Front, Right and Up Vectors using the updated Euler angles
		updateCameraVectors();
	}

	private void updateCameraVectors() {
		// calculate the new Front vector
		final Vector3f l_front = new Vector3f();
		l_front.x = MathUtils.cos(MathUtils.toRad(yaw)) * MathUtils.cos(MathUtils.toRad(pitch));
		l_front.y = MathUtils.sin(MathUtils.toRad(pitch));
		l_front.z = MathUtils.sin(MathUtils.toRad(yaw)) * MathUtils.cos(MathUtils.toRad(pitch));
		front = l_front.normalize();
		// also re-calculate the Right and Up vector
		right = (new Vector3f(front).cross(worldUp)).normalize();  // normalize the vectors, because their length gets closer to 0 the more you look up or down which results in slower movement.
		up = (new Vector3f(right).cross(front)).normalize();
	}

	public void processMouseMovement() {
		if (glfwGetMouseButton(hwnd, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS) {
			glfwSetInputMode(hwnd, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
			if (!mouseLocked) {
				glfwSetCursorPos(hwnd, screenWidthHalf, screenHeightHalf);
			}

			mouseLocked = true;
		} else {
			glfwSetInputMode(hwnd, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
			mouseLocked = false;
		}

		if (mouseLocked) {
			final DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
			final DoubleBuffer y = BufferUtils.createDoubleBuffer(1);

			glfwGetCursorPos(hwnd, x, y);
			x.rewind();
			y.rewind();

			newX = x.get();
			newY = y.get();

			double deltaX = newX - screenWidthHalf;
			double deltaY = newY - screenHeightHalf;
			processMouseMovement(deltaX, deltaY);

			glfwSetCursorPos(hwnd, screenWidthHalf, screenHeightHalf);
		} else {
			processMouseMovement(0f, 0f);
		}
	}

	public Vector3f getPos() {
		return position;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}
}
