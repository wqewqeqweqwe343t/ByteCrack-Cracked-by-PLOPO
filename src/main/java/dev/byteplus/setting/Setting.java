package dev.byteplus.setting;

import com.google.gson.JsonElement;
import java.util.function.BooleanSupplier;

public abstract class Setting<T> {
   private final String name;
   protected T value;
   private final T defaultValue;
   private BooleanSupplier visibility = () -> true;

   protected Setting(String name, T defaultValue) {
      this.name = name;
      this.value = defaultValue;
      this.defaultValue = defaultValue;
   }

   public String getName() {
      return this.name;
   }

   public T get() {
      return this.value;
   }

   public void set(T value) {
      this.value = value;
   }

   public T getDefault() {
      return this.defaultValue;
   }

   public <S extends Setting<T>> S visibleWhen(BooleanSupplier supplier) {
      this.visibility = supplier;
      return (S)this;
   }

   public boolean isVisible() {
      return this.visibility.getAsBoolean();
   }

   public abstract JsonElement save();

   public abstract void load(JsonElement var1);
}
