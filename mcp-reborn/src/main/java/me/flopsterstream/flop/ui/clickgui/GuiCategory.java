package me.flopsterstream.flop.ui.clickgui;

import java.util.ArrayList;
import java.util.List;

import me.flopsterstream.flop.Flop;
import me.flopsterstream.flop.module.Category;
import me.flopsterstream.flop.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import me.flopsterstream.flop.module.Module;
public class GuiCategory {
	
	private final Category category;
	private int x, y;
	private List<ModuleButton> moduleButtons = new ArrayList<>();
	private final int categorytTitleHeight = 20;
	private boolean expanded = false;
	
	private boolean isDragging = false;
	private int dragOffsetX, dragOffsetY;
	
	private Module selectedModule = null;
	private boolean showOptions= false;
	
	public GuiCategory(Category category, int x, int y) {
		this.category = category;
		this.x = x;
		this.y = y;

	
	
		//Starts module buttons below the category title
		int buttonY = y + categorytTitleHeight + 5;
		
		//renders Module
		for(Module module : ModuleManager.getModulesByCategory(category)) {
			moduleButtons.add(new ModuleButton(module, x, buttonY));
	        buttonY += 20;
		}
	}
	
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		if (isDragging) {
			x = mouseX - dragOffsetX;
			y = mouseY - dragOffsetY;
		}
		
		int categoryTextWidth = Minecraft.getInstance().font.width(category.name());
		int backgroundColor = expanded ? 0xFFFFFFFF : 0xFF000000;
		
		graphics.fill(x, y, x + categoryTextWidth + 8, y + 13, backgroundColor);
		graphics.drawString(Minecraft.getInstance().font, category.name(), x + 5, y + 3, 0xAdd8e6);
		
		if(expanded) {
			int moduleY = y + 17;
			int offsetX = 5;
			for(me.flopsterstream.flop.module.Module module : Flop.instance.modManager.getModulesByCategory(category)) {
				int moduleTextWidth = Minecraft.getInstance().font.width(module.getName());
				
				
				//Draw gray background
				graphics.fill(x + 5 - offsetX, moduleY, x + moduleTextWidth + 10 - offsetX, moduleY + 15, 0xFF4F4F4F); // Gray
				
				graphics.fill(x + 5 - offsetX, moduleY, x + moduleTextWidth + 10 - offsetX, moduleY + 1, 0xFF000000); //Top border
				graphics.fill(x + 5 - offsetX, moduleY + 14, x + moduleTextWidth + 10 - offsetX, moduleY + 15, 0xFF000000); //Bottom border
				graphics.fill(x + 5 - offsetX, moduleY, x + 6 - offsetX, moduleY + 15, 0xFF000000); //Left border
				graphics.fill(x + moduleTextWidth + 10 - offsetX, moduleY, x + moduleTextWidth + 11 - offsetX, moduleY + 15, 0xFF000000); //Right border
				
				int textColor = module.isToggled() ? 0xFFFF0000 : 0xFFFFFFFF;
				
				//Draw module names inside of box
				graphics.drawString(Minecraft.getInstance().font, module.getName(), x + 3, moduleY + 3, textColor);
				
				//Draw Module Description
				if(mouseX >= x + 5 - offsetX && mouseX <= x + moduleTextWidth + 10 - offsetX && mouseY >= moduleY && mouseY <= moduleY + 15) {
					String description = module.getDescription();
					int descriptionWidth = Minecraft.getInstance().font.width(description);
					
					
					
					graphics.fill(mouseX, mouseY, mouseX + descriptionWidth + 8, mouseY + 12, 0xFF000000);
					graphics.drawString(Minecraft.getInstance().font, description, mouseX + 4, mouseY + 2, 0xFFFFFFFF);
					
					
				

				}
				
				moduleY += 13;
				
				if(module == selectedModule && showOptions ) {
					module.renderOptions(graphics, x + 20 + offsetX, moduleY -11);
					
					
				}
				
			}
		}
		
		
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int categoryTextWidth = Minecraft.getInstance().font.width(category.name());
		if (mouseX >= x && mouseX <= x + categoryTextWidth + 10 && mouseY >= y && mouseY <= y + 15) {
			if(button == 0) {
				isDragging = true;
				dragOffsetX = (int)(mouseX -x);
				dragOffsetY = (int)(mouseY -y);
				
			}else if(button == 1) {
				toggleExpanded();
			}

		}
		
		if(expanded) {
			int moduleY = y + 17;
			int offsetX = 5;
			for(Module module : ModuleManager.getModulesByCategory(category)) {
				int moduleTextWidth = Minecraft.getInstance().font.width(module.getName());
				
				if(mouseX >= x && mouseX <= x + moduleTextWidth + 5 && mouseY >= moduleY && mouseY <= moduleY + 15) {
					if(button == 0) {
						module.toggle();
					}else if(button == 1) {
						selectedModule = module;
						showOptions = !showOptions;
					}
					return true;
				}
				
				moduleY += 13;
			}
		}
		
		if(selectedModule != null && selectedModule.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}
		return false;
	}
	
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if(button == 0) {
			isDragging = false;
			return true;
			
		}
		return false;
		

	}
	
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	
	public Category getCategory() {
		return category;
	}
	
	public boolean isExpanded() {
		return expanded;
	}
	
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}
	
	public void toggleExpanded() {
		this.expanded = !this.expanded;
		
		ClickGuiMain.expandedStates.put(this.category, this.expanded);
	}

}