package dev.byteplus.render.knife;

import dev.byteplus.BytePlus;
import dev.byteplus.module.Module;
import dev.byteplus.module.modules.visuals.KnivesModule;
import dev.byteplus.render.Projection;
import net.minecraft.class_11659;
import net.minecraft.class_12249;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_811;
import org.joml.Quaternionf;

public final class KnifeRenderer {
   private static final class_1921 TYPE = class_12249.method_75994(class_2960.method_60655("byteplus", "textures/misc/white.png"));

   private KnifeRenderer() {
   }

   private static KnivesModule module() {
      if (BytePlus.modules() == null) {
         return null;
      }

      for (Module m : BytePlus.modules().all()) {
         if (m instanceof KnivesModule k) {
            return k.isEnabled() ? k : null;
         }
      }

      return null;
   }

   public static boolean tryRender(class_1309 entity, class_1799 stack, class_811 ctx, class_4587 pose, class_11659 collector, int light) {
      if (ctx != class_811.field_4322 && ctx != class_811.field_4321) {
         return false;
      }

      if (entity == class_310.method_1551().field_1724 && KnivesModule.isSword(stack)) {
         KnivesModule k = module();
         if (k == null) {
            return false;
         }

         String type = k.knife.get();
         KnifeModels.Model model = KnifeModels.get(type, k.finish.get());
         pose.method_22903();
         if (ctx == class_811.field_4321) {
            pose.method_22905(-1.0F, 1.0F, 1.0F);
         }

         pose.method_46416(k.posX.getFloat(), k.posY.getFloat(), k.posZ.getFloat());
         pose.method_22907(class_7833.field_40718.rotationDegrees(k.tilt.getFloat()));
         pose.method_22907(class_7833.field_40716.rotationDegrees(-k.swayYaw * 0.55F));
         pose.method_22907(class_7833.field_40714.rotationDegrees(-k.swayPitch * 0.45F));
         pose.method_46416(-k.swayYaw * 0.0025F, k.swayPitch * 0.002F, 0.0F);
         if (k.sprint > 0.01F) {
            float s = k.sprint;
            float bob = (float)Math.sin(k.bobPhase) * 0.035F * s;
            float side = (float)Math.cos(k.bobPhase * 0.5F) * 0.03F * s;
            pose.method_46416(0.06F * s + side, -0.08F * s + bob, 0.04F * s);
            pose.method_22907(class_7833.field_40718.rotationDegrees(-18.0F * s));
            pose.method_22907(class_7833.field_40714.rotationDegrees(14.0F * s));
         } else {
            float bob = (float)Math.sin(k.bobPhase) * 0.012F;
            pose.method_46416((float)Math.cos(k.bobPhase * 0.5F) * 0.01F, bob, 0.0F);
         }

         pose.method_46416(0.070625F, 0.2F, 0.070625F);
         pose.method_22907(new Quaternionf().rotationXYZ(0.0F, (float)Math.toRadians(-90.0), (float)Math.toRadians(25.0)));
         pose.method_22905(0.68F, 0.68F, 0.68F);
         pose.method_46416(-0.08F, -0.08F, 0.0F);
         pose.method_22907(class_7833.field_40718.rotationDegrees(-45.0F));
         float s = k.size.getFloat() * 1.12F / 16.0F;
         pose.method_22905(s, s, s);
         KnifeRenderer.Parts parts = new KnifeRenderer.Parts();
         animate(pose, type, k, parts);
         attack(pose, type, entity, parts);
         drawParts(pose, collector, k.alwaysBright.isOn() ? 15728880 : light, type, model, parts);
         pose.method_22909();
         return true;
      } else {
         return false;
      }
   }

   private static void drawParts(class_4587 pose, class_11659 collector, int light, String type, KnifeModels.Model model, KnifeRenderer.Parts parts) {
      switch (type) {
         case "Butterfly":
            submit(pose, collector, light, model.parts.get("blade"), parts);
            pose.method_22903();
            pose.method_46416(-0.55F, 0.0F, 0.55F);
            pose.method_22907(class_7833.field_40718.rotationDegrees(parts.handleA));
            submit(pose, collector, light, model.parts.get("handleA"), null);
            pose.method_22909();
            pose.method_22903();
            pose.method_46416(0.6F, 0.0F, -0.55F);
            pose.method_22907(class_7833.field_40718.rotationDegrees(-parts.handleB));
            submit(pose, collector, light, model.parts.get("handleB"), null);
            pose.method_22909();
            break;
         case "Flip":
            submit(pose, collector, light, model.parts.get("handle"), null);
            pose.method_22903();
            pose.method_46416(0.1F, 0.35F, 0.0F);
            pose.method_22907(class_7833.field_40718.rotationDegrees(-parts.flipBlade));
            pose.method_46416(-0.1F, -0.35F, 0.0F);
            submit(pose, collector, light, model.parts.get("blade"), parts);
            pose.method_22909();
            break;
         default:
            submit(pose, collector, light, model.parts.get("body"), parts);
      }
   }

   private static void submit(class_4587 pose, class_11659 collector, int light, KnifeMesh mesh, KnifeRenderer.Parts shine) {
      if (mesh != null) {
         float pos = shine != null ? shine.shinePos : -1.0F;
         float amt = shine != null ? shine.shineAmt : 0.0F;
         collector.method_73483(pose, TYPE, (p, vc) -> mesh.emit(p, vc, light, 1.0F, pos, amt));
      }
   }

   private static float clamp01(float x) {
      return Math.max(0.0F, Math.min(1.0F, x));
   }

   private static float seg(float x, float a, float b) {
      return clamp01((x - a) / (b - a));
   }

   private static float outCubic(float t) {
      float u = 1.0F - t;
      return 1.0F - u * u * u;
   }

   private static float inCubic(float t) {
      return t * t * t;
   }

   private static float inOut(float t) {
      return t < 0.5F ? 4.0F * t * t * t : 1.0F - (float)Math.pow(-2.0F * t + 2.0F, 3.0) / 2.0F;
   }

   private static float outBack(float t) {
      float c1 = 1.70158F;
      float c3 = c1 + 1.0F;
      return 1.0F + c3 * (float)Math.pow(t - 1.0F, 3.0) + c1 * (float)Math.pow(t - 1.0F, 2.0);
   }

   private static float bell(float t) {
      return (float)Math.sin(Math.PI * clamp01(t));
   }

   private static void around(class_4587 pose, float px, float py, Quaternionf q) {
      pose.method_46416(px, py, 0.0F);
      pose.method_22907(q);
      pose.method_46416(-px, -py, 0.0F);
   }

   private static void animate(class_4587 pose, String type, KnivesModule k, KnifeRenderer.Parts parts) {
      if (k.drawTime >= 0.0F) {
         draw(pose, type, k.drawTime / 1.15F, parts);
      } else if (k.inspectTime >= 0.0F) {
         inspect(pose, type, k.inspectTime / 4.2F, parts);
      } else if (k.idleAnimation.isOn()) {
         float t = k.idleTime;
         pose.method_46416(0.0F, (float)Math.sin(t * 1.6) * 0.14F, (float)Math.sin(t * 0.8) * 0.08F);
         pose.method_22907(class_7833.field_40718.rotationDegrees((float)Math.sin(t * 1.05) * 1.6F));
         pose.method_22907(class_7833.field_40716.rotationDegrees((float)Math.sin(t * 0.7) * 2.2F));
         if (type.equals("Butterfly")) {
            parts.handleA = 4.0F + 4.0F * (float)Math.sin(t * 2.3);
            parts.handleB = 4.0F + 4.0F * (float)Math.sin(t * 2.3 + 1.2);
         }
      }
   }

   private static void draw(class_4587 pose, String type, float p, KnifeRenderer.Parts parts) {
      float rise = outBack(seg(p, 0.0F, 0.42F));
      pose.method_46416(0.0F, -11.0F * (1.0F - rise), -3.0F * (1.0F - rise));
      pose.method_22907(class_7833.field_40718.rotationDegrees(-40.0F * (1.0F - rise)));
      float pop = 0.55F + 0.45F * outBack(seg(p, 0.0F, 0.5F));
      pose.method_22905(pop, pop, pop);
      pose.method_22907(class_7833.field_40714.rotationDegrees(-10.0F * bell(seg(p, 0.82F, 1.0F))));
      parts.shine(seg(p, 0.7F, 1.0F), 0.75F);
      switch (type) {
         case "Butterfly":
            pose.method_22907(class_7833.field_40716.rotationDegrees(-540.0F * (1.0F - inOut(seg(p, 0.0F, 0.8F)))));
            parts.handleA = 180.0F * (1.0F - outCubic(seg(p, 0.05F, 0.5F)));
            parts.handleB = 180.0F * (1.0F - outBack(seg(p, 0.28F, 0.8F)));
            break;
         case "Karambit":
            around(pose, 0.25F, -6.42F, class_7833.field_40718.rotationDegrees(-1080.0F * (1.0F - outCubic(seg(p, 0.0F, 0.85F)))));
            pose.method_22907(class_7833.field_40716.rotationDegrees(35.0F * bell(seg(p, 0.15F, 0.85F))));
            break;
         case "Bayonet":
            float toss = seg(p, 0.1F, 0.8F);
            pose.method_46416(0.0F, 7.0F * bell(toss), 0.0F);
            around(pose, 0.0F, 2.0F, class_7833.field_40714.rotationDegrees(720.0F * (1.0F - inOut(toss))));
            pose.method_22907(class_7833.field_40716.rotationDegrees(180.0F * (1.0F - inOut(toss))));
            break;
         case "Flip":
            parts.flipBlade = 180.0F * (1.0F - outBack(seg(p, 0.18F, 0.45F)));
            pose.method_22907(class_7833.field_40718.rotationDegrees(16.0F * bell(seg(p, 0.18F, 0.45F))));
            pose.method_22907(class_7833.field_40716.rotationDegrees(360.0F * inOut(seg(p, 0.45F, 0.88F))));
            break;
         default:
            pose.method_46416(-4.0F * (1.0F - rise), 0.0F, 0.0F);
            pose.method_22907(class_7833.field_40716.rotationDegrees(-720.0F * (1.0F - inOut(seg(p, 0.0F, 0.85F)))));
            pose.method_22907(class_7833.field_40718.rotationDegrees(25.0F * bell(p)));
      }
   }

   private static void inspect(class_4587 pose, String type, float p, KnifeRenderer.Parts parts) {
      float up = inOut(seg(p, 0.0F, 0.1F)) * (1.0F - inOut(seg(p, 0.9F, 1.0F)));
      pose.method_46416(-1.3F * up, 1.8F * up, 1.6F * up);
      pose.method_22907(class_7833.field_40718.rotationDegrees(-22.0F * up));
      pose.method_22907(class_7833.field_40716.rotationDegrees(68.0F * up));
      pose.method_22907(class_7833.field_40714.rotationDegrees(-8.0F * up));
      pose.method_22907(class_7833.field_40718.rotationDegrees((float)Math.sin(p * 30.0F) * 1.2F * up));
      switch (type) {
         case "Butterfly":
            float a = seg(p, 0.12F, 0.3F);
            pose.method_22907(class_7833.field_40716.rotationDegrees(360.0F * inOut(a)));
            parts.handleB = 180.0F * bell(a);
            float b = seg(p, 0.32F, 0.5F);
            pose.method_46416(0.0F, 6.0F * bell(b), 0.0F);
            around(pose, 0.0F, 3.0F, class_7833.field_40718.rotationDegrees(360.0F * inOut(b)));
            parts.handleA = Math.max(parts.handleA, 90.0F * bell(b));
            float c = seg(p, 0.52F, 0.7F);
            if (c > 0.0F && c < 1.0F) {
               parts.handleA = 180.0F * (float)Math.abs(Math.sin((Math.PI * 2) * c));
               parts.handleB = 180.0F * (float)Math.abs(Math.sin((Math.PI * 2) * clamp01(c - 0.08F) / 0.92F));
               pose.method_22907(class_7833.field_40716.rotationDegrees(720.0F * inOut(c)));
            }

            float d = inOut(seg(p, 0.72F, 0.8F)) * (1.0F - inOut(seg(p, 0.84F, 0.9F)));
            pose.method_22907(class_7833.field_40716.rotationDegrees(-150.0F * d));
            parts.shine(seg(p, 0.74F, 0.86F), 0.85F);
            break;
         case "Karambit":
            around(pose, 0.25F, -6.42F, class_7833.field_40718.rotationDegrees(720.0F * inOut(seg(p, 0.12F, 0.34F))));
            float flip = inOut(seg(p, 0.38F, 0.5F)) * (1.0F - inOut(seg(p, 0.76F, 0.86F)));
            pose.method_22907(class_7833.field_40716.rotationDegrees(180.0F * flip));
            parts.shine(seg(p, 0.42F, 0.56F), 0.85F);
            around(pose, 0.25F, -6.42F, class_7833.field_40718.rotationDegrees(-720.0F * inOut(seg(p, 0.58F, 0.76F))));
            pose.method_22907(class_7833.field_40714.rotationDegrees(-20.0F * bell(seg(p, 0.78F, 0.88F))));
            break;
         case "Bayonet": {
            float look = inOut(seg(p, 0.12F, 0.2F)) * (1.0F - inOut(seg(p, 0.3F, 0.36F)));
            pose.method_22907(class_7833.field_40714.rotationDegrees(-55.0F * look));
            pose.method_22907(class_7833.field_40718.rotationDegrees(-25.0F * look));
            parts.shine(seg(p, 0.14F, 0.3F), 0.9F);
            float toss = seg(p, 0.38F, 0.64F);
            pose.method_46416(0.0F, 7.0F * bell(toss) - 0.8F * bell(seg(p, 0.64F, 0.7F)), 0.0F);
            around(pose, 0.0F, 2.5F, class_7833.field_40714.rotationDegrees(720.0F * inOut(toss)));
            pose.method_22907(class_7833.field_40716.rotationDegrees(360.0F * inOut(toss)));
            float side = inOut(seg(p, 0.7F, 0.78F)) * (1.0F - inOut(seg(p, 0.84F, 0.9F)));
            pose.method_22907(class_7833.field_40716.rotationDegrees(-160.0F * side));
            break;
         }
         case "Flip": {
            float close = inCubic(seg(p, 0.12F, 0.24F));
            float open = outBack(seg(p, 0.46F, 0.56F));
            parts.flipBlade = 172.0F * close * (1.0F - open);
            pose.method_22907(class_7833.field_40716.rotationDegrees(360.0F * inOut(seg(p, 0.26F, 0.44F))));
            pose.method_22907(class_7833.field_40718.rotationDegrees(-22.0F * bell(seg(p, 0.46F, 0.6F))));
            float side = inOut(seg(p, 0.62F, 0.7F)) * (1.0F - inOut(seg(p, 0.82F, 0.9F)));
            pose.method_22907(class_7833.field_40716.rotationDegrees(-150.0F * side));
            parts.shine(seg(p, 0.64F, 0.8F), 0.9F);
            break;
         }
         default:
            float admire = inOut(seg(p, 0.12F, 0.24F)) * (1.0F - inOut(seg(p, 0.34F, 0.42F)));
            pose.method_22907(class_7833.field_40716.rotationDegrees(-150.0F * admire));
            pose.method_22907(class_7833.field_40714.rotationDegrees(-15.0F * admire));
            parts.shine(seg(p, 0.16F, 0.36F), 0.9F);
            float twirl = seg(p, 0.44F, 0.64F);
            pose.method_46416(0.0F, 3.0F * bell(twirl), 0.0F);
            pose.method_22907(class_7833.field_40716.rotationDegrees(720.0F * inOut(twirl)));
            float back = inOut(seg(p, 0.68F, 0.76F)) * (1.0F - inOut(seg(p, 0.82F, 0.9F)));
            pose.method_22907(class_7833.field_40716.rotationDegrees(160.0F * back));
      }
   }

   private static void attack(class_4587 pose, String type, class_1309 entity, KnifeRenderer.Parts parts) {
      float a = entity.method_6055(Projection.partialTick());
      if (!(a <= 0.001F)) {
         float sw = bell(a);
         switch (type) {
            case "Karambit":
               around(pose, 0.25F, -6.42F, class_7833.field_40718.rotationDegrees(-360.0F * outCubic(a)));
               break;
            case "Bayonet":
               pose.method_46416(0.0F, 3.2F * sw, 0.0F);
               pose.method_22907(class_7833.field_40714.rotationDegrees(-14.0F * sw));
               break;
            case "Butterfly":
               parts.handleA = Math.max(parts.handleA, 55.0F * sw);
               pose.method_22907(class_7833.field_40718.rotationDegrees(-22.0F * sw));
               break;
            case "Flip":
               pose.method_22907(class_7833.field_40718.rotationDegrees(30.0F * sw));
               pose.method_22907(class_7833.field_40716.rotationDegrees(-20.0F * sw));
               break;
            default:
               pose.method_22907(class_7833.field_40718.rotationDegrees(34.0F * sw));
               pose.method_22907(class_7833.field_40714.rotationDegrees(-18.0F * sw));
         }
      }
   }

   private static final class Parts {
      float handleA;
      float handleB;
      float flipBlade;
      float shinePos = -1.0F;
      float shineAmt;

      void shine(float progress, float amount) {
         if (!(progress <= 0.0F) && !(progress >= 1.0F)) {
            this.shinePos = -0.15F + progress * 1.3F;
            this.shineAmt = amount * (float)Math.sin(Math.PI * progress);
         }
      }
   }
}
