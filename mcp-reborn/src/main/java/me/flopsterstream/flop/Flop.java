package me.flopsterstream.flop;

import me.flopsterstream.flop.module.KeyManager;
import me.flopsterstream.flop.module.ModuleManager;
import me.flopsterstream.flop.ui.InGameOverlay;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;


public class Flop {

	public static String name = "Flop", creator = "Flopsterstream";

	public static Flop instance = new Flop();


	public static ModuleManager modManager;
	public static KeyManager keyManager;
	public static InGameOverlay ingameoverlay;

	public static void startClient() {



		modManager = new ModuleManager();
		keyManager = new KeyManager();
		ingameoverlay = new InGameOverlay();

		System.out.println("Starting Flop Client!");
	}


	// Runs code every game tick
	public static void onTick() {
		modManager.onTick(keyManager);
	}

	public void onRender(GuiGraphics guiGraphics) {
		ingameoverlay.render(guiGraphics);
	}
}
