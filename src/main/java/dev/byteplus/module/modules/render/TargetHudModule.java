package dev.byteplus.module.modules.render;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.module.modules.misc.NameProtectModule;
import dev.byteplus.render.Render2D;
import dev.byteplus.setting.NumberSetting;
import dev.byteplus.util.Anim;
import dev.byteplus.util.Theme;
import net.minecraft.class_1657;
import net.minecraft.class_640;

public class TargetHudModule extends Module {
   private final NumberSetting scale = this.add(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.05));
   private final NumberSetting yOffset = this.add(new NumberSetting("Y Offset", 40.0, -200.0, 200.0, 1.0));
   private class_1657 target;
   private long lastSeen;
   private float anim;
   private float hpAnim;

   public TargetHudModule() {
      super("TargetHUD", "Shows details about the player you aim at.", Category.RENDER);
   }

   @Override
   public void onRenderOverlay(float sw, float sh, float dt) {
      if (mc.field_1692 instanceof class_1657 p && p != mc.field_1724) {
         if (this.target != p) {
            this.hpAnim = p.method_6032() / Math.max(1.0F, p.method_6063());
         }

         this.target = p;
         this.lastSeen = System.currentTimeMillis();
      }

      boolean show = this.target != null && this.target.method_5805() && System.currentTimeMillis() - this.lastSeen < 2500L;
      this.anim = Anim.approach(this.anim, show ? 1.0F : 0.0F, 12.0F, dt);
      if (!(this.anim <= 0.01F) && this.target != null) {
         class_1657 p = this.target;
         float v = Anim.easeOutCubic(this.anim);
         float w = 128.0F;
         float h = 42.0F;
         float s = this.scale.getFloat();
         Render2D.save();
         Render2D.translate(sw / 2.0F, sh / 2.0F + this.yOffset.getFloat());
         Render2D.scale(s * (0.9F + 0.1F * v));
         Render2D.globalAlpha(v);
         float x = -w / 2.0F;
         float y = 0.0F;
         Render2D.shadow(x, y, w, h, 6.0F, 12.0F, 1610612736);
         Render2D.roundRect(x, y, w, h, 6.0F, Theme.PANEL_BG);
         Render2D.roundRectOutline(x, y, w, h, 6.0F, 0.6F, Theme.HUD_OUTLINE);
         String name = NameProtectModule.replace(p.method_5477().getString());
         Render2D.roundRect(x + 6.0F, y + 6.0F, 30.0F, 30.0F, 5.0F, Theme.ACCENT_SOFT);
         Render2D.textCentered(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase(), x + 21.0F, y + 21.5F, 16.0F, Theme.ACCENT, 1);
         Render2D.text(name, x + 42.0F, y + 6.0F, 8.0F, Theme.TEXT, 1, 0.3F);
         float hp = p.method_6032();
         float max = Math.max(1.0F, p.method_6063());
         float abs = p.method_6067();
         this.hpAnim = Anim.approach(this.hpAnim, hp / max, 8.0F, dt);
         float dist = mc.field_1724.method_5739(p);
         int ping = -1;
         if (mc.method_1562() != null) {
            class_640 info = mc.method_1562().method_2871(p.method_5667());
            if (info != null) {
               ping = info.method_2959();
            }
         }

         String sub = String.format("%.1fm", dist) + (ping >= 0 ? "  " + ping + "ms" : "") + "  " + p.method_6096() + " armor";
         Render2D.text(sub, x + 42.0F, y + 16.0F, 6.5F, Theme.TEXT_DIM, 0);
         float bx = x + 42.0F;
         float by = y + 28.0F;
         float bw = w - 48.0F;
         float bh = 5.0F;
         Render2D.roundRect(bx, by, bw, bh, 2.5F, -14014428);
         int c = this.hpAnim > 0.6F ? -9705867 : (this.hpAnim > 0.3F ? -14040 : -42419);
         Render2D.roundRect(bx, by, Math.max(bh, bw * Math.min(1.0F, this.hpAnim)), bh, 2.5F, c);
         if (abs > 0.0F) {
            Render2D.roundRect(bx, by, Math.max(bh, bw * Math.min(1.0F, abs / max)), bh, 2.5F, -855648179);
         }

         String hpText = String.format("%.1f", hp + abs);
         float hw = Render2D.textWidth(hpText, 6.5F, 1);
         Render2D.text(hpText, x + w - 6.0F - hw, y + 6.5F, 6.5F, c, 1);
         float mine = mc.field_1724.method_6032() + mc.field_1724.method_6067();
         boolean winning = mine >= hp + abs;
         Render2D.roundRect(x + w - 6.0F - 5.0F, y + 17.0F, 5.0F, 5.0F, 2.5F, winning ? -9705867 : -42419);
         Render2D.restore();
      } else {
         if (this.anim <= 0.01F) {
            this.target = null;
         }
      }
   }
}
