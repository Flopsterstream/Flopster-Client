package me.flopsterstream.flop.module.modules.movement;
import me.flopsterstream.flop.module.Category;
import me.flopsterstream.flop.module.Module;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class NoFall extends Module{
	
	public NoFall() {
		super("NoFall", "Prevents fall damage", Category.MOVEMENT, 0);
	}
	
	private boolean wasFlying = true;
	
	
	@Override
	public void onUpdate() {
		if(this.isToggled() && mc.player != null) {
			if(mc.player.fallDistance > 0.0F) {
				mc.player.fallDistance = 0.0F;
				
				mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true, mc.player.onGround()));	
	

			}
		}
	}
	
	@Override
	public void setupOptions() {
		
	}

}
