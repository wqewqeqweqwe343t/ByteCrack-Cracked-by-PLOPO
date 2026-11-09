package dev.byteplus.mixin;

import dev.byteplus.module.modules.visuals.CosmeticsModule;
import dev.byteplus.render.CosmeticsRenderer;
import net.minecraft.class_10042;
import net.minecraft.class_10055;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_746;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_922.class)
public abstract class LivingEntityRendererMixin {
   @Inject(
      method = "method_4054(Lnet/minecraft/class_10042;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V",
      at = @At("TAIL")
   )
   private void byteplus$cosmetics(class_10042 state, class_4587 pose, class_11659 collector, class_12075 camera, CallbackInfo ci) {
      if (state instanceof class_10055 avatar && !state.field_53333) {
         class_746 player = class_310.method_1551().field_1724;
         if (player != null && avatar.field_53528 == player.method_5628()) {
            CosmeticsModule m = CosmeticsModule.instance();
            if (m != null && m.isEnabled()) {
               CosmeticsRenderer.render(m, avatar, pose, collector);
            }
         }
      }
   }
}
