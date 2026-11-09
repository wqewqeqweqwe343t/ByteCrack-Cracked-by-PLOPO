package dev.byteplus.gui;

import dev.byteplus.BytePlus;
import dev.byteplus.config.Config;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

public class ClickGuiScreen extends class_437 {
   public ClickGuiScreen() {
      super(class_2561.method_43470("Cracked By PLOPO"));
   }

   public static ClickGuiScreen get() {
      return new ClickGuiScreen();
   }

   protected void method_25426() {
      ClickGui.INSTANCE.onOpen();
   }

   public void draw(float screenW, float screenH, float dt) {
      float[] m = GuiDev.mouse();
      ClickGui.INSTANCE.draw(screenW, screenH, m[0], m[1], dt);
   }

   public void method_25394(class_332 graphics, int mouseX, int mouseY, float delta) {
   }

   public void method_25420(class_332 graphics, int mouseX, int mouseY, float delta) {
   }

   public boolean method_25421() {
      return false;
   }

   public boolean method_25402(class_11909 event, boolean doubleClick) {
      return ClickGui.INSTANCE.mouseClicked(event.comp_4798(), event.comp_4799(), event.method_74245());
   }

   public boolean method_25406(class_11909 event) {
      ClickGui.INSTANCE.mouseReleased();
      return true;
   }

   public boolean method_25403(class_11909 event, double dx, double dy) {
      return true;
   }

   public boolean method_25401(double x, double y, double horizontal, double vertical) {
      ClickGui.INSTANCE.mouseScrolled(vertical);
      return true;
   }

   public boolean method_25404(class_11908 event) {
      if (ClickGui.INSTANCE.keyPressed(event.comp_4795())) {
         return true;
      }

      // The global keybind opens this screen.  Handling that same key here as
      // a close request makes the menu flicker shut on platforms that deliver
      // the opening key event after the screen becomes active.  Escape is the
      // single, predictable close action; every other key is consumed so it
      // cannot leak into gameplay while the menu is visible.
      if (event.comp_4795() == 256) {
         this.method_25419();
      }
      return true;
   }

   public boolean method_25400(class_11905 event) {
      return ClickGui.INSTANCE.charTyped(event.method_74226());
   }

   public void method_25432() {
      ClickGui.INSTANCE.onClose();
      Config.save();
   }
}
