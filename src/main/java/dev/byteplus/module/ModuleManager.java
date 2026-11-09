package dev.byteplus.module;

import dev.byteplus.BytePlus;
import dev.byteplus.module.modules.anticheat.AirJumpModule;
import dev.byteplus.module.modules.anticheat.BlinkModule;
import dev.byteplus.module.modules.anticheat.BoatflyModule;
import dev.byteplus.module.modules.anticheat.ClickTPModule;
import dev.byteplus.module.modules.anticheat.ElytraFlightModule;
import dev.byteplus.module.modules.anticheat.FlightModule;
import dev.byteplus.module.modules.anticheat.NoFallModule;
import dev.byteplus.module.modules.anticheat.SpeedMineModule;
import dev.byteplus.module.modules.anticheat.SpeedModule;
import dev.byteplus.module.modules.anticheat.SpiderModule;
import dev.byteplus.module.modules.anticheat.StepModule;
import dev.byteplus.module.modules.anticheat.VelocityModule;
import dev.byteplus.module.modules.basefinding.ActivityInspectorModule;
import dev.byteplus.module.modules.basefinding.ChunkFinderModule;
import dev.byteplus.module.modules.basefinding.LightDebugModule;
import dev.byteplus.module.modules.basefinding.RelogMethodModule;
import dev.byteplus.module.modules.basefinding.SusChunkFinderModule;
import dev.byteplus.module.modules.byteplus.BytePlusModule;
import dev.byteplus.module.modules.byteplus.DiscordPresenceModule;
import dev.byteplus.module.modules.combat.AimAssistModule;
import dev.byteplus.module.modules.combat.AnchorMacroModule;
import dev.byteplus.module.modules.combat.AutoCrystalModule;
import dev.byteplus.module.modules.combat.AutoInvTotemModule;
import dev.byteplus.module.modules.combat.AutoTotemModule;
import dev.byteplus.module.modules.combat.TriggerbotModule;
import dev.byteplus.module.modules.donut.BlockEntityDebugModule;
import dev.byteplus.module.modules.donut.PlayerChunksModule;
import dev.byteplus.module.modules.donut.PrimeChunkFinderModule;
import dev.byteplus.module.modules.donut.RtpBaseFinderModule;
import dev.byteplus.module.modules.donut.TunnelBaseFinderModule;
import dev.byteplus.module.modules.misc.AutoMineModule;
import dev.byteplus.module.modules.misc.AutoReconnectModule;
import dev.byteplus.module.modules.misc.AutoToolModule;
import dev.byteplus.module.modules.misc.AutoWalkModule;
import dev.byteplus.module.modules.misc.FastPlaceModule;
import dev.byteplus.module.modules.misc.FreecamModule;
import dev.byteplus.module.modules.misc.FriendsModule;
import dev.byteplus.module.modules.misc.NameProtectModule;
import dev.byteplus.module.modules.misc.SkinProtectModule;
import dev.byteplus.module.modules.misc.WeatherNotifierModule;
import dev.byteplus.module.modules.render.BlockEspModule;
import dev.byteplus.module.modules.render.FreeLookModule;
import dev.byteplus.module.modules.render.HudModule;
import dev.byteplus.module.modules.render.JumpCirclesModule;
import dev.byteplus.module.modules.render.NameTagsModule;
import dev.byteplus.module.modules.render.PlayerEspModule;
import dev.byteplus.module.modules.render.StorageEspModule;
import dev.byteplus.module.modules.render.SwingSpeedModule;
import dev.byteplus.module.modules.render.TargetHudModule;
import dev.byteplus.module.modules.visuals.BlockOutlineModule;
import dev.byteplus.module.modules.visuals.CosmeticsModule;
import dev.byteplus.module.modules.visuals.CustomCrosshairModule;
import dev.byteplus.module.modules.visuals.CustomSkyModule;
import dev.byteplus.module.modules.visuals.FullbrightModule;
import dev.byteplus.module.modules.visuals.GlowEspModule;
import dev.byteplus.module.modules.visuals.HitEffectsModule;
import dev.byteplus.module.modules.visuals.KnivesModule;
import dev.byteplus.module.modules.visuals.ZoomModule;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2487;
import net.minecraft.class_2591;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3419;
import net.minecraft.class_3675;
import net.minecraft.class_6606;

public final class ModuleManager {
   private final List<Module> modules = new ArrayList<>();
   private final Set<Integer> heldKeys = new HashSet<>();
   public final HudModule hud;
   public final StorageEspModule storageEsp;
   public final BytePlusModule clickGui;
   public final FreecamModule freecam;
   public final FreeLookModule freeLook;
   public final NameProtectModule nameProtect;
   public final SkinProtectModule skinProtect;
   public final SwingSpeedModule swingSpeed;
   public final NameTagsModule nameTags;
   public final AutoReconnectModule autoReconnect;

   public ModuleManager() {
      this.register(new AutoTotemModule());
      this.register(new AutoInvTotemModule());
      this.register(new AnchorMacroModule());
      this.register(new AutoCrystalModule());
      this.register(new TriggerbotModule());
      this.register(new AimAssistModule());
      this.register(new FlightModule());
      this.register(new BoatflyModule());
      this.register(new SpeedModule());
      this.register(new SpiderModule());
      this.register(new NoFallModule());
      this.register(new StepModule());
      this.register(new VelocityModule());
      this.register(new SpeedMineModule());
      this.register(new AirJumpModule());
      this.register(new ClickTPModule());
      this.register(new BlinkModule());
      this.register(new ElytraFlightModule());
      this.skinProtect = this.register(new SkinProtectModule());
      this.autoReconnect = this.register(new AutoReconnectModule());
      this.register(new AutoMineModule());
      this.register(new AutoWalkModule());
      this.register(new WeatherNotifierModule());
      this.freecam = this.register(new FreecamModule());
      this.register(new AutoToolModule());
      this.register(new FastPlaceModule());
      this.nameProtect = this.register(new NameProtectModule());
      this.register(new RtpBaseFinderModule());
      this.register(new BlockEntityDebugModule());
      this.register(new PlayerChunksModule());
      this.register(new PrimeChunkFinderModule());
      this.register(new TunnelBaseFinderModule());
      this.register(new SusChunkFinderModule());
      this.register(new ChunkFinderModule());
      this.register(new ActivityInspectorModule());
      this.register(new LightDebugModule());
      this.register(new RelogMethodModule());
      this.register(new PlayerEspModule());
      this.freeLook = this.register(new FreeLookModule());
      this.register(new BlockEspModule());
      this.storageEsp = this.register(new StorageEspModule());
      this.register(new TargetHudModule());
      this.register(new JumpCirclesModule());
      this.hud = this.register(new HudModule());
      this.swingSpeed = this.register(new SwingSpeedModule());
      this.nameTags = this.register(new NameTagsModule());
      this.register(new DiscordPresenceModule());
      this.register(new FriendsModule());
      this.register(new KnivesModule());
      this.register(new CustomSkyModule());
      this.register(new CustomCrosshairModule());
      this.register(new BlockOutlineModule());
      this.register(new HitEffectsModule());
      this.register(new ZoomModule());
      this.register(new FullbrightModule());
      this.register(new GlowEspModule());
      this.register(new CosmeticsModule());
      this.clickGui = this.register(new BytePlusModule());
   }

   private <M extends Module> M register(M module) {
      this.modules.add(module);
      return module;
   }

   public List<Module> all() {
      return this.modules;
   }

   public List<Module> byCategory(Category category) {
      List<Module> out = new ArrayList<>();

      for (Module m : this.modules) {
         if (m.getCategory() == category) {
            out.add(m);
         }
      }

      return out;
   }

   public Module byName(String name) {
      for (Module m : this.modules) {
         if (m.getName().equalsIgnoreCase(name)) {
            return m;
         }
      }

      return null;
   }

   private void each(ModuleManager.Call c) {
      for (Module m : this.modules) {
         if (m.isEnabled()) {
            try {
               c.run(m);
            } catch (Throwable t) {
               BytePlus.LOGGER.error("Error in module {}", m.getName(), t);
            }
         }
      }
   }

   public void onTick() {
      this.each(Module::onTick);
   }

   public void onTickAlways() {
      this.each(Module::onTickAlways);
      this.autoReconnect.globalTick();
   }

   public void onFrame(float dt) {
      this.each(m -> m.onFrame(dt));
   }

   public void renderGlobal(float sw, float sh, float dt) {
      this.each(m -> m.renderGlobal(sw, sh, dt));
      this.autoReconnect.drawCountdown(sw, sh);
   }

   public void onJoin() {
      for (Module m : this.modules) {
         try {
            m.onJoin();
         } catch (Throwable t) {
            BytePlus.LOGGER.error("Error in module {}", m.getName(), t);
         }
      }
   }

   public void onBlockUpdate(class_2338 pos, class_2680 state) {
      this.each(m -> m.onBlockUpdate(pos, state));
   }

   public void onBlockChange(class_2338 pos, class_2680 oldState, class_2680 newState) {
      this.each(m -> m.onBlockChange(pos, oldState, newState));
   }

   public void onBlockEntityData(String source, class_2338 pos, class_2591<?> type, class_2487 tag) {
      this.each(m -> m.onBlockEntityData(source, pos, type, tag));
   }

   public void onLightData(int cx, int cz, class_6606 data) {
      this.each(m -> m.onLightData(cx, cz, data));
   }

   public void onChunkLoad(int cx, int cz) {
      this.each(m -> m.onChunkLoad(cx, cz));
   }

   public void onLightUpdatePacket(int cx, int cz, class_6606 data) {
      this.each(m -> m.onLightUpdatePacket(cx, cz, data));
   }

   public void onSoundPacket(class_3419 source, double x, double y, double z) {
      this.each(m -> m.onSoundPacket(source, x, y, z));
   }

   public void onLevelEvent(int type, class_2338 pos) {
      this.each(m -> m.onLevelEvent(type, pos));
   }

   public void onBlockEvent(class_2338 pos, class_2248 block, int b0, int b1) {
      this.each(m -> m.onBlockEvent(pos, block, b0, b1));
   }

   public boolean onScroll(double amount) {
      if (class_310.method_1551().field_1755 != null) {
         return false;
      }

      for (Module m : this.modules) {
         if (m.isEnabled() && m.onScroll(amount)) {
            return true;
         }
      }

      return false;
   }

   public boolean onTurn(double dx, double dy) {
      for (Module m : this.modules) {
         if (m.isEnabled() && m.onTurn(dx, dy)) {
            return true;
         }
      }

      return false;
   }

   public void pollKeybinds() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1755 != null) {
         this.heldKeys.clear();
      } else {
         Set<Integer> pressed = new HashSet<>();
         Set<Integer> checked = new HashSet<>();

         for (Module m : this.modules) {
            int key = m.keybind.getKey();
            if (key != -1 && checked.add(key)) {
               boolean down = class_3675.method_15987(mc.method_22683(), key);
               if (down && this.heldKeys.add(key)) {
                  pressed.add(key);
               } else if (!down) {
                  this.heldKeys.remove(key);
               }
            }
         }

         if (!pressed.isEmpty()) {
            for (Module m : new ArrayList<>(this.modules)) {
               if (pressed.contains(m.keybind.getKey())) {
                  m.toggle();
               }
            }
         }
      }
   }

   private interface Call {
      void run(Module var1);
   }
}
