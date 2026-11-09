package dev.byteplus.module.modules.misc;

import dev.byteplus.BytePlus;
import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.StringSetting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_5481;

public class NameProtectModule extends Module {
   public final StringSetting name = this.add(new StringSetting("Name", "Byte+User", 16));

   public NameProtectModule() {
      super("Name Protect", "Replaces your username everywhere on screen.", Category.MISC);
   }

   private static NameProtectModule active() {
      if (BytePlus.modules() == null) {
         return null;
      }

      NameProtectModule m = BytePlus.modules().nameProtect;
      return m != null && m.isEnabled() ? m : null;
   }

   private static String realName() {
      return class_310.method_1551().method_1548().method_1676();
   }

   public static String replace(String s) {
      NameProtectModule m = active();
      if (m != null && s != null) {
         String real = realName();
         return !real.isEmpty() && s.contains(real) ? s.replace(real, m.name.get()) : s;
      } else {
         return s;
      }
   }

   public static class_5481 replace(class_5481 seq) {
      NameProtectModule m = active();
      if (m != null && seq != null) {
         String real = realName();
         if (real.isEmpty()) {
            return seq;
         }

         StringBuilder sb = new StringBuilder();
         List<class_2583> styles = new ArrayList<>();
         seq.accept((index, style, cp) -> {
            sb.appendCodePoint(cp);

            for (int ix = 0; ix < Character.charCount(cp); ix++) {
               styles.add(style);
            }

            return true;
         });
         String text = sb.toString();
         int at = text.indexOf(real);
         if (at < 0) {
            return seq;
         }

         String fake = m.name.get();
         StringBuilder out = new StringBuilder();
         List<class_2583> outStyles = new ArrayList<>();
         int i = 0;

         while (i < text.length()) {
            if (text.startsWith(real, i)) {
               class_2583 st = styles.get(i);

               for (int k = 0; k < fake.length(); k++) {
                  out.append(fake.charAt(k));
                  outStyles.add(st);
               }

               i += real.length();
            } else {
               out.append(text.charAt(i));
               outStyles.add(styles.get(i));
               i++;
            }
         }

         String result = out.toString();
         return sink -> {
            int kx = 0;

            while (kx < result.length()) {
               int cp = result.codePointAt(kx);
               if (!sink.accept(kx, outStyles.get(kx), cp)) {
                  return false;
               }

               kx += Character.charCount(cp);
            }

            return true;
         };
      } else {
         return seq;
      }
   }
}
