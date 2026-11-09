package dev.byteplus.dev;

import dev.byteplus.BytePlus;
import dev.byteplus.gui.ByteMenuScreen;
import dev.byteplus.gui.ClickGuiScreen;
import dev.byteplus.gui.GuiDev;
import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.module.modules.basefinding.LightDebugModule;
import dev.byteplus.module.modules.visuals.BlockOutlineModule;
import dev.byteplus.module.modules.visuals.CosmeticsModule;
import dev.byteplus.module.modules.visuals.CustomSkyModule;
import dev.byteplus.module.modules.visuals.FullbrightModule;
import dev.byteplus.module.modules.visuals.HitEffectsModule;
import dev.byteplus.module.modules.visuals.KnivesModule;
import dev.byteplus.render.CustomSkyRenderer;
import dev.byteplus.render.LightTileRenderer;
import dev.byteplus.render.knife.KnifeModels;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleConsumer;
import java.util.stream.Stream;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1041;
import net.minecraft.class_1132;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_310;
import net.minecraft.class_433;
import net.minecraft.class_5498;
import net.minecraft.class_746;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL33C;

public final class DevCapture {
   private static boolean active;
   private static Path out;
   private static Path footageRoot;
   private static final Deque<DevCapture.Step> steps = new ArrayDeque<>();
   private static DevCapture.Step current;
   private static String pendingShot;
   public static Float mouseX;
   public static Float mouseY;
   private static double clock;
   private static double lastReal = -1.0;
   private static DoubleConsumer frameAnim;
   private static double frameAnimStart;
   private static final int REC_FPS = 30;
   private static final int REC_BUFFERS = 16;
   private static final int REC_THREADS = 6;
   private static String recName;
   private static Path recDir;
   private static double recStart;
   private static double recNext;
   private static int recIndex;
   private static int recDropped;
   private static int recW;
   private static int recH;
   private static final List<Double> recTimes = new ArrayList<>();
   private static final List<String> clips = new ArrayList<>();
   private static ExecutorService recPool;
   private static ArrayBlockingQueue<ByteBuffer> recBuffers;
   private static final AtomicInteger recPending = new AtomicInteger();

   private DevCapture() {
   }

   public static boolean active() {
      return active;
   }

   public static void init() {
      if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
         Path gameDir = FabricLoader.getInstance().getGameDir();
         Path marker = gameDir.resolve("byteplus-devcapture.txt");
         if (Files.exists(marker)) {
            try {
               String what = Files.readString(marker).toLowerCase();
               out = gameDir.resolve("devcapture").resolve(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")));
               Files.createDirectories(out);
               footageRoot = gameDir.toAbsolutePath().getParent().resolve("devlog").resolve("footage");
               build(what);
               active = true;
               BytePlus.LOGGER.info("[DevCapture] active: {}", what.trim());
            } catch (Exception e) {
               BytePlus.LOGGER.error("[DevCapture] failed to start", e);
            }
         }
      }
   }

   private static double now() {
      return clock;
   }

   private static class_310 mc() {
      return class_310.method_1551();
   }

   private static void advanceClock() {
      double real = System.nanoTime() / 1.0E9;
      if (lastReal >= 0.0) {
         clock = clock + Math.min(0.1, real - lastReal);
      }

      lastReal = real;
   }

   private static void run(Runnable r) {
      steps.add(() -> {
         r.run();
         return true;
      });
   }

   private static void sleep(double s) {
      double[] until = new double[]{-1.0};
      steps.add(() -> {
         if (until[0] < 0.0) {
            until[0] = now() + s;
         }

         return now() >= until[0];
      });
   }

   private static void waitFor(BooleanSupplier cond, double timeout) {
      double[] until = new double[]{-1.0};
      steps.add(() -> {
         if (until[0] < 0.0) {
            until[0] = now() + timeout;
         }

         return cond.getAsBoolean() || now() >= until[0];
      });
   }

   private static void shot(String name) {
      boolean[] requested = new boolean[]{false};
      steps.add(() -> {
         if (!requested[0]) {
            pendingShot = name;
            requested[0] = true;
         }

         return pendingShot == null;
      });
   }

   private static void mouse(Float x, Float y) {
      run(() -> {
         mouseX = x;
         mouseY = y;
      });
   }

   private static void server(String cmd) {
      run(() -> {
         class_1132 server = mc().method_1576();
         if (server != null) {
            server.execute(() -> server.method_3734().method_44252(server.method_3739(), cmd));
         }
      });
   }

   private static <T extends Module> T module(Class<T> type) {
      for (Module m : BytePlus.modules().all()) {
         if (type.isInstance(m)) {
            return type.cast(m);
         }
      }

      throw new IllegalStateException("no module " + type);
   }

   private static void animate(double seconds, DoubleConsumer f) {
      double[] until = new double[]{-1.0};
      steps.add(() -> {
         if (until[0] < 0.0) {
            until[0] = now() + seconds;
            frameAnimStart = now();
            frameAnim = f;
            f.accept(0.0);
         }

         if (now() < until[0]) {
            return false;
         }

         frameAnim = null;
         f.accept(seconds);
         return true;
      });
   }

   private static void animStart(DoubleConsumer f) {
      run(() -> {
         frameAnimStart = now();
         frameAnim = f;
         f.accept(0.0);
      });
   }

   private static void animStop() {
      run(() -> frameAnim = null);
   }

   private static float smooth(double t) {
      float k = (float)Math.max(0.0, Math.min(1.0, t));
      return k * k * (3.0F - 2.0F * k);
   }

   private static float lerp(float a, float b, float t) {
      return a + (b - a) * t;
   }

   private static void look(float yaw, float pitch) {
      class_746 p = mc().field_1724;
      if (p != null) {
         p.method_36456(yaw);
         p.field_5982 = yaw;
         p.method_36457(pitch);
         p.field_6004 = pitch;
         p.method_5847(yaw);
         p.field_6259 = yaw;
         p.field_6283 = yaw;
         p.field_6220 = yaw;
      }
   }

   private static void build(String what) {
      run(() -> GLFW.glfwMaximizeWindow(mc().method_22683().method_4490()));
      waitFor(() -> mc().method_18506() == null && mc().field_1755 instanceof ByteMenuScreen, 120.0);
      sleep(1.5);
      if (what.contains("devlog")) {
         buildDevlog();
         run(() -> {
            BytePlus.LOGGER.info("[DevCapture] done");
            mc().method_1592();
         });
      } else {
         if (what.contains("menu")) {
            run(() -> mc().method_1507(new ByteMenuScreen()));
            sleep(0.35);
            shot("menu_intro_a");
            sleep(0.45);
            shot("menu_intro_b");
            sleep(3.2);
            shot("menu_idle");
            run(() -> {
               float[] c = ((ByteMenuScreen)mc().field_1755).devButtonCenter(1);
               mouseX = c[0];
               mouseY = c[1];
            });
            sleep(0.7);
            shot("menu_hover");
            run(() -> {
               class_1041 w = mc().method_22683();
               mouseX = w.method_4486() * 0.72F;
               mouseY = w.method_4502() * 0.45F;
            });
            sleep(0.8);
            shot("menu_mouse_right");
            mouse(null, null);
         }

         if (what.contains("gui")) {
            run(() -> BytePlus.modules().clickGui.guiStyle.set("Window"));
            run(() -> mc().method_1507(ClickGuiScreen.get()));
            sleep(1.3);
            shot("gui_window_open");
            run(() -> GuiDev.select(Category.VISUALS));
            sleep(0.9);
            shot("gui_window_visuals");
            run(() -> GuiDev.expand("Custom Sky"));
            sleep(1.0);
            shot("gui_window_expanded");
            run(() -> {
               float[] p = GuiDev.rowCenter("CSGO Knives");
               if (p != null) {
                  mouseX = p[0];
                  mouseY = p[1];
               }
            });
            sleep(0.6);
            shot("gui_window_hover");
            mouse(null, null);
            run(() -> GuiDev.select(Category.BYTEPLUS));
            run(() -> GuiDev.expand("Byte+"));
            sleep(1.0);
            shot("gui_window_byteplus");
            run(() -> mc().method_1507(null));
            run(() -> BytePlus.modules().clickGui.guiStyle.set("Dropdown"));
            run(() -> mc().method_1507(ClickGuiScreen.get()));
            sleep(1.3);
            shot("gui_dropdown");
            run(() -> mc().method_1507(null));
            sleep(0.5);
         }

         boolean all = what.contains("world");
         boolean sky = all || what.contains("sky");
         boolean knife = all || what.contains("knife");
         boolean fx = all || what.contains("fx");
         if (sky || knife || fx) {
            openWorld();
            if (sky) {
               run(() -> {
                  class_746 p = mc().field_1724;
                  p.method_31549().field_7479 = true;
                  p.method_7355();
               });
               server("execute as @a at @s run tp @s ~ ~45 ~");
               run(() -> {
                  mc().field_1690.method_42433().method_41748(false);
                  mc().field_1690.method_42524().method_41748(260);
               });
               run(() -> {
                  mc().field_1724.method_36456(180.0F);
                  mc().field_1724.method_36457(-22.0F);
                  mc().field_1690.field_1842 = true;
               });
               sleep(3.0);
               run(
                  () -> BytePlus.LOGGER
                     .info(
                        "[DevCapture] perf vanilla sky: {} fps at {}x{}",
                        new Object[]{mc().method_47599(), mc().method_1522().field_1482, mc().method_1522().field_1481}
                     )
               );
               run(() -> module(CustomSkyModule.class).setEnabled(true));

               for (int i = 0; i < CustomSkyModule.SKIES.length; i++) {
                  String s = CustomSkyModule.SKIES[i];
                  int idx = i;
                  run(() -> module(CustomSkyModule.class).sky.set(s));
                  sleep(1.3);
                  run(() -> CustomSkyRenderer.devStats(idx));
                  sleep(1.6);
                  run(() -> BytePlus.LOGGER.info("[DevCapture] perf {}: {} fps, {}", new Object[]{s, mc().method_47599(), CustomSkyRenderer.devStats(idx)}));
                  shot("sky_" + s.toLowerCase().replace(' ', '_'));
               }

               server("execute as @a at @s run tp @s ~ ~-45 ~");
               run(() -> {
                  class_746 p = mc().field_1724;
                  p.method_31549().field_7479 = false;
                  p.method_7355();
               });
               sleep(1.5);
            }

            run(() -> {
               module(CustomSkyModule.class).setEnabled(false);
               mc().field_1724.method_36457(4.0F);
               mc().field_1690.field_1842 = false;
            });
            server("time set 6000");
            sleep(1.0);
            if (knife) {
               shot("vanilla_sword");
               run(() -> module(KnivesModule.class).setEnabled(true));

               for (String type : KnifeModels.TYPES) {
                  run(() -> module(KnivesModule.class).knife.set(type));
                  run(() -> module(KnivesModule.class).drawTime = 0.0F);
                  sleep(0.5);
                  shot("knife_" + type.toLowerCase() + "_draw");
                  sleep(1.2);
                  shot("knife_" + type.toLowerCase() + "_idle");
                  run(() -> module(KnivesModule.class).inspectTime = 0.0F);
                  sleep(1.6);
                  shot("knife_" + type.toLowerCase() + "_inspect");
                  sleep(2.8);
               }
            }

            if (all) {
               run(() -> {
                  CosmeticsModule c = module(CosmeticsModule.class);
                  c.setEnabled(true);
                  c.wings.set("Angel");
                  c.halo.set(true);
                  c.cape.set("Galaxy");
                  mc().field_1690.method_31043(class_5498.field_26665);
               });
               sleep(1.5);
               shot("cosmetics_back");
               run(() -> mc().field_1690.method_31043(class_5498.field_26666));
               sleep(1.0);
               shot("cosmetics_front");
               run(() -> mc().field_1690.method_31043(class_5498.field_26664));
            }

            if (fx) {
               run(() -> module(HitEffectsModule.class).setEnabled(true));
               server("execute as @a at @s run summon zombie ^ ^ ^3 {NoAI:1b,PersistenceRequired:1b,Silent:1b}");
               sleep(1.0);
               run(DevCapture::hitNearest);
               server("execute as @a at @s run damage @e[type=zombie,limit=1,sort=nearest] 5");
               sleep(0.25);
               shot("hit_a");
               sleep(0.35);
               shot("hit_b");
               run(DevCapture::hitNearest);
               server("execute as @a at @s run damage @e[type=zombie,limit=1,sort=nearest] 7");
               sleep(0.3);
               shot("hit_c");
               run(DevCapture::hitNearest);
               server("kill @e[type=zombie]");
               sleep(0.3);
               shot("kill_a");
               sleep(0.6);
               shot("kill_b");
               run(() -> {
                  module(BlockOutlineModule.class).setEnabled(true);
                  mc().field_1724.method_36457(55.0F);
               });

               for (String style : new String[]{"Neon", "Gradient", "Corners"}) {
                  run(() -> module(BlockOutlineModule.class).style.set(style));
                  sleep(0.8);
                  shot("outline_" + style.toLowerCase());
               }

               run(() -> BlockOutlineModule.devProgress = 0.62F);
               sleep(0.8);
               shot("outline_mining");
               run(() -> BlockOutlineModule.devProgress = -1.0F);
            }
         }

         if (what.contains("light")) {
            if (!sky && !knife && !fx) {
               openWorld();
            }

            buildLightDebug();
         }

         run(() -> {
            BytePlus.LOGGER.info("[DevCapture] done");
            mc().method_1592();
         });
      }
   }

   private static void buildLightDebug() {
      Class<LightDebugModule> light = LightDebugModule.class;
      run(() -> module(light).setEnabled(false));
      run(() -> {
         class_746 p = mc().field_1724;
         p.method_31549().field_7479 = true;
         p.method_7355();
         mc().field_1690.field_1842 = true;
      });
      String at = "execute as @a at @s run ";
      server(at + "fill ~-10 -20 ~-10 ~10 -16 ~10 air");
      server(at + "fill ~10 -20 ~-1 ~44 -18 ~1 air");
      server(at + "fill ~-34 -40 ~12 ~-18 -35 ~28 air");
      server(at + "fill ~-33 -40 ~13 ~-19 -40 ~27 lava");
      server(at + "setblock ~ -20 ~ torch");
      server(at + "setblock ~-7 -20 ~6 lantern");
      server(at + "setblock ~6 -20 ~-7 torch");
      server(at + "setblock ~-7 -16 ~-7 glowstone");

      for (int x = 18; x <= 42; x += 8) {
         server(at + "setblock ~" + x + " -20 ~ torch");
      }

      server(at + "fill ~-6 -62 ~30 ~6 -58 ~42 air");
      server(at + "setblock ~ -62 ~36 torch");
      sleep(2.0);
      run(() -> {
         mc().field_1690.method_42433().method_41748(false);
         mc().field_1690.method_42524().method_41748(260);
      });
      server(at + "tp @s ~-4 ~24 ~-26 -25 48");
      sleep(2.0);
      run(() -> BytePlus.LOGGER.info("[DevCapture] perf light off: {} fps", mc().method_47599()));
      shot("light_off");
      run(() -> {
         module(light).setEnabled(true);
         LightTileRenderer.profile = true;
      });
      sleep(5.0);
      run(LightTileRenderer::devStats);
      sleep(2.0);
      run(() -> BytePlus.LOGGER.info("[DevCapture] perf light on: {} fps, {}", mc().method_47599(), LightTileRenderer.devStats()));
      shot("light_above");
      server(at + "tp @s ~4 -19 ~18 -20 15");
      sleep(2.0);
      shot("light_room");
      run(() -> {
         LightTileRenderer.profile = false;
         module(light).setEnabled(false);
         mc().field_1690.field_1842 = false;
      });
   }

   private static void openWorld() {
      run(() -> {
         mc().field_1690.field_1837 = false;
         mc().method_41735().method_57784("New World", () -> {});
      });
      waitFor(() -> mc().field_1724 != null && mc().field_1687 != null && mc().field_1755 == null, 120.0);
      sleep(7.0);
      run(
         () -> {
            for (Class<? extends Module> c : List.of(
               CustomSkyModule.class, KnivesModule.class, CosmeticsModule.class, HitEffectsModule.class, BlockOutlineModule.class, FullbrightModule.class
            )) {
               module(c).setEnabled(false);
            }
         }
      );
      server("time set 18000");
      server("weather clear");
      server("gamerule advance_time false");
      server("gamemode creative @a");
      server("kill @e[type=zombie]");
      server("item replace entity @a weapon.mainhand with diamond_sword");
      sleep(1.5);
   }

   private static void buildDevlog() {
      run(
         () -> {
            mc().field_1690.field_1837 = false;
            mc().field_1690.method_42433().method_41748(false);
            mc().field_1690.method_42524().method_41748(260);
            BytePlus.modules().clickGui.guiStyle.set("Window");

            for (Class<? extends Module> c : List.of(
               CustomSkyModule.class, KnivesModule.class, CosmeticsModule.class, HitEffectsModule.class, BlockOutlineModule.class, FullbrightModule.class
            )) {
               module(c).setEnabled(false);
            }
         }
      );
      run(() -> {
         ByteMenuScreen.devResetIntro();
         class_1041 w = mc().method_22683();
         mouseX = w.method_4486() * 0.7F;
         mouseY = w.method_4502() * 0.4F;
      });
      recordStart("menu_intro");
      run(() -> mc().method_1507(new ByteMenuScreen()));
      sleep(3.6);
      recordStop();
      recordStart("menu_hover");
      float[] from = new float[2];
      run(() -> {
         from[0] = mouseX;
         from[1] = mouseY;
      });
      animate(3.6, t -> {
         if (mc().field_1755 instanceof ByteMenuScreen m) {
            float[] var10 = m.devButtonCenter(0);
            float[] b1 = m.devButtonCenter(1);
            float[] b2 = m.devButtonCenter(2);
            float x;
            float y;
            if (t < 0.8) {
               float k = smooth(t / 0.8);
               x = lerp(from[0], var10[0] + 20.0F, k);
               y = lerp(from[1], var10[1], k);
            } else if (t < 1.9) {
               float k = smooth((t - 1.4) / 0.5);
               x = lerp(var10[0] + 20.0F, b1[0] - 10.0F, k);
               y = lerp(var10[1], b1[1], k);
            } else {
               float k = smooth((t - 2.6) / 0.5);
               x = lerp(b1[0] - 10.0F, b2[0] + 30.0F, k);
               y = lerp(b1[1], b2[1], k);
            }

            mouseX = x;
            mouseY = y;
         }
      });
      recordStop();
      recordStart("menu_warp");
      run(() -> {
         if (mc().field_1755 instanceof ByteMenuScreen m) {
            int i = m.devButtonIndex("Byte+ Settings");
            float[] c = m.devButtonCenter(i);
            mouseX = c[0];
            mouseY = c[1];
         }
      });
      sleep(0.35);
      run(() -> {
         if (mc().field_1755 instanceof ByteMenuScreen m) {
            m.devActivate(m.devButtonIndex("Byte+ Settings"));
         }
      });
      sleep(0.6);
      float[] m0 = new float[2];
      run(() -> {
         m0[0] = mouseX;
         m0[1] = mouseY;
      });
      animate(2.2, t -> {
         class_1041 w = mc().method_22683();
         float k = smooth(t / 1.6);
         mouseX = lerp(m0[0], w.method_4486() * 0.56F, k);
         mouseY = lerp(m0[1], w.method_4502() * 0.42F, k);
      });
      recordStop();
      run(() -> mc().method_1507(null));
      mouse(null, null);
      sleep(1.0);
      openWorld();
      run(() -> {
         module(FullbrightModule.class).setEnabled(true);
         module(CustomSkyModule.class).setEnabled(true);
         module(CustomSkyModule.class).sky.set("Galaxy");
         mc().field_1690.field_1842 = true;
      });
      run(() -> {
         class_746 p = mc().field_1724;
         p.method_31549().field_7479 = true;
         p.method_7355();
      });
      server("execute as @a at @s run tp @s ~ ~45 ~");
      sleep(2.5);
      Object[][] skies = new Object[][]{
         {"Black Hole", 164.0F, -23.0F},
         {"Ringed Planet", 212.0F, -21.0F},
         {"Aurora", 180.0F, -16.0F},
         {"Galaxy", 180.0F, -20.0F},
         {"Blood Moon", 157.0F, -25.0F},
         {"Nebula", 180.0F, -24.0F},
         {"Synthwave", 180.0F, -7.0F},
         {"Sunset", 180.0F, -6.0F},
         {"Deep Space", 220.0F, -36.0F}
      };

      for (Object[] s : skies) {
         String name = (String)s[0];
         float yaw = (Float)s[1];
         float pitch = (Float)s[2];
         run(() -> {
            module(CustomSkyModule.class).sky.set(name);
            look(yaw - 5.0F, pitch);
         });
         sleep(1.3);
         recordStart("sky_" + name.toLowerCase().replace(' ', '_'));
         animate(2.7, t -> look(yaw - 5.0F + (float)t * 4.0F, pitch - (float)t * 0.5F));
         recordStop();
      }

      run(() -> {
         module(CustomSkyModule.class).sky.set("Galaxy");
         look(160.0F, -23.0F);
      });
      sleep(1.5);
      recordStart("sky_switch");
      animStart(t -> look(160.0F + (float)t * 3.0F, -23.0F));
      sleep(0.5);
      run(() -> module(CustomSkyModule.class).sky.set("Black Hole"));
      sleep(2.2);
      animStop();
      recordStop();
      server("execute as @a at @s run tp @s ~ ~-45 ~");
      run(() -> {
         class_746 p = mc().field_1724;
         p.method_31549().field_7479 = false;
         p.method_7355();
      });
      sleep(1.0);
      server("execute as @a at @s positioned over world_surface run tp @s ~ ~0.05 ~");
      sleep(1.5);
      run(() -> {
         module(CustomSkyModule.class).sky.set("Galaxy");
         mc().field_1690.field_1842 = false;
         look(180.0F, -8.0F);
      });
      sleep(1.5);
      run(() -> {
         class_1041 w = mc().method_22683();
         mouseX = w.method_4486() * 0.6F;
         mouseY = w.method_4502() * 0.55F;
      });
      recordStart("gui_tour");
      run(() -> mc().method_1507(ClickGuiScreen.get()));
      sleep(1.1);
      run(() -> GuiDev.select(Category.VISUALS));
      sleep(0.8);
      glideMouseToRow("Custom Sky", 0.5);
      run(() -> GuiDev.expand("Custom Sky"));
      sleep(1.0);
      glideMouseToRow("CSGO Knives", 0.45);
      run(() -> GuiDev.toggle("CSGO Knives"));
      sleep(0.9);
      run(() -> GuiDev.select(Category.RENDER));
      sleep(0.8);
      run(() -> GuiDev.select(Category.BYTEPLUS));
      sleep(0.8);
      run(() -> GuiDev.type("s"));
      sleep(0.14);
      run(() -> GuiDev.type("k"));
      sleep(0.14);
      run(() -> GuiDev.type("y"));
      sleep(1.3);
      recordStop();
      run(() -> {
         for (int i = 0; i < 3; i++) {
            GuiDev.key(259);
         }

         GuiDev.key(256);
         mc().method_1507(null);
      });
      mouse(null, null);
      sleep(1.0);
      run(() -> {
         module(KnivesModule.class).setEnabled(true);
         look(180.0F, -9.0F);
      });

      for (String type : KnifeModels.TYPES) {
         run(() -> {
            module(KnivesModule.class).knife.set(type);
            module(KnivesModule.class).drawTime = 0.0F;
         });
         recordStart("knife_" + type.toLowerCase());
         sleep(1.35);
         run(() -> module(KnivesModule.class).inspectTime = 0.0F);
         sleep(4.5);
         recordStop();
         sleep(0.3);
      }

      run(() -> module(KnivesModule.class).knife.set("Butterfly"));
      run(() -> {
         module(HitEffectsModule.class).setEnabled(true);
         look(180.0F, 3.0F);
      });
      server("kill @e[type=zombie]");
      sleep(0.5);
      server("execute as @a at @s run summon zombie ^-0.7 ^ ^2.6 {NoAI:1b,PersistenceRequired:1b,Silent:1b}");
      server("execute as @a at @s run summon zombie ^1.1 ^ ^3.6 {NoAI:1b,PersistenceRequired:1b,Silent:1b}");
      sleep(1.5);
      recordStart("hits");
      sleep(0.4);
      attack("execute as @a at @s run damage @e[type=zombie,limit=1,sort=nearest] 5");
      sleep(0.6);
      attack("execute as @a at @s run damage @e[type=zombie,limit=1,sort=nearest] 6");
      sleep(0.6);
      attack("execute as @a at @s run kill @e[type=zombie,limit=1,sort=nearest]");
      sleep(1.3);
      attack("execute as @a at @s run damage @e[type=zombie,limit=1,sort=nearest] 7");
      sleep(0.6);
      attack("execute as @a at @s run kill @e[type=zombie,limit=1,sort=nearest]");
      sleep(1.8);
      recordStop();
      run(() -> {
         module(BlockOutlineModule.class).setEnabled(true);
         module(BlockOutlineModule.class).style.set("Neon");
         look(150.0F, 52.0F);
      });
      sleep(0.8);
      recordStart("outline");
      animStart(t -> look(150.0F + (float)t * 9.0F, 52.0F + (float)Math.sin(t * 1.3) * 3.0F));
      sleep(2.4);
      run(() -> module(BlockOutlineModule.class).style.set("Gradient"));
      sleep(1.4);
      run(() -> module(BlockOutlineModule.class).style.set("Corners"));
      sleep(1.2);
      animStop();
      run(() -> module(BlockOutlineModule.class).style.set("Neon"));
      animate(1.6, t -> BlockOutlineModule.devProgress = (float)Math.min(1.0, t / 1.4) * 0.98F);
      sleep(0.35);
      run(() -> BlockOutlineModule.devProgress = -1.0F);
      recordStop();
      run(() -> module(BlockOutlineModule.class).setEnabled(false));
      run(() -> {
         CosmeticsModule c = module(CosmeticsModule.class);
         c.setEnabled(true);
         c.wings.set("Angel");
         c.halo.set(true);
         c.cape.set("Galaxy");
         mc().field_1690.method_31043(class_5498.field_26665);
         look(200.0F, 6.0F);
      });
      sleep(1.2);
      recordStart("wings");
      animStart(t -> look(200.0F + (float)t * 10.0F, 6.0F));
      sleep(2.3);
      run(() -> mc().field_1690.method_31043(class_5498.field_26666));
      sleep(2.3);
      animStop();
      recordStop();
      run(() -> mc().field_1690.method_31043(class_5498.field_26664));
      run(DevCapture::writeClipIndex);
      waitFor(() -> recPending.get() == 0, 120.0);
   }

   private static void glideMouseToRow(String module, double seconds) {
      float[] a = new float[2];
      run(() -> {
         a[0] = mouseX;
         a[1] = mouseY;
      });
      animate(seconds, t -> {
         float[] p = GuiDev.rowCenter(module);
         if (p != null) {
            float k = smooth(t / seconds);
            mouseX = lerp(a[0], p[0] - 30.0F, k);
            mouseY = lerp(a[1], p[1], k);
         }
      });
   }

   private static void attack(String command) {
      run(() -> {
         mc().field_1724.method_6104(class_1268.field_5808);
         hitNearest();
      });
      server(command);
   }

   private static void hitNearest() {
      class_746 p = mc().field_1724;
      class_1297 best = null;

      for (class_1297 e : mc().field_1687.method_18112()) {
         if (e.method_5864() == class_1299.field_6051 && e.method_5805() && (best == null || e.method_5858(p) < best.method_5858(p))) {
            best = e;
         }
      }

      if (best != null) {
         HitEffectsModule.onAttack(best);
      }
   }

   private static void recordStart(String name) {
      run(() -> beginRecording(name));
   }

   private static void recordStop() {
      run(DevCapture::endRecording);
   }

   private static void beginRecording(String name) {
      try {
         if (recPool == null) {
            recPool = Executors.newFixedThreadPool(6, r -> {
               Thread t = new Thread(r, "Byte+ clip encoder");
               t.setDaemon(true);
               return t;
            });
         }

         recDir = footageRoot.resolve(name);
         if (Files.isDirectory(recDir)) {
            try (Stream<Path> list = Files.list(recDir)) {
               for (Path p : (Iterable<Path>) list::iterator) {
                  Files.deleteIfExists(p);
               }
            }
         }

         Files.createDirectories(recDir);
         recName = name;
         recIndex = 0;
         recDropped = 0;
         recTimes.clear();
         recStart = now();
         recNext = recStart;
      } catch (Exception e) {
         BytePlus.LOGGER.error("[DevCapture] can't record {}", name, e);
         recName = null;
      }
   }

   private static void endRecording() {
      if (recName != null) {
         String name = recName;
         recName = null;
         StringBuilder sb = new StringBuilder();
         sb.append("{\"name\":\"")
            .append(name)
            .append("\",\"fps\":")
            .append(30)
            .append(",\"width\":")
            .append(recW)
            .append(",\"height\":")
            .append(recH)
            .append(",\"frames\":")
            .append(recIndex)
            .append(",\"dropped\":")
            .append(recDropped)
            .append(",\"times\":[");

         for (int i = 0; i < recTimes.size(); i++) {
            if (i > 0) {
               sb.append(',');
            }

            sb.append(String.format(Locale.ROOT, "%.4f", recTimes.get(i)));
         }

         sb.append("]}");

         try {
            Files.writeString(recDir.resolve("clip.json"), sb.toString());
         } catch (Exception e) {
            BytePlus.LOGGER.error("[DevCapture] clip.json", e);
         }

         clips.add(name);
         BytePlus.LOGGER.info("[DevCapture] clip {}: {} frames, {} dropped", new Object[]{name, recIndex, recDropped});
      }
   }

   private static void writeClipIndex() {
      StringBuilder sb = new StringBuilder("[");

      for (int i = 0; i < clips.size(); i++) {
         if (i > 0) {
            sb.append(',');
         }

         sb.append('"').append(clips.get(i)).append('"');
      }

      sb.append(']');

      try {
         Files.writeString(footageRoot.resolve("clips.json"), sb.toString());
      } catch (Exception e) {
         BytePlus.LOGGER.error("[DevCapture] clips.json", e);
      }
   }

   private static void readFrame(int w, int h, ByteBuffer buf) {
      int prevRead = GL33C.glGetInteger(36010);
      int prevReadBuffer = GL33C.glGetInteger(3074);
      int prevPack = GL33C.glGetInteger(3333);
      int prevPbo = GL33C.glGetInteger(35053);
      GL33C.glBindBuffer(35051, 0);
      GL33C.glBindFramebuffer(36008, 0);
      GL33C.glReadBuffer(1029);
      GL33C.glPixelStorei(3333, 1);
      GL33C.glReadPixels(0, 0, w, h, 6408, 5121, buf);
      GL33C.glPixelStorei(3333, prevPack);
      GL33C.glBindFramebuffer(36008, prevRead);
      GL33C.glReadBuffer(prevReadBuffer);
      GL33C.glBindBuffer(35051, prevPbo);
   }

   private static void captureClipFrame(int w, int h, double t) {
      if (recBuffers == null || recW != w || recH != h) {
         recW = w;
         recH = h;
         recBuffers = new ArrayBlockingQueue<>(16);

         for (int i = 0; i < 16; i++) {
            recBuffers.add(BufferUtils.createByteBuffer(w * h * 4));
         }
      }

      recNext += 0.03333333333333333;
      if (recNext < t) {
         recNext = t + 0.016666666666666666;
      }

      ByteBuffer buf = recBuffers.poll();
      if (buf == null) {
         recDropped++;
      } else {
         readFrame(w, h, buf);
         int index = recIndex++;
         recTimes.add(t - recStart);
         File file = recDir.resolve(String.format(Locale.ROOT, "%05d.jpg", index)).toFile();
         ArrayBlockingQueue<ByteBuffer> pool = recBuffers;
         recPending.incrementAndGet();
         recPool.submit(() -> {
            try {
               byte[] rgba = new byte[w * h * 4];
               buf.get(0, rgba);
               pool.offer(buf);
               writeJpeg(rgba, w, h, file);
            } catch (Throwable e) {
               BytePlus.LOGGER.error("[DevCapture] frame {} failed", file, e);
            } finally {
               recPending.decrementAndGet();
            }
         });
      }
   }

   private static void writeJpeg(byte[] rgba, int w, int h, File file) throws Exception {
      BufferedImage img = new BufferedImage(w, h, 5);
      byte[] dst = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();

      for (int y = 0; y < h; y++) {
         int s = (h - 1 - y) * w * 4;
         int d = y * w * 3;

         for (int x = 0; x < w; d += 3) {
            dst[d] = rgba[s + 2];
            dst[d + 1] = rgba[s + 1];
            dst[d + 2] = rgba[s];
            x++;
            s += 4;
         }
      }

      ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();

      try (ImageOutputStream os = ImageIO.createImageOutputStream(file)) {
         ImageWriteParam p = writer.getDefaultWriteParam();
         p.setCompressionMode(2);
         p.setCompressionQuality(0.9F);
         writer.setOutput(os);
         writer.write(null, new IIOImage(img, null, null), p);
      } finally {
         writer.dispose();
      }
   }

   public static void tick() {
      if (active) {
         long window = mc().method_22683().method_4490();
         if (GLFW.glfwGetWindowAttrib(window, 131074) == 1) {
            GLFW.glfwRestoreWindow(window);
            lastReal = -1.0;
         } else {
            if (mc().field_1755 instanceof class_433) {
               mc().method_1507(null);
            }

            try {
               for (int guard = 0; guard < 50; guard++) {
                  if (current == null) {
                     current = steps.poll();
                  }

                  if (current == null) {
                     return;
                  }

                  if (!current.run()) {
                     return;
                  }

                  current = null;
               }
            } catch (Throwable t) {
               BytePlus.LOGGER.error("[DevCapture] step failed", t);
               current = null;
            }
         }
      }
   }

   public static void onFrameEnd(int w, int h) {
      if (active) {
         advanceClock();
         double t = now();
         if (recName != null && t >= recNext) {
            try {
               captureClipFrame(w, h, t);
            } catch (Throwable e) {
               BytePlus.LOGGER.error("[DevCapture] record failed", e);
            }
         }

         if (frameAnim != null) {
            try {
               frameAnim.accept(t - frameAnimStart);
            } catch (Throwable e) {
               BytePlus.LOGGER.error("[DevCapture] animation failed", e);
               frameAnim = null;
            }
         }

         if (pendingShot != null) {
            String name = pendingShot;

            try {
               ByteBuffer buf = BufferUtils.createByteBuffer(w * h * 4);
               readFrame(w, h, buf);
               Thread th = new Thread(() -> save(buf, w, h, name), "Byte+ capture");
               th.setDaemon(false);
               th.start();
            } catch (Throwable e) {
               BytePlus.LOGGER.error("[DevCapture] capture failed", e);
            }

            pendingShot = null;
         }
      }
   }

   private static void save(ByteBuffer buf, int w, int h, String name) {
      try {
         BufferedImage img = new BufferedImage(w, h, 1);

         for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
               int i = ((h - 1 - y) * w + x) * 4;
               int r = buf.get(i) & 255;
               int g = buf.get(i + 1) & 255;
               int b = buf.get(i + 2) & 255;
               img.setRGB(x, y, r << 16 | g << 8 | b);
            }
         }

         File f = out.resolve(name + ".png").toFile();
         ImageIO.write(img, "png", f);
         BytePlus.LOGGER.info("[DevCapture] saved {}", f);
      } catch (Exception e) {
         BytePlus.LOGGER.error("[DevCapture] save failed", e);
      }
   }

   private interface Step {
      boolean run();
   }
}
