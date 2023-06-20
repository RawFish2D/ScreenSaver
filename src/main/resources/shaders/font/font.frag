#version 330 core

in vec4 vColor;
in vec2 vTexCoord;

uniform sampler2D texture;
out vec4 colorOut;

void main()
{
	colorOut = vec4(vColor.rgb, texture2D(texture, vTexCoord).r * vColor.a);
}  
