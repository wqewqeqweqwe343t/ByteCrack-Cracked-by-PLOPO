package dev.byteplus.gui;

import dev.byteplus.BytePlus;
import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.render.Icons;
import java.util.ArrayList;
import java.util.List;

final class CategoryPanel extends Panel {
   private final Category category;
   private final List<ModuleButton> buttons = new ArrayList<>();

   CategoryPanel(Category category, float x, float y) {
      super(category.name(), x, y);
      this.category = category;

      for (Module m : BytePlus.modules().byCategory(category)) {
         this.buttons.add(new ModuleButton(m));
      }
   }

   @Override
   String title() {
      return this.category.displayName;
   }

   @Override
   void drawIcon(float x, float y, float size, int color) {
      Icons.category(this.category, x, y, size, color);
   }

   @Override
   List<ModuleButton> buttons() {
      return this.buttons;
   }
}
