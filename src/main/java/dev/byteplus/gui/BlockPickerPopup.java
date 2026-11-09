package dev.byteplus.gui;

import dev.byteplus.render.Render2D;
import dev.byteplus.setting.BlockListSetting;
import dev.byteplus.util.Anim;
import dev.byteplus.util.Theme;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_7923;

final class BlockPickerPopup extends Popup {
   private static final float CELL = 20.0F;
   private static final float GAP = 3.0F;
   private static final List<class_2248> ALL = new ArrayList<>();
   private static final Map<class_2248, Integer> ICONS = new HashMap<>();
   private static final Map<class_2248, String> IDS = new HashMap<>();
   private final String title;
   private final BlockListSetting setting;
   private final Map<class_2248, Integer> working;
   private final StringBuilder query = new StringBuilder();
   private boolean selectedTab;
   private float scroll;
   private float scrollTarget;
   private float caret;
   private List<class_2248> shown = new ArrayList<>();
   private int iconLoadsThisFrame;

   BlockPickerPopup(String title, BlockListSetting setting) {
      this.title = title;
      this.setting = setting;
      this.working = new LinkedHashMap<>(setting.get());
      if (ALL.isEmpty()) {
         for (class_2248 b : class_7923.field_41175) {
            if (b != class_2246.field_10124 && b != class_2246.field_10543 && b != class_2246.field_10243) {
               ALL.add(b);
               IDS.put(b, class_7923.field_41175.method_10221(b).method_12832());
            }
         }
      }

      this.refresh();
   }

   @Override
   String title() {
      return "Block Colors: " + this.title + " (" + this.working.size() + " blocks)";
   }

   private void refresh() {
      String q = this.query.toString().toLowerCase().replace(' ', '_');
      List<class_2248> list = new ArrayList<>();
      List<class_2248> pool = this.setting.choices() != null ? this.setting.choices() : ALL;

      for (class_2248 b : this.selectedTab ? new ArrayList<>(this.working.keySet()) : pool) {
         if (q.isEmpty() || IDS.get(b).contains(q)) {
            list.add(b);
         }
      }

      this.shown = list;
   }

   @Override
   void layout(float sw, float sh) {
      this.w = Math.min(310.0F, sw - 20.0F);
      this.h = Math.min(230.0F, sh - 20.0F);
      this.x = (sw - this.w) / 2.0F;
      this.y = (sh - this.h) / 2.0F;
   }

   private float gridX() {
      return this.x + 10.0F;
   }

   private float gridY() {
      return this.y + 58.0F;
   }

   private float gridW() {
      return this.w - 20.0F;
   }

   private float gridH() {
      return this.h - 58.0F - 26.0F;
   }

   private int cols() {
      return Math.max(1, (int)((this.gridW() + 3.0F) / 23.0F));
   }

   private int icon(class_2248 b) {
      Integer cached = ICONS.get(b);
      if (cached != null) {
         return cached;
      }

      if (this.iconLoadsThisFrame++ > 12) {
         return 0;
      }

      String id = IDS.get(b);
      String[] tries = new String[]{
         "textures/block/" + id + ".png",
         "textures/block/" + id + "_side.png",
         "textures/block/" + id + "_top.png",
         "textures/block/" + id + "_front.png",
         "textures/item/" + id + ".png",
         "textures/block/" + id.replace("_wall", "") + ".png",
         "textures/block/"
            + id.replace("_stairs", "").replace("_slab", "").replace("_wall", "").replace("_fence_gate", "_planks").replace("_fence", "_planks")
            + ".png",
         "textures/block/" + id.replace("_stairs", "s").replace("_slab", "s") + ".png"
      };
      int img = 0;

      for (String t : tries) {
         img = Render2D.mcTexture(t);
         if (img != 0) {
            break;
         }
      }

      ICONS.put(b, img == 0 ? -1 : img);
      return img == 0 ? -1 : img;
   }

   @Override
   void drawContent(float mx, float my, float dt) {
      this.iconLoadsThisFrame = 0;
      this.caret += dt;
      this.scroll = Anim.approach(this.scroll, this.scrollTarget, 16.0F, dt);
      boolean hAll = button(
         this.setting.choices() != null ? "ALL (" + this.setting.choices().size() + ")" : "ALL BLOCKS",
         this.x + 10.0F,
         this.y + 25.0F,
         70.0F,
         13.0F,
         this.selectedTab ? Theme.TOGGLE_OFF : Theme.ACCENT,
         this.selectedTab ? Theme.TEXT : Theme.ACCENT_DARK,
         mx,
         my
      );
      button(
         "SELECTED (" + this.working.size() + ")",
         this.x + 84.0F,
         this.y + 25.0F,
         80.0F,
         13.0F,
         this.selectedTab ? Theme.ACCENT : Theme.TOGGLE_OFF,
         this.selectedTab ? Theme.ACCENT_DARK : Theme.TEXT,
         mx,
         my
      );
      float sy = this.y + 41.0F;
      Render2D.roundRect(this.x + 10.0F, sy, this.w - 20.0F, 13.0F, 3.0F, -14869736);
      Render2D.roundRectOutline(this.x + 10.0F, sy, this.w - 20.0F, 13.0F, 3.0F, 0.7F, this.query.isEmpty() ? 587202559 : Theme.ACCENT);
      String q = this.query.toString();
      float end = q.isEmpty()
         ? Render2D.text("SEARCH...", this.x + 15.0F, sy + 3.2F, 6.6F, Theme.TEXT_DIM, 1, 0.4F)
         : Render2D.text(q, this.x + 15.0F, sy + 3.2F, 6.6F, Theme.TEXT, 1, 0.2F);
      if (this.caret % 1.0F < 0.55F) {
         Render2D.rect(q.isEmpty() ? this.x + 14.0F : end + 0.6F, sy + 2.5F, 0.9F, 8.0F, Theme.ACCENT);
      }

      float gx = this.gridX();
      float gy = this.gridY();
      float gw = this.gridW();
      float gh = this.gridH();
      int cols = this.cols();
      int rows = (this.shown.size() + cols - 1) / cols;
      float contentH = rows * 23.0F;
      float maxScroll = Math.max(0.0F, contentH - gh);
      this.scrollTarget = Math.max(-maxScroll, Math.min(0.0F, this.scrollTarget));
      Render2D.roundRect(gx - 3.0F, gy - 3.0F, gw + 6.0F, gh + 6.0F, 4.0F, -15461615);
      Render2D.save();
      Render2D.scissor(gx - 2.0F, gy - 2.0F, gw + 4.0F, gh + 4.0F);
      class_2248 hovered = null;
      int firstRow = Math.max(0, (int)(-this.scroll / 23.0F) - 1);
      int lastRow = Math.min(rows, firstRow + (int)(gh / 23.0F) + 3);

      for (int r = firstRow; r < lastRow; r++) {
         for (int c = 0; c < cols; c++) {
            int i = r * cols + c;
            if (i >= this.shown.size()) {
               break;
            }

            class_2248 b = this.shown.get(i);
            float cx = gx + c * 23.0F;
            float cy = gy + r * 23.0F + this.scroll;
            boolean sel = this.working.containsKey(b);
            boolean hov = in(mx, my, cx, cy, 20.0F, 20.0F) && in(mx, my, gx, gy, gw, gh);
            if (hov) {
               hovered = b;
            }

            int bg = sel ? Anim.lerpColor(-14014687, 0xFF000000 | this.working.get(b), 0.35F) : (hov ? -13751514 : -14541029);
            Render2D.roundRect(cx, cy, 20.0F, 20.0F, 3.0F, bg);
            int img = this.icon(b);
            if (img > 0) {
               Render2D.image(img, cx + 3.0F, cy + 3.0F, 14.0F, 14.0F, 1.0F);
            } else {
               Render2D.roundRect(cx + 4.0F, cy + 4.0F, 12.0F, 12.0F, 2.0F, 0xFF000000 | BlockListSetting.defaultColor(b));
            }

            if (sel) {
               Render2D.circle(cx + 20.0F - 3.5F, cy + 3.5F, 2.8F, 0xFF000000 | this.working.get(b));
               Render2D.circleOutline(cx + 20.0F - 3.5F, cy + 3.5F, 2.8F, 0.6F, -855638017);
               Render2D.roundRectOutline(cx, cy, 20.0F, 20.0F, 3.0F, 0.8F, Theme.ACCENT);
            }
         }
      }

      Render2D.restore();
      if (maxScroll > 0.0F) {
         float barH = Math.max(14.0F, gh * gh / contentH);
         float barY = gy + -this.scroll / maxScroll * (gh - barH);
         Render2D.roundRect(gx + gw + 1.0F, barY, 2.0F, barH, 1.0F, Theme.ACCENT);
      }

      String hint = hovered != null
         ? hovered.method_9518().getString() + (this.working.containsKey(hovered) ? "  -  right-click: color" : "")
         : this.shown.size() + " shown";
      Render2D.text(hint, this.x + 10.0F, this.y + this.h - 15.5F, 6.4F, Theme.TEXT_DIM, 1, 0.2F);
      button("CLEAR ALL", this.x + this.w - 168.0F, this.y + this.h - 19.0F, 54.0F, 14.0F, Theme.DANGER, -19533, mx, my);
      button("CANCEL", this.x + this.w - 110.0F, this.y + this.h - 19.0F, 48.0F, 14.0F, Theme.TOGGLE_OFF, Theme.TEXT, mx, my);
      button("SAVE", this.x + this.w - 58.0F, this.y + this.h - 19.0F, 48.0F, 14.0F, Theme.ACCENT, Theme.ACCENT_DARK, mx, my);
   }

   @Override
   boolean mouseClicked(float mx, float my, int button) {
      if (!this.contains(mx, my)) {
         this.close();
         return true;
      }

      if (in(mx, my, this.x + 10.0F, this.y + 25.0F, 70.0F, 13.0F)) {
         this.selectedTab = false;
         this.scrollTarget = 0.0F;
         this.refresh();
         return true;
      }

      if (in(mx, my, this.x + 84.0F, this.y + 25.0F, 80.0F, 13.0F)) {
         this.selectedTab = true;
         this.scrollTarget = 0.0F;
         this.refresh();
         return true;
      }

      if (in(mx, my, this.x + this.w - 168.0F, this.y + this.h - 19.0F, 54.0F, 14.0F)) {
         this.working.clear();
         this.refresh();
         return true;
      }

      if (in(mx, my, this.x + this.w - 110.0F, this.y + this.h - 19.0F, 48.0F, 14.0F)) {
         this.close();
         return true;
      }

      if (in(mx, my, this.x + this.w - 58.0F, this.y + this.h - 19.0F, 48.0F, 14.0F)) {
         this.setting.set(this.working);
         this.close();
         return true;
      }

      float gx = this.gridX();
      float gy = this.gridY();
      if (in(mx, my, gx, gy, this.gridW(), this.gridH())) {
         int cols = this.cols();
         int c = (int)((mx - gx) / 23.0F);
         int r = (int)((my - gy - this.scroll) / 23.0F);
         if (c < 0 || c >= cols || r < 0) {
            return true;
         }

         int i = r * cols + c;
         if (i >= this.shown.size()) {
            return true;
         }

         class_2248 b = this.shown.get(i);
         if (button == 1 && this.working.containsKey(b)) {
            ClickGui.INSTANCE
               .openPopup(new ColorPickerPopup(b.method_9518().getString(), 0xFF000000 | this.working.get(b), col -> this.working.put(b, col & 16777215)));
         } else if (button == 0) {
            if (this.working.containsKey(b)) {
               this.working.remove(b);
            } else {
               this.working.put(b, this.setting.startColor(b));
            }

            if (this.selectedTab) {
               this.refresh();
            }
         }
      }

      return true;
   }

   @Override
   void scroll(double amount) {
      this.scrollTarget += (float)amount * 24.0F;
   }

   @Override
   boolean keyPressed(int key) {
      if (key == 256) {
         this.close();
         return true;
      }

      if (key == 259 && !this.query.isEmpty()) {
         this.query.deleteCharAt(this.query.length() - 1);
         this.scrollTarget = 0.0F;
         this.refresh();
      }

      return true;
   }

   @Override
   boolean charTyped(String chars) {
      for (char c : chars.toCharArray()) {
         if (c >= ' ' && this.query.length() < 30) {
            this.query.append(c);
         }
      }

      this.scrollTarget = 0.0F;
      this.caret = 0.0F;
      this.refresh();
      return true;
   }
}
