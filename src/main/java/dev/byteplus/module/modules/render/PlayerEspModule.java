package dev.byteplus.module.modules.render;

import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.render.Esp;
import dev.byteplus.render.Projection;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.ColorSetting;
import dev.byteplus.setting.NumberSetting;
import dev.byteplus.util.Friends;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import net.minecraft.class_243;

public class PlayerEspModule extends Module {
   private final ColorSetting color = this.add(new ColorSetting("Color", -14040));
   private final BooleanSetting box = this.add(new BooleanSetting("Box", true));
   private final BooleanSetting fill = this.add(new BooleanSetting("Fill", true));
   private final BooleanSetting tracers = this.add(new BooleanSetting("Tracers", true));
   private final NumberSetting alpha = this.add(new NumberSetting("Alpha", 60.0, 0.0, 255.0, 1.0));
   private final NumberSetting range = this.add(new NumberSetting("Range", 256.0, 16.0, 1024.0, 16.0));

   public PlayerEspModule() {
      super("Player ESP", "Shows players through walls.", Category.RENDER);
   }

   @Override
   public void onRenderWorld() {
      float pt = Projection.partialTick();
      double maxSq = this.range.get() * this.range.get();

      for (class_1657 p : mc.field_1687.method_18456()) {
         if (p != mc.field_1724 && p.method_5805() && !(p.method_5858(mc.field_1724) > maxSq)) {
            class_243 pos = p.method_30950(pt);
            int rgb = Friends.isFriend(p) ? Friends.color() & 16777215 : this.color.rgb();
            class_238 bb = p.method_5829().method_997(pos.method_1020(p.method_73189()));
            if (this.box.isOn() || this.fill.isOn()) {
               Esp.box(
                  bb.field_1323,
                  bb.field_1322,
                  bb.field_1321,
                  bb.field_1320,
                  bb.field_1325,
                  bb.field_1324,
                  rgb,
                  this.fill.isOn() ? this.alpha.getInt() : 0,
                  this.box.isOn() ? 230 : 0
               );
            }

            if (this.tracers.isOn()) {
               Esp.tracer(pos.field_1352, pos.field_1351 + p.method_17682() * 0.55, pos.field_1350, rgb, 230);
            }
         }
      }
   }
}
