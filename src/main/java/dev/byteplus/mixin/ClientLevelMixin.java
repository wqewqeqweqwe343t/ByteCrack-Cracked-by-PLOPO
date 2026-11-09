package dev.byteplus.mixin;

import dev.byteplus.BytePlus;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_638.class)
public abstract class ClientLevelMixin {
   @Inject(method = "method_41928", at = @At("HEAD"))
   private void byteplus$onBlockChange(class_2338 pos, class_2680 state, int flags, CallbackInfo ci) {
      if (BytePlus.modules() != null) {
         class_2680 old = ((class_638)(Object)this).method_8320(pos);
         BytePlus.modules().onBlockChange(pos.method_10062(), old, state);
      }
   }
}
