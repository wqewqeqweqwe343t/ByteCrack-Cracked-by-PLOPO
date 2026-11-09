package dev.byteplus.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.byteplus.BytePlus;
import dev.byteplus.module.Module;
import dev.byteplus.module.modules.misc.FriendsModule;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.class_1657;

public final class Friends {
   private static final Set<String> names = new LinkedHashSet<>();

   private Friends() {
   }

   public static boolean isFriend(String name) {
      return name != null && names.contains(name.toLowerCase());
   }

   public static boolean isFriend(class_1657 p) {
      return p != null && isFriend(p.method_7334().name());
   }

   public static boolean add(String name) {
      return names.add(name.toLowerCase());
   }

   public static boolean remove(String name) {
      return names.remove(name.toLowerCase());
   }

   public static void clear() {
      names.clear();
   }

   public static List<String> list() {
      return new ArrayList<>(names);
   }

   public static boolean toggle(String name) {
      if (remove(name)) {
         return false;
      }

      add(name);
      return true;
   }

   public static int color() {
      if (BytePlus.modules() != null) {
         for (Module m : BytePlus.modules().all()) {
            if (m instanceof FriendsModule f) {
               return f.color.argb();
            }
         }
      }

      return -11800454;
   }

   public static JsonArray save() {
      JsonArray a = new JsonArray();

      for (String n : names) {
         a.add(n);
      }

      return a;
   }

   public static void load(JsonElement e) {
      names.clear();
      if (e != null && e.isJsonArray()) {
         for (JsonElement n : e.getAsJsonArray()) {
            names.add(n.getAsString().toLowerCase());
         }
      }
   }
}
