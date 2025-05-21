package me.flopsterstream.flop.ui;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import me.flopsterstream.flop.Flop;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class InGameOverlay {
	
	private final Minecraft mc = Minecraft.getInstance();
	
	public InGameOverlay() {}
	
	public void render(GuiGraphics guiGraphics) {
		if(mc.player != null) {
			
			
			
			//Renders Client Name
			String clientName = "Flopster Client / ";
			guiGraphics.drawString(mc.font, clientName, 10, 5, 0xAdd8e6);
			
			int clientNameWidth = mc.font.width(clientName);
			//Render FPS
			int fps = mc.getFps();
			String fpsText = "FPS: " + fps;
			guiGraphics.drawString(mc.font, fpsText, 10 + clientNameWidth, 5, 0xAdd8e6);
			
			//Claculate FPS Width

			int fpsTextWidth = mc.font.width(fpsText);
			
			//Calculate the total width and height
			int totalWidth = clientNameWidth + fpsTextWidth + 5;
			int totalHeight = mc.font.lineHeight + 2;
			
			//rect cords
			int x1 = 10;
			int y1 = 5;
			int x2 = x1 + totalWidth;
			int y2 =y1 + totalHeight;
			
			guiGraphics.fill(x1 - 5, y1 - 3, x2, y2, 0x99000000);
			
			renderEnableMods(guiGraphics);
			
			
			
			
			
		}
	}
	
	private void renderEnableMods(GuiGraphics guigraphics) {
		List<me.flopsterstream.flop.module.Module> enabledMods = Flop.instance.modManager.getEnabledModules();
		
		int screenWidth = mc.getWindow().getGuiScaledWidth();
		int yOffset = 5;
		
		
		for(int i = 0; i < enabledMods.size(); i++) {
			me.flopsterstream.flop.module.Module mod = enabledMods.get(i);
			String modName = mod.getName();
			
			if(mod.getName().equalsIgnoreCase("ClickGui")) {
				continue; // Skip ClickGui module
			}
			
			int modNameWidth = mc.font.width(modName);
			
			guigraphics.drawString(mc.font, modName, screenWidth - modNameWidth -15, yOffset, 0xAdd8e6);
			
			yOffset += mc.font.lineHeight +2;
			
		}
		

		
	}
	
	

}
