package dev.byteplus.mixin;

import dev.byteplus.module.modules.misc.NameProtectModule;
import net.minecraft.class_327;
import net.minecraft.class_5481;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(class_327.class)
public abstract class FontMixin {
   @ModifyVariable(method = "method_71796(Ljava/lang/String;FFIZI)Lnet/minecraft/class_327$class_11465;", at = @At("HEAD"), argsOnly = true)
   private String byteplus$prepareString(String text) {
      return NameProtectModule.replace(text);
   }

   @ModifyVariable(method = "method_71795(Lnet/minecraft/class_5481;FFIZZI)Lnet/minecraft/class_327$class_11465;", at = @At("HEAD"), argsOnly = true)
   private class_5481 byteplus$prepareSeq(class_5481 text) {
      return NameProtectModule.replace(text);
   }

   @ModifyVariable(method = "method_1727(Ljava/lang/String;)I", at = @At("HEAD"), argsOnly = true)
   private String byteplus$widthString(String text) {
      return NameProtectModule.replace(text);
   }

   @ModifyVariable(method = "method_30880(Lnet/minecraft/class_5481;)I", at = @At("HEAD"), argsOnly = true)
   private class_5481 byteplus$widthSeq(class_5481 text) {
      return NameProtectModule.replace(text);
   }
}
