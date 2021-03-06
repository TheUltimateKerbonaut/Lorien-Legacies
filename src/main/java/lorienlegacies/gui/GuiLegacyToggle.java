package lorienlegacies.gui;

import java.util.Map;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;

import lorienlegacies.core.LorienLegacies;
import lorienlegacies.keybinds.ModKeybinds;
import lorienlegacies.network.NetworkHandler;
import lorienlegacies.network.mesages.MessageToggleLegacy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

public class GuiLegacyToggle extends Screen
{	
	private static final int TOGGLED_COLOUR = 0xff0039e6;
	private static final int NOT_TOGGLED_COLOUR = 0xffbbbbbb;
	private static final int HIGHLIGHTED_TOGGLED_COLOUR = 0xff4d79ff;
	private static final int HIGHLIGHTED_NOT_TOGGLED_COLOUR = 0xffffffff;
	
	private static final int MIN_WHEEL_DISTANCE = 10;
	private static final int DISTANCE_FROM_WHEEL = 100;
	
	private static final int WHEEL_EXPAND_SPEED = 5;
	private int framesSinceStart = 0;
	
	public GuiLegacyToggle()
	{
		super(new StringTextComponent(""));
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		// Draw background
		super.renderBackground(matrixStack);
		
		// Get number of enabled legacies
		int numLegacies = 0;
		for (Map.Entry<String, Integer> entry : LorienLegacies.proxy.GetClientLegacyData().legacies.entrySet())
			if (entry.getValue() > 0 && LorienLegacies.proxy.GetLegacyManager().GetLegacies().get(entry.getKey()).IsToggleable()) 
				numLegacies++;
		
		if (numLegacies == 0)
		{
			Minecraft.getInstance().player.sendMessage(new StringTextComponent("�cYou do not have any toggleable legacies"), Minecraft.getInstance().player.getUniqueID());
			Minecraft.getInstance().player.closeScreen();
			return;
		}
		
		// Calculate positions and size
		int wheelX = this.width / 2;
		int wheelY = this.height / 2;
		double radiansPerSegment = 2.0 * Math.PI / (double) numLegacies;  // Each legacy will occupy a segment of the wheel
		
		// Get segment mouse is over by converting to polar coordinate angle and add half of radiansPerSegment to put boundary between options
		double mouseAngle = Math.PI/2 + Math.atan2((double)mouseY - (double)wheelY, (double)mouseX - (double)wheelX) + radiansPerSegment/2;		
		if (mouseAngle < 0.0f) mouseAngle += Math.PI*2.0f; // Get rid of negative angles for easier maths
		int mouseSegment = (int) (mouseAngle / radiansPerSegment);
		
		// Distance from wheel
		double distanceFromWheel = Math.sqrt(Math.pow((double)mouseY - (double)wheelY, 2) + Math.pow((double)mouseX - (double)wheelX, 2));
		
		// Expanding effect
		int distanceFromCentre = Math.min(DISTANCE_FROM_WHEEL, framesSinceStart*WHEEL_EXPAND_SPEED);
		framesSinceStart++;
		
		// Draw segments
		int count = 0;
		String selectedLegacy = "";
		for (Map.Entry<String, Integer> entry : LorienLegacies.proxy.GetClientLegacyData().legacies.entrySet())
		{
			// If we have the legacy and it's toggleable
			if (entry.getValue() != 0 && LorienLegacies.proxy.GetLegacyManager().GetLegacies().get(entry.getKey()).IsToggleable())
			{	
				// Rotate angle by 90 degrees anticlockwise
				double angle = radiansPerSegment * count - Math.PI/2;
				
				// Get position by going from polar coordinates to cartesian
				int x = (int) (distanceFromCentre * Math.cos(angle));
				int y = (int) (distanceFromCentre * Math.sin(angle));
				
				// Is legacy currently toggled?
				boolean toggled = LorienLegacies.proxy.GetClientLegacyData().legacyToggles.get(entry.getKey());
				boolean hovered = mouseSegment == count && distanceFromWheel > MIN_WHEEL_DISTANCE;
				
				if (toggled) super.drawCenteredString(matrixStack, font, entry.getKey(), wheelX + x, wheelY + y, hovered ? HIGHLIGHTED_TOGGLED_COLOUR : TOGGLED_COLOUR );
				else super.drawCenteredString(matrixStack, font, entry.getKey(), wheelX + x, wheelY + y, hovered ? HIGHLIGHTED_NOT_TOGGLED_COLOUR * 2: NOT_TOGGLED_COLOUR * 2);
				
				if (mouseSegment == count) selectedLegacy = entry.getKey(); // For releasing of key below
				
				count++;
			}
		}
		
		// Close if alt key not held down
		if (GLFW.glfwGetKey(Minecraft.getInstance().getMainWindow().getHandle(), ModKeybinds.keyToggleLegacies.getKey().getKeyCode()) == 0 && selectedLegacy != "")
		{
			// Toggle if distance from wheel is great enough
			if (distanceFromWheel > MIN_WHEEL_DISTANCE)
			{		
				// Send to server
				MessageToggleLegacy message = new MessageToggleLegacy();
				message.legacy = selectedLegacy;
				NetworkHandler.sendToServer(message);
				
				// Toggle on client too
				LorienLegacies.proxy.GetClientLegacyData().ToggleLegacy(selectedLegacy);
			}

			Minecraft.getInstance().player.closeScreen();
		}
	}
	
	@Override
	public boolean isPauseScreen() { return false; }
	
}
