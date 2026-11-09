package dev.byteplus.mixin;

import dev.byteplus.BytePlus;
import dev.byteplus.module.ModuleManager;
import dev.byteplus.module.modules.anticheat.VelocityModule;
import dev.byteplus.util.Commands;
import dev.byteplus.util.TickRate;
import net.minecraft.class_2622;
import net.minecraft.class_2623;
import net.minecraft.class_2626;
import net.minecraft.class_2637;
import net.minecraft.class_2664;
import net.minecraft.class_2672;
import net.minecraft.class_2673;
import net.minecraft.class_2676;
import net.minecraft.class_2743;
import net.minecraft.class_2761;
import net.minecraft.class_2767;
import net.minecraft.class_634;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_634.class)
public abstract class ClientPacketListenerMixin {
   @Inject(method = "method_45729", at = @At("HEAD"), cancellable = true)
   private void byteplus$commands(String message, CallbackInfo ci) {
      if (Commands.handle(message)) {
         ci.cancel();
      }
   }

   @Inject(method = "method_11132", at = @At("HEAD"), cancellable = true)
   private void byteplus$onMotion(class_2743 packet, CallbackInfo ci) {
      if (VelocityModule.onMotion(packet)) {
         ci.cancel();
      }
   }

   @Inject(method = "method_11124", at = @At("TAIL"))
   private void byteplus$onExplosion(class_2664 packet, CallbackInfo ci) {
      VelocityModule.afterExplosion(packet);
   }

   @Inject(method = "method_11079", at = @At("TAIL"))
   private void byteplus$onTime(class_2761 packet, CallbackInfo ci) {
      TickRate.onTimePacket();
   }

   @Inject(method = "method_11136", at = @At("TAIL"))
   private void byteplus$onBlockUpdate(class_2626 packet, CallbackInfo ci) {
      BytePlus.modules().onBlockUpdate(packet.method_11309(), packet.method_11308());
   }

   @Inject(method = "method_11100", at = @At("TAIL"))
   private void byteplus$onSectionUpdate(class_2637 packet, CallbackInfo ci) {
      packet.method_30621((pos, state) -> BytePlus.modules().onBlockUpdate(pos.method_10062(), state));
   }

   @Inject(method = "method_11128", at = @At("TAIL"))
   private void byteplus$onChunk(class_2672 packet, CallbackInfo ci) {
      ModuleManager modules = BytePlus.modules();
      modules.onChunkLoad(packet.method_11523(), packet.method_11524());
      modules.onLightData(packet.method_11523(), packet.method_11524(), packet.method_38599());
      packet.method_38598()
         .method_38587(packet.method_11523(), packet.method_11524())
         .accept((pos, type, tag) -> modules.onBlockEntityData("Chunk", pos.method_10062(), type, tag));
   }

   @Inject(method = "method_11143", at = @At("TAIL"))
   private void byteplus$onLight(class_2676 packet, CallbackInfo ci) {
      BytePlus.modules().onLightData(packet.method_11558(), packet.method_11554(), packet.method_38600());
      BytePlus.modules().onLightUpdatePacket(packet.method_11558(), packet.method_11554(), packet.method_38600());
   }

   @Inject(method = "method_11146", at = @At("TAIL"))
   private void byteplus$onSound(class_2767 packet, CallbackInfo ci) {
      BytePlus.modules().onSoundPacket(packet.method_11888(), packet.method_11890(), packet.method_11889(), packet.method_11893());
   }

   @Inject(method = "method_11098", at = @At("TAIL"))
   private void byteplus$onLevelEvent(class_2673 packet, CallbackInfo ci) {
      BytePlus.modules().onLevelEvent(packet.method_11532(), packet.method_11531());
   }

   @Inject(method = "method_11158", at = @At("TAIL"))
   private void byteplus$onBlockEvent(class_2623 packet, CallbackInfo ci) {
      BytePlus.modules().onBlockEvent(packet.method_11298(), packet.method_11295(), packet.method_11294(), packet.method_11296());
   }

   @Inject(method = "method_11094", at = @At("TAIL"))
   private void byteplus$onBlockEntity(class_2622 packet, CallbackInfo ci) {
      BytePlus.modules().onBlockEntityData("Update", packet.method_11293(), packet.method_11291(), packet.method_11290());
   }
}
