package dev.byteplus.render;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public final class Render2D {
   private static long vg;
   private static int fontRegular = -1;
   private static int fontBold = -1;
   private static ByteBuffer regularData;
   private static ByteBuffer boldData;
   private static NVGColor color;
   private static NVGColor color2;
   private static boolean failed;
   public static final int REGULAR = 0;
   public static final int BOLD = 1;
   private static final Map<String, Integer> images = new HashMap<>();
   private static final Map<Integer, int[]> imageSizes = new HashMap<>();
   private static final Map<Integer, Integer> glImages = new HashMap<>();

   private Render2D() {
   }

   public static boolean init() {
      if (vg != 0L) {
         return true;
      }

      if (failed) {
         return false;
      }

      try {
         vg = NanoVGGL3.nvgCreate(3);
         if (vg == 0L) {
            throw new IllegalStateException("nvgCreate returned null");
         }

         regularData = load("/assets/byteplus/fonts/Inter-Regular.otf");
         boldData = load("/assets/byteplus/fonts/Inter-SemiBold.otf");
         fontRegular = NanoVG.nvgCreateFontMem(vg, "inter", regularData, false);
         fontBold = NanoVG.nvgCreateFontMem(vg, "inter-semibold", boldData, false);
         color = NVGColor.malloc();
         color2 = NVGColor.malloc();
         return true;
      } catch (Throwable t) {
         failed = true;
         System.err.println("[Byte+] Failed to initialise NanoVG renderer");
         t.printStackTrace();
         return false;
      }
   }

   private static ByteBuffer load(String path) throws IOException {
      try (InputStream in = Render2D.class.getResourceAsStream(path)) {
         if (in == null) {
            throw new IOException("Missing resource " + path);
         }

         byte[] bytes = in.readAllBytes();
         ByteBuffer buf = MemoryUtil.memAlloc(bytes.length);
         buf.put(bytes).flip();
         return buf;
      }
   }

   public static long vg() {
      return vg;
   }

   public static void beginFrame(float width, float height) {
      NanoVG.nvgBeginFrame(vg, width, height, 1.0F);
   }

   public static void endFrame() {
      NanoVG.nvgEndFrame(vg);
   }

   public static void save() {
      NanoVG.nvgSave(vg);
   }

   public static void restore() {
      NanoVG.nvgRestore(vg);
   }

   public static void translate(float x, float y) {
      NanoVG.nvgTranslate(vg, x, y);
   }

   public static void scale(float s) {
      NanoVG.nvgScale(vg, s, s);
   }

   public static void scaleXY(float sx, float sy) {
      NanoVG.nvgScale(vg, sx, sy);
   }

   public static void rotate(float rad) {
      NanoVG.nvgRotate(vg, rad);
   }

   public static void globalAlpha(float a) {
      NanoVG.nvgGlobalAlpha(vg, Math.max(0.0F, Math.min(1.0F, a)));
   }

   public static void scissor(float x, float y, float w, float h) {
      NanoVG.nvgIntersectScissor(vg, x, y, Math.max(0.0F, w), Math.max(0.0F, h));
   }

   public static void resetScissor() {
      NanoVG.nvgResetScissor(vg);
   }

   public static NVGColor col(int argb) {
      return rgba(color, argb);
   }

   private static NVGColor rgba(NVGColor c, int argb) {
      c.r((argb >> 16 & 0xFF) / 255.0F);
      c.g((argb >> 8 & 0xFF) / 255.0F);
      c.b((argb & 0xFF) / 255.0F);
      c.a((argb >>> 24 & 0xFF) / 255.0F);
      return c;
   }

   public static void rect(float x, float y, float w, float h, int argb) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgRect(vg, x, y, w, h);
      NanoVG.nvgFillColor(vg, col(argb));
      NanoVG.nvgFill(vg);
   }

   public static void roundRect(float x, float y, float w, float h, float r, int argb) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgRoundedRect(vg, x, y, w, h, r);
      NanoVG.nvgFillColor(vg, col(argb));
      NanoVG.nvgFill(vg);
   }

   public static void roundRect(float x, float y, float w, float h, float tl, float tr, float br, float bl, int argb) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgRoundedRectVarying(vg, x, y, w, h, tl, tr, br, bl);
      NanoVG.nvgFillColor(vg, col(argb));
      NanoVG.nvgFill(vg);
   }

   public static void roundRectOutline(float x, float y, float w, float h, float r, float width, int argb) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgRoundedRect(vg, x, y, w, h, r);
      NanoVG.nvgStrokeWidth(vg, width);
      NanoVG.nvgStrokeColor(vg, col(argb));
      NanoVG.nvgStroke(vg);
   }

   public static void shadow(float x, float y, float w, float h, float r, float blur, int argb) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         NVGPaint paint = NVGPaint.malloc(stack);
         NanoVG.nvgBoxGradient(vg, x, y, w, h, r * 2.0F, blur, rgba(color, argb), rgba(color2, argb & 16777215), paint);
         NanoVG.nvgBeginPath(vg);
         NanoVG.nvgRect(vg, x - blur, y - blur, w + blur * 2.0F, h + blur * 2.0F);
         NanoVG.nvgRoundedRect(vg, x, y, w, h, r);
         NanoVG.nvgPathWinding(vg, 2);
         NanoVG.nvgFillPaint(vg, paint);
         NanoVG.nvgFill(vg);
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
   }

   public static void horizontalGradient(float x, float y, float w, float h, float r, int left, int right) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         NVGPaint paint = NVGPaint.malloc(stack);
         NanoVG.nvgLinearGradient(vg, x, y, x + w, y, rgba(color, left), rgba(color2, right), paint);
         NanoVG.nvgBeginPath(vg);
         NanoVG.nvgRoundedRect(vg, x, y, w, h, r);
         NanoVG.nvgFillPaint(vg, paint);
         NanoVG.nvgFill(vg);
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
   }

   public static void circle(float cx, float cy, float r, int argb) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgCircle(vg, cx, cy, r);
      NanoVG.nvgFillColor(vg, col(argb));
      NanoVG.nvgFill(vg);
   }

   public static void circleOutline(float cx, float cy, float r, float width, int argb) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgCircle(vg, cx, cy, r);
      NanoVG.nvgStrokeWidth(vg, width);
      NanoVG.nvgStrokeColor(vg, col(argb));
      NanoVG.nvgStroke(vg);
   }

   public static void arc(float cx, float cy, float r, float a0, float a1, float width, int argb) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgArc(vg, cx, cy, r, a0, a1, 2);
      NanoVG.nvgStrokeWidth(vg, width);
      NanoVG.nvgLineCap(vg, 1);
      NanoVG.nvgStrokeColor(vg, col(argb));
      NanoVG.nvgStroke(vg);
   }

   public static void line(float x1, float y1, float x2, float y2, float width, int argb) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgMoveTo(vg, x1, y1);
      NanoVG.nvgLineTo(vg, x2, y2);
      NanoVG.nvgStrokeWidth(vg, width);
      NanoVG.nvgLineCap(vg, 1);
      NanoVG.nvgStrokeColor(vg, col(argb));
      NanoVG.nvgStroke(vg);
   }

   public static void triangle(float x1, float y1, float x2, float y2, float x3, float y3, int argb) {
      NanoVG.nvgBeginPath(vg);
      NanoVG.nvgMoveTo(vg, x1, y1);
      NanoVG.nvgLineTo(vg, x2, y2);
      NanoVG.nvgLineTo(vg, x3, y3);
      NanoVG.nvgClosePath(vg);
      NanoVG.nvgFillColor(vg, col(argb));
      NanoVG.nvgFill(vg);
   }

   public static void beginPath() {
      NanoVG.nvgBeginPath(vg);
   }

   public static void moveTo(float x, float y) {
      NanoVG.nvgMoveTo(vg, x, y);
   }

   public static void lineTo(float x, float y) {
      NanoVG.nvgLineTo(vg, x, y);
   }

   public static void closePath() {
      NanoVG.nvgClosePath(vg);
   }

   public static void fillPath(int argb) {
      NanoVG.nvgFillColor(vg, col(argb));
      NanoVG.nvgFill(vg);
   }

   public static void strokePath(float width, int argb) {
      NanoVG.nvgStrokeWidth(vg, width);
      NanoVG.nvgLineCap(vg, 1);
      NanoVG.nvgLineJoin(vg, 1);
      NanoVG.nvgStrokeColor(vg, col(argb));
      NanoVG.nvgStroke(vg);
   }

   private static void font(int font, float size, float spacing) {
      NanoVG.nvgFontFaceId(vg, font == 1 ? fontBold : fontRegular);
      NanoVG.nvgFontSize(vg, size);
      NanoVG.nvgTextLetterSpacing(vg, spacing);
   }

   public static float text(String s, float x, float y, float size, int argb, int font) {
      return text(s, x, y, size, argb, font, 0.0F);
   }

   public static float text(String s, float x, float y, float size, int argb, int font, float spacing) {
      font(font, size, spacing);
      NanoVG.nvgTextAlign(vg, 17);
      NanoVG.nvgFillColor(vg, col(argb));
      return NanoVG.nvgText(vg, x, y + size / 2.0F, s);
   }

   public static void textCentered(String s, float cx, float cy, float size, int argb, int font) {
      font(font, size, 0.0F);
      NanoVG.nvgTextAlign(vg, 18);
      NanoVG.nvgFillColor(vg, col(argb));
      NanoVG.nvgText(vg, cx, cy, s);
   }

   public static float textWidth(String s, float size, int font) {
      return textWidth(s, size, font, 0.0F);
   }

   public static float textWidth(String s, float size, int font, float spacing) {
      if (vg == 0L) {
         return s.length() * size * 0.55F;
      }

      font(font, size, spacing);
      return NanoVG.nvgTextBounds(vg, 0.0F, 0.0F, s, (float[])null);
   }

   public static int modImage(String path) {
      return images.computeIfAbsent("mod:" + path, k -> {
         try (InputStream in = Render2D.class.getResourceAsStream(path)) {
            return in == null ? 0 : createImage(in.readAllBytes(), 1);
         } catch (IOException e) {
            return 0;
         }
      });
   }

   public static int mcTexture(String path) {
      return images.computeIfAbsent("mc:" + path, k -> {
         try {
            class_3300 rm = class_310.method_1551().method_1478();
            Optional<class_3298> res = rm.method_14486(class_2960.method_60656(path));
            if (res.isEmpty()) {
               return 0;
            }

            try (InputStream in = res.get().method_14482()) {
               return createImage(in.readAllBytes(), 32);
            }
         } catch (Exception e) {
            return 0;
         }
      });
   }

   private static int createImage(byte[] bytes, int flags) {
      if (vg == 0L) {
         return 0;
      }

      ByteBuffer buf = MemoryUtil.memAlloc(bytes.length);

      try {
         buf.put(bytes).flip();
         int id = NanoVG.nvgCreateImageMem(vg, flags, buf);
         if (id != 0) {
            int[] w = new int[1];
            int[] h = new int[1];
            NanoVG.nvgImageSize(vg, id, w, h);
            imageSizes.put(id, new int[]{w[0], h[0]});
         }

         return id;
      } finally {
         MemoryUtil.memFree(buf);
      }
   }

   public static int[] imageSize(int id) {
      return imageSizes.getOrDefault(id, new int[]{1, 1});
   }

   public static void image(int id, float x, float y, float w, float h, float alpha) {
      if (id != 0) {
         int[] size = imageSize(id);
         float frames = Math.max(1.0F, (float)size[1] / size[0]);
         MemoryStack stack = MemoryStack.stackPush();

         try {
            NVGPaint paint = NVGPaint.malloc(stack);
            NanoVG.nvgImagePattern(vg, x, y, w, h * frames, 0.0F, id, alpha, paint);
            NanoVG.nvgBeginPath(vg);
            NanoVG.nvgRect(vg, x, y, w, h);
            NanoVG.nvgFillPaint(vg, paint);
            NanoVG.nvgFill(vg);
         } catch (Throwable var12) {
            if (stack != null) {
               try {
                  stack.close();
               } catch (Throwable var11) {
                  var12.addSuppressed(var11);
               }
            }

            throw var12;
         }

         if (stack != null) {
            stack.close();
         }
      }
   }

   public static void imageTriangle(
      int id, float u0, float v0, float x0, float y0, float u1, float v1, float x1, float y1, float u2, float v2, float x2, float y2, float alpha
   ) {
      if (id != 0) {
         int[] size = imageSize(id);
         float iw = size[0];
         float ih = size[1];
         float a0 = u0 * iw;
         float b0 = v0 * ih;
         float a1 = u1 * iw;
         float b1 = v1 * ih;
         float a2 = u2 * iw;
         float b2 = v2 * ih;
         float det = (a1 - a0) * (b2 - b0) - (a2 - a0) * (b1 - b0);
         if (!(Math.abs(det) < 1.0E-6F)) {
            float m0 = ((x1 - x0) * (b2 - b0) - (x2 - x0) * (b1 - b0)) / det;
            float m2 = ((x2 - x0) * (a1 - a0) - (x1 - x0) * (a2 - a0)) / det;
            float m1 = ((y1 - y0) * (b2 - b0) - (y2 - y0) * (b1 - b0)) / det;
            float m3 = ((y2 - y0) * (a1 - a0) - (y1 - y0) * (a2 - a0)) / det;
            float m4 = x0 - m0 * a0 - m2 * b0;
            float m5 = y0 - m1 * a0 - m3 * b0;
            MemoryStack stack = MemoryStack.stackPush();

            try {
               NVGPaint paint = NVGPaint.malloc(stack);
               NanoVG.nvgImagePattern(vg, 0.0F, 0.0F, iw, ih, 0.0F, id, alpha, paint);
               FloatBuffer xf = paint.xform();
               xf.put(0, m0);
               xf.put(1, m1);
               xf.put(2, m2);
               xf.put(3, m3);
               xf.put(4, m4);
               xf.put(5, m5);
               NanoVG.nvgBeginPath(vg);
               NanoVG.nvgMoveTo(vg, x0, y0);
               NanoVG.nvgLineTo(vg, x1, y1);
               NanoVG.nvgLineTo(vg, x2, y2);
               NanoVG.nvgClosePath(vg);
               NanoVG.nvgFillPaint(vg, paint);
               NanoVG.nvgFill(vg);
            } catch (Throwable var34) {
               if (stack != null) {
                  try {
                     stack.close();
                  } catch (Throwable var33) {
                     var34.addSuppressed(var33);
                  }
               }

               throw var34;
            }

            if (stack != null) {
               stack.close();
            }
         }
      }
   }

   public static void verticalGradient(float x, float y, float w, float h, float r, int top, int bottom) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         NVGPaint paint = NVGPaint.malloc(stack);
         NanoVG.nvgLinearGradient(vg, x, y, x, y + h, rgba(color, top), rgba(color2, bottom), paint);
         NanoVG.nvgBeginPath(vg);
         NanoVG.nvgRoundedRect(vg, x, y, w, h, r);
         NanoVG.nvgFillPaint(vg, paint);
         NanoVG.nvgFill(vg);
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
   }

   public static void radialRoundRect(float x, float y, float w, float h, float r, float cx, float cy, float inner, float outer, int cIn, int cOut) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         NVGPaint paint = NVGPaint.malloc(stack);
         NanoVG.nvgRadialGradient(vg, cx, cy, inner, outer, rgba(color, cIn), rgba(color2, cOut), paint);
         NanoVG.nvgBeginPath(vg);
         NanoVG.nvgRoundedRect(vg, x, y, w, h, r);
         NanoVG.nvgFillPaint(vg, paint);
         NanoVG.nvgFill(vg);
      } catch (Throwable var15) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var14) {
               var15.addSuppressed(var14);
            }
         }

         throw var15;
      }

      if (stack != null) {
         stack.close();
      }
   }

   public static void glow(float cx, float cy, float radius, int argb) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         NVGPaint paint = NVGPaint.malloc(stack);
         NanoVG.nvgRadialGradient(vg, cx, cy, 0.0F, radius, rgba(color, argb), rgba(color2, argb & 16777215), paint);
         NanoVG.nvgBeginPath(vg);
         NanoVG.nvgCircle(vg, cx, cy, radius);
         NanoVG.nvgFillPaint(vg, paint);
         NanoVG.nvgFill(vg);
      } catch (Throwable var8) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (stack != null) {
         stack.close();
      }
   }

   public static void gradientOutline(float x, float y, float w, float h, float r, float width, float x0, float y0, float x1, float y1, int c0, int c1) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         NVGPaint paint = NVGPaint.malloc(stack);
         NanoVG.nvgLinearGradient(vg, x0, y0, x1, y1, rgba(color, c0), rgba(color2, c1), paint);
         NanoVG.nvgBeginPath(vg);
         NanoVG.nvgRoundedRect(vg, x, y, w, h, r);
         NanoVG.nvgStrokeWidth(vg, width);
         NanoVG.nvgStrokePaint(vg, paint);
         NanoVG.nvgStroke(vg);
      } catch (Throwable var16) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var15) {
               var16.addSuppressed(var15);
            }
         }

         throw var16;
      }

      if (stack != null) {
         stack.close();
      }
   }

   public static float textGradient(String s, float x, float y, float size, int left, int right, int font, float spacing) {
      float w = textWidth(s, size, font, spacing);
      MemoryStack stack = MemoryStack.stackPush();

      float var11;
      try {
         NVGPaint paint = NVGPaint.malloc(stack);
         NanoVG.nvgLinearGradient(vg, x, y, x + Math.max(1.0F, w), y, rgba(color, left), rgba(color2, right), paint);
         font(font, size, spacing);
         NanoVG.nvgTextAlign(vg, 17);
         NanoVG.nvgFillPaint(vg, paint);
         var11 = NanoVG.nvgText(vg, x, y + size / 2.0F, s);
      } catch (Throwable var13) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var12) {
               var13.addSuppressed(var12);
            }
         }

         throw var13;
      }

      if (stack != null) {
         stack.close();
      }

      return var11;
   }

   public static void textCenteredTop(String s, float cx, float y, float size, int argb, int font, float spacing) {
      float w = textWidth(s, size, font, spacing);
      text(s, cx - w / 2.0F, y, size, argb, font, spacing);
   }

   public static int glImage(int glTexture, int w, int h) {
      return vg != 0L && glTexture > 0 ? glImages.computeIfAbsent(glTexture, id -> {
         int img = NanoVGGL3.nvglCreateImageFromHandle(vg, id, w, h, 65568);
         if (img != 0) {
            imageSizes.put(img, new int[]{w, h});
         }

         return img;
      }) : 0;
   }

   public static void imageRegion(int id, float sx, float sy, float sw, float sh, float x, float y, float w, float h, float alpha) {
      if (id != 0) {
         int[] size = imageSize(id);
         float kx = w / sw;
         float ky = h / sh;
         MemoryStack stack = MemoryStack.stackPush();

         try {
            NVGPaint paint = NVGPaint.malloc(stack);
            NanoVG.nvgImagePattern(vg, x - sx * kx, y - sy * ky, size[0] * kx, size[1] * ky, 0.0F, id, alpha, paint);
            NanoVG.nvgBeginPath(vg);
            NanoVG.nvgRect(vg, x, y, w, h);
            NanoVG.nvgFillPaint(vg, paint);
            NanoVG.nvgFill(vg);
         } catch (Throwable var17) {
            if (stack != null) {
               try {
                  stack.close();
               } catch (Throwable var16) {
                  var17.addSuppressed(var16);
               }
            }

            throw var17;
         }

         if (stack != null) {
            stack.close();
         }
      }
   }

   public static int withAlpha(int argb, float alpha) {
      int a = (int)((argb >>> 24 & 0xFF) * Math.max(0.0F, Math.min(1.0F, alpha)));
      return a << 24 | argb & 16777215;
   }
}
