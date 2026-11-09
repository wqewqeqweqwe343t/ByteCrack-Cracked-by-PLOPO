package dev.byteplus.mixin;

import dev.byteplus.module.modules.misc.SkinProtectModule;
import dev.byteplus.module.modules.visuals.CosmeticsModule;
import net.minecraft.class_310;
import net.minecraft.class_742;
import net.minecraft.class_8685;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_742.class)
public abstract class AbstractClientPlayerMixin {
   @Inject(method = "method_52814", at = @At("RETURN"), cancellable = true)
   private void byteplus$skin(CallbackInfoReturnable<class_8685> cir) {
      if ((Object)this == class_310.method_1551().field_1724) {
         class_8685 s = SkinProtectModule.activeSkin();
         if (s != null) {
            cir.setReturnValue(s);
         }

         class_8685 caped = CosmeticsModule.withCape((class_8685)cir.getReturnValue());
         if (caped != null) {
            cir.setReturnValue(caped);
         }
      }
   }
}
