#version 140

in vec3 v;

out vec3 pos;

void main() {
	gl_Position = vec4(v, 1);
	pos = v;
}