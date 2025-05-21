package me.flopsterstream.flop.module.combat;
import me.flopsterstream.flop.module.Category;
import me.flopsterstream.flop.module.Module;

public class Triggerbot extends Module {
		public Triggerbot() {
		super("Triggerbot", "Automatically attacks when the crosshair is on an entity", Category.COMBAT, 0);
	}


	

	@Override
	public void setupOptions() {}
	
	private static final double ATTACK_RANGE = 4.5; 
	
	
	public void onupdate() {
		if(this.isToggled() && mc.player != null && mc.level != null) {
			if(mc.crosshairPickEntity != null && mc.crosshairPickEntity.distanceTo(mc.player) <= ATTACK_RANGE) {
				mc.player.attack(mc.crosshairPickEntity);
			}
		}
	}

	

}
