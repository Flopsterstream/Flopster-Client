package me.flopsterstream.flop.module;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;

public class KeyManager {

	private final Minecraft mc = Minecraft.getInstance();
	private boolean[] keysPressed = new boolean[GLFW.GLFW_KEY_LAST];

	public void checkKeys(ModuleManager moduleManager) {
		if (RenderSystem.isOnRenderThread()) {

			long windowhandle = mc.getWindow().getWindow();

			for (Module module : moduleManager.getAllModules()) {
				int keyCode = module.getKeyCode();
				if (keyCode > 0 && GLFW.glfwGetKey(windowhandle, keyCode) == GLFW.GLFW_PRESS) {
					if (!keysPressed[keyCode]) {
						module.toggle();
						keysPressed[keyCode] = true;

					}

				} else {
					keysPressed[keyCode] = false;
				}
			}
		}else {
			mc.execute(() -> checkKeys(moduleManager));
		}
	}
}
