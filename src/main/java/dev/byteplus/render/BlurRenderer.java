package dev.byteplus.render;

import dev.byteplus.BytePlus;
import org.lwjgl.opengl.GL33C;

public final class BlurRenderer {
   private static final int MAX_LEVELS = 6;
   private static int down = -1;
   private static int up = -1;
   private static int blit = -1;
   private static int vao;
   private static int uDownHalf;
   private static int uDownOffset;
   private static int uUpHalf;
   private static int uUpOffset;
   private static int uBlitAlpha;
   private static final int[] tex = new int[7];
   private static final int[] fbo = new int[7];
   private static final int[] tw = new int[7];
   private static final int[] th = new int[7];
   private static int sizeW;
   private static int sizeH;
   private static boolean failed;

   private BlurRenderer() {
   }

   public static void blurScreen(int w, int h, int strength, float alpha) {
      if (!failed && !(alpha <= 0.01F) && w >= 16 && h >= 16) {
         try {
            if (down == -1 && !init()) {
               return;
            }

            int levels = Math.min(6, 1 + (strength + 1) / 2);
            float offset = 1.0F + strength * 0.22F;
            ensureTargets(w, h);
            GL33C.glDisable(2929);
            GL33C.glDisable(2884);
            GL33C.glDisable(3089);
            GL33C.glDisable(2960);
            GL33C.glDisable(3042);
            GL33C.glColorMask(true, true, true, true);
            GL33C.glBindVertexArray(vao);
            GL33C.glActiveTexture(33984);
            GL33C.glBindSampler(0, 0);
            GL33C.glBindFramebuffer(36008, 0);
            GL33C.glBindFramebuffer(36009, fbo[1]);
            GL33C.glBlitFramebuffer(0, 0, w, h, 0, 0, tw[1], th[1], 16384, 9729);
            GL33C.glUseProgram(down);

            for (int i = 1; i < levels; i++) {
               pass(i, i + 1, uDownHalf, uDownOffset, offset);
            }

            GL33C.glUseProgram(up);

            for (int i = levels; i > 1; i--) {
               pass(i, i - 1, uUpHalf, uUpOffset, offset);
            }

            GL33C.glBindFramebuffer(36160, 0);
            GL33C.glViewport(0, 0, w, h);
            GL33C.glEnable(3042);
            GL33C.glBlendFuncSeparate(770, 771, 0, 1);
            GL33C.glUseProgram(blit);
            GL33C.glUniform1f(uBlitAlpha, alpha);
            GL33C.glBindTexture(3553, tex[1]);
            GL33C.glDrawArrays(4, 0, 3);
            GL33C.glDisable(3042);
         } catch (Throwable t) {
            failed = true;
            BytePlus.LOGGER.error("Byte+ blur failed, disabling it", t);
         }
      }
   }

   private static void pass(int from, int to, int uHalf, int uOffset, float offset) {
      GL33C.glBindFramebuffer(36160, fbo[to]);
      GL33C.glViewport(0, 0, tw[to], th[to]);
      GL33C.glUniform2f(uHalf, 0.5F / tw[from], 0.5F / th[from]);
      GL33C.glUniform1f(uOffset, offset);
      GL33C.glBindTexture(3553, tex[from]);
      GL33C.glDrawArrays(4, 0, 3);
   }

   private static void ensureTargets(int w, int h) {
      if (w != sizeW || h != sizeH) {
         sizeW = w;
         sizeH = h;

         for (int i = 1; i <= 6; i++) {
            tw[i] = Math.max(1, w >> i);
            th[i] = Math.max(1, h >> i);
            if (tex[i] == 0) {
               tex[i] = GL33C.glGenTextures();
            }

            GL33C.glBindTexture(3553, tex[i]);
            GL33C.glTexImage2D(3553, 0, 32856, tw[i], th[i], 0, 6408, 5121, 0L);
            GL33C.glTexParameteri(3553, 10241, 9729);
            GL33C.glTexParameteri(3553, 10240, 9729);
            GL33C.glTexParameteri(3553, 10242, 33071);
            GL33C.glTexParameteri(3553, 10243, 33071);
            if (fbo[i] == 0) {
               fbo[i] = GL33C.glGenFramebuffers();
            }

            GL33C.glBindFramebuffer(36160, fbo[i]);
            GL33C.glFramebufferTexture2D(36160, 36064, 3553, tex[i], 0);
         }
      }
   }

   private static boolean init() {
      String vs = "#version 330 core\nout vec2 uv;\nvoid main() {\n    vec2 p = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);\n    uv = p;\n    gl_Position = vec4(p * 2.0 - 1.0, 0.0, 1.0);\n}\n";
      String downFs = "#version 330 core\nin vec2 uv; out vec4 color;\nuniform sampler2D tex; uniform vec2 halfPixel; uniform float offset;\nvoid main() {\n    vec4 s = texture(tex, uv) * 4.0;\n    s += texture(tex, uv - halfPixel * offset);\n    s += texture(tex, uv + halfPixel * offset);\n    s += texture(tex, uv + vec2(halfPixel.x, -halfPixel.y) * offset);\n    s += texture(tex, uv - vec2(halfPixel.x, -halfPixel.y) * offset);\n    color = s / 8.0;\n}\n";
      String upFs = "#version 330 core\nin vec2 uv; out vec4 color;\nuniform sampler2D tex; uniform vec2 halfPixel; uniform float offset;\nvoid main() {\n    vec4 s = texture(tex, uv + vec2(-halfPixel.x * 2.0, 0.0) * offset);\n    s += texture(tex, uv + vec2(-halfPixel.x, halfPixel.y) * offset) * 2.0;\n    s += texture(tex, uv + vec2(0.0, halfPixel.y * 2.0) * offset);\n    s += texture(tex, uv + vec2(halfPixel.x, halfPixel.y) * offset) * 2.0;\n    s += texture(tex, uv + vec2(halfPixel.x * 2.0, 0.0) * offset);\n    s += texture(tex, uv + vec2(halfPixel.x, -halfPixel.y) * offset) * 2.0;\n    s += texture(tex, uv + vec2(0.0, -halfPixel.y * 2.0) * offset);\n    s += texture(tex, uv + vec2(-halfPixel.x, -halfPixel.y) * offset) * 2.0;\n    color = s / 12.0;\n}\n";
      String blitFs = "#version 330 core\nin vec2 uv; out vec4 color;\nuniform sampler2D tex; uniform float alpha;\nvoid main() { color = vec4(texture(tex, uv).rgb, alpha); }\n";
      down = link(vs, downFs);
      up = link(vs, upFs);
      blit = link(vs, blitFs);
      if (down != 0 && up != 0 && blit != 0) {
         uDownHalf = GL33C.glGetUniformLocation(down, "halfPixel");
         uDownOffset = GL33C.glGetUniformLocation(down, "offset");
         uUpHalf = GL33C.glGetUniformLocation(up, "halfPixel");
         uUpOffset = GL33C.glGetUniformLocation(up, "offset");
         uBlitAlpha = GL33C.glGetUniformLocation(blit, "alpha");

         for (int p : new int[]{down, up, blit}) {
            GL33C.glUseProgram(p);
            GL33C.glUniform1i(GL33C.glGetUniformLocation(p, "tex"), 0);
         }

         vao = GL33C.glGenVertexArrays();
         return true;
      } else {
         failed = true;
         return false;
      }
   }

   private static int link(String vsSrc, String fsSrc) {
      int vs = compile(35633, vsSrc);
      int fs = compile(35632, fsSrc);
      if (vs != 0 && fs != 0) {
         int p = GL33C.glCreateProgram();
         GL33C.glAttachShader(p, vs);
         GL33C.glAttachShader(p, fs);
         GL33C.glLinkProgram(p);
         GL33C.glDeleteShader(vs);
         GL33C.glDeleteShader(fs);
         if (GL33C.glGetProgrami(p, 35714) == 0) {
            BytePlus.LOGGER.error("Byte+ blur shader link failed: {}", GL33C.glGetProgramInfoLog(p));
            return 0;
         } else {
            return p;
         }
      } else {
         return 0;
      }
   }

   private static int compile(int type, String src) {
      int s = GL33C.glCreateShader(type);
      GL33C.glShaderSource(s, src);
      GL33C.glCompileShader(s);
      if (GL33C.glGetShaderi(s, 35713) == 0) {
         BytePlus.LOGGER.error("Byte+ blur shader compile failed: {}", GL33C.glGetShaderInfoLog(s));
         return 0;
      } else {
         return s;
      }
   }
}
