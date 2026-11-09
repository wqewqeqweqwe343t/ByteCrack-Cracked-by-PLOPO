package dev.byteplus.module.modules.misc;

import dev.byteplus.config.Config;
import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.ColorSetting;
import dev.byteplus.setting.InfoSetting;
import dev.byteplus.util.Friends;
import net.minecraft.class_1657;
import org.lwjgl.glfw.GLFW;

public class FriendsModule extends Module {
   public final BooleanSetting middleClick = this.add(new BooleanSetting("Middle Click", true));
   public final ColorSetting color = this.add(new ColorSetting("Friend Color", -11800454));
   public final InfoSetting count = this.add(new InfoSetting("Friends", () -> Friends.list().size() + " added"));
   private boolean wasDown;

   public FriendsModule() {
      super("Friends", "Middle-click players to friend them. Friends are safe from combat modules.", Category.MISC);
   }

   @Override
   public boolean showInArrayList() {
      return false;
   }

   @Override
   public void onFrame(float dt) {
      boolean down = mc.field_1755 == null && GLFW.glfwGetMouseButton(mc.method_22683().method_4490(), 2) == 1;
      if (down && !this.wasDown && this.middleClick.isOn() && mc.field_1692 instanceof class_1657 p) {
         String name = p.method_7334().name();
         boolean now = Friends.toggle(name);
         this.info(now ? "Added " + name : "Removed " + name);
         Config.save();
      }

      this.wasDown = down;
   }
}
