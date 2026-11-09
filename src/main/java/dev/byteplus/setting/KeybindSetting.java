package dev.byteplus.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.lwjgl.glfw.GLFW;

public class KeybindSetting extends Setting<Integer> {
   public static final int NONE = -1;

   public KeybindSetting(String name, int defaultKey) {
      super(name, defaultKey);
   }

   public int getKey() {
      return this.value;
   }

   public String keyName() {
      if (this.value == -1) {
         return "NONE";
      }

      String n = GLFW.glfwGetKeyName(this.value, 0);
      if (n != null) {
         return n.toUpperCase();
      }

      return switch (this.value) {
         case 32 -> "SPACE";
         case 258 -> "TAB";
         case 260 -> "INSERT";
         case 261 -> "DELETE";
         case 262 -> "RIGHT";
         case 263 -> "LEFT";
         case 264 -> "DOWN";
         case 265 -> "UP";
         case 266 -> "PGUP";
         case 267 -> "PGDN";
         case 268 -> "HOME";
         case 269 -> "END";
         case 280 -> "CAPS";
         case 340 -> "LSHIFT";
         case 341 -> "LCTRL";
         case 342 -> "LALT";
         case 344 -> "RSHIFT";
         case 345 -> "RCTRL";
         case 346 -> "RALT";
         default -> this.value >= 290 && this.value <= 314 ? "F" + (this.value - 290 + 1) : "KEY " + this.value;
      };
   }

   @Override
   public JsonElement save() {
      return new JsonPrimitive(this.value);
   }

   @Override
   public void load(JsonElement e) {
      if (e.isJsonPrimitive()) {
         this.value = e.getAsInt();
      }
   }
}
