package me.flopsterstream.flop.module.gui;

import me.flopsterstream.flop.module.Module;
import me.flopsterstream.flop.ui.clickgui.ClickGuiMain;

import org.lwjgl.glfw.GLFW;

import me.flopsterstream.flop.module.Category;


public class ClickGui extends Module{
	private boolean isGuiOpen = false;

	public ClickGui() {
		super("ClickGui"," Displays Clickgui", Category.GUI, GLFW.GLFW_KEY_RIGHT_SHIFT);
	}
	@Override
	public void onRender() {
		if(this.isToggled() && !isGuiOpen) {
			openGui();
		

		}
	}
	private void openGui() {
		if (!isGuiOpen && mc.screen == null) {
			mc.setScreen(new ClickGuiMain(this));
			isGuiOpen = true;
			super.onEnable();
		}
	}
	
	public void closeGui() {
		if(isGuiOpen) {
			mc.setScreen(null);
			isGuiOpen = false;
			this.toggle();
			super.onDisable();

		}
	}
	@Override
	public void setupOptions() {
		// TODO Auto-generated method stub
		
	}

}
