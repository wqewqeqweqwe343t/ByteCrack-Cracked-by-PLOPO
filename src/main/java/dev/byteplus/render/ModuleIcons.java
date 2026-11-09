package dev.byteplus.render;

import dev.byteplus.module.Module;
import org.lwjgl.nanovg.NanoVG;

public final class ModuleIcons {
   private ModuleIcons() {
   }

   public static void draw(Module m, float x, float y, float s, int c) {
      float w = s * 0.12F;
      float cx = x + s / 2.0F;
      float cy = y + s / 2.0F;
      switch (m.getName()) {
         case "Auto Totem":
         case "Auto Inv Totem":
            totem(x, y, s, c, w);
            break;
         case "Anchor Macro":
            anchor(cx, cy, s, c, w);
            break;
         case "Auto Crystal":
            crystal(cx, cy, s, c, w);
            break;
         case "Triggerbot":
         case "Aim Assist":
         case "Custom Crosshair":
            target(cx, cy, s, c, w);
            break;
         case "Skin Protect":
         case "Name Protect":
            shield(cx, cy, s, c, w);
            break;
         case "Auto Reconnect":
         case "Relog Method":
            Icons.refresh(x, y, s, c);
            break;
         case "Auto Mine":
         case "Auto Tool":
         case "Tunnel Base Finder":
            pickaxe(x, y, s, c, w);
            break;
         case "Auto Walk":
            arrow(cx, cy, s, c, w);
            break;
         case "Weather Notifier":
         case "Weather Changer":
            cloud(cx, cy, s, c, w);
            break;
         case "Freecam":
         case "FreeLook":
         case "Zoom":
            camera(cx, cy, s, c, w);
            break;
         case "Fast Place":
         case "Block ESP":
         case "Block Outline":
            cube(cx, cy, s, c, w);
            break;
         case "Discord Presence":
            bubble(cx, cy, s, c, w);
            break;
         case "RTP Base Finder":
            globe(cx, cy, s, c, w);
            break;
         case "Block Entity Debug":
            bug(cx, cy, s, c, w);
            break;
         case "Player Chunks":
         case "Player ESP":
         case "Friends":
            Icons.person(x, y, s, c);
            break;
         case "Prime Chunk Finder":
         case "Sus Chunk Finder":
         case "Chunk Finder":
            Icons.radar(x, y, s, c);
            break;
         case "Activity Inspector":
            pulse(x, y, s, c, w);
            break;
         case "Light Debug":
            bulb(cx, cy, s, c, w);
            break;
         case "Storage ESP":
            chest(x, y, s, c, w);
            break;
         case "TargetHUD":
         case "Hit Effects":
            heart(cx, cy, s, c);
            break;
         case "JumpCircles":
            rings(cx, cy, s, c, w);
            break;
         case "HUD":
            layout(x, y, s, c, w);
            break;
         case "SwingSpeed":
            swing(cx, cy, s, c, w);
            break;
         case "NameTags":
            tag(x, y, s, c, w);
            break;
         case "CSGO Knives":
            knife(x, y, s, c, w);
            break;
         case "Custom Sky":
         case "Time Changer":
            moon(cx, cy, s, c);
            break;
         case "Fullbright":
            sun(cx, cy, s, c, w);
            break;
         case "Glow ESP":
            glow(cx, cy, s, c, w);
            break;
         case "Cosmetics":
         case "Trails":
            cape(x, y, s, c, w);
            break;
         case "Byte+":
            Icons.wand(x, y, s, c);
            break;
         case "Speed":
            Icons.speed(x, y, s, c);
            break;
         case "SpeedMine":
            pickaxe(x, y, s, c, w);
            break;
         case "ClickTP":
            target(cx, cy, s, c, w);
            break;
         case "Step":
         case "Spider":
            cube(cx, cy, s, c, w);
            break;
         case "Flight":
         case "ElytraFlight":
         case "AirJump":
         case "Boatfly":
            arrow(cx, cy, s, c, w);
            break;
         default:
            Icons.category(m.getCategory(), x, y, s, c);
      }
   }

   private static void totem(float x, float y, float s, int c, float w) {
      Render2D.roundRectOutline(x + s * 0.28F, y + s * 0.08F, s * 0.44F, s * 0.5F, s * 0.12F, w, c);
      Render2D.circle(x + s * 0.42F, y + s * 0.3F, s * 0.05F, c);
      Render2D.circle(x + s * 0.58F, y + s * 0.3F, s * 0.05F, c);
      Render2D.line(x + s * 0.12F, y + s * 0.62F, x + s * 0.88F, y + s * 0.62F, w, c);
      Render2D.line(x + s * 0.5F, y + s * 0.58F, x + s * 0.5F, y + s * 0.92F, w, c);
   }

   private static void anchor(float cx, float cy, float s, int c, float w) {
      Render2D.circleOutline(cx, cy - s * 0.3F, s * 0.1F, w, c);
      Render2D.line(cx, cy - s * 0.2F, cx, cy + s * 0.38F, w, c);
      Render2D.line(cx - s * 0.22F, cy - s * 0.06F, cx + s * 0.22F, cy - s * 0.06F, w, c);
      Render2D.arc(cx, cy + s * 0.06F, s * 0.34F, (float)Math.toRadians(20.0), (float)Math.toRadians(160.0), w, c);
   }

   private static void crystal(float cx, float cy, float s, int c, float w) {
      Render2D.beginPath();
      Render2D.moveTo(cx, cy - s * 0.42F);
      Render2D.lineTo(cx + s * 0.3F, cy);
      Render2D.lineTo(cx, cy + s * 0.42F);
      Render2D.lineTo(cx - s * 0.3F, cy);
      Render2D.closePath();
      Render2D.strokePath(w, c);
      Render2D.line(cx - s * 0.3F, cy, cx + s * 0.3F, cy, w * 0.8F, c);
   }

   private static void target(float cx, float cy, float s, int c, float w) {
      Render2D.circleOutline(cx, cy, s * 0.34F, w, c);
      Render2D.circle(cx, cy, s * 0.08F, c);
      Render2D.line(cx, cy - s * 0.48F, cx, cy - s * 0.2F, w, c);
      Render2D.line(cx, cy + s * 0.2F, cx, cy + s * 0.48F, w, c);
      Render2D.line(cx - s * 0.48F, cy, cx - s * 0.2F, cy, w, c);
      Render2D.line(cx + s * 0.2F, cy, cx + s * 0.48F, cy, w, c);
   }

   private static void shield(float cx, float cy, float s, int c, float w) {
      Render2D.beginPath();
      Render2D.moveTo(cx, cy - s * 0.42F);
      Render2D.lineTo(cx + s * 0.34F, cy - s * 0.28F);
      Render2D.lineTo(cx + s * 0.3F, cy + s * 0.1F);
      Render2D.lineTo(cx, cy + s * 0.44F);
      Render2D.lineTo(cx - s * 0.3F, cy + s * 0.1F);
      Render2D.lineTo(cx - s * 0.34F, cy - s * 0.28F);
      Render2D.closePath();
      Render2D.strokePath(w, c);
   }

   private static void pickaxe(float x, float y, float s, int c, float w) {
      Render2D.line(x + s * 0.2F, y + s * 0.85F, x + s * 0.68F, y + s * 0.32F, w * 1.2F, c);
      Render2D.arc(x + s * 0.5F, y + s * 0.55F, s * 0.42F, (float)Math.toRadians(-110.0), (float)Math.toRadians(-10.0), w * 1.3F, c);
   }

   private static void arrow(float cx, float cy, float s, int c, float w) {
      Render2D.line(cx - s * 0.36F, cy, cx + s * 0.36F, cy, w, c);
      Render2D.line(cx + s * 0.36F, cy, cx + s * 0.1F, cy - s * 0.24F, w, c);
      Render2D.line(cx + s * 0.36F, cy, cx + s * 0.1F, cy + s * 0.24F, w, c);
   }

   private static void cloud(float cx, float cy, float s, int c, float w) {
      Render2D.circle(cx - s * 0.16F, cy + s * 0.04F, s * 0.2F, c);
      Render2D.circle(cx + s * 0.08F, cy - s * 0.08F, s * 0.24F, c);
      Render2D.circle(cx + s * 0.26F, cy + s * 0.08F, s * 0.16F, c);
      Render2D.roundRect(cx - s * 0.36F, cy + s * 0.04F, s * 0.78F, s * 0.2F, s * 0.1F, c);
   }

   private static void camera(float cx, float cy, float s, int c, float w) {
      Render2D.roundRectOutline(cx - s * 0.42F, cy - s * 0.24F, s * 0.84F, s * 0.54F, s * 0.1F, w, c);
      Render2D.circleOutline(cx, cy + s * 0.03F, s * 0.15F, w, c);
      Render2D.roundRect(cx - s * 0.16F, cy - s * 0.36F, s * 0.32F, s * 0.12F, s * 0.04F, c);
   }

   private static void cube(float cx, float cy, float s, int c, float w) {
      float r = s * 0.38F;
      Render2D.beginPath();
      Render2D.moveTo(cx, cy - r);
      Render2D.lineTo(cx + r * 0.87F, cy - r * 0.5F);
      Render2D.lineTo(cx + r * 0.87F, cy + r * 0.5F);
      Render2D.lineTo(cx, cy + r);
      Render2D.lineTo(cx - r * 0.87F, cy + r * 0.5F);
      Render2D.lineTo(cx - r * 0.87F, cy - r * 0.5F);
      Render2D.closePath();
      Render2D.strokePath(w, c);
      Render2D.line(cx, cy, cx, cy + r, w * 0.8F, c);
      Render2D.line(cx, cy, cx + r * 0.87F, cy - r * 0.5F, w * 0.8F, c);
      Render2D.line(cx, cy, cx - r * 0.87F, cy - r * 0.5F, w * 0.8F, c);
   }

   private static void bubble(float cx, float cy, float s, int c, float w) {
      Render2D.roundRectOutline(cx - s * 0.4F, cy - s * 0.32F, s * 0.8F, s * 0.52F, s * 0.16F, w, c);
      Render2D.line(cx - s * 0.16F, cy + s * 0.2F, cx - s * 0.26F, cy + s * 0.4F, w, c);
      Render2D.circle(cx - s * 0.16F, cy - s * 0.06F, s * 0.05F, c);
      Render2D.circle(cx + s * 0.16F, cy - s * 0.06F, s * 0.05F, c);
   }

   private static void globe(float cx, float cy, float s, int c, float w) {
      Render2D.circleOutline(cx, cy, s * 0.4F, w, c);
      Render2D.line(cx - s * 0.4F, cy, cx + s * 0.4F, cy, w * 0.8F, c);
      Render2D.beginPath();
      Render2D.moveTo(cx, cy - s * 0.4F);
      NanoVG.nvgQuadTo(Render2D.vg(), cx - s * 0.32F, cy, cx, cy + s * 0.4F);
      NanoVG.nvgQuadTo(Render2D.vg(), cx + s * 0.32F, cy, cx, cy - s * 0.4F);
      Render2D.strokePath(w * 0.8F, c);
   }

   private static void bug(float cx, float cy, float s, int c, float w) {
      Render2D.roundRectOutline(cx - s * 0.18F, cy - s * 0.18F, s * 0.36F, s * 0.52F, s * 0.18F, w, c);
      Render2D.circle(cx, cy - s * 0.3F, s * 0.1F, c);

      for (int i = -1; i <= 1; i++) {
         Render2D.line(cx - s * 0.18F, cy + i * s * 0.14F + s * 0.08F, cx - s * 0.4F, cy + i * s * 0.18F + s * 0.08F, w * 0.8F, c);
         Render2D.line(cx + s * 0.18F, cy + i * s * 0.14F + s * 0.08F, cx + s * 0.4F, cy + i * s * 0.18F + s * 0.08F, w * 0.8F, c);
      }
   }

   private static void pulse(float x, float y, float s, int c, float w) {
      Render2D.beginPath();
      Render2D.moveTo(x + s * 0.05F, y + s * 0.55F);
      Render2D.lineTo(x + s * 0.3F, y + s * 0.55F);
      Render2D.lineTo(x + s * 0.42F, y + s * 0.2F);
      Render2D.lineTo(x + s * 0.58F, y + s * 0.85F);
      Render2D.lineTo(x + s * 0.7F, y + s * 0.45F);
      Render2D.lineTo(x + s * 0.95F, y + s * 0.45F);
      Render2D.strokePath(w, c);
   }

   private static void chest(float x, float y, float s, int c, float w) {
      Render2D.roundRectOutline(x + s * 0.1F, y + s * 0.22F, s * 0.8F, s * 0.62F, s * 0.08F, w, c);
      Render2D.line(x + s * 0.1F, y + s * 0.46F, x + s * 0.9F, y + s * 0.46F, w, c);
      Render2D.roundRect(x + s * 0.43F, y + s * 0.4F, s * 0.14F, s * 0.16F, s * 0.03F, c);
   }

   private static void heart(float cx, float cy, float s, int c) {
      Render2D.circle(cx - s * 0.17F, cy - s * 0.1F, s * 0.2F, c);
      Render2D.circle(cx + s * 0.17F, cy - s * 0.1F, s * 0.2F, c);
      Render2D.triangle(cx - s * 0.37F, cy - s * 0.04F, cx + s * 0.37F, cy - s * 0.04F, cx, cy + s * 0.4F, c);
   }

   private static void rings(float cx, float cy, float s, int c, float w) {
      Render2D.circleOutline(cx, cy + s * 0.1F, s * 0.4F, w * 0.8F, c);
      Render2D.circleOutline(cx, cy + s * 0.1F, s * 0.22F, w * 0.8F, c);
      Render2D.circle(cx, cy - s * 0.3F, s * 0.08F, c);
   }

   private static void layout(float x, float y, float s, int c, float w) {
      Render2D.roundRectOutline(x + s * 0.08F, y + s * 0.12F, s * 0.84F, s * 0.76F, s * 0.1F, w, c);
      Render2D.roundRect(x + s * 0.18F, y + s * 0.22F, s * 0.3F, s * 0.14F, s * 0.04F, c);
      Render2D.roundRect(x + s * 0.62F, y + s * 0.22F, s * 0.2F, s * 0.46F, s * 0.04F, c);
   }

   private static void swing(float cx, float cy, float s, int c, float w) {
      Render2D.arc(cx - s * 0.1F, cy + s * 0.1F, s * 0.36F, (float)Math.toRadians(-90.0), 0.0F, w, c);
      Render2D.arc(cx - s * 0.1F, cy + s * 0.1F, s * 0.2F, (float)Math.toRadians(-90.0), 0.0F, w * 0.8F, c);
      Render2D.line(cx + s * 0.26F, cy + s * 0.1F, cx + s * 0.38F, cy - s * 0.05F, w, c);
   }

   private static void tag(float x, float y, float s, int c, float w) {
      Render2D.beginPath();
      Render2D.moveTo(x + s * 0.12F, y + s * 0.2F);
      Render2D.lineTo(x + s * 0.58F, y + s * 0.2F);
      Render2D.lineTo(x + s * 0.88F, y + s * 0.5F);
      Render2D.lineTo(x + s * 0.58F, y + s * 0.8F);
      Render2D.lineTo(x + s * 0.12F, y + s * 0.8F);
      Render2D.closePath();
      Render2D.strokePath(w, c);
      Render2D.circle(x + s * 0.3F, y + s * 0.5F, s * 0.07F, c);
   }

   private static void knife(float x, float y, float s, int c, float w) {
      Render2D.beginPath();
      Render2D.moveTo(x + s * 0.9F, y + s * 0.1F);
      Render2D.lineTo(x + s * 0.42F, y + s * 0.5F);
      Render2D.lineTo(x + s * 0.52F, y + s * 0.6F);
      Render2D.closePath();
      Render2D.fillPath(c);
      Render2D.line(x + s * 0.36F, y + s * 0.48F, x + s * 0.56F, y + s * 0.68F, w, c);
      Render2D.line(x + s * 0.44F, y + s * 0.6F, x + s * 0.14F, y + s * 0.9F, w * 1.6F, c);
   }

   private static void moon(float cx, float cy, float s, int c) {
      Render2D.circle(cx, cy, s * 0.38F, c);
      Render2D.circle(cx + s * 0.18F, cy - s * 0.12F, s * 0.32F, -15330022);
      sparkle(cx + s * 0.3F, cy + s * 0.28F, s * 0.1F, c);
   }

   private static void bulb(float cx, float cy, float s, int c, float w) {
      Render2D.circleOutline(cx, cy - s * 0.1F, s * 0.26F, w, c);
      Render2D.line(cx - s * 0.12F, cy + s * 0.24F, cx + s * 0.12F, cy + s * 0.24F, w, c);
      Render2D.line(cx - s * 0.08F, cy + s * 0.36F, cx + s * 0.08F, cy + s * 0.36F, w, c);
      Render2D.circle(cx, cy - s * 0.1F, s * 0.1F, c);
   }

   private static void sun(float cx, float cy, float s, int c, float w) {
      Render2D.circle(cx, cy, s * 0.18F, c);

      for (int i = 0; i < 8; i++) {
         double a = (Math.PI * 2) * i / 8.0;
         Render2D.line(
            cx + (float)Math.cos(a) * s * 0.28F,
            cy + (float)Math.sin(a) * s * 0.28F,
            cx + (float)Math.cos(a) * s * 0.44F,
            cy + (float)Math.sin(a) * s * 0.44F,
            w,
            c
         );
      }
   }

   private static void glow(float cx, float cy, float s, int c, float w) {
      Render2D.circle(cx, cy, s * 0.42F, Render2D.withAlpha(c, 0.18F));
      Render2D.circle(cx, cy, s * 0.3F, Render2D.withAlpha(c, 0.35F));
      Icons.person(cx - s * 0.3F, cy - s * 0.3F, s * 0.6F, c);
   }

   private static void cape(float x, float y, float s, int c, float w) {
      Render2D.beginPath();
      Render2D.moveTo(x + s * 0.3F, y + s * 0.12F);
      Render2D.lineTo(x + s * 0.7F, y + s * 0.12F);
      Render2D.lineTo(x + s * 0.84F, y + s * 0.88F);
      Render2D.lineTo(x + s * 0.5F, y + s * 0.78F);
      Render2D.lineTo(x + s * 0.16F, y + s * 0.88F);
      Render2D.closePath();
      Render2D.strokePath(w, c);
   }

   private static void sparkle(float cx, float cy, float r, int c) {
      Icons.sparkle(cx, cy, r, c);
   }
}
