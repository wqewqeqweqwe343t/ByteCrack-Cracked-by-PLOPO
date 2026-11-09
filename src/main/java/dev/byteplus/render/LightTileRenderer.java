package dev.byteplus.render;

import dev.byteplus.BytePlus;
import java.nio.FloatBuffer;
import java.util.Locale;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public final class LightTileRenderer {
   private static final GlStateGuard guard = new GlStateGuard();
   private static final String VERTEX = "#version 330 core\nlayout(location = 0) in vec4 iTile;\nuniform mat4 uViewProj;\nuniform vec3 uOffset;\nout vec2 vUv;\nout float vLevel;\nout float vShade;\nout float vDist;\n// faces: -X +X -Y +Y -Z +Z\nconst vec3 N[6] = vec3[](vec3(-1,0,0), vec3(1,0,0), vec3(0,-1,0), vec3(0,1,0), vec3(0,0,-1), vec3(0,0,1));\nconst vec2 Q[6] = vec2[](vec2(0,0), vec2(1,0), vec2(1,1), vec2(0,0), vec2(1,1), vec2(0,1));\nconst float SHADE[6] = float[](0.78, 0.78, 0.55, 1.0, 0.68, 0.68);\nvoid main() {\n    int face = gl_VertexID / 6;\n    vec2 uv = Q[gl_VertexID % 6];\n    vec3 n = N[face];\n    int level = int(iTile.w) & 15;\n    int mask = int(iTile.w) >> 4;\n    vec3 base = iTile.xyz + uOffset;\n    vec3 center = base + 0.5 + n * 0.5;\n    // shared with a lit neighbour, or facing away from the camera (which sits at the origin): skip\n    if ((mask & (1 << face)) != 0 || dot(n, center) > 0.0) { gl_Position = vec4(0.0); return; }\n    vec3 local;\n    if (face < 2)      local = vec3(face == 1 ? 1.0 : 0.0, uv.x, uv.y);\n    else if (face < 4) local = vec3(uv.x, face == 3 ? 1.0 : 0.0, uv.y);\n    else               local = vec3(uv.x, uv.y, face == 5 ? 1.0 : 0.0);\n    vec3 p = base + local;\n    vUv = uv;\n    vLevel = float(level);\n    vShade = SHADE[face];\n    vDist = length(p.xz);\n    gl_Position = uViewProj * vec4(p, 1.0);\n}\n";
   private static final String FRAGMENT = "#version 330 core\nin vec2 vUv;\nin float vLevel;\nin float vShade;\nin float vDist;\nuniform vec3 uBright;\nuniform vec3 uDim;\nout vec4 fragColor;\nvoid main() {\n    float t = clamp((vLevel - 1.0) / 14.0, 0.0, 1.0);\n    float k = t * t * (3.0 - 2.0 * t);\n    vec3 col = mix(uDim, uBright, k);\n    vec2 e2 = min(vUv, 1.0 - vUv);\n    float e = min(e2.x, e2.y);\n    float w = max(fwidth(e), 1e-4);\n    // blocks only a few pixels across drop the border (no shimmering far away)\n    vec2 fw = fwidth(vUv);\n    float tilePx = 1.0 / max(max(fw.x, fw.y), 1e-4);\n    float lod = smoothstep(5.0, 14.0, tilePx);\n    float border = (1.0 - smoothstep(0.04, 0.04 + w * 1.5, e)) * lod;\n    float fill = mix(mix(0.40, 0.62, k), mix(0.20, 0.38, k), lod);\n    float a = mix(fill, mix(0.6, 0.9, k), border);\n    vec3 c = col * mix(vShade, 1.0, border * 0.6) + border * 0.08;\n    // only sideways distance fades (gently), so light right down at bedrock shows as strongly as near you\n    a *= mix(1.0, 0.7, smoothstep(128.0, 512.0, vDist));\n    fragColor = vec4(c, a);\n}\n";
   private static int program = -1;
   private static int vao;
   private static int instVbo;
   private static int locViewProj;
   private static int locOffset;
   private static int locBright;
   private static int locDim;
   private static boolean failed;
   private static int count;
   private static double anchorX;
   private static double anchorY;
   private static double anchorZ;
   private static final int PQ = 8;
   private static int[] queries;
   private static final boolean[] pending = new boolean[8];
   private static int head;
   private static double gpuMs;
   private static int gpuN;
   public static boolean profile;

   private LightTileRenderer() {
   }

   public static void upload(float[] data, int tiles, double ax, double ay, double az) {
      if (!failed && (program != -1 || init())) {
         anchorX = ax;
         anchorY = ay;
         anchorZ = az;
         count = tiles;
         int prev = GL33C.glGetInteger(34964);
         GL33C.glBindBuffer(34962, instVbo);
         if (tiles == 0) {
            GL33C.glBufferData(34962, 16L, 35048);
         } else {
            FloatBuffer fb = MemoryUtil.memAllocFloat(tiles * 4);

            try {
               fb.put(data, 0, tiles * 4).flip();
               GL33C.glBufferData(34962, fb, 35048);
            } finally {
               MemoryUtil.memFree(fb);
            }
         }

         GL33C.glBindBuffer(34962, prev);
      }
   }

   public static void clear() {
      count = 0;
   }

   public static void draw(int brightRgb, int dimRgb) {
      if (!failed && count != 0 && program != -1 && Projection.isValid()) {
         guard.save();

         try {
            if (profile) {
               profBegin();
            }

            GL33C.glDisable(2929);
            GL33C.glDepthMask(false);
            GL33C.glDisable(2884);
            GL33C.glDisable(3089);
            GL33C.glDisable(2960);
            GL33C.glColorMask(true, true, true, true);
            GL33C.glEnable(3042);
            GL33C.glBlendFuncSeparate(770, 771, 1, 771);
            GL33C.glBlendEquationSeparate(32774, 32774);
            GL33C.glUseProgram(program);
            MemoryStack stack = MemoryStack.stackPush();

            try {
               FloatBuffer m = stack.mallocFloat(16);
               Projection.viewProj().get(m);
               GL33C.glUniformMatrix4fv(locViewProj, false, m);
            } catch (Throwable var11) {
               if (stack != null) {
                  try {
                     stack.close();
                  } catch (Throwable var10) {
                     var11.addSuppressed(var10);
                  }
               }

               throw var11;
            }

            if (stack != null) {
               stack.close();
            }

            GL33C.glUniform3f(locOffset, (float)(anchorX - Projection.camX()), (float)(anchorY - Projection.camY()), (float)(anchorZ - Projection.camZ()));
            GL33C.glUniform3f(locBright, (brightRgb >> 16 & 0xFF) / 255.0F, (brightRgb >> 8 & 0xFF) / 255.0F, (brightRgb & 0xFF) / 255.0F);
            GL33C.glUniform3f(locDim, (dimRgb >> 16 & 0xFF) / 255.0F, (dimRgb >> 8 & 0xFF) / 255.0F, (dimRgb & 0xFF) / 255.0F);
            GL33C.glBindVertexArray(vao);
            GL33C.glDrawArraysInstanced(4, 0, 36, count);
            if (profile) {
               profEnd();
            }
         } catch (Throwable t) {
            failed = true;
            BytePlus.LOGGER.error("Light Debug renderer failed", t);
         } finally {
            guard.restore();
         }
      }
   }

   private static boolean init() {
      int vs = compile(
         35633,
         "#version 330 core\nlayout(location = 0) in vec4 iTile;\nuniform mat4 uViewProj;\nuniform vec3 uOffset;\nout vec2 vUv;\nout float vLevel;\nout float vShade;\nout float vDist;\n// faces: -X +X -Y +Y -Z +Z\nconst vec3 N[6] = vec3[](vec3(-1,0,0), vec3(1,0,0), vec3(0,-1,0), vec3(0,1,0), vec3(0,0,-1), vec3(0,0,1));\nconst vec2 Q[6] = vec2[](vec2(0,0), vec2(1,0), vec2(1,1), vec2(0,0), vec2(1,1), vec2(0,1));\nconst float SHADE[6] = float[](0.78, 0.78, 0.55, 1.0, 0.68, 0.68);\nvoid main() {\n    int face = gl_VertexID / 6;\n    vec2 uv = Q[gl_VertexID % 6];\n    vec3 n = N[face];\n    int level = int(iTile.w) & 15;\n    int mask = int(iTile.w) >> 4;\n    vec3 base = iTile.xyz + uOffset;\n    vec3 center = base + 0.5 + n * 0.5;\n    // shared with a lit neighbour, or facing away from the camera (which sits at the origin): skip\n    if ((mask & (1 << face)) != 0 || dot(n, center) > 0.0) { gl_Position = vec4(0.0); return; }\n    vec3 local;\n    if (face < 2)      local = vec3(face == 1 ? 1.0 : 0.0, uv.x, uv.y);\n    else if (face < 4) local = vec3(uv.x, face == 3 ? 1.0 : 0.0, uv.y);\n    else               local = vec3(uv.x, uv.y, face == 5 ? 1.0 : 0.0);\n    vec3 p = base + local;\n    vUv = uv;\n    vLevel = float(level);\n    vShade = SHADE[face];\n    vDist = length(p.xz);\n    gl_Position = uViewProj * vec4(p, 1.0);\n}\n"
      );
      int fs = compile(
         35632,
         "#version 330 core\nin vec2 vUv;\nin float vLevel;\nin float vShade;\nin float vDist;\nuniform vec3 uBright;\nuniform vec3 uDim;\nout vec4 fragColor;\nvoid main() {\n    float t = clamp((vLevel - 1.0) / 14.0, 0.0, 1.0);\n    float k = t * t * (3.0 - 2.0 * t);\n    vec3 col = mix(uDim, uBright, k);\n    vec2 e2 = min(vUv, 1.0 - vUv);\n    float e = min(e2.x, e2.y);\n    float w = max(fwidth(e), 1e-4);\n    // blocks only a few pixels across drop the border (no shimmering far away)\n    vec2 fw = fwidth(vUv);\n    float tilePx = 1.0 / max(max(fw.x, fw.y), 1e-4);\n    float lod = smoothstep(5.0, 14.0, tilePx);\n    float border = (1.0 - smoothstep(0.04, 0.04 + w * 1.5, e)) * lod;\n    float fill = mix(mix(0.40, 0.62, k), mix(0.20, 0.38, k), lod);\n    float a = mix(fill, mix(0.6, 0.9, k), border);\n    vec3 c = col * mix(vShade, 1.0, border * 0.6) + border * 0.08;\n    // only sideways distance fades (gently), so light right down at bedrock shows as strongly as near you\n    a *= mix(1.0, 0.7, smoothstep(128.0, 512.0, vDist));\n    fragColor = vec4(c, a);\n}\n"
      );
      if (vs != 0 && fs != 0) {
         int p = GL33C.glCreateProgram();
         GL33C.glAttachShader(p, vs);
         GL33C.glAttachShader(p, fs);
         GL33C.glLinkProgram(p);
         GL33C.glDeleteShader(vs);
         GL33C.glDeleteShader(fs);
         if (GL33C.glGetProgrami(p, 35714) == 0) {
            BytePlus.LOGGER.error("Light Debug shader link failed: {}", GL33C.glGetProgramInfoLog(p));
            GL33C.glDeleteProgram(p);
            failed = true;
            return false;
         } else {
            program = p;
            locViewProj = GL33C.glGetUniformLocation(p, "uViewProj");
            locOffset = GL33C.glGetUniformLocation(p, "uOffset");
            locBright = GL33C.glGetUniformLocation(p, "uBright");
            locDim = GL33C.glGetUniformLocation(p, "uDim");
            int prevVao = GL33C.glGetInteger(34229);
            int prevBuf = GL33C.glGetInteger(34964);
            vao = GL33C.glGenVertexArrays();
            GL33C.glBindVertexArray(vao);
            instVbo = GL33C.glGenBuffers();
            GL33C.glBindBuffer(34962, instVbo);
            GL33C.glBufferData(34962, 16L, 35048);
            GL33C.glEnableVertexAttribArray(0);
            GL33C.glVertexAttribPointer(0, 4, 5126, false, 16, 0L);
            GL33C.glVertexAttribDivisor(0, 1);
            GL33C.glBindVertexArray(prevVao);
            GL33C.glBindBuffer(34962, prevBuf);
            return true;
         }
      } else {
         failed = true;
         return false;
      }
   }

   private static int compile(int type, String src) {
      int s = GL33C.glCreateShader(type);
      GL33C.glShaderSource(s, src);
      GL33C.glCompileShader(s);
      if (GL33C.glGetShaderi(s, 35713) == 0) {
         BytePlus.LOGGER.error("Light Debug shader compile failed: {}", GL33C.glGetShaderInfoLog(s));
         GL33C.glDeleteShader(s);
         return 0;
      } else {
         return s;
      }
   }

   public static String devStats() {
      String s = String.format(Locale.ROOT, "%d blocks, gpu %.3f ms (n=%d)", count, gpuN == 0 ? 0.0 : gpuMs / gpuN, gpuN);
      gpuMs = 0.0;
      gpuN = 0;
      return s;
   }

   private static void profBegin() {
      if (queries == null) {
         queries = new int[8];
         GL33C.glGenQueries(queries);
      }

      int slot = head % 8;
      if (pending[slot]) {
         gpuMs = gpuMs + GL33C.glGetQueryObjecti64(queries[slot], 34918) / 1000000.0;
         gpuN++;
      }

      GL33C.glBeginQuery(35007, queries[slot]);
      pending[slot] = true;
   }

   private static void profEnd() {
      GL33C.glEndQuery(35007);
      head++;
   }
}
