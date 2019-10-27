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
#define RENDER_DEPTH 1000
#define MAX_DIST 1000
#define CLOSE_ENOUGH 0.001
#define CLEARANCE (CLOSE_ENOUGH*200)
#define SHADOW_SOFTNESS 25
#define AMBIENT_LIGHT 0.005
#define FOG_COLOUR vec3(0.005, 0.007, 0.012)
#define REFLECT_AMNT 0.4

#define GRADIENT_DIST 0.02
#define GRADIENT(pt, func) vec3( \
    func(vec3(pt.x + GRADIENT_DIST, pt.y, pt.z)) - func(vec3(pt.x - GRADIENT_DIST, pt.y, pt.z)), \
    func(vec3(pt.x, pt.y + GRADIENT_DIST, pt.z)) - func(vec3(pt.x, pt.y - GRADIENT_DIST, pt.z)), \
    func(vec3(pt.x, pt.y, pt.z + GRADIENT_DIST)) - func(vec3(pt.x, pt.y, pt.z - GRADIENT_DIST)))

struct Light {
  vec3 pos;
  vec3 col;
  float radius;
  bool flicker;
};

const Light LIGHTS[] = Light[](
  Light(vec3(50, 150, 30), vec3(0.05, 0.05, 0.1), 100, false),

  Light(vec3(10, 12, -4.5), vec3(1, 0.8, 0.4), 5, true),
  Light(vec3(-10, 12,  -4.5), vec3(1, 0.8, 0.4), 5, true),

  Light(vec3(5, 21, 6), vec3(0.1, 0.08, 0.05), 25, true),
  Light(vec3(-5, 21,  6), vec3(0.1, 0.08, 0.05), 25, true),

  Light(vec3(0, 13,  -10), vec3(0.1, 0.05, 0.01), 30, true),
  Light(vec3(0, 13,  -20), vec3(0.1, 0.05, 0.01), 30, true),
  Light(vec3(0, 13,  -30), vec3(0.1, 0.05, 0.01), 30, true)
);

///////////////////////////////////////////////////////////////////////////////
// Geometry Definitions
///////////////////////////////////////////////////////////////////////////////

// Blend shapes
float smin(float a, float b) {
  float k = 0.3;
  float h = clamp(0.5 + 0.5 * (b-a) / k, 0, 1);
  return mix(b, a, h) - k * h * (1-h);
}

float sphere(vec3 pt, float r) {
  return length(pt) - r;
}

float cuboid(vec3 pt, vec3 sz) {
  vec3 q = abs(pt) - sz;
  return length(max(q, 0.0)) + min(max(q.x, max(q.y, q.z)), 0.0);
}

float torusV(vec3 pt, vec2 r) {
  vec2 q = vec2(length(pt.yz) - r.x, pt.x);
  return length(q) - r.y;
}

float torusH(vec3 pt, vec2 r) {
  vec2 q = vec2(length(pt.xz) - r.x, pt.y);
  return length(q) - r.y;
}

float vCylinder(vec3 pt, float r) {
  return length(pt.xz) - r;
}

float column(vec3 pt) {
  float d1 = max(vCylinder(pt, 1), cuboid(pt, vec3(1, 7.5, 1)));
  pt.y /= 2;
  float cosy = cos(pt.y);
  float siny = sin(pt.y);
  float d2 = cuboid(
    vec3(
      pt.x * cosy + pt.z * siny,
      pt.y * 2,
      pt.z * cosy - pt.x * siny),
    vec3(.8, 7.5, .8)
  );
  float d3 = cuboid(
    vec3(
      pt.x * siny - pt.z * cosy,
      pt.y * 2,
      pt.z * siny + pt.x * cosy),
    vec3(.8, 7.5, .8)
  );
  float d4 = cuboid(pt - vec3(0, -3.85, 0), vec3(1.4, 0.2, 1.4));
  float d5 = cuboid(pt - vec3(0, 3.85, 0), vec3(1.4, 0.2, 1.4));
  return min(d1, min(d2, min(d3, min(d4, d5) - 0.2)));
}

float columns(vec3 pt) {
  vec3 ptc1 = vec3(
    mod(pt.x + 2.5, 5) - 2.5,
    pt.y - 11.5,
    pt.z
  );
  float c1 = column(ptc1);
  float c2 = cuboid(pt, vec3(12, 50, 50));

  return max(c1, c2);
}

float ground(vec3 pt) {
  float g1 = cuboid(pt - vec3(0,0,-14), vec3(18, 1, 30));
  float g2 = cuboid(pt - vec3(0,0,-15.5), vec3(17, 2, 28.5));
  float g3 = cuboid(pt - vec3(0,0,-17), vec3(16, 3, 27));
  float g4 = cuboid(pt - vec3(0,0,-18.5), vec3(15, 4, 25.5));
  float g5 = cuboid(pt - vec3(0,0,-8), vec3(3, 1, 36));
  return min(g1, min(g2, min(g3, min(g4, g5))));
}

float building(vec3 pt) {
  float b1 = cuboid(pt - vec3(0,0,-24), vec3(12, 19, 18));
  float b2 = cuboid(pt - vec3(0,0,-23), vec3(8, 15, 18));
  float b3 = cuboid(pt - vec3(0,10,-15), vec3(20, 1.6, 1.6)) - 0.4;
  float b4 = cuboid(pt - vec3(0,10,-25), vec3(20, 1.6, 1.6)) - 0.4;
  float b5 = cuboid(pt - vec3(0,10,-35), vec3(20, 1.6, 1.6)) - 0.4;

  return max(b1, -min(b2, min(b3, min(b4, b5))));
}

float roof(vec3 pt) {
  float r1 = cuboid(pt - vec3(0,21,-20), vec3(13, 2, 24));
  vec3 ptr1 = pt - vec3(0, 4.4, 0);
  float r2 = cuboid(vec3(
    ptr1.x * sqrt(2) / 2 + ptr1.y * sqrt(2) / 2,
    ptr1.y * sqrt(2) / 2 - ptr1.x * sqrt(2) / 2,
    ptr1.z
  ), vec3(20, 20, 50));
  vec3 ptr3 = vec3(
    mod(pt.x + 2.5, 5) - 2.5,
    pt.y - 23.5,
    pt.z
  );
  float r3 = cuboid(ptr3, vec3(1.5, 1, 50));

  return max(max(r1, r2), -r3);
}

float fence(vec3 pt) {
  vec3 ptf1 = vec3(
    mod(pt.x + 2, 4) - 2,
    pt.y,
    mod(pt.z + 2, 4) - 2
  );
  float f1 = cuboid(ptf1, vec3(0.2, 2, 0.2));
  float f2 = cuboid(pt - vec3(0,0,-8), vec3(25, 49, 51));
  float f3 = cuboid(pt - vec3(0,0,-10), vec3(29, 53.5, 53));
  float f4 = sphere(ptf1 - vec3(0, 2, 0), 0.5);

  return max(smin(f1, f4), max(-f2, f3));
}

float mooring(vec3 pt) {
  float m1 = torusV(pt - vec3(2.91, 3, 22), vec2(0.7, 0.3));
  float m2 = cuboid(pt - vec3(2.4, 3, 22), vec3(0.2, 2, 0.2));
  float m3 = torusH(pt - vec3(2.4, 1.5, 27), vec2(0.25, 0.1));
  float m4 = cuboid(pt - vec3(2.4, 1, 27), vec3(0.2, 0.6, 0.2));

  return min(min(m1, m2), smin(m3, m4));
}

float boat(vec3 pt) {
  pt -= vec3(7, 0, 24);
  float a = 0.1 * sin(currentTime * 1.5);
  float sa = sin(a);
  float ca = cos(a);
  pt = vec3(
    ca * pt.x + sa * pt.y,
    ca * pt.y - sa * pt.x,
    pt.z
  );

  vec3 ptb3 = vec3(
    pt.x,
    pt.y * sqrt(2) / 2 + (pt.z - 4) * sqrt(2) / 2,
    (pt.z - 4) * sqrt(2) / 2 - pt.y * sqrt(2) / 2
  );

  float b1 = cuboid(pt, vec3(2, 1, 4));
  float b2 = cuboid(pt - vec3(0, 1, 0), vec3(1.9, 1, 3.9));
  float b3 = cuboid(ptb3, vec3(2, 1/sqrt(2), 1/sqrt(2)));
  return max(min(b1, b3), -b2);
}

vec2 wavedx(vec2 position, vec2 direction, float speed, float frequency, float timeshift) {
  float x = dot(direction, position) * frequency + timeshift * speed;
  float wave = exp(sin(x) - 1.0);
  float dx = wave * cos(x);
  return vec2(wave, -dx);
}

float water(vec3 pt) {
  float iter = 0.0;
  float phase = 6.0;
  float speed = 2.0;
  float weight = 1.0;
  float w = 0.0;
  float ws = 0.0;
  vec2 position = pt.xz / 10;
  for(int i=0;i<8;i++){
    vec2 p = vec2(sin(iter), cos(iter));
    vec2 res = wavedx(position, p, speed, phase, currentTime);
    position += normalize(p) * res.y * weight * 0.05;
    w += res.x * weight;
    iter += 12.0;
    ws += weight;
    weight = mix(weight, 0.0, 0.2);
    phase *= 1.18;
    speed *= 1.07;
  }
  return w / ws + pt.y;
}

float geomNorm(vec3 pt) {
  return min(
    ground(pt),
    min(columns(pt),
    min(building(pt),
    min(roof(pt),
    min(mooring(pt),
    min(boat(pt),
    fence(pt)))))));
}

float geomReflect(vec3 pt) {
  return water(pt);
}

float getSDF(vec3 pt) {
  return min(geomNorm(pt), geomReflect(pt));
}

vec3 getNormal(vec3 pt) {
  return normalize(GRADIENT(pt, getSDF));
}

vec3 getColor(vec3 pt) {
  if (ground(pt) < CLOSE_ENOUGH)
    return vec3(0.3);
  else if (water(pt) < CLOSE_ENOUGH)
    return vec3(0.2, 0.4, 0.8);
  else if (roof(pt) < CLOSE_ENOUGH || boat(pt) < CLOSE_ENOUGH)
    return vec3(0.5, 0.3, 0.2);
  else if (building(pt) < CLOSE_ENOUGH)
    return vec3(1) * (1 - 0.4 * pow(abs(sin(4 * pt.y)), 10));
  else if (columns(pt) < CLOSE_ENOUGH) {
    if (pt.y < 4.61 || pt.y > 18.39)
      return vec3(0.5, 0.3, 0.2);
    else
      return vec3(1);
  }
  else if (mooring(pt) < CLOSE_ENOUGH) {
    if (pt.y > 4.5)
      return vec3(1);
    else if (pt.y < 2 || pt.x < 2.605)
      return vec3(0.1);
    else if (abs(pt.z - 22) < 0.1 || abs(pt.y - 3) < 0.1)
      return vec3(1);
    else
      return vec3(1, 0.2, 0.1);
  }
  else if (fence(pt) < CLOSE_ENOUGH) {
    if (pt.y > 2)
      return vec3(1);
    else
      return vec3(0.4, 0.2, 0.15);
  }
  else
    return vec3(0);
}

float getSpec(vec3 pt) {
  if (ground(pt) < CLOSE_ENOUGH)
    return 10;
  else if (water(pt) < CLOSE_ENOUGH)
    return 1000;
  else if (roof(pt) < CLOSE_ENOUGH)
    return 2;
  else if (building(pt) < CLOSE_ENOUGH)
    return 1;
  else if (columns(pt) < CLOSE_ENOUGH)
    return 1;
  else if (mooring(pt) < CLOSE_ENOUGH)
    return 5;
  else if (fence(pt) < CLOSE_ENOUGH)
    return 80;
  else if (boat(pt) < CLOSE_ENOUGH)
    return 0.1;
  else
    return 0;
}

///////////////////////////////////////////////////////////////////////////////
// Marching / Misc.
///////////////////////////////////////////////////////////////////////////////

vec3 getBackground(vec3 dir) {
  float u = 0.5 + atan(dir.z, -dir.x) / (2 * PI);
  float v = 0.5 - asin(dir.y) / PI;
  vec4 texColor = texture(tex, vec2(u, v));
  return pow(texColor.rgb, vec3(2.2)) * 0.4;
}

vec3 getRayDir() {
  vec3 xAxis = normalize(cross(camDir, camUp));
  return normalize(pos.x * (resolution.x / resolution.y) * xAxis + pos.y * camUp + 5 * camDir);
}

///////////////////////////////////////////////////////////////////////////////
// Lighting
///////////////////////////////////////////////////////////////////////////////

float shadow(vec3 pt, vec3 lpos) {
  vec3 l = normalize(lpos - pt);

  float kd = 1;
  int step = 0;
  for (float t = CLEARANCE; t < length(lpos - pt) && step < RENDER_DEPTH && kd > 0.0001;) {
    float d = geomNorm(pt + t * l);
    if (d < CLOSE_ENOUGH) {
      kd = 0;
    } else if (t + d < length(lpos - pt)){
      // Soften shadows
      kd = min(kd, SHADOW_SOFTNESS * d / t);
    }
    t += d;
    step++;
  }

  return kd;
}

vec3 shade(vec3 eye, vec3 pt, vec3 n, vec3 c, float is) {
  vec3 val = AMBIENT_LIGHT * c;
  
  for (int i = 0; i < LIGHTS.length(); i++) {
    vec3 l = LIGHTS[i].pos - pt;
    if (length(l) > LIGHTS[i].radius * 6)
      continue;

    vec3 ln = normalize(l);
    float dt = dot(n, ln);
    vec3 r = 2 * n * dt - ln;
    vec3 v = normalize(eye - pt);

    vec3 lr = l / LIGHTS[i].radius;
    float intensity = max(shadow(pt, LIGHTS[i].pos) / dot(lr, lr), 0);
    // Attempt a flicker
    if (LIGHTS[i].flicker) {
      float ftime = currentTime + i;
      intensity *= 1 + 0.07 * sin(ftime * 11) + 0.07 * sin(ftime * 17 + i) + 0.07 * sin(ftime * 23 + i);
    }

    val += (
        max(dt, 0) * c // Diffuse light
        + max(pow(dot(r, v),2*is), 0) * is // Specular light
      ) * intensity * LIGHTS[i].col;
  }
  return val;
}

vec3 illuminate(vec3 camPos, vec3 rayDir, vec3 pt) {
  vec3 n = getNormal(pt);
  vec3 c = getColor(pt);
  float is = getSpec(pt);
  return shade(camPos, pt, n, c, is);
}

///////////////////////////////////////////////////////////////////////////////

vec3 raymarch(vec3 camPos, vec3 rayDir) {

  // Main march
  int step = 0;
  float t = 0;
  for (float d = 1000; step < RENDER_DEPTH && d > CLOSE_ENOUGH && t <= MAX_DIST; t += abs(d)) {
    d = getSDF(camPos + t * rayDir);
    step++;
  }

  // Background
  if (step == RENDER_DEPTH || t > MAX_DIST) {
    return mix(
      getBackground(rayDir),
      FOG_COLOUR,
      pow(clamp(1 - dot(rayDir, vec3(0,1,0)),0,1), 2)); // Only in horizontal
  }
  // Show iters
  else if (showStepDepth) {
    return vec3(float(step) / RENDER_DEPTH);
  }
  // Geometry
  else {
    vec3 hitPos = camPos + t * rayDir;
    vec3 col = illuminate(camPos, rayDir, hitPos);

    // Reflection ray
    if (geomReflect(hitPos) < CLOSE_ENOUGH) {
      vec3 v = hitPos - camPos;
      vec3 n = normalize(GRADIENT(hitPos, getSDF));
      rayDir = normalize(v - 2 * n * dot(v, n));

      int step = 0;
      float t = 0;
      for (float d = 1000; step < RENDER_DEPTH && d > CLOSE_ENOUGH && t <= MAX_DIST; t += abs(d)) {
        d = geomNorm(hitPos + t * rayDir);
        step++;
      }

      if (step == RENDER_DEPTH || t > MAX_DIST) {
        col += getBackground(rayDir) * REFLECT_AMNT;
      } else {
        col += illuminate(hitPos, rayDir, hitPos + t * rayDir) * REFLECT_AMNT;
      }
    }

    return mix(
      col,
      FOG_COLOUR,
      pow(t/MAX_DIST, 1) // Volumetric
        * pow(clamp(1 - dot(rayDir, vec3(0,1,0)),0,1), 2)); // Only horizontal
  }
}

///////////////////////////////////////////////////////////////////////////////

void main() {
  color = raymarch(camPos, getRayDir());
  color = color / (1+color);
  color = pow(color, vec3(1/2.2));
}