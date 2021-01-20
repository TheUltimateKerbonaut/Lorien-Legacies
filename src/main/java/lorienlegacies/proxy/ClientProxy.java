package lorienlegacies.proxy;

import lorienlegacies.blocks.ModBlocks;
import lorienlegacies.entities.ModEntities;
import lorienlegacies.items.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	
	@Override
    public void preInit(FMLPreInitializationEvent e) 
	{
        super.preInit(e);
        ModEntities.register();
    }
	
	@Override
	public void postInit(FMLPostInitializationEvent e)
	{
		super.postInit(e);
	}
	
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
    	ModBlocks.registerModels();
    	ModItems.registerModels();
    }
	
    @Override
    public EntityPlayer getPlayerEntityFromContext(MessageContext ctx)
	{
		return Minecraft.getMinecraft().player;
	}
    
}