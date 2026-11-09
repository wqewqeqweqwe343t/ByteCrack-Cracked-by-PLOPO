package dev.byteplus.mixin;

import dev.byteplus.module.modules.anticheat.BlinkModule;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.class_2535;
import net.minecraft.class_2596;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_2535.class)
public abstract class ConnectionMixin {
   @Inject(method = "method_52906(Lnet/minecraft/class_2596;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At("HEAD"), cancellable = true)
   private void byteplus$send(class_2596<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
      if (BlinkModule.onSend(packet)) {
         ci.cancel();
      }
   }
}
