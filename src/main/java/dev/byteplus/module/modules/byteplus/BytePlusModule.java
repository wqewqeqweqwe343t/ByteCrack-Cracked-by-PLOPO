package dev.byteplus.module.modules.byteplus;

import dev.byteplus.gui.ClickGuiScreen;
import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.BooleanSetting;
import dev.byteplus.setting.ColorSetting;
import dev.byteplus.setting.ModeSetting;
import dev.byteplus.setting.NumberSetting;
import dev.byteplus.setting.StringSetting;
import dev.byteplus.util.UiSounds;
import java.awt.Color;
import net.minecraft.class_1109;
import net.minecraft.class_3414;
import net.minecraft.class_3417;

public class BytePlusModule extends Module {
   public final ModeSetting guiStyle = this.add(new ModeSetting("GUI Style", "Dropdown", "Dropdown", "Window"));
   public final ModeSetting theme = this.add(
      new ModeSetting("Theme", "Purple", "Custom", "Purple", "Midnight", "Sakura", "Ocean", "Mono", "Blood", "Mint", "Sunset")
   );
   public final ColorSetting accent = this.add(new ColorSetting("Accent", 0xFFA855F7));
   public final ColorSetting accent2 = this.add(new ColorSetting("Accent 2", 0xFF7C3AED));
   public final BooleanSetting rainbow = this.add(new BooleanSetting("Rainbow Accent", false));
   public final NumberSetting rainbowSpeed = this.add(new NumberSetting("Rainbow Speed", 1.0, 0.1, 5.0, 0.1).visibleWhen(this::rainbowOn));
   public final ColorSetting panelColor = this.add(new ColorSetting("Panel Color", 0xE8140B2D));
   public final ColorSetting textColor = this.add(new ColorSetting("Text Color", 0xFFF3E8FF));
   public final NumberSetting scale = this.add(new NumberSetting("GUI Scale", 1.6, 0.6, 1.6, 0.05));
   public final ModeSetting background = this.add(new ModeSetting("Background", "Dim", "Dim", "Gradient", "Vignette", "Particles", "None"));
   public final NumberSetting backgroundOpacity = this.add(new NumberSetting("Background Opacity", 0.48, 0.0, 1.0, 0.02));
   public final BooleanSetting blur = this.add(new BooleanSetting("Blur", true));
   public final NumberSetting blurStrength = this.add(new NumberSetting("Blur Strength", 4.0, 1.0, 8.0, 1.0).visibleWhen(this::blurOn));
   public final ModeSetting openAnimation = this.add(new ModeSetting("Open Animation", "Slide", "Slide", "Scale", "Fade", "None"));
   public final NumberSetting animationSpeed = this.add(new NumberSetting("Animation Speed", 1.0, 0.3, 3.0, 0.1));
   public final BooleanSetting panelGlow = this.add(new BooleanSetting("Panel Glow", true));
   public final BooleanSetting tooltips = this.add(new BooleanSetting("Tooltips", true));
   public final ModeSetting mainMenu = this.add(new ModeSetting("Main Menu", "Byte+", "Byte+", "Classic", "Vanilla"));
   public final StringSetting lastServer = this.add(new StringSetting("lastServer", "", 128).visibleWhen(() -> false));
   public final BooleanSetting clickSounds = this.add(new BooleanSetting("Click Sounds", true));
   public final BooleanSetting openSound = this.add(new BooleanSetting("Open Sound", true));
   public final ModeSetting sound = this.add(new ModeSetting("Sound", "Byte+", "Byte+", "Click", "Pop", "Pling", "Bell", "Hat", "Chime"));
   public final NumberSetting volume = this.add(new NumberSetting("Volume", 0.5, 0.05, 1.0, 0.05));
   private String lastTheme;

   public BytePlusModule() {
      super("Cracked By PLOPO", "Opens the menu. Customise the whole ClickGUI here: colours, background, effects and sounds.", Category.BYTEPLUS, 344);
      UiSounds.load();
   }

   private boolean rainbowOn() {
      return this.rainbow.isOn();
   }

   private boolean blurOn() {
      return this.blur.isOn();
   }

   private static int[] preset(String name) {
      return switch (name) {
         case "Purple" -> new int[]{0xFFA855F7, 0xFF7C3AED, 0xE8140B2D, 0xFFF3E8FF};
         case "Midnight" -> new int[]{-8614657, -4883201, -233893344, -2235659};
         case "Sakura" -> new int[]{-28744, -19503, -232909543, -661780};
         case "Ocean" -> new int[]{-13642776, -12941569, -234022368, -2231819};
         case "Mono" -> new int[]{-855310, -5723992, -233565164, -1513240};
         case "Blood" -> new int[]{-50373, -5041641, -233304819, -860450};
         case "Mint" -> new int[]{-11800410, -14695742, -233957099, -2034196};
         case "Sunset" -> new int[]{-34235, -49793, -233041384, -662306};
         default -> null;
      };
   }

   public void syncTheme() {
      String t = this.theme.get();
       if (this.lastTheme == null || !t.equals(this.lastTheme)) {
         this.lastTheme = t;
         int[] p = preset(t);
         if (p != null) {
            this.accent.set(p[0]);
            this.accent2.set(p[1]);
            this.panelColor.set(p[2]);
            this.textColor.set(p[3]);
            this.rainbow.set(false);
         }
      }
   }

   public boolean windowStyle() {
      return this.guiStyle.is("Window");
   }

   @Override
   public boolean isToggleable() {
      return false;
   }

   @Override
   public boolean showInArrayList() {
      return false;
   }

   @Override
   public void toggle() {
      if (mc.field_1755 == null) {
         mc.method_1507(ClickGuiScreen.get());
      }
   }

   public int accentColor() {
      if (!this.rainbow.isOn()) {
         return this.accent.argb();
      }

      float hue = (float)(System.nanoTime() / 1.0E9 * this.rainbowSpeed.get() * 0.12 % 1.0);
      return 0xFF000000 | Color.HSBtoRGB(hue, 0.62F, 1.0F);
   }

   public int accent2Color() {
      if (!this.rainbow.isOn()) {
         return this.accent2.argb();
      }

      float hue = (float)((System.nanoTime() / 1.0E9 * this.rainbowSpeed.get() * 0.12 + 0.08) % 1.0);
      return 0xFF000000 | Color.HSBtoRGB(hue, 0.7F, 1.0F);
   }

   public void playClick(float pitchShift) {
      if (this.clickSounds.isOn()) {
         if (!this.sound.is("Byte+") || !UiSounds.play(pitchShift < 1.0F ? UiSounds.Kind.CLICK_OFF : UiSounds.Kind.CLICK_ON, this.volume.getFloat())) {
            this.play(pitchShift);
         }
      }
   }

   public void playOpen() {
      if (this.openSound.isOn()) {
         if (!this.sound.is("Byte+") || !UiSounds.play(UiSounds.Kind.OPEN, this.volume.getFloat() * 0.8F)) {
            this.play(0.85F);
         }
      }
   }

   private void play(float pitchShift) {
      if (mc.method_1483() != null) {
         float v = this.volume.getFloat();

         class_3414 e = switch ((String)this.sound.get()) {
            case "Pop" -> class_3417.field_15197;
            case "Pling" -> (class_3414)class_3417.field_14622.comp_349();
            case "Bell" -> (class_3414)class_3417.field_14793.comp_349();
            case "Hat" -> (class_3414)class_3417.field_15204.comp_349();
            case "Chime" -> (class_3414)class_3417.field_14725.comp_349();
            default -> (class_3414)class_3417.field_15015.comp_349();
         };

         float pitch = switch ((String)this.sound.get()) {
            case "Pop" -> 1.4F;
            case "Pling" -> 1.6F;
            case "Bell" -> 1.3F;
            case "Hat" -> 1.2F;
            case "Chime" -> 1.5F;
            default -> 1.0F;
         };
         class_1109 s = class_1109.method_4757(e, pitch * pitchShift, v);
         mc.method_1483().method_4873(s);
      }
   }
}
