#version 330

uniform vec2 resolution;
uniform float currentTime;
uniform vec3 camPos;
uniform vec3 camDir;
uniform vec3 camUp;
uniform sampler2D texSky;
uniform sampler2D texBed;
uniform sampler2D texNorm;
uniform sampler2D texBump;
uniform bool showStepDepth;

in vec3 pos;

out vec3 color;

#define PI 3.1415926535897932384626433832795
#define SQRT2 1.4142135623730950488016887242097
#define RENDER_DEPTH 1000
#define MAX_DIST 400
#define CLOSE_ENOUGH 0.001
#define CLEARANCE (CLOSE_ENOUGH*200)
#define SHADOW_SOFTNESS 25
#define AMBIENT_LIGHT 0.001
#define FRESNEL_DEFAULT 0.5
#define BED_SCALE 0.1
#define SEA_SCALE 0.02
#define SKY_BRIGHTNESS 0.08

#define MAX_SAMPLES 18
#define MIN_SAMPLES 4

#define GRADIENT_DIST 0.005
#define GRADIENT(pt, func) vec3( \
    func(vec3(pt.x + GRADIENT_DIST, pt.y, pt.z)) - func(vec3(pt.x - GRADIENT_DIST, pt.y, pt.z)), \
    func(vec3(pt.x, pt.y + GRADIENT_DIST, pt.z)) - func(vec3(pt.x, pt.y - GRADIENT_DIST, pt.z)), \
    func(vec3(pt.x, pt.y, pt.z + GRADIENT_DIST)) - func(vec3(pt.x, pt.y, pt.z - GRADIENT_DIST)))


#define DELT 0.5

#define AABB(fn, pt, x, y, z, dx, dy, dz) \
  (cuboid(pt - vec3(x,y,z), vec3(dx + DELT,dy + DELT,dz + DELT)) < 0 \
  ? fn(pt) \
  : cuboid(pt - vec3(x,y,z), vec3(dx,dy,dz))) \

//#define AABB(fn, pt, x, y, z, dx, dy, dz) fn(pt)



struct Light {
  vec3 pos;
  vec3 col;
  float radius;
  bool flicker;
};

const Light LIGHTS[] = Light[](
  Light(vec3(50, 150, 30), vec3(0.05, 0.05, 0.1), 100, false),
  Light(vec3(0, -2, -10), vec3(0.3,0.7,1), 40, false),

  Light(vec3(10, 12, -4.5), vec3(1, 0.8, 0.4), 5, true),
  Light(vec3(-10, 12,  -4.5), vec3(1, 0.8, 0.4), 5, true),

  Light(vec3(5, 21, 6), vec3(0.1, 0.08, 0.04), 25, true),
  Light(vec3(-5, 21,  6), vec3(0.1, 0.08, 0.04), 25, true),

  Light(vec3(0, 13,  -10), vec3(0.1, 0.05, 0.01), 30, true),
  Light(vec3(0, 13,  -20), vec3(0.1, 0.05, 0.01), 30, true),
  Light(vec3(0, 13,  -30), vec3(0.1, 0.05, 0.01), 30, true)
);

struct Material {
  vec3 col;
  vec3 norm;
  float spec;
  float fres;
};

const Material MATERIALS[] = Material[](
  // Ground
  Material(vec3(0.3), vec3(0), 10, 0),
  // Seabed
  Material(vec3(0), vec3(0), 0.0002, 0),
  // Water
  Material(vec3(0.02, 0.04, 0.08), vec3(0), 1000, 1.34),
  // Roof
  Material(vec3(0.5, 0.3, 0.15), vec3(0), 2, 0),
  // Building
  Material(vec3(0), vec3(0), 1, 0),
  // Columns
  Material(vec3(0), vec3(0), 1, 0),
  // Mooring
  Material(vec3(0), vec3(0), 5, 0),
  // Fence
  Material(vec3(0), vec3(0), 80, 1.08),
  // Boat
  Material(vec3(0.5, 0.3, 0.2), vec3(0), 0.1, 0),
  // Default
  Material(vec3(1, 0, 0), vec3(0), 0, 0)
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
  float cosy = cos(pt.y * 0.5);
  float siny = sin(pt.y * 0.5);
  float d2 = cuboid(
    vec3(
      pt.x * cosy + pt.z * siny,
      pt.y,
      pt.z * cosy - pt.x * siny),
    vec3(.8, 7.5, .8)
  );
  float d3 = cuboid(
    vec3(
      pt.x * siny - pt.z * cosy,
      pt.y,
      pt.z * siny + pt.x * cosy),
    vec3(.8, 7.5, .8)
  );
  float d4 = cuboid(pt - vec3(0, -7.7, 0), vec3(1.4, 0.4, 1.4));
  float d5 = cuboid(pt - vec3(0, 7.7, 0), vec3(1.4, 0.4, 1.4));
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
  float g2 = cuboid(pt - vec3(0,1,-15.5), vec3(17, 1, 28.5));
  float g3 = cuboid(pt - vec3(0,1.5,-17), vec3(16, 1.5, 27));
  float g4 = cuboid(pt - vec3(0,2,-18.5), vec3(15, 2, 25.5));
  float g5 = cuboid(pt - vec3(0,0,-8), vec3(3, 1, 36));
  return min(g1, min(g2, min(g3, min(g4, g5))));
}

float building(vec3 pt) {
  float b1 = cuboid(pt - vec3(0,10,-24), vec3(12, 9, 18));
  float b2 = cuboid(pt - vec3(0,0,-23), vec3(8, 15, 18));
  vec3 ptb3 = vec3(
    pt.x,
    pt.y - 10,
    mod(pt.z + 2.5, 10) - 7.5
  );
  float b3 = cuboid(ptb3, vec3(20, 1.6, 1.6)) - 0.4;
  float b4 = cuboid(pt - vec3(0,10,-25), vec3(20, 20, 15));

  return max(b1, -min(b2, max(b3, b4)));
}

float roof(vec3 pt) {
  float r1 = cuboid(pt - vec3(0,21,-20), vec3(13, 2, 24));
  vec3 ptr1 = pt - vec3(0, 4.4, 0);
  float r2 = cuboid(vec3(
    ptr1.x * SQRT2 / 2 + ptr1.y * SQRT2 / 2,
    ptr1.y * SQRT2 / 2 - ptr1.x * SQRT2 / 2,
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
  float f1 = cuboid(ptf1 - vec3(0, -8, 0), vec3(0.2, 10, 0.2));
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
  float a = 0.08 * sin(currentTime * 1.5);
  float sa = sin(a);
  float ca = cos(a);
  pt = vec3(
    ca * pt.x + sa * pt.y,
    ca * pt.y - sa * pt.x,
    pt.z
  );

  vec3 ptb3 = vec3(
    pt.x,
    pt.y * SQRT2 / 2 + (pt.z - 4) * SQRT2 / 2,
    (pt.z - 4) * SQRT2 / 2 - pt.y * SQRT2 / 2
  );

  float b1 = cuboid(pt, vec3(2, 1, 4));
  float b2 = cuboid(pt - vec3(0, 1, 0), vec3(1.9, 1, 3.9));
  float b3 = cuboid(ptb3, vec3(2, 1/SQRT2, 1/SQRT2));
  float b4 = cuboid(pt - vec3(0, 2, 0), vec3(0.2, 4, 0.2));
  return min(max(min(b1, b3), -b2), b4);
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
  float weight = 1;
  float w = 0.0;
  float ws = 0.0;
  vec2 position = pt.xz * 0.1;
  for(int i=0;i<10;i++){
    vec2 p = vec2(sin(iter), cos(iter));
    vec2 res = wavedx(position, p, speed, phase, currentTime);
    position += p * res.y * weight * 0.02;
    w += res.x * weight;
    iter += 12.0;
    ws += weight;
    weight = mix(weight, 0.0, 0.2);
    phase *= 1.18;
    speed *= 1.07;
  }
  return 0.4 * w / ws + pt.y;
}

float seabed(vec3 pt) {
  return pt.y + 6;
}

float geomNorm(vec3 pt) {
  return min(
    AABB(ground, pt, 0, 2, -8, 19, 3, 36),
    min(AABB(columns, pt, 0, 11.5, 0, 12, 7.5, 2),
    min(AABB(building, pt, 0, 11.5,-24, 12, 7.5, 18),
    min(AABB(roof, pt, 0,21,-20, 13, 2, 24),
    min(AABB(mooring, pt, 2.6, 3, 24, 1, 3, 3),
    min(AABB(boat, pt, 7, 2, 24, 2, 4, 5),
    min(AABB(fence, pt, 0, -2, -10, 29, 10, 53),
    seabed(pt)
    )))))));
}

float geomReflect(vec3 pt) {
  return water(pt);
}

float getSDF(vec3 pt) {
  return min(geomNorm(pt), geomReflect(pt));
}

Material getMaterial(vec3 pt, vec3 dir) {
  Material mat;
  if (ground(pt) < CLOSE_ENOUGH)
    mat = MATERIALS[0];
  else if (seabed(pt) < CLOSE_ENOUGH) {
    mat = MATERIALS[1];

    float fParallaxLimit = -length(dir.xz) / dir.y;
    fParallaxLimit *= BED_SCALE;

    vec2 offsetDir = normalize(dir.xz);
    vec2 maxOffset = fParallaxLimit * offsetDir;
    int nSamples = int(mix(MAX_SAMPLES, MIN_SAMPLES, dot(dir, vec3(0, -1, 0))));
    float fStepSize = 1.0 / nSamples;

    float fCurrRayHeight = 1.0;
    vec2 vCurrOffset = vec2(0);
    vec2 vLastOffset = vec2(0);

    float fLastSampledHeight = 1;
    float fCurrSampledHeight = 1;

    int currSample = 0;

    while (currSample < nSamples) {
      fCurrSampledHeight = texture(texBump, pt.xz * SEA_SCALE + vCurrOffset).r;

      if (fCurrSampledHeight > fCurrRayHeight) {
        float d1 = fCurrSampledHeight - fCurrRayHeight;
        float d2 = (fCurrRayHeight + fStepSize) - fLastSampledHeight;

        float rat = d1 / (d1+d2);
        vCurrOffset = rat * vLastOffset + (1-rat) * vCurrOffset;
        currSample = nSamples + 1;
      } else {
        currSample++;
        fCurrRayHeight -= fStepSize;
        vLastOffset = vCurrOffset;
        vCurrOffset += fStepSize * maxOffset;
        fLastSampledHeight = fCurrSampledHeight;
      }
    }

    vec4 texColor = texture(texBed, pt.xz * SEA_SCALE + vCurrOffset);
    mat.col = pow(texColor.rgb, vec3(2.2));
    mat.norm = normalize(texture(texNorm, pt.xz * SEA_SCALE + vCurrOffset).xzy * 2.0 - vec3(1, 0, 1));
  }
  else if (water(pt) < CLOSE_ENOUGH)
    mat = MATERIALS[2];
  else if (roof(pt) < CLOSE_ENOUGH)
    mat = MATERIALS[3];
  else if (building(pt) < CLOSE_ENOUGH) {
    mat = MATERIALS[4];
    mat.col = vec3(1) * (1 - 0.4 * pow(abs(sin(4 * pt.y)), 10));
  }
  else if (columns(pt) < CLOSE_ENOUGH) {
    mat = MATERIALS[5];
    if (pt.y < 4.61 || pt.y > 18.39)
      mat.col = vec3(0.5, 0.3, 0.2);
    else
      mat.col = vec3(1);
  }
  else if (mooring(pt) < CLOSE_ENOUGH) {
    mat = MATERIALS[6];
    if (pt.y > 4.5)
      mat.col = vec3(1);
    else if (pt.y < 2 || pt.x < 2.605)
      mat.col = vec3(0.1);
    else if (abs(pt.z - 22) < 0.1 || abs(pt.y - 3) < 0.1)
      mat.col = vec3(1);
    else
      mat.col = vec3(1, 0.2, 0.1);
  }
  else if (fence(pt) < CLOSE_ENOUGH) {
    mat = MATERIALS[7];
    if (pt.y > 2)
      mat.col = vec3(1);
    else
      mat.col = vec3(0.4, 0.2, 0.15);
  }
  else if (boat(pt) < CLOSE_ENOUGH)
    mat = MATERIALS[8];
  else
    mat = MATERIALS[0];

  if (mat.norm == vec3(0))
    mat.norm = normalize(GRADIENT(pt, getSDF));

  return mat;
}

///////////////////////////////////////////////////////////////////////////////
// Marching / Misc.
///////////////////////////////////////////////////////////////////////////////

vec3 getBackground(vec3 dir) {
  float u = 0.5 + atan(dir.z, -dir.x) / (2 * PI);
  float v = 0.5 - asin(dir.y) / PI;
  vec4 texColor = texture(texSky, vec2(u, v));
  return pow(texColor.rgb, vec3(2.2)) * SKY_BRIGHTNESS;
}

vec3 getRayDir() {
  vec3 xAxis = normalize(cross(camDir, camUp));
  return normalize(pos.x * (resolution.x / resolution.y) * xAxis + pos.y * camUp + 5 * camDir);
}

///////////////////////////////////////////////////////////////////////////////
// Lighting
///////////////////////////////////////////////////////////////////////////////

float shadow(vec3 pt, vec3 lpos) {
  float llen = length(lpos - pt);
  vec3 l = (lpos - pt) / llen;

  float kd = 1;
  int step = 0;
  for (float t = CLEARANCE; t < llen && step < RENDER_DEPTH && kd > 0.0001;) {
    float d = geomNorm(pt + t * l);
    if (t + d < llen) {
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
    float llen = length(l);
    if (llen > LIGHTS[i].radius * 6)
      continue;

    vec3 ln = l / llen;
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

vec3 illuminate(vec3 camPos, vec3 rayDir, vec3 pt, vec3 rLCol, vec3 rFCol) {
  Material m = getMaterial(pt, rayDir);

  float kr = 0;

  if (m.fres > 0) {
    float cosi = dot(m.norm, normalize(pt - camPos)); 
    // Compute sini using Snell's law
    float sint = 1 / m.fres * sqrt(max(0.f, 1 - cosi * cosi)); 
    // Total internal reflection
    if (sint >= 1) { 
        kr = 1; 
    } 
    else { 
        float cost = sqrt(max(0.f, 1 - sint * sint)); 
        cosi = abs(cosi); 
        float Rs = ((m.fres * cosi) - (1 * cost)) / ((m.fres * cosi) + (1 * cost)); 
        float Rp = ((1 * cosi) - (m.fres * cost)) / ((1 * cosi) + (m.fres * cost)); 
        kr = (Rs * Rs + Rp * Rp) / 2; 
    } 
  }

  if (m.fres > 1.1) {
    return rLCol * kr + rFCol * (1 - kr);
  } else {
    return shade(camPos, pt, m.norm, m.col, m.spec) + rLCol * kr;
  }
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
    return getBackground(rayDir);
  }
  // Show iters
  else if (showStepDepth) {
    return vec3(float(step) / RENDER_DEPTH);
  }
  // Geometry
  else {
    vec3 hitPos = camPos + t * rayDir;
    vec3 rLCol = vec3(FRESNEL_DEFAULT);
    vec3 rFCol = vec3(0);

    if (geomReflect(hitPos) < CLOSE_ENOUGH) {
      // Refraction ray
      Material m = getMaterial(hitPos, rayDir);
      vec3 n = m.norm;

      float cosi = -dot(rayDir, n); 
      float eta = 1 / m.fres; 
      float k = 1 - eta * eta * (1 - cosi * cosi);  

      if (k > 0) {
        vec3 refDir = normalize(eta * rayDir + (eta * cosi - sqrt(k)) * n);

        int step = 0;
        float t = 0;
        for (float d = 1000; step < RENDER_DEPTH && d > CLOSE_ENOUGH && t <= MAX_DIST; t += abs(d)) {
          d = geomNorm(hitPos + t * refDir);
          step++;
        }

        rFCol = illuminate(hitPos, refDir, hitPos + t * refDir, vec3(0), vec3(0));
      }

      // Reflection ray
      vec3 v = hitPos - camPos;
      vec3 refDir = normalize(v - 2 * n * dot(v, n));

      step = 0;
      t = 0;
      for (float d = 1000; step < RENDER_DEPTH && d > CLOSE_ENOUGH && t <= MAX_DIST; t += abs(d)) {
        d = geomNorm(hitPos + t * refDir);
        step++;
      }

      if (step == RENDER_DEPTH || t > MAX_DIST) {
        rLCol = getBackground(refDir);
      } else {
        rLCol = illuminate(hitPos, refDir, hitPos + t * refDir, vec3(0), vec3(0));
      }
    }
    return illuminate(camPos, rayDir, hitPos, rLCol, rFCol);
  }
}

///////////////////////////////////////////////////////////////////////////////

void main() {
  color = raymarch(camPos, getRayDir());
  color = color / (1+color);
  color = pow(color, vec3(1/2.2));
}