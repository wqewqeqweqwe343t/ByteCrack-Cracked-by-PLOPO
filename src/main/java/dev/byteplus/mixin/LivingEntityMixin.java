package dev.byteplus.mixin;

import dev.byteplus.module.modules.render.SwingSpeedModule;
import net.minecraft.class_1309;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1309.class)
public abstract class LivingEntityMixin {
   @Inject(method = "method_6028", at = @At("RETURN"), cancellable = true)
   private void byteplus$swing(CallbackInfoReturnable<Integer> cir) {
      if ((Object)this == class_310.method_1551().field_1724) {
         int o = SwingSpeedModule.override();
         if (o > 0) {
            cir.setReturnValue(o);
         }
      }
   }
}
