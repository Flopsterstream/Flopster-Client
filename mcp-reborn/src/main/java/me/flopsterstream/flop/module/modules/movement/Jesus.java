package me.flopsterstream.flop.module.modules.movement;
import me.flopsterstream.flop.module.Category;
	import me.flopsterstream.flop.module.Module;
import net.minecraft.world.level.block.Blocks;

public class Jesus extends Module {
	
	public Jesus() {
		super("Jesus", "Allows you to walk on water", Category.MOVEMENT, 0);
	}
	
	@Override
	public void onUpdate() {
		if(this.isToggled() && mc.player != null) {
			if(mc.level.getBlockState(mc.player.blockPosition().below()).getBlock() == Blocks.WATER || mc.level.getBlockState(mc.player.blockPosition().below()).getBlock() == Blocks.SEAGRASS || mc.level.getBlockState(mc.player.blockPosition().below()).getBlock() == Blocks.KELP || mc.level.getBlockState(mc.player.blockPosition().below()).getBlock() == Blocks.TALL_SEAGRASS) {
				
				if(mc.player.getDeltaMovement().y < 0) {
					mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, 0, mc.player.getDeltaMovement().z);
					
					mc.player.setPos(mc.player.getX(), Math.floor(mc.player.getY()), mc.player.getZ());
					
					mc.player.setOnGround(true);
				}

			}

		}
	}
	
	
	
	
	
	@Override
	public void setupOptions() {

		
	}
	


}
