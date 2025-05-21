package me.flopsterstream.flop.module.modules.movement;

import org.lwjgl.glfw.GLFW;

import me.flopsterstream.flop.module.Category;
import me.flopsterstream.flop.module.Module;


public class Flight extends Module{

	public Flight() {
		super("Flight", "Allows you to fly in survival", Category.MOVEMENT, GLFW.GLFW_KEY_F);
	}
	
	@Override
	public void onEnable() {
		if(mc.player != null) {
		mc.player.getAbilities().flying = true;
		mc.player.getAbilities().mayfly = true;
		
		}
		super.onEnable();
	}
	
	@Override
	public void onDisable() {
		if(mc.player != null) {
		mc.player.getAbilities().flying = false;
		mc.player.getAbilities().mayfly = false;
		
		}
		super.onDisable();
	}
	
	@Override
	public void onUpdate() {
		if (this.isToggled() && mc.player != null) {
			mc.player.getAbilities().mayfly = true;
		}
	}

	@Override
	public void setupOptions() {
		// TODO Auto-generated method stub
		
	}

}
