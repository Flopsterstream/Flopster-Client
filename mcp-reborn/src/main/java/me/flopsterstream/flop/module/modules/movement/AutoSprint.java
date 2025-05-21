package me.flopsterstream.flop.module.modules.movement;

import me.flopsterstream.flop.module.Category;
import me.flopsterstream.flop.module.Module;


public class AutoSprint extends Module{
	public AutoSprint() {
		super("AutoSprint", "Automatically sprints when you move", Category.MOVEMENT, 0);
	}
	
	@Override
	public void onUpdate() {
		if(this.isToggled() && mc.player != null) {
			if(mc.player.zza > 0 && !mc.player.isSprinting()) {
				mc.player.setSprinting(true);
			}
		}

	}
	
	@Override
	public void setupOptions() {
		
	}
	
	

}
