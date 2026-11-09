package dev.byteplus.module.modules.misc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.yggdrasil.ProfileResult;
import dev.byteplus.BytePlus;
import dev.byteplus.module.Category;
import dev.byteplus.module.Module;
import dev.byteplus.setting.StringSetting;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_8685;

public class SkinProtectModule extends Module {
   public final StringSetting username = this.add(new StringSetting("Username", "Notch", 16));
   private static volatile class_8685 skin;
   private String loadedFor = "";
   private boolean loading;

   public SkinProtectModule() {
      super("Skin Protect", "Wears another Minecraft user's skin on your end.", Category.MISC);
   }

   public static class_8685 activeSkin() {
      SkinProtectModule m = BytePlus.modules() == null ? null : BytePlus.modules().skinProtect;
      return m != null && m.isEnabled() ? skin : null;
   }

   @Override
   protected void onEnable() {
      this.loadedFor = "";
   }

   @Override
   protected void onDisable() {
      skin = null;
      this.loadedFor = "";
   }

   @Override
   public void onTickAlways() {
      String name = this.username.get().trim();
      if (!this.loading && !name.isEmpty() && !name.equalsIgnoreCase(this.loadedFor)) {
         this.loading = true;
         this.loadedFor = name;
         CompletableFuture.<UUID>supplyAsync(() -> lookupUuid(name)).thenAccept(uuid -> {
            if (uuid == null) {
               this.loading = false;
               mc.execute(() -> this.info("Couldn't find " + name));
            } else {
               try {
                  ProfileResult result = mc.method_73361().comp_837().fetchProfile(uuid, true);
                  if (result == null) {
                     this.loading = false;
                     return;
                  }

                  mc.execute(() -> mc.method_1582().method_52863(result.profile()).thenAccept(opt -> {
                     opt.ifPresent(s -> skin = s);
                     this.loading = false;
                  }));
               } catch (Exception e) {
                  this.loading = false;
               }
            }
         });
      }
   }

   private static UUID lookupUuid(String name) {
      try {
         HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).build();
         HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.mojang.com/users/profiles/minecraft/" + name))
            .timeout(Duration.ofSeconds(8L))
            .GET()
            .build();
         HttpResponse<String> res = client.send(req, BodyHandlers.ofString());
         if (res.statusCode() != 200) {
            return null;
         }

         JsonObject o = JsonParser.parseString(res.body()).getAsJsonObject();
         String id = o.get("id").getAsString();
         return UUID.fromString(id.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
      } catch (Exception e) {
         return null;
      }
   }
}
