package dev.byteplus.mixin;

import dev.byteplus.BytePlus;
import dev.byteplus.module.modules.misc.FreecamModule;
import net.minecraft.class_10185;
import net.minecraft.class_241;
import net.minecraft.class_743;
import net.minecraft.class_744;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_743.class)
public abstract class KeyboardInputMixin extends class_744 {
   @Inject(method = "method_3129", at = @At("TAIL"))
   private void byteplus$freeze(CallbackInfo ci) {
      if (BytePlus.modules() != null) {
         FreecamModule freecam = BytePlus.modules().freecam;
         if (freecam.isEnabled()) {
            freecam.captureInput(this.field_54155, this.field_55868);
            this.field_54155 = freecam.heldInput != null ? freecam.heldInput : class_10185.field_54098;
            this.field_55868 = freecam.heldMove != null ? freecam.heldMove : class_241.field_1340;
         }
      }
   }
}
