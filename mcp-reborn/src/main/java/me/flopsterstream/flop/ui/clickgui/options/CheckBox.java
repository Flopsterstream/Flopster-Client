package me.flopsterstream.flop.ui.clickgui.options;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class CheckBox {
	 
	private String label;
	private boolean checked;
	private int x, y;
 
	public CheckBox(String label, boolean initialState, int x, int y) {
		this.label = label;
		this.checked = initialState;
		this.x = x;
		this.y = y;
	}
 
	public void render(GuiGraphics graphics) {
		int boxSize = 10;
		int padding = 2;
		int labelWidth = Minecraft.getInstance().font.width(label);
		int boxWidth = boxSize + 10 + labelWidth;
 
		//Draws background and black box
		graphics.fill(x - padding, y - padding, x + boxWidth + padding, y + boxSize + padding, 0xFF4F4F4F); // Gray background
        graphics.fill(x - padding, y - padding, x + boxWidth + padding, y - padding + 1, 0xFF000000); // Top border
        graphics.fill(x - padding, y + boxSize + padding - 1, x + boxWidth + padding, y + boxSize + padding, 0xFF000000); // Bottom border
        graphics.fill(x - padding, y - padding, x - padding + 1, y + boxSize + padding, 0xFF000000); // Left border
        graphics.fill(x + boxWidth + padding - 1, y - padding, x + boxWidth + padding, y + boxSize + padding, 0xFF000000); // Right border
 
		//Draws checkbox
		graphics.fill(x, y, x + boxSize, y + boxSize, 0xFF4F4F4F); //Gray Background
		graphics.fill(x + 1, y + 1, x + boxSize - 1, y + boxSize - 1, checked ? 0xFF00FF00 : 0xFFFFFFFF); // Checkmark or white square
 
		//Label
		graphics.drawString(Minecraft.getInstance().font, label, x + boxSize + 5, y + 1, 0xFFFFFFFF);
	}
 
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int boxSize = 10;
		if (mouseX >= x && mouseX <= x + boxSize && mouseY >= y && mouseY <= y + boxSize && button == 0) {
			this.checked = !this.checked;
			return true;
		}
		return false;
	}
 
	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
 
	public String getLabel() {
		return label;
	}
 
	public boolean isChecked() {
		return checked;
	}
 
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
 
}