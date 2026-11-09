package dev.byteplus.gui;

import dev.byteplus.BytePlus;
import dev.byteplus.dev.DevCapture;
import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import net.minecraft.class_1041;
import net.minecraft.class_310;

public final class GuiDev {
   private GuiDev() {
   }

   public static void select(Category c) {
      ClickGui.INSTANCE.window().devSelect(c);
   }

   public static void expand(String module) {
      ClickGui.INSTANCE.window().devExpand(module);
   }

   public static float[] rowCenter(String module) {
      return ClickGui.INSTANCE.window().devRowCenter(module);
   }

   public static void type(String text) {
      for (char c : text.toCharArray()) {
         ClickGui.INSTANCE.charTyped(String.valueOf(c));
      }
   }

   public static void key(int glfwKey) {
      ClickGui.INSTANCE.keyPressed(glfwKey);
   }

   public static void toggle(String module) {
      for (Module m : BytePlus.modules().all()) {
         if (m.getName().equals(module)) {
            m.toggle();
         }
      }
   }

   public static float[] mouse() {
      if (DevCapture.mouseX != null) {
         return new float[]{DevCapture.mouseX, DevCapture.mouseY};
      }

      class_310 mc = class_310.method_1551();
      class_1041 window = mc.method_22683();
      return new float[]{
         (float)(mc.field_1729.method_1603() * window.method_4486() / Math.max(1, window.method_4480())),
         (float)(mc.field_1729.method_1604() * window.method_4502() / Math.max(1, window.method_4507()))
      };
   }
}
