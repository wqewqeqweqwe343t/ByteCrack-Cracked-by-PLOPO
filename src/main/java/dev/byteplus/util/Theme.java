package dev.byteplus.util;

import dev.byteplus.BytePlus;
import dev.byteplus.module.modules.byteplus.BytePlusModule;

public final class Theme {
   public static int PANEL_BG = -233302251;
   public static int PANEL_HEADER = -98558180;
   public static int ROW_HOVER = 285209814;
   public static int ROW_ENABLED = 385861928;
   public static int HEADER_TEXT = -11713;
   public static int TEXT = -1777190;
   public static int TEXT_DIM = -7567232;
   public static int ACCENT = -14040;
   public static int ACCENT_DARK = -15067376;
   public static int ACCENT_SOFT = -12962780;
   public static int TOGGLE_OFF = -13356498;
   public static int SETTINGS_BG = -15593201;
   public static int OUTLINE = 486535368;
   public static int DANGER = -10869210;
   public static int HUD_BG = -1005119980;
   public static int HUD_OUTLINE = 822069544;
   public static int HUD_ACCENT = -14040;
   public static int HUD_ACCENT_2 = -24804;
   public static int HUD_TEXT = -1185053;

   private Theme() {
   }

   public static void update() {
      if (BytePlus.modules() != null) {
         BytePlusModule m = BytePlus.modules().clickGui;
         m.syncTheme();
         int accent = m.accentColor();
         int accent2 = m.accent2Color();
         int panel = m.panelColor.argb();
         int text = m.textColor.argb() | 0xFF000000;
         int panelOpaque = panel | 0xFF000000;
         ACCENT = accent | 0xFF000000;
         HUD_ACCENT = ACCENT;
         HUD_ACCENT_2 = accent2 | 0xFF000000;
         HEADER_TEXT = mix(ACCENT, -1, 0.12F);
         ROW_ENABLED = ACCENT & 16777215 | 369098752;
         HUD_OUTLINE = ACCENT & 16777215 | 805306368;
         OUTLINE = mix(ACCENT, -1, 0.7F) & 16777215 | 469762048;
         ACCENT_DARK = luminance(ACCENT) > 0.55F ? mix(panelOpaque, -16777216, 0.3F) : -1;
         ACCENT_SOFT = mix(panelOpaque, ACCENT, 0.2F);
         int alpha = panel >>> 24;
         PANEL_BG = panel;
         PANEL_HEADER = mix(panelOpaque, -1, 0.035F) & 16777215 | Math.min(255, alpha + 8) << 24;
         SETTINGS_BG = mix(panelOpaque, -16777216, 0.25F);
         TOGGLE_OFF = mix(panelOpaque, -1, 0.12F);
         HUD_BG = panel & 16777215 | Math.min(alpha, 196) << 24;
         TEXT = text;
         HUD_TEXT = mix(text, -1, 0.25F);
         TEXT_DIM = mix(text, panelOpaque, 0.45F);
         ROW_HOVER = mix(text, -1, 0.5F) & 16777215 | 268435456;
      }
   }

   public static int mix(int a, int b, float t) {
      int aa = a >>> 24;
      int ar = a >> 16 & 0xFF;
      int ag = a >> 8 & 0xFF;
      int ab = a & 0xFF;
      int ba = b >>> 24;
      int br = b >> 16 & 0xFF;
      int bg = b >> 8 & 0xFF;
      int bb = b & 0xFF;
      return Math.round(aa + (ba - aa) * t) << 24 | Math.round(ar + (br - ar) * t) << 16 | Math.round(ag + (bg - ag) * t) << 8 | Math.round(ab + (bb - ab) * t);
   }

   private static float luminance(int c) {
      return (0.299F * (c >> 16 & 0xFF) + 0.587F * (c >> 8 & 0xFF) + 0.114F * (c & 0xFF)) / 255.0F;
   }
}
