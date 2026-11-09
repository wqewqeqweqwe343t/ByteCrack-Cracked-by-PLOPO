package dev.byteplus.gui;

import com.google.gson.JsonObject;
import dev.byteplus.BytePlus;
import dev.byteplus.hud.Hud;
import dev.byteplus.module.Category;
import dev.byteplus.module.modules.byteplus.BytePlusModule;
import dev.byteplus.render.Render2D;
import dev.byteplus.util.Anim;
import dev.byteplus.util.Theme;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class ClickGui {
   public static final ClickGui INSTANCE = new ClickGui();
   private final List<Panel> panels = new ArrayList<>();
   private final List<Popup> popups = new ArrayList<>();
   private SearchPanel search;
   private JsonObject pendingLayout;
   private boolean laidOut;
   private float openAnim;
   private float scale = 1.0F;
   private float scrollTarget;
   private float scroll;
   private float tooltipTimer;
   private ModuleButton tooltipButton;
   private float lastMx;
   private float lastMy;
   private static final float GAP = 4.0F;
   private static final float ROW_GAP = 10.0F;
   private static final float TOP = 48.0F;
   private float layoutW = 1.0F;
   private float layoutH = 1.0F;
   private static final int PARTICLES = 70;
   private final float[] px = new float[70];
   private final float[] py = new float[70];
   private final float[] pv = new float[70];
   private final float[] ps = new float[70];
   private boolean particlesReady;
   private final WindowGui window = new WindowGui();
   private static final int LAYOUT_VERSION = 3;

   private ClickGui() {
   }

   private void ensurePanels(float screenW, float screenH) {
      if (this.panels.isEmpty()) {
         for (Category c : Category.values()) {
            this.panels.add(new CategoryPanel(c, 0.0F, 48.0F));
         }

         this.search = new SearchPanel(0.0F, 48.0F);
         this.panels.add(this.search);
         float user = BytePlus.modules().clickGui.scale.getFloat();
         int n = this.panels.size();
         int bestCols = n;
         float[] d = this.layoutSize(bestCols);
         this.layoutW = d[0];
         this.layoutH = d[1];
         float s = Math.min(user, Math.min(screenW / this.layoutW, screenH / this.layoutH));
         float areaW = screenW / s;
         float y = 48.0F;

         for (int start = 0; start < n; start += bestCols) {
            int end = Math.min(n, start + bestCols);
            float rowW = (end - start) * 104.0F + (end - start - 1) * 4.0F;
            float x = Math.max(3.0F, (areaW - rowW) / 2.0F);
            float rowH = 0.0F;

            for (int i = start; i < end; i++) {
               Panel p = this.panels.get(i);
               p.x = x;
               p.y = y;
               x += 108.0F;
               rowH = Math.max(rowH, panelHeight(p));
            }

            y += rowH + 10.0F;
         }

         this.laidOut = true;
         if (this.pendingLayout != null) {
            this.applyLayout(this.pendingLayout);
         }
      }
   }

   private static float panelHeight(Panel p) {
      return 18.0F + p.measureExtra() + p.bodyHeight() + 3.0F;
   }

   private float[] layoutSize(int cols) {
      int n = this.panels.size();
      float w = cols * 104.0F + (cols - 1) * 4.0F + 6.0F;
      float h = 48.0F;

      for (int start = 0; start < n; start += cols) {
         float rowH = 0.0F;

         for (int i = start; i < Math.min(n, start + cols); i++) {
            rowH = Math.max(rowH, panelHeight(this.panels.get(i)));
         }

         h += rowH + 10.0F;
      }

      return new float[]{w, h + 20.0F};
   }

   void onOpen() {
      this.openAnim = 0.0F;
      this.window.onOpen();
      BytePlus.modules().clickGui.playOpen();
   }

   private void drawBackground(float w, float h, float open, float dt) {
      BytePlusModule m = BytePlus.modules().clickGui;
      float a = m.backgroundOpacity.getFloat() * open;
      if (!(a <= 0.001F)) {
         int dark = -16448509;
         switch ((String)m.background.get()) {
            case "Dim":
               Render2D.rect(0.0F, 0.0F, w, h, Render2D.withAlpha(dark, a));
               break;
            case "Gradient":
               Render2D.verticalGradient(
                  0.0F, 0.0F, w, h, 0.0F, Render2D.withAlpha(Theme.mix(dark, Theme.ACCENT, 0.28F), a), Render2D.withAlpha(dark, Math.min(1.0F, a * 1.2F))
               );
               break;
            case "Vignette":
               Render2D.rect(0.0F, 0.0F, w, h, Render2D.withAlpha(dark, a * 0.35F));
               float edge = Math.min(w, h) * 0.35F;
               Render2D.verticalGradient(0.0F, 0.0F, w, edge, 0.0F, Render2D.withAlpha(dark, a), 0);
               Render2D.verticalGradient(0.0F, h - edge, w, edge, 0.0F, 0, Render2D.withAlpha(dark, a));
               Render2D.horizontalGradient(0.0F, 0.0F, edge, h, 0.0F, Render2D.withAlpha(dark, a), 0);
               Render2D.horizontalGradient(w - edge, 0.0F, edge, h, 0.0F, 0, Render2D.withAlpha(dark, a));
               break;
            case "Particles":
               Render2D.rect(0.0F, 0.0F, w, h, Render2D.withAlpha(dark, a * 0.8F));
               if (!this.particlesReady) {
                  Random r = new Random();

                  for (int i = 0; i < 70; i++) {
                     this.px[i] = r.nextFloat() * w;
                     this.py[i] = r.nextFloat() * h;
                     this.pv[i] = 6.0F + r.nextFloat() * 18.0F;
                     this.ps[i] = 0.6F + r.nextFloat() * 1.6F;
                  }

                  this.particlesReady = true;
               }

               for (int i = 0; i < 70; i++) {
                  this.py[i] = this.py[i] - this.pv[i] * dt;
                  this.px[i] = this.px[i] + (float)Math.sin((System.nanoTime() / 1.0E9 + i) * 0.7) * 4.0F * dt;
                  if (this.py[i] < -4.0F) {
                     this.py[i] = h + 4.0F;
                     this.px[i] = (float)(Math.random() * w);
                  }

                  float tw = 0.45F + 0.55F * (float)Math.abs(Math.sin(System.nanoTime() / 1.0E9 * 1.3 + i));
                  Render2D.circle(this.px[i], this.py[i], this.ps[i], Render2D.withAlpha(Theme.ACCENT, open * tw * 0.8F));
               }
         }
      }
   }

   void openPopup(Popup p) {
      this.popups.add(p);
   }

   private Popup topPopup() {
      return this.popups.isEmpty() ? null : this.popups.get(this.popups.size() - 1);
   }

   private float effectiveScale(float screenW, float screenH) {
      float user = BytePlus.modules().clickGui.scale.getFloat();
      return Math.min(user, Math.min(screenW / this.layoutW, screenH / this.layoutH));
   }

   public void draw(float screenW, float screenH, float mx, float my, float dt) {
      this.ensurePanels(screenW, screenH);
      this.lastMx = mx;
      this.lastMy = my;
      Hud.mouseMoved(mx, my);
      BytePlusModule settings = BytePlus.modules().clickGui;
      String anim = settings.openAnimation.get();
      if (anim.equals("None")) {
         this.openAnim = 1.0F;
      } else {
         this.openAnim = Anim.approach(this.openAnim, 1.0F, 10.0F * settings.animationSpeed.getFloat(), dt);
      }

      float open = Anim.easeOutCubic(this.openAnim);
      this.scroll = Anim.approach(this.scroll, this.scrollTarget, 14.0F, dt);
      this.drawBackground(screenW, screenH, open, dt);
      this.scale = this.effectiveScale(screenW, screenH);
      boolean blocked = !this.popups.isEmpty();
      float lmx = blocked ? -9999.0F : mx / this.scale;
      float lmy = blocked ? -9999.0F : my / this.scale - this.scroll;
      Render2D.save();
      Render2D.globalAlpha(open);
      if (anim.equals("Scale")) {
         float s = 0.88F + 0.12F * open;
         Render2D.translate(screenW / 2.0F, screenH / 2.0F);
         Render2D.scale(s);
         Render2D.translate(-screenW / 2.0F, -screenH / 2.0F);
      }

      boolean windowStyle = settings.windowStyle();
      if (windowStyle) {
         Render2D.translate(0.0F, anim.equals("Slide") ? (1.0F - open) * -14.0F : 0.0F);
         this.window.draw(screenW, screenH, blocked ? -9999.0F : mx, blocked ? -9999.0F : my, dt);
      } else {
         Render2D.scale(this.scale);
         Render2D.translate(0.0F, this.scroll + (anim.equals("Slide") ? (1.0F - open) * -10.0F : 0.0F));

         for (Panel p : this.panels) {
            p.draw(dt, lmx, lmy);
         }
      }

      Render2D.restore();
      if (!windowStyle && !blocked && settings.tooltips.isOn()) {
         this.drawTooltip(screenW, screenH, lmx, lmy, dt, open);
      }

      String hint = "DRAG HUD ELEMENTS TO MOVE THEM";
      float hw = Render2D.textWidth(hint, 6.5F, 1, 0.5F);
      Render2D.text(hint, (screenW - hw) / 2.0F, screenH - 14.0F, 6.5F, Render2D.withAlpha(Theme.TEXT_DIM, open * 0.5F), 1, 0.5F);

      for (int i = 0; i < this.popups.size(); i++) {
         Popup p = this.popups.get(i);
         boolean top = i == this.popups.size() - 1;
         p.draw(screenW, screenH, top ? mx : -9999.0F, top ? my : -9999.0F, dt);
      }

      this.popups.removeIf(Popup::finished);
   }

   private void drawTooltip(float screenW, float screenH, float lmx, float lmy, float dt, float open) {
      ModuleButton hovered = null;

      for (int i = this.panels.size() - 1; i >= 0; i--) {
         hovered = this.panels.get(i).hoveredButton(lmx, lmy);
         if (hovered != null) {
            break;
         }
      }

      if (hovered != this.tooltipButton) {
         this.tooltipButton = hovered;
         this.tooltipTimer = 0.0F;
      }

      if (hovered != null) {
         this.tooltipTimer += dt;
         float a = Math.min(1.0F, Math.max(0.0F, (this.tooltipTimer - 0.45F) * 6.0F)) * open;
         if (!(a <= 0.0F)) {
            String text = hovered.module.getDescription();
            if (text != null && !text.isEmpty()) {
               float ts = 7.5F;
               float tw = Render2D.textWidth(text, ts, 0) + 14.0F;
               float th = 16.0F;
               float tx = (screenW - tw) / 2.0F;
               float ty = screenH - th - 74.0F;
               Render2D.save();
               Render2D.globalAlpha(a);
               Render2D.shadow(tx, ty, tw, th, 4.0F, 8.0F, 1426063360);
               Render2D.roundRect(tx, ty, tw, th, 4.0F, Theme.PANEL_BG);
               Render2D.roundRectOutline(tx, ty, tw, th, 4.0F, 0.6F, Render2D.withAlpha(Theme.ACCENT, 0.5F));
               Render2D.text(text, tx + 7.0F, ty + (th - ts) / 2.0F, ts, Theme.TEXT, 0);
               Render2D.restore();
            }
         }
      }
   }

   private float lx(double mx) {
      return (float)mx / this.scale;
   }

   private float ly(double my) {
      return (float)my / this.scale - this.scroll;
   }

   WindowGui window() {
      return this.window;
   }

   public float openProgress() {
      return Anim.easeOutCubic(this.openAnim);
   }

   private boolean windowStyle() {
      return BytePlus.modules().clickGui.windowStyle();
   }

   public boolean mouseClicked(double mx, double my, int button) {
      Popup top = this.topPopup();
      if (top != null) {
         return top.mouseClicked((float)mx, (float)my, button);
      }

      if (this.windowStyle()) {
         return this.window.mouseClicked(mx, my, button) ? true : button == 0 && Hud.mouseClicked(mx, my);
      }

      float x = this.lx(mx);
      float y = this.ly(my);
      if (this.search != null && !this.search.contains(x, y)) {
         this.search.unfocus();
      }

      for (int i = this.panels.size() - 1; i >= 0; i--) {
         Panel p = this.panels.get(i);
         if (p.contains(x, y) || p.isOverHeader(x, y)) {
            for (Panel other : this.panels) {
               if (other != p) {
                  other.stopEditing();
               }
            }

            if (!p.isOverHeader(x, y)) {
               BytePlus.modules().clickGui.playClick(button == 1 ? 0.85F : 1.0F);
            }

            if (p.mouseClicked(x, y, button)) {
               this.panels.remove(i);
               this.panels.add(p);
            }

            return true;
         }
      }

      for (Panel p : this.panels) {
         p.stopEditing();
      }

      return button == 0 && Hud.mouseClicked(mx, my);
   }

   public void mouseReleased() {
      Popup top = this.topPopup();
      if (top != null) {
         top.mouseReleased();
      }

      for (Panel p : this.panels) {
         p.mouseReleased();
      }

      this.window.mouseReleased();
      Hud.mouseReleased();
   }

   public void mouseScrolled(double amount) {
      Popup top = this.topPopup();
      if (top != null) {
         top.scroll(amount);
      } else if (this.windowStyle()) {
         this.window.mouseScrolled(this.lastMx, this.lastMy, amount);
      } else {
         this.scrollTarget += (float)amount * 18.0F;
         this.scrollTarget = Math.max(-1600.0F, Math.min(200.0F, this.scrollTarget));
      }
   }

   public boolean keyPressed(int key) {
      Popup top = this.topPopup();
      if (top != null) {
         if (!top.keyPressed(key)) {
         }

         return true;
      } else {
         if (this.windowStyle()) {
            return this.window.keyPressed(key);
         }

         for (Panel p : this.panels) {
            if (p.isListening()) {
               return p.keyPressed(key);
            }
         }

         return this.search != null && this.search.keyPressed(key);
      }
   }

   public boolean isCapturingKeys() {
      if (!this.popups.isEmpty()) {
         return true;
      }

      if (this.windowStyle()) {
         return this.window.isCapturingKeys();
      }

      for (Panel p : this.panels) {
         if (p.isListening()) {
            return true;
         }
      }

      return false;
   }

   public boolean charTyped(String chars) {
      Popup top = this.topPopup();
      if (top != null) {
         return top.charTyped(chars);
      }

      if (this.windowStyle()) {
         return this.window.charTyped(chars);
      }

      for (Panel p : this.panels) {
         if (p.charTyped(chars)) {
            return true;
         }
      }

      for (Panel p : this.panels) {
         if (p.isListening()) {
            return true;
         }
      }

      return this.search != null && this.search.charTyped(chars);
   }

   public void onClose() {
      this.popups.clear();

      for (Panel p : this.panels) {
         p.stopEditing();
         p.mouseReleased();
      }

      this.window.onClose();
      Hud.mouseReleased();
   }

   public JsonObject saveLayout() {
      if (!this.laidOut) {
         return this.pendingLayout != null ? this.pendingLayout : new JsonObject();
      }

      JsonObject o = new JsonObject();
      o.addProperty("version", 3);

      for (Panel p : this.panels) {
         JsonObject po = new JsonObject();
         po.addProperty("x", p.x);
         po.addProperty("y", p.y);
         po.addProperty("collapsed", p.collapsed);
         o.add(p.id, po);
      }

      return o;
   }

   public void loadLayout(JsonObject o) {
      if (!this.laidOut) {
         this.pendingLayout = o;
      } else {
         this.applyLayout(o);
      }
   }

   private void applyLayout(JsonObject o) {
      if (o.has("version") && o.get("version").getAsInt() >= 3) {
         for (Panel p : this.panels) {
            if (!o.has(p.id)) {
               return;
            }
         }

         for (Panel p : this.panels) {
            if (o.has(p.id)) {
               JsonObject po = o.getAsJsonObject(p.id);
               p.x = po.get("x").getAsFloat();
               p.y = po.get("y").getAsFloat();
               p.collapsed = po.get("collapsed").getAsBoolean();
            }
         }
      }
   }
}
