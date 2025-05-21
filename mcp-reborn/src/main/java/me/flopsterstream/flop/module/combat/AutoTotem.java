package me.flopsterstream.flop.module.combat;
import me.flopsterstream.flop.module.Category;
import me.flopsterstream.flop.module.Module;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;

public class AutoTotem extends Module{
	
	public AutoTotem() {
		super("AutoTotem", "Automaticaly puts toems in your offhand", Category.COMBAT, 0);
	}
	
	@Override
	public void onUpdate() {
	    if (this.isToggled() && mc.player != null) {
	        // Check if the offhand is empty
	        if (mc.player.getOffhandItem().isEmpty()) {
	            // Iterate through the player's inventory to find a Totem of Undying
	            for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
	                if (mc.player.getInventory().getItem(i).getItem() == Items.TOTEM_OF_UNDYING) {
	                    // Simulate inventory click to pick up the totem
	                    mc.gameMode.handleInventoryMouseClick(
	                        mc.player.containerMenu.containerId, // Container ID
	                        i,                                  // Slot index of the totem
	                        0,                                  // Mouse button (left click)
	                        ClickType.PICKUP,                   // Click type
	                        mc.player                           // Player
	                    );

	                    // Simulate inventory click to place the totem in the offhand slot (slot 45)
	                    mc.gameMode.handleInventoryMouseClick(
	                        mc.player.containerMenu.containerId, // Container ID
	                        45,                                  // Offhand slot index
	                        0,                                   // Mouse button (left click)
	                        ClickType.PICKUP,                    // Click type
	                        mc.player                            // Player
	                    );

	                    break; // Exit the loop after moving the totem
	                }
	            }
	        }
	    }
	}

	@Override
	public void setupOptions() {

		
	}

}
