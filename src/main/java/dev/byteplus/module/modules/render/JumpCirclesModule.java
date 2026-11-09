package dev.byteplus.module.modules.render;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.render.Esp;
import dev.byteplus.render.Projection;
import dev.byteplus.render.Render2D;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.ColorSetting;
import dev.byteplus.setting.NumberSetting;
import dev.byteplus.util.Anim;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.joml.Vector4f;

public class JumpCirclesModule extends Module {
   private final NumberSetting size = this.add(new NumberSetting("Size", 1.4, 0.5, 4.0, 0.1));
   private final NumberSetting duration = this.add(new NumberSetting("Duration", 1.2, 0.3, 4.0, 0.1));
   private final BooleanSetting ring = this.add(new BooleanSetting("Ring", true));
   private final ColorSetting color = this.add(new ColorSetting("Ring Color", -14040));
   private final List<JumpCirclesModule.Circle> circles = new ArrayList<>();
   private boolean wasOnGround = true;

   public JumpCirclesModule() {
      super("JumpCircles", "Shows the Byte+ logo on the floor when you jump.", Category.RENDER);
   }

   @Override
   protected void onDisable() {
      this.circles.clear();
   }

   @Override
   public void onTick() {
      boolean onGround = mc.field_1724.method_24828();
      if (this.wasOnGround && !onGround && mc.field_1724.method_18798().field_1351 > 0.2) {
         this.circles
            .add(
               new JumpCirclesModule.Circle(mc.field_1724.method_23317(), mc.field_1724.method_23318() + 0.02, mc.field_1724.method_23321(), System.nanoTime())
            );
      }

      this.wasOnGround = onGround;
   }

   @Override
   public void onRenderScreen(int fbW, int fbH, float guiScale) {
      int logo = Render2D.modImage("/assets/byteplus/textures/logo.png");
      float life = this.duration.getFloat();
      Iterator<JumpCirclesModule.Circle> it = this.circles.iterator();

      while (it.hasNext()) {
         JumpCirclesModule.Circle c = it.next();
         float t = (float)(System.nanoTime() - c.start) / 1.0E9F / life;
         if (t >= 1.0F) {
            it.remove();
         } else {
            float r = this.size.getFloat() * (0.35F + 0.65F * Anim.easeOutCubic(t));
            float alpha = t < 0.15F ? t / 0.15F : 1.0F - Anim.easeInOutCubic((t - 0.15F) / 0.85F);
            int n = 6;
            float[][] sx = new float[n + 1][n + 1];
            float[][] sy = new float[n + 1][n + 1];
            boolean[][] ok = new boolean[n + 1][n + 1];
            Vector4f v = new Vector4f();

            for (int i = 0; i <= n; i++) {
               for (int j = 0; j <= n; j++) {
                  double wx = c.x - r + 2.0F * r * i / n;
                  double wz = c.z - r + 2.0F * r * j / n;
                  Projection.toClip(wx, c.y, wz, v);
                  ok[i][j] = v.w > 0.05F;
                  if (ok[i][j]) {
                     sx[i][j] = (v.x / v.w * 0.5F + 0.5F) * fbW;
                     sy[i][j] = (0.5F - v.y / v.w * 0.5F) * fbH;
                  }
               }
            }

            for (int i = 0; i < n; i++) {
               for (int j = 0; j < n; j++) {
                  if (ok[i][j] && ok[i + 1][j] && ok[i][j + 1] && ok[i + 1][j + 1]) {
                     float u0 = (float)i / n;
                     float u1 = (float)(i + 1) / n;
                     float v0 = (float)j / n;
                     float v1 = (float)(j + 1) / n;
                     Render2D.imageTriangle(logo, u0, v0, sx[i][j], sy[i][j], u1, v0, sx[i + 1][j], sy[i + 1][j], u0, v1, sx[i][j + 1], sy[i][j + 1], alpha);
                     Render2D.imageTriangle(
                        logo, u1, v0, sx[i + 1][j], sy[i + 1][j], u1, v1, sx[i + 1][j + 1], sy[i + 1][j + 1], u0, v1, sx[i][j + 1], sy[i][j + 1], alpha
                     );
                  }
               }
            }

            if (this.ring.isOn()) {
               Render2D.beginPath();
               boolean started = false;

               for (int k = 0; k <= 48; k++) {
                  double a = (Math.PI * 2) * k / 48.0;
                  Projection.toClip(c.x + Math.cos(a) * r * 1.05, c.y, c.z + Math.sin(a) * r * 1.05, v);
                  if (v.w <= 0.05F) {
                     started = false;
                  } else {
                     float x = (v.x / v.w * 0.5F + 0.5F) * fbW;
                     float y = (0.5F - v.y / v.w * 0.5F) * fbH;
                     if (!started) {
                        Render2D.moveTo(x, y);
                        started = true;
                     } else {
                        Render2D.lineTo(x, y);
                     }
                  }
               }

               int a8 = (int)(alpha * 220.0F);
               Render2D.strokePath(Esp.px() * 3.5F, a8 / 5 << 24 | this.color.rgb());
               Render2D.strokePath(Esp.px() * 1.3F, a8 << 24 | this.color.rgb());
            }
         }
      }
   }

   private record Circle(double x, double y, double z, long start) {
   }
}
