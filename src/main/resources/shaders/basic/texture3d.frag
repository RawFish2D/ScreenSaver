#version 330 core

in vec2 vTexCoord;
in vec4 vColor;
uniform sampler2D u_texture;
out vec4 colorOut;

void main()
{
	vec4 col = texture2D(u_texture, vTexCoord) * vColor;
	if (col.a <= 0.001) {
		discard;
	}
	colorOut = col;
}
