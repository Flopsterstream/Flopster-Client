package me.flopsterstream.flop.module.player;
import me.flopsterstream.flop.module.Category;
import me.flopsterstream.flop.module.Module;


public class AutoRespawn extends Module{
	
	public AutoRespawn() {
		super("AutoRespawn", "Automatically respawns when you die", Category.PLAYER, 0);
	}
	
	@Override
	public void onUpdate() {
		if(this.isToggled() && mc.player != null) {
			if(mc.player.isDeadOrDying()) {
				mc.player.respawn();
			}
		}
	}
	@Override
	public void setupOptions() {
		
	}
	


}
