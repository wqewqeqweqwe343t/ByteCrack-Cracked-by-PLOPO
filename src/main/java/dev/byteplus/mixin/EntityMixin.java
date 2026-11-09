package dev.byteplus.mixin;

import dev.byteplus.BytePlus;
import dev.byteplus.module.modules.visuals.GlowEspModule;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1297.class)
public abstract class EntityMixin {
   @Inject(method = "method_22861", at = @At("RETURN"), cancellable = true)
   private void byteplus$glowColor(CallbackInfoReturnable<Integer> cir) {
      class_1297 self = (class_1297)(Object)this;
      if (GlowEspModule.shouldGlow(self) && !self.method_5851()) {
         cir.setReturnValue(GlowEspModule.color(self));
      }
   }

   @Inject(method = "method_5872", at = @At("HEAD"), cancellable = true)
   private void byteplus$turn(double dx, double dy, CallbackInfo ci) {
      if ((Object)this == class_310.method_1551().field_1724 && BytePlus.modules() != null) {
         if (BytePlus.modules().onTurn(dx, dy)) {
            ci.cancel();
         }
      }
   }
}
