package lorienlegacies.legacies.implementations;

import lorienlegacies.core.LorienLegacies;
import lorienlegacies.legacies.Legacy;
import net.minecraft.entity.player.EntityPlayer;

public class Glacen extends Legacy
{
	
	public Glacen()
	{
		NAME = "Glacen";
		DESCRIPTION = "Grants ice powers";
	}

	@Override
	protected void OnLegacyTick(EntityPlayer player)
	{
		LorienLegacies.logger.info("Glacen - OnLegacyTick()");
	}

}