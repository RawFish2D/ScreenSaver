#version 330 core

in vec3 aPos;
in vec4 aColor;
in vec2 aTexCoord;

out vec2 vTexCoord;
out vec4 vColor;

uniform mat4 proj;
uniform mat4 view;
uniform mat4 model;

void main()
{
	vec4 modelPos = model * vec4(aPos, 1.0);
	gl_Position = proj * view * modelPos;
	vTexCoord = aTexCoord;
	vColor = aColor;
}
