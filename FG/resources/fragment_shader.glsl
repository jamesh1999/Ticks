#version 330

uniform vec2 resolution;
uniform float currentTime;
uniform vec3 camPos;
uniform vec3 camDir;
uniform vec3 camUp;
uniform sampler2D tex;
uniform bool showStepDepth;

in vec3 pos;

out vec3 color;

#define PI 3.1415926535897932384626433832795
#define RENDER_DEPTH 800
#define CLOSE_ENOUGH 0.00001

#define BACKGROUND -1
#define BALL 0
#define BASE 1

#define GRADIENT(pt, func) vec3( \
    func(vec3(pt.x + 0.0001, pt.y, pt.z)) - func(vec3(pt.x - 0.0001, pt.y, pt.z)), \
    func(vec3(pt.x, pt.y + 0.0001, pt.z)) - func(vec3(pt.x, pt.y - 0.0001, pt.z)), \
    func(vec3(pt.x, pt.y, pt.z + 0.0001)) - func(vec3(pt.x, pt.y, pt.z - 0.0001)))

const vec3 LIGHT_POS[] = vec3[](vec3(5, 18, 10));

///////////////////////////////////////////////////////////////////////////////

vec3 getBackground(vec3 dir) {
  float u = 0.5 + atan(dir.z, -dir.x) / (2 * PI);
  float v = 0.5 - asin(dir.y) / PI;
  vec4 texColor = texture(tex, vec2(u, v));
  return texColor.rgb;
}

vec3 getRayDir() {
  vec3 xAxis = normalize(cross(camDir, camUp));
  return normalize(pos.x * (resolution.x / resolution.y) * xAxis + pos.y * camUp + 5 * camDir);
}

///////////////////////////////////////////////////////////////////////////////

float sphere(vec3 pt) {
  return length(pt) - 1;
}

float cube(vec3 pt) {
  vec3 q = abs(pt) - 1;
  return length(max(q, 0.0)) + min(max(q.x, max(q.y, q.z)), 0.0);
}

float smin(float a, float b) {
  float k = 0.2;
  float h = clamp(0.5 + 0.5 * (b-a) / k, 0, 1);
  return mix(b, a, h) - k * h * (1-h);
}

float geomOperations(vec3 pt) {
  // Pair 1 (union)
  vec3 r1 = pt - vec3(-3, 0, -3);
  float d1 = min(cube(r1), sphere(r1 - vec3(1, 0, 1)));

  // Pair 2 (difference)
  vec3 r2 = pt - vec3( 3, 0, -3);
  float d2 = max(cube(r2), -sphere(r2 - vec3(1, 0, 1)));

  // Pair 3 (intersection)
  vec3 r3 = pt - vec3( 3, 0,  3);
  float d3 = max(cube(r3), sphere(r3 - vec3(1, 0, 1)));

  // Pair 4 (blending)
  vec3 r4 = pt - vec3(-3, 0,  3);
  float d4 = smin(cube(r4), sphere(r4 - vec3(1, 0, 1)));

  return min(d1, min(d2, min(d3, d4)));
}

float geomGrounded(vec3 pt) {
  return min(geomOperations(pt), pt.y + 1);
}

vec3 getNormal(vec3 pt) {
  return normalize(GRADIENT(pt, geomGrounded));
}

vec3 getColor(vec3 pt) {
  if (pt.y > -0.9999)
    return vec3(1);
  else {
    float m = mod(geomOperations(pt), 5);
    if (m > 4.75)
      return vec3(0);
    else
      return mix(vec3(0.4, 1, 0.4), vec3(0.4, 0.4, 1), mod(m, 1));
  }
}

///////////////////////////////////////////////////////////////////////////////

float shade(vec3 eye, vec3 pt, vec3 n) {
  float val = 0;
  
  val += 0.1;  // Ambient
  
  for (int i = 0; i < LIGHT_POS.length(); i++) {
    vec3 l = normalize(LIGHT_POS[i] - pt); 
    val += max(dot(n, l), 0);
  }
  return val;
}

vec3 illuminate(vec3 camPos, vec3 rayDir, vec3 pt) {
  vec3 c, n;
  n = getNormal(pt);
  c = getColor(pt);
  return shade(camPos, pt, n) * c;
}

///////////////////////////////////////////////////////////////////////////////

vec3 raymarch(vec3 camPos, vec3 rayDir) {
  int step = 0;
  float t = 0;

  for (float d = 1000; step < RENDER_DEPTH && abs(d) > CLOSE_ENOUGH; t += abs(d)) {

    d = geomGrounded(camPos + t * rayDir);
    step++;
  }

  if (step == RENDER_DEPTH) {
    return getBackground(rayDir);
  } else if (showStepDepth) {
    return vec3(float(step) / RENDER_DEPTH);
  } else {
    return illuminate(camPos, rayDir, camPos + t * rayDir);
  }
}

///////////////////////////////////////////////////////////////////////////////

void main() {
  color = raymarch(camPos, getRayDir());
}