package dev.byteplus.mixin;

import dev.byteplus.BytePlus;
import dev.byteplus.gui.ByteMenuScreen;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_442;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_310.class)
public abstract class MinecraftScreenMixin {
   @Inject(method = "method_1507", at = @At("HEAD"), cancellable = true)
   private void byteplus$mainMenu(class_437 screen, CallbackInfo ci) {
      if (screen instanceof class_442 && BytePlus.modules() != null && BytePlus.modules().clickGui.mainMenu.is("Byte+")) {
         ci.cancel();
         ((class_310)(Object)this).method_1507(new ByteMenuScreen());
      }
   }
}
