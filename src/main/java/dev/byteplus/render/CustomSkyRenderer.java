package dev.byteplus.render;

import dev.byteplus.BytePlus;
import dev.byteplus.dev.DevCapture;
import dev.byteplus.module.Module;
import dev.byteplus.module.modules.visuals.CustomSkyModule;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Locale;
import java.util.SplittableRandom;
import net.minecraft.class_10868;
import net.minecraft.class_276;
import net.minecraft.class_310;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public final class CustomSkyRenderer {
   private static final GlStateGuard guard = new GlStateGuard();
   private static final int SKY_COUNT = CustomSkyModule.SKIES.length;
   private static final int UNIT3 = 12;
   private static final int UNIT2 = 13;
   private static final CustomSkyRenderer.Program[] programs = new CustomSkyRenderer.Program[SKY_COUNT];
   private static int vao;
   private static int fbo;
   private static int noise3Tex;
   private static int noise2Tex;
   private static boolean failed;
   private static double skyTime;
   private static long lastNanos = System.nanoTime();
   private static final CustomSkyRenderer.Fade WORLD = new CustomSkyRenderer.Fade();
   private static final CustomSkyRenderer.Fade MENU = new CustomSkyRenderer.Fade();
   private static double menuTime;
   private static long lastMenuNanos = System.nanoTime();
   private static final int PQ = 8;
   private static int[] profQueries;
   private static final int[] profKind = new int[]{-1, -1, -1, -1, -1, -1, -1, -1};
   private static int profHead;
   private static final double[] gpuMs = new double[32];
   private static final double[] cpuMs = new double[32];
   private static final int[] gpuN = new int[32];
   private static final int[] cpuN = new int[32];
   private static final String VERTEX = "#version 330 core\nout vec2 vNdc;\nvoid main() {\n    vec2 p = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2) * 2.0 - 1.0;\n    vNdc = p;\n    gl_Position = vec4(p, 0.0, 1.0);\n}\n";
   private static final String COMMON = "#version 330 core\nin vec2 vNdc;\nout vec4 fragColor;\nuniform mat4 uInvViewProj;\nuniform float uTime;\nuniform float uBright;\nuniform float uAlpha;\nuniform sampler3D uNoise3;\nuniform sampler2D uNoise2;\n\nfloat gPx; // angular size of one pixel (radians), so stars are never thinner than a pixel\n\nfloat hash13(vec3 p) { p = fract(p * 0.1031); p += dot(p, p.zyx + 31.32); return fract((p.x + p.y) * p.z); }\nfloat hash12(vec2 p) { vec3 q = fract(vec3(p.xyx) * 0.1031); q += dot(q, q.yzx + 33.33); return fract((q.x + q.y) * q.z); }\nvec3 hash33(vec3 p) { p = fract(p * vec3(0.1031, 0.1030, 0.0973)); p += dot(p, p.yxz + 33.33); return fract((p.xxy + p.yxx) * p.zyx); }\n\n// value noise through the GPU's texture filter: one lookup instead of 8 hashes, with smooth interpolation\nfloat noise3(vec3 x) {\n    vec3 i = floor(x), f = fract(x);\n    f = f * f * (3.0 - 2.0 * f);\n    return textureLod(uNoise3, (i + f + 0.5) * (1.0 / 64.0), 0.0).r;\n}\nfloat noise2(vec2 x) {\n    vec2 i = floor(x), f = fract(x);\n    f = f * f * (3.0 - 2.0 * f);\n    return textureLod(uNoise2, (i + f + 0.5) * (1.0 / 256.0), 0.0).r;\n}\nfloat fbm3(vec3 p) { float a = 0.5, s = 0.0; for (int i = 0; i < 5; i++) { s += a * noise3(p); p = p * 2.03 + vec3(1.7, 9.2, 3.1); a *= 0.5; } return s; }\n// 3 octaves (for soft layers and warps), scaled to the same range as fbm3\nfloat fbm3lo(vec3 p) { float a = 0.5, s = 0.0; for (int i = 0; i < 3; i++) { s += a * noise3(p); p = p * 2.03 + vec3(1.7, 9.2, 3.1); a *= 0.5; } return s * 1.107; }\nfloat fbm2(vec2 p) { float a = 0.5, s = 0.0; for (int i = 0; i < 5; i++) { s += a * noise2(p); p = p * 2.01 + vec2(3.1, 7.4); a *= 0.5; } return s; }\n\nmat3 rotY(float a) { float c = cos(a), s = sin(a); return mat3(c, 0, -s, 0, 1, 0, s, 0, c); }\nmat2 rot2(float a) { float c = cos(a), s = sin(a); return mat2(c, -s, s, c); }\n\n// One layer of stars on the sphere: a round gaussian core (at least a pixel wide, so nothing shimmers)\n// with a soft glow; bright ones get small diffraction spikes. Colours follow star temperatures.\nvec3 starLayer(vec3 d, float scale, float density, float t, float seed, bool spikes) {\n    vec3 p = d * scale;\n    vec3 id = floor(p);\n    float h = hash13(id + seed);\n    if (h < 1.0 - density) return vec3(0.0);\n    vec3 r1 = hash33(id + seed + 3.1);\n    vec3 r2 = hash33(id + seed + 9.4);\n    vec3 q = (fract(p) - 0.5) - (r1 - 0.5) * 0.4;\n    float radial = dot(q, d);\n    q -= d * radial;                                   // onto the sky plane: stars stay round\n    float r = length(q);\n    float mag = r2.x * r2.x * r2.x;                     // most are faint, a few are brilliant\n    float px = gPx * scale;\n    float sz = max(0.045 + 0.06 * mag, px * 0.75);\n    float tw = 0.72 + 0.28 * sin(t * (1.3 + h * 6.0) + r2.z * 91.0);\n    float core = exp(-r * r / (sz * sz)) * (0.04 + 0.06 * mag) / sz;   // same energy whatever the size\n    float halo = exp(-r / (0.035 + 0.09 * mag)) * mag * 0.6;\n    float k = r2.y;\n    vec3 tint = k < 0.28 ? vec3(0.62, 0.76, 1.0) : (k < 0.84 ? vec3(1.0, 0.97, 0.93) : vec3(1.0, 0.74, 0.5));\n    vec3 col = tint * (core * (0.6 + 2.6 * mag) + halo);\n    if (spikes && mag > 0.4) {\n        vec3 up = abs(d.y) < 0.99 ? vec3(0.0, 1.0, 0.0) : vec3(1.0, 0.0, 0.0);\n        vec3 t1 = normalize(cross(up, d)), t2 = cross(d, t1);\n        vec2 q2 = vec2(dot(q, t1), dot(q, t2));\n        float w = px * 0.6 + 0.004;\n        float sp = exp(-abs(q2.x) / w) * exp(-abs(q2.y) * 10.0) + exp(-abs(q2.y) / w) * exp(-abs(q2.x) * 10.0);\n        col += tint * sp * (mag - 0.4) * 1.8;\n    }\n    float fade = smoothstep(0.5, 0.3, r) * smoothstep(0.32, 0.16, abs(radial));\n    return col * tw * fade;\n}\n\nvec3 stars(vec3 d, float density, float t) {\n    return starLayer(d, 64.0, density * 0.3, t, 0.0, true) * 1.5\n         + starLayer(d, 140.0, density, t, 17.0, false)\n         + starLayer(d, 300.0, min(1.0, density * 1.5), t, 41.0, false) * 0.5;\n}\n\nvec3 shootingStar(vec3 d, float t, float period) {\n    float cyc = floor(t / period), local = fract(t / period);\n    if (local > 0.22) return vec3(0.0);\n    vec3 a = normalize(vec3(hash12(vec2(cyc, 1.0)) * 2.0 - 1.0, 0.45 + hash12(vec2(cyc, 2.0)) * 0.5, hash12(vec2(cyc, 3.0)) * 2.0 - 1.0));\n    vec3 dir = normalize(cross(a, vec3(hash12(vec2(cyc, 4.0)) - 0.5, -1.0, hash12(vec2(cyc, 5.0)) - 0.5)));\n    vec3 b = normalize(a + dir * 0.5);\n    float prog = local / 0.22;\n    vec3 n = normalize(cross(a, b));\n    float dist = abs(dot(d, n));\n    if (dist > 0.03) return vec3(0.0);\n    vec3 proj = normalize(d - n * dot(d, n));\n    if (dot(cross(a, proj), n) < 0.0) return vec3(0.0);\n    float ang = acos(clamp(dot(proj, a), -1.0, 1.0));\n    float head = acos(clamp(dot(normalize(mix(a, b, prog)), a), -1.0, 1.0));\n    if (ang > head || ang < head - 0.22) return vec3(0.0);\n    float tail = 1.0 - (head - ang) / 0.22;\n    float w = max(0.0025, gPx * 1.2);\n    vec3 c = mix(vec3(0.55, 0.8, 1.0), vec3(1.0), tail * tail);\n    return c * exp(-dist * dist / (w * w)) * tail * tail * (1.0 - prog * 0.5) * 2.4;\n}\n";
   private static final String MAIN = "void main() {\n    vec4 p = uInvViewProj * vec4(vNdc, 1.0, 1.0);\n    vec3 d = normalize(p.xyz / p.w);\n    gPx = max(length(fwidth(d)) * 0.7, 1e-5);\n    float t = uTime;\n    vec3 dr = rotY(t * 0.004) * d;\n    vec3 col = %CALL%;\n    col *= uBright;\n    col = col / (1.0 + col * 0.15);\n    col += (hash12(gl_FragCoord.xy) - 0.5) / 255.0;\n    fragColor = vec4(max(col, 0.0), uAlpha);\n}\n";
   private static final String[] SKY_CALL = new String[]{
      "galaxy(dr, t)",
      "aurora(d, t)",
      "nebula(dr, t)",
      "starryNight(dr, t)",
      "deepSpace(dr, t)",
      "sunset(d, t)",
      "synthwave(d, t)",
      "bloodMoon(d, t)",
      "cottonCandy(d, t)",
      "voidSky(dr, t)",
      "blackHole(d, t)",
      "ringedPlanet(d, t)"
   };
   private static final String[] SKY_SOURCE = new String[]{
      "vec3 galaxy(vec3 d, float t) {\n    vec3 n = normalize(vec3(0.35, 1.0, 0.25));           // galactic pole\n    float bd = dot(d, n);\n    vec3 coreDir = normalize(cross(n, vec3(0.3, 0.0, 1.0)));\n    float band = exp(-bd * bd * 13.0);\n    float bulge = exp(-bd * bd * 30.0) * pow(max(dot(d, coreDir), 0.0), 5.0);\n    vec3 col = mix(vec3(0.008, 0.007, 0.028), vec3(0.035, 0.026, 0.085), band);\n    float dust = 0.0;\n    if (band + bulge > 0.004) {                            // the Milky Way, only where it can be seen\n        float w = fbm3lo(d * 2.6 + vec3(0.0, 0.0, t * 0.01));\n        float neb = fbm3(d * 4.4 + w * 1.8 + vec3(3.0));\n        vec3 gc = mix(vec3(0.34, 0.16, 0.85), vec3(0.12, 0.55, 1.0), smoothstep(0.3, 0.7, neb));\n        gc = mix(gc, vec3(1.0, 0.32, 0.6), smoothstep(0.55, 0.8, w));\n        col += gc * band * (0.22 + 1.5 * neb * neb);\n        col += vec3(1.0, 0.78, 0.5) * bulge * (1.1 + 1.2 * neb);\n        col += vec3(1.0, 0.93, 0.96) * exp(-bd * bd * 120.0) * 0.55 * neb;\n        dust = smoothstep(0.48, 0.74, fbm3(d * 8.5 + w * 1.3));\n        col *= 1.0 - (band + bulge) * dust * 0.8;\n        col += vec3(1.0, 0.3, 0.55) * pow(fbm3lo(d * 13.0 + 7.0), 4.0) * band * 2.4;\n    }\n    col += stars(d, 0.1 + 0.3 * band, t) * (1.0 - band * dust * 0.6);\n    col += shootingStar(d, t, 9.0);\n    return col;\n}\n",
      "// One curtain of aurora: a wavy line on the \"ground\", lit at rising altitudes so the light forms tall\n// vertical sheets with rays, green at the bottom and violet at the top. Only the altitudes where this\n// view ray can actually meet the curtain are sampled, so the samples go where the light is.\nvec3 auroraCurtain(vec3 d, float t, float seed, float dist, float yaw) {\n    vec3 rd = rotY(yaw) * d;\n    if (rd.y <= 0.02) return vec3(0.0);\n    float k = -rd.z / rd.y;                               // how far \"north\" the ray gets per unit of height\n    float lo = dist - 7.7, hi = dist + 7.7;               // wave amplitude + sheet glow\n    float h0 = 1.0, h1 = 3.25;\n    if (k > 1e-4) { h0 = max(h0, lo / k); h1 = min(h1, hi / k); }\n    else if (k < -1e-4) { h0 = max(h0, hi / k); h1 = min(h1, lo / k); }\n    else if (lo > 0.0) return vec3(0.0);\n    if (h1 <= h0) return vec3(0.0);\n    // the sheet looks thinner (in altitude) the flatter you look through it: sample just densely enough\n    int N = int(clamp(ceil((h1 - h0) * max(abs(k), 0.5) / 0.3), 6.0, 20.0));\n    float dh = (h1 - h0) / float(N);\n    // interleaved-gradient dither: a fine, even pattern instead of clumpy grain\n    float jit = fract(52.9829189 * fract(dot(gl_FragCoord.xy + seed * 23.0, vec2(0.06711056, 0.00583715))));\n    vec3 acc = vec3(0.0);\n    for (int i = 0; i < 20; i++) {\n        if (i >= N) break;\n        float h = h0 + (float(i) + jit) * dh;\n        vec2 p = rd.xz * (h / rd.y);\n        float path = sin(p.x * 0.16 + seed + t * 0.05) * 2.4 + sin(p.x * 0.43 - t * 0.07 + seed * 2.0) * 0.9\n                   + (noise2(vec2(p.x * 0.22 + seed, t * 0.025)) - 0.5) * 3.2;\n        float sheet = exp(-abs(-p.y - dist - path) * 1.5);\n        if (sheet < 0.012) continue;\n        float rays = 0.35 + 0.65 * noise2(vec2(p.x * 2.4 + seed * 3.0, t * 0.3));\n        rays *= 0.55 + 0.45 * noise2(vec2(p.x * 8.0 - t * 0.5, seed * 5.0));\n        float fi = (h - 1.0) / 0.075;                     // colour by altitude\n        float lower = smoothstep(0.0, 2.5, fi);\n        float fall = exp(-fi * 0.085);\n        vec3 c = mix(vec3(1.0, 0.35, 0.6), vec3(0.15, 1.0, 0.45), smoothstep(0.0, 2.0, fi));\n        c = mix(c, vec3(0.2, 0.95, 0.8), smoothstep(6.0, 13.0, fi));\n        c = mix(c, vec3(0.78, 0.3, 1.0) * 1.3, smoothstep(12.0, 24.0, fi));\n        acc += c * sheet * rays * lower * fall;\n    }\n    // each sample stands for dh of height (the look was tuned with samples 0.075 apart)\n    return acc * (dh / 0.075) * 0.36 * smoothstep(0.03, 0.16, rd.y);\n}\n\nvec3 aurora(vec3 d, float t) {\n    vec3 col = mix(vec3(0.02, 0.06, 0.08), vec3(0.0, 0.008, 0.03), smoothstep(0.0, 0.7, d.y));\n    col += stars(d, 0.12, t) * 0.9;\n    vec3 a = auroraCurtain(d, t, 0.0, 6.0, 0.0)\n           + auroraCurtain(d, t * 1.15, 4.7, 9.0, 0.9) * 0.8\n           + auroraCurtain(d, t * 0.85, 9.3, 5.0, -1.4) * 0.7;\n    col += a * (1.0 + 0.25 * sin(t * 0.7));\n    col += vec3(0.04, 0.3, 0.18) * exp(-abs(d.y) * 7.0) * 0.45;   // green airglow on the horizon\n    col += shootingStar(d, t, 11.0);\n    return col;\n}\n",
      "vec3 nebula(vec3 d, float t) {\n    vec3 col = vec3(0.012, 0.008, 0.03);\n    vec3 q = d * 2.2 + vec3(0.0, t * 0.006, 0.0);\n    float w1 = fbm3lo(q);\n    float w2 = fbm3lo(q * 1.6 + w1 * 2.2 + vec3(4.1, 1.3, 7.7));\n    float n = fbm3(q * 2.1 + w2 * 2.6 + vec3(1.0, 5.0, 3.0));\n    vec3 c = mix(vec3(0.45, 0.2, 0.95), vec3(1.0, 0.25, 0.55), smoothstep(0.35, 0.65, w2));\n    c = mix(c, vec3(0.1, 0.65, 0.9), smoothstep(0.55, 0.8, w1) * 0.8);\n    float dens = pow(smoothstep(0.25, 0.85, n), 1.6);\n    col += c * dens * 1.7;\n    col += vec3(1.0, 0.72, 0.38) * pow(smoothstep(0.5, 0.95, n * w2 * 1.7), 3.0) * 2.0;\n    col *= 1.0 - smoothstep(0.55, 0.8, fbm3lo(d * 6.0 + w2)) * 0.6;\n    col += stars(d, 0.12 + dens * 0.2, t);\n    return col;\n}\n",
      "vec3 starryNight(vec3 d, float t) {\n    vec3 col = mix(vec3(0.07, 0.11, 0.24), vec3(0.008, 0.016, 0.05), smoothstep(0.0, 0.7, d.y));\n    col += stars(d, 0.26, t) * 1.3;\n    vec3 n = normalize(vec3(-0.4, 1.0, 0.5));\n    float bd = dot(d, n);\n    float band = exp(-bd * bd * 10.0);\n    if (band > 0.01) col += vec3(0.2, 0.28, 0.5) * band * fbm3lo(d * 4.0 + t * 0.004) * 0.35;\n    col += vec3(1.0, 0.55, 0.25) * exp(-abs(d.y) * 14.0) * 0.08;   // a far-off town's glow\n    col += shootingStar(d, t, 5.0) + shootingStar(d, t + 2.7, 7.0);\n    return col;\n}\n",
      "vec3 deepSpace(vec3 d, float t) {\n    vec3 col = vec3(0.0, 0.0, 0.008);\n    col += vec3(0.05, 0.08, 0.2) * pow(fbm3lo(d * 2.0 + t * 0.003), 3.0) * 0.7;\n    col += stars(d, 0.2, t) * 1.2;\n    vec3 c = normalize(vec3(0.5, 0.6, -0.6));\n    float cd = dot(d, c);\n    if (cd > 0.85) {\n        vec3 tx = normalize(cross(c, vec3(0.0, 1.0, 0.0)));\n        vec3 ty = cross(tx, c);\n        vec2 q = vec2(dot(d, tx), dot(d, ty)) * vec2(1.0, 1.6);\n        float r = length(q) + 1e-4;\n        float th = atan(q.y, q.x);\n        float spiral = 0.5 + 0.5 * cos(th * 2.0 - log(r) * 6.0 + t * 0.05);\n        float glow = exp(-r * 22.0) * 1.3 + exp(-r * 9.0) * spiral * 0.5;\n        col += mix(vec3(0.35, 0.5, 1.0), vec3(1.0, 0.95, 0.9), exp(-r * 40.0)) * glow;\n    }\n    return col;\n}\n",
      "vec3 sunset(vec3 d, float t) {\n    vec3 s = normalize(vec3(sin(t * 0.01) * 0.3, 0.07, -1.0));\n    float e = d.y;\n    vec3 col = mix(vec3(1.0, 0.5, 0.22), vec3(0.32, 0.2, 0.48), smoothstep(-0.05, 0.5, e));\n    col = mix(col, vec3(0.07, 0.06, 0.2), smoothstep(0.35, 1.0, e));\n    float sd = max(dot(d, s), 0.0);\n    col += vec3(1.0, 0.6, 0.3) * pow(sd, 40.0) + vec3(0.7, 0.3, 0.12) * pow(sd, 6.0) * 0.6;\n    col = mix(col, vec3(1.0, 0.92, 0.75), smoothstep(0.9986, 0.9991, dot(d, s)));\n    if (d.y > 0.01) {\n        vec2 p = d.xz / d.y;\n        float c = fbm2(p * 0.55 + t * vec2(0.02, 0.006));\n        float cl = smoothstep(0.48, 0.78, c) * smoothstep(0.01, 0.15, d.y);\n        vec3 cc = mix(vec3(0.3, 0.15, 0.25), vec3(1.0, 0.55, 0.32), pow(sd, 2.5));\n        col = mix(col, cc, cl * 0.85);\n    }\n    if (d.y > 0.3) col += stars(d, 0.05, t) * smoothstep(0.35, 0.8, d.y) * 0.6;\n    return col;\n}\n",
      "vec3 synthwave(vec3 d, float t) {\n    vec3 col = mix(vec3(1.0, 0.32, 0.55), vec3(0.28, 0.05, 0.38), smoothstep(0.0, 0.35, d.y));\n    col = mix(col, vec3(0.03, 0.0, 0.08), smoothstep(0.3, 0.9, d.y));\n    if (d.y > 0.2) col += stars(d, 0.1, t) * smoothstep(0.25, 0.6, d.y);\n    vec3 s = normalize(vec3(0.0, 0.13, -1.0));\n    float sa = acos(clamp(dot(d, s), -1.0, 1.0));\n    float rel = d.y - s.y;\n    if (sa < 0.2) {\n        vec3 sc = mix(vec3(1.0, 0.25, 0.6), vec3(1.0, 0.9, 0.3), smoothstep(-0.15, 0.15, rel));\n        float stripe = rel < 0.02 ? step(0.55 + rel * 6.0, fract(rel * 38.0 - t * 0.25)) : 1.0;\n        col = mix(col, sc, smoothstep(0.2, 0.195, sa) * stripe);\n    }\n    col += vec3(1.0, 0.3, 0.65) * exp(-sa * 6.0) * 0.35;\n    col += vec3(1.0, 0.35, 0.75) * exp(-abs(d.y) * 30.0) * 0.6;\n    return col;\n}\n",
      "vec3 bloodMoon(vec3 d, float t) {\n    vec3 col = mix(vec3(0.28, 0.025, 0.02), vec3(0.03, 0.0, 0.006), smoothstep(-0.05, 0.75, d.y));\n    float mist = fbm3lo(d * 2.2 + vec3(t * 0.012, 0.0, t * 0.006));\n    col += vec3(0.45, 0.04, 0.03) * pow(mist, 2.5) * 0.8;\n    col += stars(d, 0.07, t) * vec3(1.0, 0.55, 0.5) * 0.8;\n    vec3 m = normalize(vec3(-0.35, 0.42, -0.84));\n    float md = dot(d, m);\n    float ang = acos(clamp(md, -1.0, 1.0));\n    float R = 0.11;                                              // a huge moon\n    col += vec3(1.0, 0.13, 0.05) * (exp(-ang * 8.0) * 0.5 + exp(-max(ang - R, 0.0) * 40.0) * 0.55);\n    if (ang < R + gPx * 2.0) {\n        vec3 tx = normalize(cross(m, vec3(0.0, 1.0, 0.0)));\n        vec3 ty = cross(tx, m);\n        vec2 q = vec2(dot(d, tx), dot(d, ty)) / sin(R);\n        float rr = length(q);\n        float z = sqrt(max(0.0, 1.0 - rr * rr));\n        vec3 nrm = vec3(q, z);\n        vec2 uv = q / (1.0 + z * 0.35);                         // curve the surface around the sphere\n        float maria = smoothstep(0.42, 0.66, fbm2(uv * 1.7 + 4.0));\n        float crat = 0.0;\n        for (int i = 0; i < 3; i++) {\n            vec2 g = uv * (4.0 + float(i) * 3.5) + float(i) * 7.3;\n            vec2 cid = floor(g), f = fract(g) - 0.5;\n            float hh = hash12(cid + float(i) * 13.0);\n            vec2 o = (vec2(hash12(cid + 2.3), hash12(cid + 7.1)) - 0.5) * 0.4;\n            float cr = length(f - o);\n            float rad = 0.12 + 0.18 * hash12(cid + 4.4);\n            float pit = smoothstep(rad, rad * 0.55, cr);\n            float rim = smoothstep(rad * 0.7, rad, cr) * smoothstep(rad * 1.35, rad, cr);\n            crat += (rim * 0.4 - pit * 0.3) * step(0.5, hh);\n        }\n        float light = 0.4 + 0.6 * max(dot(nrm, normalize(vec3(-0.45, 0.35, 0.82))), 0.0);\n        vec3 surf = mix(vec3(1.0, 0.36, 0.2), vec3(0.5, 0.06, 0.04), maria) * (0.9 + crat) * light;\n        surf *= 0.5 + 0.5 * z;                                  // limb darkening\n        surf += vec3(1.0, 0.22, 0.1) * pow(1.0 - z, 3.0) * 0.7;  // red rim\n        float edge = smoothstep(R + gPx, R - gPx, ang);\n        col = mix(col, surf * 1.5, edge);\n    }\n    col += vec3(0.55, 0.05, 0.03) * smoothstep(0.55, 0.85, fbm3lo(d * 4.0 + vec3(t * 0.02, 0.0, 0.0))) * 0.35;\n    return col;\n}\n",
      "vec3 cottonCandy(vec3 d, float t) {\n    vec3 col = mix(vec3(1.0, 0.74, 0.86), vec3(0.55, 0.74, 1.0), smoothstep(0.0, 0.75, d.y));\n    if (d.y > 0.01) {\n        vec2 p = d.xz / d.y;\n        float c = fbm2(p * 0.45 + t * vec2(0.015, 0.01));\n        float cl = smoothstep(0.45, 0.72, c) * smoothstep(0.01, 0.2, d.y);\n        col = mix(col, mix(vec3(1.0, 0.85, 0.95), vec3(1.0), c), cl * 0.9);\n    }\n    return col;\n}\n",
      "vec3 voidSky(vec3 d, float t) {\n    vec3 col = vec3(0.004, 0.0, 0.01);\n    float w = fbm3(d * 2.0 + fbm3lo(d * 4.0 + t * 0.04) * 1.5);\n    col += vec3(0.35, 0.08, 0.6) * pow(w, 4.0) * 1.5;\n    col += stars(d, 0.06, t) * 0.8;\n    return col;\n}\n",
      "// background for the black hole: nebula + stars (sampled through the lens)\nvec3 bhBackground(vec3 d, float t) {\n    float w = fbm3lo(d * 2.0 + vec3(0.0, 0.0, t * 0.005));\n    float n = fbm3(d * 3.5 + w * 2.0);\n    vec3 col = vec3(0.008, 0.006, 0.02);\n    col += mix(vec3(0.24, 0.08, 0.44), vec3(0.08, 0.2, 0.52), n) * n * n * 0.9;\n    col += vec3(1.0, 0.55, 0.3) * pow(max(n - 0.5, 0.0), 2.0) * 1.4;\n    col += stars(d, 0.18, t);\n    return col;\n}\n\nvec3 blackHole(vec3 d, float t) {\n    vec3 c = normalize(vec3(-0.25, 0.4, -0.88));\n    float cd = dot(d, c);\n    if (cd < 0.15) return bhBackground(d, t);\n    vec3 tx = normalize(cross(c, vec3(0.0, 1.0, 0.0)));\n    vec3 ty = cross(tx, c);\n    vec2 r = vec2(dot(d, tx), dot(d, ty)) / cd;            // flat coordinates around the hole\n    float rs = 0.085;\n    float dd = length(r);\n    vec2 rn = r / max(dd, 1e-4);\n    float bend = rs * rs * 1.7 / max(dd, rs * 0.6) * smoothstep(0.15, 0.45, cd);\n    vec2 lr = r + rn * bend;                               // gravitational lensing\n    float shadow = smoothstep(rs * 1.02, rs * 0.95, dd);\n    vec3 col = shadow < 1.0 ? bhBackground(normalize(c + tx * lr.x + ty * lr.y), t) : vec3(0.0);\n    vec2 q = rot2(-0.22) * r;\n    vec2 e = vec2(q.x, q.y / 0.24);\n    float er = length(e);\n    float inner = rs * 1.5, outer = rs * 4.8;\n    float band = smoothstep(inner, inner * 1.18, er) * (1.0 - smoothstep(outer * 0.6, outer, er));\n    vec3 disk = vec3(0.0);\n    if (band > 0.001) {\n        float ang = atan(e.y, e.x);\n        float spin = t * 1.6 * pow(rs / max(er, rs), 1.5);\n        float swirl = fbm2(vec2(ang * 2.5 + spin * 3.0, log(max(er, 1e-4)) * 7.0 - t * 0.2));\n        float streaks = 0.55 + 0.45 * sin(ang * 14.0 + spin * 8.0 + swirl * 6.0);\n        float heat = smoothstep(outer * 0.7, inner, er);\n        vec3 hot = mix(mix(vec3(0.9, 0.3, 0.08), vec3(1.0, 0.65, 0.25), heat), vec3(1.0, 0.97, 0.9), pow(heat, 2.5));\n        float beam = 0.5 + 0.8 * (0.5 + 0.5 * cos(ang + 0.4));\n        disk = hot * band * (0.35 + 0.9 * swirl) * streaks * beam * 2.4;\n        if (q.y > 0.0) disk *= 1.0 - shadow;\n    }\n    float halo = exp(-pow((dd - rs * 1.28) / (rs * 0.34), 2.0));\n    vec3 haloCol = vec3(0.0);\n    if (halo > 0.003) {\n        float hang = atan(rn.y, rn.x);\n        float hswirl = fbm2(vec2(hang * 3.0 - t * 0.9, dd * 9.0 / rs));\n        vec2 qn = rot2(-0.22) * rn;\n        float upper = smoothstep(-0.35, 0.6, qn.y);\n        haloCol = vec3(1.0, 0.72, 0.4) * halo * (0.35 + 0.8 * hswirl) * (0.35 + 0.9 * upper) * 1.5;\n    }\n    float photon = exp(-pow((dd - rs * 1.04) / (rs * 0.035), 2.0));\n    vec3 glow = vec3(1.0, 0.5, 0.2) * exp(-max(dd - rs, 0.0) / (rs * 1.8)) * 0.3;\n    // relativistic jets along the spin axis\n    float jet = exp(-abs(q.x) / (rs * 0.06 + abs(q.y) * 0.06)) * exp(-abs(q.y) / (rs * 7.0)) * smoothstep(rs * 1.1, rs * 2.2, abs(q.y));\n    vec3 jets = jet > 0.002 ? vec3(0.45, 0.65, 1.0) * jet * (0.6 + 0.4 * noise2(vec2(q.y * 4.0 / rs - t * 2.0, 0.5))) * 0.9 : vec3(0.0);\n    col *= 1.0 - shadow;\n    col += disk + haloCol * (1.0 - shadow) + vec3(1.0, 0.93, 0.8) * photon * 1.2 + glow * (1.0 - shadow * 0.7) + jets * (1.0 - shadow);\n    return col;\n}\n",
      "float sphereHit(vec3 ro, vec3 rd, vec3 c, float r) {\n    vec3 oc = ro - c;\n    float b = dot(oc, rd);\n    float h = b * b - dot(oc, oc) + r * r;\n    if (h < 0.0) return -1.0;\n    return -b - sqrt(h);\n}\n\nvec3 ringedPlanet(vec3 d, float t) {\n    vec3 col = mix(vec3(0.03, 0.05, 0.1), vec3(0.008, 0.012, 0.035), smoothstep(0.0, 0.6, abs(d.y)));\n    col += vec3(0.12, 0.2, 0.42) * pow(fbm3lo(d * 2.5 + 3.0), 3.0) * 0.6;\n    col += stars(d, 0.17, t);\n    vec3 dirP = normalize(vec3(0.5, 0.36, -0.79));\n    vec3 P = dirP * 10.0;\n    float R = 1.55;\n    vec3 sun = normalize(vec3(-0.85, 0.3, 0.25));\n    vec3 N = normalize(vec3(0.2, 1.0, 0.36));\n    float angR = asin(R / 10.0);\n    float ang = acos(clamp(dot(d, dirP), -1.0, 1.0));\n    if (ang > 0.52) return col;                              // nowhere near the planet, its rings or its moon\n    float onPlanet = smoothstep(angR + gPx, angR - gPx, ang);\n    // planet surface (rays near the limb are pulled onto the silhouette so the edge is smooth)\n    vec3 pcol = vec3(0.0);\n    float tp = -1.0;\n    if (ang < angR + gPx * 2.0) {\n        vec3 dd = normalize(mix(dirP, d, min(1.0, (angR - gPx * 0.5) / max(ang, 1e-5))));\n        if (ang < angR - gPx * 0.5) dd = d;\n        tp = sphereHit(vec3(0.0), dd, P, R);\n        if (tp < 0.0) tp = dot(P, dd);\n        vec3 hp = dd * tp;\n        vec3 n = normalize(hp - P);\n        float lat = dot(n, N);\n        vec3 bx = normalize(cross(N, vec3(0.0, 0.0, 1.0)));\n        vec3 bz = cross(bx, N);\n        float lon = atan(dot(n, bz), dot(n, bx)) + t * 0.01;\n        float turb = fbm2(vec2(lon * 3.0, lat * 16.0) + t * 0.004);\n        float bnd = sin(lat * 24.0 + turb * 3.2) * 0.5 + 0.5;\n        vec3 pc = mix(vec3(0.78, 0.56, 0.38), vec3(0.98, 0.9, 0.76), bnd);\n        pc = mix(pc, vec3(0.55, 0.36, 0.24), smoothstep(0.58, 0.85, fbm2(vec2(lon * 6.0, lat * 38.0))) * 0.55);\n        float spot = smoothstep(0.16, 0.06, length(vec2(sin((lon - 0.7) * 0.5) * 2.0, (lat + 0.3) * 3.2)));\n        pc = mix(pc, vec3(0.86, 0.4, 0.26), spot * 0.85);\n        float diff = max(dot(n, sun), 0.0);\n        float ringShadow = 1.0;\n        float trs = dot(P - hp, N) / dot(sun, N);\n        if (trs > 0.0) {\n            float rr = length(hp + sun * trs - P) / R;\n            if (rr > 1.25 && rr < 2.35) ringShadow = 0.3;\n        }\n        pcol = pc * (0.025 + 1.2 * diff * ringShadow);\n        float rim = pow(1.0 - max(dot(n, -dd), 0.0), 3.0);\n        pcol += vec3(0.5, 0.68, 1.0) * rim * smoothstep(-0.2, 0.3, dot(n, sun)) * 0.7;\n    }\n    col = mix(col, pcol, onPlanet);\n    // rings\n    float denom = dot(d, N);\n    float tr = abs(denom) > 1e-5 ? dot(P, N) / denom : -1.0;\n    if (tr > 0.0) {\n        vec3 wp = d * tr;\n        float rr = length(wp - P) / R;\n        if (rr > 1.2 && rr < 2.4) {\n            float bands = 0.55 + 0.45 * sin(rr * 58.0) * sin(rr * 23.0 + 1.0);\n            float fine = noise2(vec2(rr * 70.0, 0.5));\n            float gap = smoothstep(0.0, 0.025, abs(rr - 1.8)) * smoothstep(0.0, 0.012, abs(rr - 2.07));\n            float edge = smoothstep(1.24, 1.32, rr) * smoothstep(2.36, 2.24, rr);\n            float a = edge * gap * (0.35 + 0.55 * bands * (0.6 + 0.4 * fine));\n            vec3 rc = mix(vec3(0.86, 0.76, 0.6), vec3(0.62, 0.52, 0.42), fine);\n            rc = mix(rc, vec3(0.98, 0.9, 0.78), smoothstep(1.4, 1.7, rr) * (1.0 - smoothstep(1.72, 1.8, rr)));\n            float lit = 0.4 + 0.6 * abs(dot(N, sun));\n            float sh = sphereHit(wp, sun, P, R) > 0.0 ? 0.07 : 1.0;\n            bool front = tp < 0.0 || tr < tp || onPlanet < 0.5;\n            if (front) col = mix(col, rc * lit * sh, clamp(a, 0.0, 1.0));\n        }\n    }\n    // a small icy moon\n    vec3 mdir = normalize(vec3(0.82, 0.22, -0.53));\n    float ma = acos(clamp(dot(d, mdir), -1.0, 1.0));\n    float mr = 0.018;\n    if (ma < mr + gPx * 2.0) {\n        vec3 mt = normalize(cross(mdir, vec3(0.0, 1.0, 0.0)));\n        vec3 mb = cross(mt, mdir);\n        vec2 mq = vec2(dot(d, mt), dot(d, mb)) / mr;\n        vec3 mn = vec3(mq, sqrt(max(0.0, 1.0 - dot(mq, mq))));\n        float ml = max(dot(mn, normalize(vec3(-0.9, 0.3, 0.3))), 0.0);\n        vec3 mc = vec3(0.8, 0.86, 0.95) * (0.02 + ml) * (0.85 + 0.15 * noise2(mq * 6.0));\n        col = mix(col, mc, smoothstep(mr + gPx, mr - gPx, ma));\n    }\n    return col;\n}\n"
   };

   private CustomSkyRenderer() {
   }

   public static boolean draw() {
      if (!failed && Projection.isValid()) {
         CustomSkyModule m = BytePlus.modules() == null ? null : skyModule();
         if (m == null) {
            return false;
         }

         int sky = m.skyIndex();
         float k = WORLD.mix(sky);
         return drawWith(new Matrix4f(Projection.viewProj()).invert(), sky, WORLD.previous, k, m.brightness.getFloat(), m.speed.getFloat());
      } else {
         return false;
      }
   }

   public static boolean drawMenu() {
      CustomSkyModule m = BytePlus.modules() == null ? null : skyModule();
      return drawMenu(m != null ? m.skyIndex() : 0, 0.0F, 0.0F);
   }

   public static boolean drawMenu(int sky, float yawOffset, float pitchOffset) {
      if (!failed && BytePlus.modules() != null) {
         CustomSkyModule m = skyModule();
         class_276 target = class_310.method_1551().method_1522();
         float aspect = (float)target.field_1482 / Math.max(1, target.field_1481);
         float yaw = (float)(menuTime * 2.2) + yawOffset;
         Matrix4f vp = new Matrix4f()
            .perspective((float)Math.toRadians(80.0), aspect, 0.05F, 100.0F)
            .rotateX((float)Math.toRadians(-12.0F + pitchOffset))
            .rotateY((float)Math.toRadians(yaw));
         long now = System.nanoTime();
         menuTime = menuTime + Math.min(0.1, (now - lastMenuNanos) / 1.0E9);
         lastMenuNanos = now;
         float k = MENU.mix(sky);
         return drawWith(vp.invert(), sky, MENU.previous, k, m != null ? m.brightness.getFloat() : 1.0F, m != null ? m.speed.getFloat() : 1.0F);
      } else {
         return false;
      }
   }

   public static String devStats(int sky) {
      String s = String.format(
         Locale.ROOT,
         "gpu %.3f ms (n=%d)  cpu %.3f ms (n=%d)",
         gpuN[sky] == 0 ? 0.0 : gpuMs[sky] / gpuN[sky],
         gpuN[sky],
         cpuN[sky] == 0 ? 0.0 : cpuMs[sky] / cpuN[sky],
         cpuN[sky]
      );
      gpuMs[sky] = cpuMs[sky] = 0.0;
      gpuN[sky] = cpuN[sky] = 0;
      return s;
   }

   private static void profBegin(int sky) {
      if (profQueries == null) {
         profQueries = new int[8];
         GL33C.glGenQueries(profQueries);
      }

      int slot = profHead % 8;
      if (profKind[slot] >= 0) {
         long ns = GL33C.glGetQueryObjecti64(profQueries[slot], 34918);
         gpuMs[profKind[slot]] = gpuMs[profKind[slot]] + ns / 1000000.0;
         gpuN[profKind[slot]]++;
      }

      GL33C.glBeginQuery(35007, profQueries[slot]);
      profKind[slot] = sky;
   }

   private static void profEnd() {
      GL33C.glEndQuery(35007);
      profHead++;
   }

   private static boolean drawWith(Matrix4f inv, int skyIndex, int prevIndex, float mix, float brightness, float speed) {
      class_276 target = class_310.method_1551().method_1522();
      if (!(target.method_30277() instanceof class_10868 color)) {
         return false;
      } else if (skyIndex >= 0 && skyIndex < SKY_COUNT) {
         long now = System.nanoTime();
         skyTime = skyTime + Math.min(0.1, (now - lastNanos) / 1.0E9) * speed;
         lastNanos = now;
         boolean fading = prevIndex >= 0 && prevIndex < SKY_COUNT && prevIndex != skyIndex && mix < 1.0F;
         boolean prof = DevCapture.active() && !fading;
         long cpu0 = System.nanoTime();
         guard.save();
         int saved3d = -1;
         int saved2d = 0;
         int savedS3 = 0;
         int savedS2 = 0;

         try {
            GL33C.glActiveTexture(33996);
            saved3d = GL33C.glGetInteger(32874);
            savedS3 = GL33C.glGetInteger(35097);
            GL33C.glActiveTexture(33997);
            saved2d = GL33C.glGetInteger(32873);
            savedS2 = GL33C.glGetInteger(35097);
            if (noise3Tex == 0) {
               createNoise();
            }

            CustomSkyRenderer.Program cur = program(skyIndex);
            if (cur == null) {
               return false;
            }

            CustomSkyRenderer.Program prev = fading ? program(prevIndex) : null;
            if (fbo == 0) {
               fbo = GL33C.glGenFramebuffers();
            }

            if (vao == 0) {
               vao = GL33C.glGenVertexArrays();
            }

            GL33C.glBindFramebuffer(36160, fbo);
            GL33C.glFramebufferTexture2D(36160, 36064, 3553, color.method_68427(), 0);
            if (GL33C.glCheckFramebufferStatus(36160) != 36053) {
               return false;
            }

            GL33C.glViewport(0, 0, target.field_1482, target.field_1481);
            GL33C.glDisable(2929);
            GL33C.glDepthMask(false);
            GL33C.glDisable(2884);
            GL33C.glDisable(3089);
            GL33C.glDisable(2960);
            GL33C.glColorMask(true, true, true, true);
            GL33C.glActiveTexture(33996);
            GL33C.glBindTexture(32879, noise3Tex);
            GL33C.glBindSampler(12, 0);
            GL33C.glActiveTexture(33997);
            GL33C.glBindTexture(3553, noise2Tex);
            GL33C.glBindSampler(13, 0);
            GL33C.glBindVertexArray(vao);
            MemoryStack stack = MemoryStack.stackPush();

            try {
               FloatBuffer m = stack.mallocFloat(16);
               inv.get(m);
               GL33C.glDisable(3042);
               if (prev != null) {
                  drawProgram(prev, m, 1.0F, brightness);
                  GL33C.glEnable(3042);
                  GL33C.glBlendFuncSeparate(770, 771, 0, 1);
                  drawProgram(cur, m, mix, brightness);
               } else {
                  if (prof) {
                     profBegin(skyIndex);
                  }

                  drawProgram(cur, m, 1.0F, brightness);
                  if (prof) {
                     profEnd();
                  }
               }
            } catch (Throwable var29) {
               if (stack != null) {
                  try {
                     stack.close();
                  } catch (Throwable var28) {
                     var29.addSuppressed(var28);
                  }
               }

               throw var29;
            }

            if (stack != null) {
               stack.close();
            }

            return true;
         } catch (Throwable t) {
            failed = true;
            BytePlus.LOGGER.error("Custom Sky failed, falling back to the vanilla sky", t);
            return false;
         } finally {
            if (saved3d >= 0) {
               GL33C.glActiveTexture(33996);
               GL33C.glBindTexture(32879, saved3d);
               GL33C.glBindSampler(12, savedS3);
               GL33C.glActiveTexture(33997);
               GL33C.glBindTexture(3553, saved2d);
               GL33C.glBindSampler(13, savedS2);
            }

            guard.restore();
            if (prof) {
               cpuMs[skyIndex] = cpuMs[skyIndex] + (System.nanoTime() - cpu0) / 1000000.0;
               cpuN[skyIndex]++;
            }
         }
      } else {
         return false;
      }
   }

   private static void drawProgram(CustomSkyRenderer.Program p, FloatBuffer invViewProj, float alpha, float brightness) {
      GL33C.glUseProgram(p.id);
      GL33C.glUniformMatrix4fv(p.uInvViewProj, false, invViewProj);
      GL33C.glUniform1f(p.uTime, (float)skyTime);
      GL33C.glUniform1f(p.uBright, brightness);
      GL33C.glUniform1f(p.uAlpha, alpha);
      GL33C.glDrawArrays(4, 0, 3);
   }

   private static CustomSkyModule skyModule() {
      for (Module mod : BytePlus.modules().all()) {
         if (mod instanceof CustomSkyModule s) {
            return s;
         }
      }

      return null;
   }

   private static void createNoise() {
      int unpackBuffer = GL33C.glGetInteger(35055);
      GL33C.glBindBuffer(35052, 0);
      GL33C.glPixelStorei(3317, 1);
      GL33C.glPixelStorei(3314, 0);
      GL33C.glPixelStorei(3316, 0);
      GL33C.glPixelStorei(3315, 0);
      GL33C.glPixelStorei(32878, 0);
      GL33C.glPixelStorei(32877, 0);
      SplittableRandom r = new SplittableRandom(727013L);
      ByteBuffer b3 = MemoryUtil.memAlloc(262144);
      ByteBuffer b2 = MemoryUtil.memAlloc(65536);

      try {
         for (int i = 0; i < 262144; i++) {
            b3.put(i, (byte)r.nextInt(256));
         }

         for (int i = 0; i < 65536; i++) {
            b2.put(i, (byte)r.nextInt(256));
         }

         GL33C.glActiveTexture(33996);
         noise3Tex = GL33C.glGenTextures();
         GL33C.glBindTexture(32879, noise3Tex);
         GL33C.glTexImage3D(32879, 0, 33321, 64, 64, 64, 0, 6403, 5121, b3);
         params(32879);
         GL33C.glTexParameteri(32879, 32882, 10497);
         GL33C.glActiveTexture(33997);
         noise2Tex = GL33C.glGenTextures();
         GL33C.glBindTexture(3553, noise2Tex);
         GL33C.glTexImage2D(3553, 0, 33321, 256, 256, 0, 6403, 5121, b2);
         params(3553);
      } finally {
         MemoryUtil.memFree(b3);
         MemoryUtil.memFree(b2);
         GL33C.glBindBuffer(35052, unpackBuffer);
      }
   }

   private static void params(int target) {
      GL33C.glTexParameteri(target, 10241, 9729);
      GL33C.glTexParameteri(target, 10240, 9729);
      GL33C.glTexParameteri(target, 10242, 10497);
      GL33C.glTexParameteri(target, 10243, 10497);
      GL33C.glTexParameteri(target, 33084, 0);
      GL33C.glTexParameteri(target, 33085, 0);
   }

   private static CustomSkyRenderer.Program program(int sky) {
      CustomSkyRenderer.Program p = programs[sky];
      if (p == null) {
         int id = link(sky);
         p = new CustomSkyRenderer.Program(id);
         programs[sky] = p;
         if (id != 0) {
            GL33C.glUseProgram(id);
            GL33C.glUniform1i(GL33C.glGetUniformLocation(id, "uNoise3"), 12);
            GL33C.glUniform1i(GL33C.glGetUniformLocation(id, "uNoise2"), 13);
         }
      }

      return p.id == 0 ? null : p;
   }

   private static int link(int sky) {
      String src = "#version 330 core\nin vec2 vNdc;\nout vec4 fragColor;\nuniform mat4 uInvViewProj;\nuniform float uTime;\nuniform float uBright;\nuniform float uAlpha;\nuniform sampler3D uNoise3;\nuniform sampler2D uNoise2;\n\nfloat gPx; // angular size of one pixel (radians), so stars are never thinner than a pixel\n\nfloat hash13(vec3 p) { p = fract(p * 0.1031); p += dot(p, p.zyx + 31.32); return fract((p.x + p.y) * p.z); }\nfloat hash12(vec2 p) { vec3 q = fract(vec3(p.xyx) * 0.1031); q += dot(q, q.yzx + 33.33); return fract((q.x + q.y) * q.z); }\nvec3 hash33(vec3 p) { p = fract(p * vec3(0.1031, 0.1030, 0.0973)); p += dot(p, p.yxz + 33.33); return fract((p.xxy + p.yxx) * p.zyx); }\n\n// value noise through the GPU's texture filter: one lookup instead of 8 hashes, with smooth interpolation\nfloat noise3(vec3 x) {\n    vec3 i = floor(x), f = fract(x);\n    f = f * f * (3.0 - 2.0 * f);\n    return textureLod(uNoise3, (i + f + 0.5) * (1.0 / 64.0), 0.0).r;\n}\nfloat noise2(vec2 x) {\n    vec2 i = floor(x), f = fract(x);\n    f = f * f * (3.0 - 2.0 * f);\n    return textureLod(uNoise2, (i + f + 0.5) * (1.0 / 256.0), 0.0).r;\n}\nfloat fbm3(vec3 p) { float a = 0.5, s = 0.0; for (int i = 0; i < 5; i++) { s += a * noise3(p); p = p * 2.03 + vec3(1.7, 9.2, 3.1); a *= 0.5; } return s; }\n// 3 octaves (for soft layers and warps), scaled to the same range as fbm3\nfloat fbm3lo(vec3 p) { float a = 0.5, s = 0.0; for (int i = 0; i < 3; i++) { s += a * noise3(p); p = p * 2.03 + vec3(1.7, 9.2, 3.1); a *= 0.5; } return s * 1.107; }\nfloat fbm2(vec2 p) { float a = 0.5, s = 0.0; for (int i = 0; i < 5; i++) { s += a * noise2(p); p = p * 2.01 + vec2(3.1, 7.4); a *= 0.5; } return s; }\n\nmat3 rotY(float a) { float c = cos(a), s = sin(a); return mat3(c, 0, -s, 0, 1, 0, s, 0, c); }\nmat2 rot2(float a) { float c = cos(a), s = sin(a); return mat2(c, -s, s, c); }\n\n// One layer of stars on the sphere: a round gaussian core (at least a pixel wide, so nothing shimmers)\n// with a soft glow; bright ones get small diffraction spikes. Colours follow star temperatures.\nvec3 starLayer(vec3 d, float scale, float density, float t, float seed, bool spikes) {\n    vec3 p = d * scale;\n    vec3 id = floor(p);\n    float h = hash13(id + seed);\n    if (h < 1.0 - density) return vec3(0.0);\n    vec3 r1 = hash33(id + seed + 3.1);\n    vec3 r2 = hash33(id + seed + 9.4);\n    vec3 q = (fract(p) - 0.5) - (r1 - 0.5) * 0.4;\n    float radial = dot(q, d);\n    q -= d * radial;                                   // onto the sky plane: stars stay round\n    float r = length(q);\n    float mag = r2.x * r2.x * r2.x;                     // most are faint, a few are brilliant\n    float px = gPx * scale;\n    float sz = max(0.045 + 0.06 * mag, px * 0.75);\n    float tw = 0.72 + 0.28 * sin(t * (1.3 + h * 6.0) + r2.z * 91.0);\n    float core = exp(-r * r / (sz * sz)) * (0.04 + 0.06 * mag) / sz;   // same energy whatever the size\n    float halo = exp(-r / (0.035 + 0.09 * mag)) * mag * 0.6;\n    float k = r2.y;\n    vec3 tint = k < 0.28 ? vec3(0.62, 0.76, 1.0) : (k < 0.84 ? vec3(1.0, 0.97, 0.93) : vec3(1.0, 0.74, 0.5));\n    vec3 col = tint * (core * (0.6 + 2.6 * mag) + halo);\n    if (spikes && mag > 0.4) {\n        vec3 up = abs(d.y) < 0.99 ? vec3(0.0, 1.0, 0.0) : vec3(1.0, 0.0, 0.0);\n        vec3 t1 = normalize(cross(up, d)), t2 = cross(d, t1);\n        vec2 q2 = vec2(dot(q, t1), dot(q, t2));\n        float w = px * 0.6 + 0.004;\n        float sp = exp(-abs(q2.x) / w) * exp(-abs(q2.y) * 10.0) + exp(-abs(q2.y) / w) * exp(-abs(q2.x) * 10.0);\n        col += tint * sp * (mag - 0.4) * 1.8;\n    }\n    float fade = smoothstep(0.5, 0.3, r) * smoothstep(0.32, 0.16, abs(radial));\n    return col * tw * fade;\n}\n\nvec3 stars(vec3 d, float density, float t) {\n    return starLayer(d, 64.0, density * 0.3, t, 0.0, true) * 1.5\n         + starLayer(d, 140.0, density, t, 17.0, false)\n         + starLayer(d, 300.0, min(1.0, density * 1.5), t, 41.0, false) * 0.5;\n}\n\nvec3 shootingStar(vec3 d, float t, float period) {\n    float cyc = floor(t / period), local = fract(t / period);\n    if (local > 0.22) return vec3(0.0);\n    vec3 a = normalize(vec3(hash12(vec2(cyc, 1.0)) * 2.0 - 1.0, 0.45 + hash12(vec2(cyc, 2.0)) * 0.5, hash12(vec2(cyc, 3.0)) * 2.0 - 1.0));\n    vec3 dir = normalize(cross(a, vec3(hash12(vec2(cyc, 4.0)) - 0.5, -1.0, hash12(vec2(cyc, 5.0)) - 0.5)));\n    vec3 b = normalize(a + dir * 0.5);\n    float prog = local / 0.22;\n    vec3 n = normalize(cross(a, b));\n    float dist = abs(dot(d, n));\n    if (dist > 0.03) return vec3(0.0);\n    vec3 proj = normalize(d - n * dot(d, n));\n    if (dot(cross(a, proj), n) < 0.0) return vec3(0.0);\n    float ang = acos(clamp(dot(proj, a), -1.0, 1.0));\n    float head = acos(clamp(dot(normalize(mix(a, b, prog)), a), -1.0, 1.0));\n    if (ang > head || ang < head - 0.22) return vec3(0.0);\n    float tail = 1.0 - (head - ang) / 0.22;\n    float w = max(0.0025, gPx * 1.2);\n    vec3 c = mix(vec3(0.55, 0.8, 1.0), vec3(1.0), tail * tail);\n    return c * exp(-dist * dist / (w * w)) * tail * tail * (1.0 - prog * 0.5) * 2.4;\n}\n"
         + SKY_SOURCE[sky]
         + "void main() {\n    vec4 p = uInvViewProj * vec4(vNdc, 1.0, 1.0);\n    vec3 d = normalize(p.xyz / p.w);\n    gPx = max(length(fwidth(d)) * 0.7, 1e-5);\n    float t = uTime;\n    vec3 dr = rotY(t * 0.004) * d;\n    vec3 col = %CALL%;\n    col *= uBright;\n    col = col / (1.0 + col * 0.15);\n    col += (hash12(gl_FragCoord.xy) - 0.5) / 255.0;\n    fragColor = vec4(max(col, 0.0), uAlpha);\n}\n"
            .replace("%CALL%", SKY_CALL[sky]);
      int vs = compile(
         35633,
         "#version 330 core\nout vec2 vNdc;\nvoid main() {\n    vec2 p = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2) * 2.0 - 1.0;\n    vNdc = p;\n    gl_Position = vec4(p, 0.0, 1.0);\n}\n",
         sky
      );
      int fs = compile(35632, src, sky);
      if (vs != 0 && fs != 0) {
         int p = GL33C.glCreateProgram();
         GL33C.glAttachShader(p, vs);
         GL33C.glAttachShader(p, fs);
         GL33C.glLinkProgram(p);
         GL33C.glDeleteShader(vs);
         GL33C.glDeleteShader(fs);
         if (GL33C.glGetProgrami(p, 35714) == 0) {
            BytePlus.LOGGER.error("Custom Sky shader link failed ({}): {}", CustomSkyModule.SKIES[sky], GL33C.glGetProgramInfoLog(p));
            GL33C.glDeleteProgram(p);
            return 0;
         } else {
            return p;
         }
      } else {
         if (vs != 0) {
            GL33C.glDeleteShader(vs);
         }

         if (fs != 0) {
            GL33C.glDeleteShader(fs);
         }

         return 0;
      }
   }

   private static int compile(int type, String src, int sky) {
      int s = GL33C.glCreateShader(type);
      GL33C.glShaderSource(s, src);
      GL33C.glCompileShader(s);
      if (GL33C.glGetShaderi(s, 35713) == 0) {
         BytePlus.LOGGER.error("Custom Sky shader compile failed ({}): {}", CustomSkyModule.SKIES[sky], GL33C.glGetShaderInfoLog(s));
         GL33C.glDeleteShader(s);
         return 0;
      } else {
         return s;
      }
   }

   private static final class Fade {
      int current = -1;
      int previous = -1;
      long changedAt;

      float mix(int sky) {
         long now = System.nanoTime();
         if (this.current == -1) {
            this.current = sky;
            this.previous = sky;
         }

         if (sky != this.current) {
            this.previous = this.current;
            this.current = sky;
            this.changedAt = now;
         }

         float k = Math.min(1.0F, (float)(now - this.changedAt) / 1.0E9F / 1.1F);
         if (k >= 1.0F) {
            this.previous = this.current;
         }

         return k * k * (3.0F - 2.0F * k);
      }
   }

   private static final class Program {
      final int id;
      final int uInvViewProj;
      final int uTime;
      final int uBright;
      final int uAlpha;

      Program(int id) {
         this.id = id;
         this.uInvViewProj = id == 0 ? -1 : GL33C.glGetUniformLocation(id, "uInvViewProj");
         this.uTime = id == 0 ? -1 : GL33C.glGetUniformLocation(id, "uTime");
         this.uBright = id == 0 ? -1 : GL33C.glGetUniformLocation(id, "uBright");
         this.uAlpha = id == 0 ? -1 : GL33C.glGetUniformLocation(id, "uAlpha");
      }
   }
}
