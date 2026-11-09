package dev.byteplus.util;

import dev.byteplus.BytePlus;
import dev.byteplus.config.Config;
import dev.byteplus.module.Module;
import java.util.Arrays;
import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_5250;
import net.minecraft.class_3675.class_306;

public final class Commands {
   public static final String PREFIX = ".";

   private Commands() {
   }

   public static boolean handle(String message) {
      if (message.startsWith(".") && message.length() >= 2 && !message.startsWith("..")) {
         String[] parts = message.substring(".".length()).trim().split("\\s+");
         String cmd = parts[0].toLowerCase();
         String[] args = Arrays.copyOfRange(parts, 1, parts.length);

         try {
            switch (cmd) {
               case "help":
               case "h":
               case "commands":
                  help();
                  break;
               case "toggle":
               case "t":
                  toggle(args);
                  break;
               case "bind":
               case "b":
                  bind(args);
                  break;
               case "friend":
               case "friends":
               case "f":
                  friend(args);
                  break;
               case "config":
               case "profile":
               case "cfg":
                  config(args);
                  break;
               default:
                  error("Unknown command. Type .help");
            }
         } catch (Exception e) {
            error("Something went wrong: " + e.getMessage());
         }

         return true;
      } else {
         return false;
      }
   }

   private static void help() {
      info("Commands:");
      line(".t <module>", "toggle a module");
      line(".bind <module> <key|none>", "set a keybind");
      line(".friend add|remove|list|clear [name]", "manage friends");
      line(".config save|load|delete|list [name]", "config profiles");
   }

   private static void toggle(String[] args) {
      if (args.length == 0) {
         error("Usage: .t <module>");
      } else {
         Module m = module(String.join(" ", args));
         if (m == null) {
            error("No module called \"" + String.join(" ", args) + "\"");
         } else {
            m.toggle();
            if (m.isToggleable()) {
               info(m.getName() + (m.isEnabled() ? " enabled" : " disabled"));
            }
         }
      }
   }

   private static void bind(String[] args) {
      if (args.length < 2) {
         error("Usage: .bind <module> <key|none>");
      } else {
         String keyName = args[args.length - 1].toLowerCase();
         String name = String.join(" ", Arrays.copyOf(args, args.length - 1));
         Module m = module(name);
         if (m == null) {
            error("No module called \"" + name + "\"");
         } else {
            if (!keyName.equals("none") && !keyName.equals("clear")) {
               class_306 key = class_3675.method_15981("key.keyboard." + keyName);
               if (key == class_3675.field_16237 || key.method_1444() < 0) {
                  error("Unknown key \"" + keyName + "\"");
                  return;
               }

               m.keybind.set(key.method_1444());
               info("Bound " + m.getName() + " to " + m.keybind.keyName());
            } else {
               m.keybind.set(-1);
               info("Unbound " + m.getName());
            }

            Config.save();
         }
      }
   }

   private static void friend(String[] args) {
      String sub = args.length > 0 ? args[0].toLowerCase() : "list";
      switch (sub) {
         case "add":
            if (args.length < 2) {
               error("Usage: .friend add <name>");
               return;
            }

            info(Friends.add(args[1]) ? "Added " + args[1] + " as a friend" : args[1] + " is already a friend");
            break;
         case "remove":
         case "del":
         case "delete":
            if (args.length < 2) {
               error("Usage: .friend remove <name>");
               return;
            }

            info(Friends.remove(args[1]) ? "Removed " + args[1] : args[1] + " isn't a friend");
            break;
         case "clear":
            Friends.clear();
            info("Cleared your friends list");
            break;
         default:
            List<String> list = Friends.list();
            info(
               list.isEmpty() ? "No friends yet. Middle-click a player or use .friend add <name>" : "Friends (" + list.size() + "): " + String.join(", ", list)
            );
            return;
      }

      Config.save();
   }

   private static void config(String[] args) {
      String sub = args.length > 0 ? args[0].toLowerCase() : "list";
      String name = args.length > 1 ? args[1] : null;
      switch (sub) {
         case "save":
            if (name == null) {
               error("Usage: .config save <name>");
               return;
            }

            info(Config.saveProfile(name) ? "Saved profile \"" + name + "\"" : "Couldn't save \"" + name + "\"");
            break;
         case "load":
            if (name == null) {
               error("Usage: .config load <name>");
               return;
            }

            info(Config.loadProfile(name) ? "Loaded profile \"" + name + "\"" : "No profile called \"" + name + "\"");
            break;
         case "delete":
         case "remove":
            if (name == null) {
               error("Usage: .config delete <name>");
               return;
            }

            info(Config.deleteProfile(name) ? "Deleted \"" + name + "\"" : "No profile called \"" + name + "\"");
            break;
         default:
            List<String> list = Config.profiles();
            info(list.isEmpty() ? "No profiles yet. Use .config save <name>" : "Profiles: " + String.join(", ", list));
      }
   }

   private static Module module(String name) {
      String n = norm(name);

      for (Module m : BytePlus.modules().all()) {
         if (norm(m.getName()).equals(n)) {
            return m;
         }
      }

      for (Module m : BytePlus.modules().all()) {
         if (norm(m.getName()).startsWith(n)) {
            return m;
         }
      }

      return null;
   }

   private static String norm(String s) {
      return s.toLowerCase().replaceAll("[^a-z0-9]", "");
   }

   private static class_5250 tag() {
      return class_2561.method_43470("Byte+ ").method_54663(Theme.ACCENT & 16777215).method_10852(class_2561.method_43470("» ").method_54663(7829367));
   }

   private static void send(class_2561 c) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1705 != null) {
         mc.field_1705.method_1743().method_1812(c);
      }
   }

   private static void info(String s) {
      send(tag().method_10852(class_2561.method_43470(s).method_54663(15263976)));
   }

   private static void error(String s) {
      send(tag().method_10852(class_2561.method_43470(s).method_54663(16739179)));
   }

   private static void line(String cmd, String what) {
      send(class_2561.method_43470("  " + cmd).method_54663(Theme.ACCENT & 16777215).method_10852(class_2561.method_43470("  " + what).method_54663(10132122)));
   }
}
