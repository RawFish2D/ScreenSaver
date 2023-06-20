#version 330 core

in vec2 aPos;
in vec4 aColor;
in vec2 aTexCoord;

out vec4 vColor;
out vec2 vTexCoord;

uniform vec2 u_resolution;
uniform float u_scale;

void main()
{
	float x = (aPos.x * u_scale) / (u_resolution.x / 2.0) - 1.0;
	float y = (aPos.y * u_scale) / (u_resolution.y / 2.0) - 1.0;
	gl_Position = vec4(x, -y, 1.0, 1.0);
	vColor = aColor.bgra;
	vTexCoord = aTexCoord;
}
