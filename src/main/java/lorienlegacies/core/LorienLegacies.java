package lorienlegacies.core;

import org.apache.logging.log4j.Logger;

import lorienlegacies.legacies.LegacyManager;
import lorienlegacies.proxy.CommonProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

@Mod(modid = LorienLegacies.MODID, name = LorienLegacies.NAME, version = LorienLegacies.VERSION)
public class LorienLegacies
{
	// Forge mod configuration and naming
    public static final String MODID = "lorienlegacies";
    public static final String NAME = "Lorien Legacies";
    public static final String VERSION = "1.0";

    // Logging
    public static Logger logger;

    // Proxy
    @SidedProxy(clientSide = "lorienlegacies.proxy.ClientProxy", serverSide = "lorienlegacies.proxy.ServerProxy")
	public static CommonProxy proxy;
    
    // Legacies
    private LegacyManager legacyManager = new LegacyManager();
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        logger.info("Initialising {} version {}", NAME, VERSION);
        
        MinecraftForge.EVENT_BUS.register(this);
        
        legacyManager.RegisterLegacies();
        
        proxy.init(event);
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    	proxy.postInit(event);
    }
    
    @SubscribeEvent
	public void PlayerLoggedInEvent(PlayerLoggedInEvent event)
	{
    	// Server-side
    	if (!event.player.world.isRemote) legacyManager.RegisterPlayer(event.player);
	}
    
    @SubscribeEvent
    public void PlayerLoggedOutEvent(PlayerLoggedOutEvent event)
    {
    	// Server-side
    	if (!event.player.world.isRemote) legacyManager.DisconnectPlayer(event.player);
    }
    
    @SubscribeEvent
    public void onWorldTick(WorldTickEvent event)
    {
    	// Server-side
    	if (!event.world.isRemote) legacyManager.OnLegacyTick(event.world);
    }
    
}
