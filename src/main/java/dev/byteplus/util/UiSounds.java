package dev.byteplus.util;

import dev.byteplus.BytePlus;
import java.util.Random;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.FloatControl.Type;
import net.minecraft.class_310;
import net.minecraft.class_3419;

public final class UiSounds {
   private static final int RATE = 44100;
   private static final int POOL = 4;
   private static Clip[] clickHi;
   private static Clip[] clickLo;
   private static Clip[] open;
   private static Clip[] hover;
   private static Clip[] whoosh;
   private static int next;
   private static volatile boolean ready;
   private static volatile boolean failed;
   private static volatile boolean loading;

   private UiSounds() {
   }

   public static boolean play(UiSounds.Kind kind, float volume) {
      if (failed) {
         return false;
      }

      if (!ready) {
         load();
         return false;
      }

      Clip[] pool = switch (kind) {
         case CLICK_ON -> clickHi;
         case CLICK_OFF -> clickLo;
         case OPEN -> open;
         case HOVER -> hover;
         case WHOOSH -> whoosh;
      };
      Clip c = pool[next++ % 4];

      try {
         float master = class_310.method_1551().field_1690.method_1630(class_3419.field_15250);
         float v = Math.max(1.0E-4F, volume * master);
         if (c.isControlSupported(Type.MASTER_GAIN)) {
            FloatControl g = (FloatControl)c.getControl(Type.MASTER_GAIN);
            g.setValue(Math.max(g.getMinimum(), Math.min(g.getMaximum(), (float)(20.0 * Math.log10(v)))));
         }

         c.stop();
         c.setFramePosition(0);
         c.start();
         return true;
      } catch (Throwable t) {
         return false;
      }
   }

   public static void load() {
      if (!ready && !failed && !loading) {
         loading = true;
         Thread t = new Thread(() -> {
            try {
               clickHi = pool(tick(1.0F));
               clickLo = pool(tick(0.82F));
               open = pool(chime());
               hover = pool(softTick());
               whoosh = pool(whooshSound());
               ready = true;
            } catch (Throwable e) {
               failed = true;
               BytePlus.LOGGER.warn("Byte+ menu sounds unavailable, using vanilla sounds", e);
            }
         }, "Byte+ UI sounds");
         t.setDaemon(true);
         t.start();
      }
   }

   private static Clip[] pool(byte[] pcm) throws Exception {
      AudioFormat fmt = new AudioFormat(44100.0F, 16, 1, true, false);
      Clip[] clips = new Clip[4];

      for (int i = 0; i < 4; i++) {
         Clip c = AudioSystem.getClip();
         c.open(fmt, pcm, 0, pcm.length);
         clips[i] = c;
      }

      return clips;
   }

   private static byte[] tick(float pitch) {
      int n = 2425;
      float[] s = new float[n];
      Random r = new Random(7L);
      float prevIn = 0.0F;
      float hp = 0.0F;

      for (int i = 0; i < n; i++) {
         float t = i / 44100.0F;
         float noise = r.nextFloat() * 2.0F - 1.0F;
         hp = 0.86F * (hp + noise - prevIn);
         prevIn = noise;
         float snap = hp * (float)Math.exp(-t / 0.0018) * 0.55F;
         float pluck = (float)(Math.sin(14765.485471872027 * pitch * t) * Math.exp(-t / 0.0065)) * 0.42F;
         float overtone = (float)(Math.sin(29530.970943744054 * pitch * t) * Math.exp(-t / 0.0028)) * 0.12F;
         float body = (float)(Math.sin(1193.8052083641214 * pitch * t) * Math.exp(-t / 0.009)) * 0.28F;
         float attack = Math.min(1.0F, t / 4.0E-4F);
         s[i] = (snap + pluck + overtone + body) * attack;
      }

      return toPcm(s, 0.9F);
   }

   private static byte[] chime() {
      int n = 14112;
      float[] s = new float[n];

      for (int i = 0; i < n; i++) {
         float t = i / 44100.0F;
         float a = (float)(Math.sin(5529.203070318036 * t) + 0.35 * Math.sin(11058.406140636072 * t)) * (float)Math.exp(-t / 0.09);
         float t2 = Math.max(0.0F, t - 0.045F);
         float b = t < 0.045F ? 0.0F : (float)(Math.sin(8284.379827516284 * t2) + 0.3 * Math.sin(16568.75965503257 * t2)) * (float)Math.exp(-t2 / 0.12);
         float attack = Math.min(1.0F, t / 0.006F);
         s[i] = (a * 0.38F + b * 0.34F) * attack;
      }

      return toPcm(s, 0.75F);
   }

   private static byte[] softTick() {
      int n = 1323;
      float[] s = new float[n];

      for (int i = 0; i < n; i++) {
         float t = i / 44100.0F;
         s[i] = (float)(Math.sin(20106.192982974677 * t) * Math.exp(-t / 0.004)) * Math.min(1.0F, t / 5.0E-4F);
      }

      return toPcm(s, 0.5F);
   }

   private static byte[] whooshSound() {
      int n = 24255;
      float[] s = new float[n];
      Random r = new Random(11L);
      float lp = 0.0F;
      float bp = 0.0F;

      for (int i = 0; i < n; i++) {
         float t = i / 44100.0F;
         float k = t / 0.55F;
         float cutoff = 400.0F + 5200.0F * k * k;
         float a = (float)(1.0 - Math.exp((-Math.PI * 2) * cutoff / 44100.0));
         float x = r.nextFloat() * 2.0F - 1.0F;
         lp += a * (x - lp);
         bp += a * (lp - bp);
         float env = (float)(Math.sin(Math.PI * Math.min(1.0, k * 1.1)) * Math.exp(-k * 0.6));
         float tone = (float)Math.sin((Math.PI * 2) * (180.0F + 420.0F * k * k) * t) * 0.18F;
         s[i] = (lp - bp * 0.6F + tone) * env;
      }

      return toPcm(s, 0.8F);
   }

   private static byte[] toPcm(float[] s, float gain) {
      float peak = 1.0E-6F;

      for (float v : s) {
         peak = Math.max(peak, Math.abs(v));
      }

      byte[] out = new byte[s.length * 2];

      for (int i = 0; i < s.length; i++) {
         int v = Math.round(s[i] / peak * gain * 32767.0F);
         out[i * 2] = (byte)v;
         out[i * 2 + 1] = (byte)(v >> 8);
      }

      return out;
   }

   public enum Kind {
      CLICK_ON,
      CLICK_OFF,
      OPEN,
      HOVER,
      WHOOSH;
   }
}
