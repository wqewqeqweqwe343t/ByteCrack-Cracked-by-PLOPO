package dev.byteplus.render;

import dev.byteplus.BytePlus;
import dev.byteplus.dev.DevCapture;
import dev.byteplus.gui.ByteMenuScreen;
import dev.byteplus.gui.ClickGui;
import dev.byteplus.gui.ClickGuiScreen;
import dev.byteplus.gui.TitleScreenOverlay;
import dev.byteplus.hud.Hud;
import dev.byteplus.module.Module;
import dev.byteplus.util.Theme;
import net.minecraft.class_1041;
import net.minecraft.class_310;
import net.minecraft.class_408;
import net.minecraft.class_437;
import org.lwjgl.opengl.GL33C;

public final class RenderHook {
   private static final GlStateGuard guard = new GlStateGuard();
   private static long lastFrameNanos = System.nanoTime();
   private static float deltaSeconds;

   private RenderHook() {
   }

   public static float delta() {
      return deltaSeconds;
   }

   public static boolean worldOverlaysAllowed(class_437 screen) {
      return screen == null || screen instanceof class_408 || screen instanceof ClickGuiScreen;
   }

   public static void onFrame() {
      long now = System.nanoTime();
      deltaSeconds = Math.min(0.1F, (float)(now - lastFrameNanos) / 1.0E9F);
      lastFrameNanos = now;
      class_310 mc = class_310.method_1551();
      Theme.update();
      BytePlus.modules().pollKeybinds();
      BytePlus.modules().onFrame(deltaSeconds);
      class_1041 window = mc.method_22683();
      int fbW = window.method_4489();
      int fbH = window.method_4506();
      if (fbW > 0 && fbH > 0) {
         guard.save();

         try {
            if (Render2D.init()) {
               GL33C.glBindFramebuffer(36160, 0);
               GL33C.glViewport(0, 0, fbW, fbH);
               GL33C.glBindSampler(0, 0);
               if (mc.field_1755 instanceof ClickGuiScreen && BytePlus.modules().clickGui.blur.isOn()) {
                  BlurRenderer.blurScreen(fbW, fbH, BytePlus.modules().clickGui.blurStrength.getInt(), ClickGui.INSTANCE.openProgress());
                  GL33C.glBindFramebuffer(36160, 0);
                  GL33C.glViewport(0, 0, fbW, fbH);
                  GL33C.glBindSampler(0, 0);
                  GL33C.glBindVertexArray(0);
                  GL33C.glUseProgram(0);
               }

               Render2D.beginFrame(fbW, fbH);
               float guiScale = window.method_4495();
               float sw = window.method_4486();
               float sh = window.method_4502();
               boolean inWorld = mc.field_1687 != null && mc.field_1724 != null;
               boolean overlays = inWorld && worldOverlaysAllowed(mc.field_1755);
               if (overlays && Projection.isValid()) {
                  Esp.begin(fbW, fbH, guiScale);

                  for (Module m : BytePlus.modules().all()) {
                     if (m.isEnabled()) {
                        safe(m, () -> m.onRenderWorld());
                     }
                  }

                  Esp.flush(0.5F);

                  for (Module m : BytePlus.modules().all()) {
                     if (m.isEnabled()) {
                        safe(m, () -> m.onRenderScreen(fbW, fbH, guiScale));
                     }
                  }
               }

               if (overlays && !mc.field_1690.field_1842) {
                  Render2D.save();
                  Render2D.scale(guiScale);

                  for (Module m : BytePlus.modules().all()) {
                     if (m.isEnabled()) {
                        safe(m, () -> m.onRenderOverlay(sw, sh, deltaSeconds));
                     }
                  }

                  if (BytePlus.modules().hud.isEnabled()) {
                     Hud.render(sw, sh, deltaSeconds, mc.field_1755 instanceof ClickGuiScreen);
                  }

                  Render2D.restore();
               }

               Render2D.save();
               Render2D.scale(guiScale);
               BytePlus.modules().renderGlobal(sw, sh, deltaSeconds);
               Render2D.restore();
               if (mc.field_1755 instanceof ByteMenuScreen menu) {
                  Render2D.save();
                  Render2D.scale(guiScale);
                  menu.draw(sw, sh, deltaSeconds);
                  Render2D.restore();
               }

               if (TitleScreenOverlay.active()) {
                  Render2D.save();
                  Render2D.scale(guiScale);
                  TitleScreenOverlay.draw(sw, sh, deltaSeconds);
                  Render2D.restore();
               } else {
                  TitleScreenOverlay.reset();
               }

               if (mc.field_1755 instanceof ClickGuiScreen gui) {
                  Render2D.save();
                  Render2D.scale(guiScale);
                  gui.draw(sw, sh, deltaSeconds);
                  Render2D.restore();
               }

               Render2D.endFrame();
               DevCapture.onFrameEnd(fbW, fbH);
               return;
            }
         } catch (Throwable t) {
            BytePlus.LOGGER.error("Byte+ render error", t);
            return;
         } finally {
            guard.restore();
            Projection.invalidate();
         }
      }
   }

   private static void safe(Module m, Runnable r) {
      try {
         r.run();
      } catch (Throwable t) {
         BytePlus.LOGGER.error("Render error in {}", m.getName(), t);
      }
   }
}
