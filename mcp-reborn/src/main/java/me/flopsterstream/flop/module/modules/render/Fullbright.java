package me.flopsterstream.flop.module.modules.render;

import me.flopsterstream.flop.module.Category;
import me.flopsterstream.flop.module.Module;

public class Fullbright extends Module {

    private static boolean enabled = false;

    public Fullbright() {
        super("Fullbright", "Removes gamma limit for bright vision", Category.RENDER, 0);
    }

    public static boolean isEnabledStatic() {
        return enabled;
    }

    @Override
    public void onEnable() {
        enabled = true;
    }

    @Override
    public void onDisable() {
        enabled = false;
        // Don't set gamma directly here!
        // The mixin will reset it automatically.
    }

    @Override
    public void onUpdate() {
        // No need to set gamma here either.
        // The mixin forcibly updates it when options.gamma() is accessed.
    }

    @Override
    public void setupOptions() {
        // no options needed
    }
}
