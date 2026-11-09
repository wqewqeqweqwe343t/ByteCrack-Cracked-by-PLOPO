package dev.byteplus.render;

import dev.byteplus.BytePlus;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_10868;
import net.minecraft.class_276;
import net.minecraft.class_310;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.system.MemoryStack;

public final class FullscreenPass {
   private static final GlStateGuard guard = new GlStateGuard();
   private static final String VERTEX = "#version 330 core\nout vec2 vNdc;\nvoid main() {\n    vec2 p = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2) * 2.0 - 1.0;\n    vNdc = p;\n    gl_Position = vec4(p, 0.0, 1.0);\n}\n";
   private final String name;
   private final String fragment;
   private int program = -1;
   private int vao;
   private int fbo;
   private boolean failed;
   private final Map<String, Integer> locations = new HashMap<>();

   public FullscreenPass(String name, String fragment) {
      this.name = name;
      this.fragment = fragment;
   }

   public boolean failed() {
      return this.failed;
   }

   public boolean draw(FullscreenPass.Uniforms uniforms) {
      if (this.failed) {
         return false;
      }

      class_276 target = class_310.method_1551().method_1522();
      if (target.method_30277() instanceof class_10868 color) {
         guard.save();

         try {
            if (this.program == -1 && !this.init()) {
               return false;
            }

            if (this.fbo == 0) {
               this.fbo = GL33C.glGenFramebuffers();
            }

            GL33C.glBindFramebuffer(36160, this.fbo);
            GL33C.glFramebufferTexture2D(36160, 36064, 3553, color.method_68427(), 0);
            if (GL33C.glCheckFramebufferStatus(36160) != 36053) {
               return false;
            }

            GL33C.glViewport(0, 0, target.field_1482, target.field_1481);
            GL33C.glDisable(2929);
            GL33C.glDepthMask(false);
            GL33C.glDisable(3042);
            GL33C.glDisable(2884);
            GL33C.glDisable(3089);
            GL33C.glDisable(2960);
            GL33C.glColorMask(true, true, true, true);
            GL33C.glUseProgram(this.program);
            this.set2("uRes", target.field_1482, target.field_1481);
            uniforms.set(this);
            GL33C.glBindVertexArray(this.vao);
            GL33C.glDrawArrays(4, 0, 3);
            return true;
         } catch (Throwable t) {
            this.failed = true;
            BytePlus.LOGGER.error("{} shader failed, using the fallback", this.name, t);
            return false;
         } finally {
            guard.restore();
         }
      } else {
         return false;
      }
   }

   private int loc(String n) {
      return this.locations.computeIfAbsent(n, k -> GL33C.glGetUniformLocation(this.program, k));
   }

   public void set1(String n, float v) {
      GL33C.glUniform1f(this.loc(n), v);
   }

   public void set1i(String n, int v) {
      GL33C.glUniform1i(this.loc(n), v);
   }

   public void set2(String n, float a, float b) {
      GL33C.glUniform2f(this.loc(n), a, b);
   }

   public void set3(String n, float a, float b, float c) {
      GL33C.glUniform3f(this.loc(n), a, b, c);
   }

   public void setRgb(String n, int argb) {
      this.set3(n, (argb >> 16 & 0xFF) / 255.0F, (argb >> 8 & 0xFF) / 255.0F, (argb & 0xFF) / 255.0F);
   }

   public void setMat4(String n, Matrix4f m) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         FloatBuffer fb = stack.mallocFloat(16);
         m.get(fb);
         GL33C.glUniformMatrix4fv(this.loc(n), false, fb);
      } catch (Throwable var7) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (stack != null) {
         stack.close();
      }
   }

   private boolean init() {
      int vs = this.compile(
         35633,
         "#version 330 core\nout vec2 vNdc;\nvoid main() {\n    vec2 p = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2) * 2.0 - 1.0;\n    vNdc = p;\n    gl_Position = vec4(p, 0.0, 1.0);\n}\n"
      );
      int fs = this.compile(35632, this.fragment);
      if (vs != 0 && fs != 0) {
         int p = GL33C.glCreateProgram();
         GL33C.glAttachShader(p, vs);
         GL33C.glAttachShader(p, fs);
         GL33C.glLinkProgram(p);
         GL33C.glDeleteShader(vs);
         GL33C.glDeleteShader(fs);
         if (GL33C.glGetProgrami(p, 35714) == 0) {
            BytePlus.LOGGER.error("{} shader link failed: {}", this.name, GL33C.glGetProgramInfoLog(p));
            GL33C.glDeleteProgram(p);
            this.failed = true;
            return false;
         } else {
            this.program = p;
            this.vao = GL33C.glGenVertexArrays();
            return true;
         }
      } else {
         this.failed = true;
         return false;
      }
   }

   private int compile(int type, String src) {
      int s = GL33C.glCreateShader(type);
      GL33C.glShaderSource(s, src);
      GL33C.glCompileShader(s);
      if (GL33C.glGetShaderi(s, 35713) == 0) {
         BytePlus.LOGGER.error("{} shader compile failed: {}", this.name, GL33C.glGetShaderInfoLog(s));
         GL33C.glDeleteShader(s);
         return 0;
      } else {
         return s;
      }
   }

   public interface Uniforms {
      void set(FullscreenPass var1);
   }
}
