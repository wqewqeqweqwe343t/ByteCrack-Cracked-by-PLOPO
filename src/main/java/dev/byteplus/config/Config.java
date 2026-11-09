package dev.byteplus.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.byteplus.BytePlus;
import dev.byteplus.gui.ClickGui;
import dev.byteplus.module.Module;
import dev.byteplus.setting.Setting;
import dev.byteplus.util.Friends;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;

public final class Config {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Path DIR = FabricLoader.getInstance().getGameDir().resolve("byteplus");
   private static final Path FILE = DIR.resolve("config.json");
   private static final Path PROFILES = DIR.resolve("profiles");

   private Config() {
   }

   public static void save() {
      write(FILE, snapshot());
   }

   public static void load() {
      JsonObject root = read(FILE);
      if (root != null) {
         apply(root);
      }
   }

   private static Path profile(String name) {
      return PROFILES.resolve(name.replaceAll("[^A-Za-z0-9_-]", "_") + ".json");
   }

   public static boolean saveProfile(String name) {
      return write(profile(name), snapshot());
   }

   public static boolean loadProfile(String name) {
      JsonObject root = read(profile(name));
      if (root == null) {
         return false;
      }

      apply(root);
      save();
      return true;
   }

   public static boolean deleteProfile(String name) {
      try {
         return Files.deleteIfExists(profile(name));
      } catch (IOException e) {
         return false;
      }
   }

   public static List<String> profiles() {
      List<String> out = new ArrayList<>();
      if (!Files.isDirectory(PROFILES)) {
         return out;
      }

      try (Stream<Path> s = Files.list(PROFILES)) {
         s.filter(p -> p.toString().endsWith(".json")).forEach(p -> out.add(p.getFileName().toString().replace(".json", "")));
      } catch (IOException var6) {
      }

      return out;
   }

   private static JsonObject snapshot() {
      JsonObject root = new JsonObject();
      JsonObject mods = new JsonObject();

      for (Module m : BytePlus.modules().all()) {
         JsonObject o = new JsonObject();
         o.addProperty("enabled", m.isEnabled());
         o.add("key", m.keybind.save());
         JsonObject settings = new JsonObject();

         for (Setting<?> s : m.getSettings()) {
            JsonElement e = s.save();
            if (!e.isJsonNull()) {
               settings.add(s.getName(), e);
            }
         }

         o.add("settings", settings);
         mods.add(m.getName(), o);
      }

      root.add("modules", mods);
      root.add("friends", Friends.save());
      root.add("gui", ClickGui.INSTANCE.saveLayout());
      return root;
   }

   private static void apply(JsonObject root) {
      try {
         if (root.has("modules")) {
            JsonObject mods = root.getAsJsonObject("modules");

            for (Module m : BytePlus.modules().all()) {
               if (mods.has(m.getName())) {
                  JsonObject o = mods.getAsJsonObject(m.getName());
                  if (o.has("key")) {
                     m.keybind.load(o.get("key"));
                  }

                  if (o.has("settings")) {
                     JsonObject settings = o.getAsJsonObject("settings");

                     for (Setting<?> s : m.getSettings()) {
                        if (settings.has(s.getName())) {
                           s.load(settings.get(s.getName()));
                        }
                     }
                  }

                  if (o.has("enabled") && m.isToggleable() && m.persistEnabled()) {
                     m.setEnabled(o.get("enabled").getAsBoolean());
                  }
               }
            }
         }

         if (root.has("friends")) {
            Friends.load(root.get("friends"));
         }

         if (root.has("gui")) {
            ClickGui.INSTANCE.loadLayout(root.getAsJsonObject("gui"));
         }
      } catch (RuntimeException e) {
         BytePlus.LOGGER.error("Failed to apply config", e);
      }
   }

   private static boolean write(Path file, JsonObject root) {
      try {
         Files.createDirectories(file.getParent());
         Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
         return true;
      } catch (Exception e) {
         BytePlus.LOGGER.error("Failed to save {}", file, e);
         return false;
      }
   }

   private static JsonObject read(Path file) {
      if (!Files.exists(file)) {
         return null;
      }

      try {
         return JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
      } catch (IOException | RuntimeException e) {
         BytePlus.LOGGER.error("Failed to read {}", file, e);
         return null;
      }
   }
}
