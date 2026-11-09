package dev.byteplus.module;

public enum Category {
   COMBAT("Combat"),
   ANTICHEAT("Anti-Cheat"),
   MISC("Misc"),
   DONUT("Donut"),
   BASEFINDING("BaseFinding"),
   RENDER("Render"),
   VISUALS("Visuals"),
   BYTEPLUS("Cracked By PLOPO");

   public final String displayName;

   Category(String displayName) {
      this.displayName = displayName;
   }
}
