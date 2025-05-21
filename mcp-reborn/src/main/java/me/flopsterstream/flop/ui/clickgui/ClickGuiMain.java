package me.flopsterstream.flop.ui.clickgui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import me.flopsterstream.flop.module.Category;
import me.flopsterstream.flop.module.gui.ClickGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClickGuiMain extends Screen{
	
	private final ClickGui clickGui;
	private final Minecraft mc = Minecraft.getInstance();
	private List<GuiCategory> categories = new ArrayList<>();
	private final int categoryWidth = 50;
	private final int categoryHeight = 20;
	private final int categorySpacing = 10;
	
	public static Map<Category, int[]> categoryPositions = new HashMap<>();
	public static Map<Category, Boolean> expandedStates = new HashMap<>();
	
	public ClickGuiMain(ClickGui clickGui) {
		super(Component.literal("ClickGui"));
		this.clickGui = clickGui;
		
		int screenWidth = mc.getWindow().getGuiScaledWidth();
		int xOffset = 20;
		int yOffset = 40;
		
		for (Category category : Category.values()) {
			if(category == Category.GUI) {
				continue;

			}
			int[] positions = categoryPositions.getOrDefault(category, new int[] {xOffset, yOffset});
			int savedX = positions[0];
			int savedY = positions[1];
			
			boolean isExpanded = expandedStates.getOrDefault(category, false);
			
			GuiCategory guiCategory = new GuiCategory(category, savedX, savedY);
			guiCategory.setExpanded(isExpanded);
			categories.add(guiCategory);
			
			xOffset += categoryWidth + categorySpacing;
			
			

		}
		
		
	}
	
	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		graphics.fill(0, 0, width, height, 0x00000000);
		
		for (GuiCategory category : categories) {
			category.render(graphics, mouseX, mouseY, delta);
		}
		
		super.render(graphics, mouseX, mouseY, delta);
		

	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		for (GuiCategory category : categories) {
			if (category.mouseClicked(mouseX, mouseY, button)) {

				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		for (GuiCategory category : categories) {
			if (category.mouseReleased(mouseX, mouseY, button)) {
				categoryPositions.put(category.getCategory(), new int[] {category.getX(), category.getY()});
				
				expandedStates.put(category.getCategory(), category.isExpanded());
			}
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			clickGui.closeGui();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean isPauseScreen() {
		return false;
	}

}
