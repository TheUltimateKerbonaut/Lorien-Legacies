package lorienlegacies.legacies.implementations;

import java.util.Map;
import java.util.Random;

import lorienlegacies.config.ConfigLorienLegacies;
import lorienlegacies.legacies.Legacy;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.SnowballEntity;
import net.minecraft.item.Items;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion.Mode;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Lumen extends Legacy 
{
	private static final int FIREBALL_EXPLOSION_RADIUS = 3;
	private static final int FIREWAVE_PROJECTILES = 10;
	private static final float FIREWAVE_FALL_AMOUNT = -0.3f;
	
	private Random random = new Random();
	
	public Lumen(Map<LegacyAbility, String> legacyAbilities)
	{
		NAME = "Lumen";
		DESCRIPTION = "Grants fire powers";
		STAMINA_PER_TICK = 0; // Giving zero stamina per tick disables the toggle GUI
		
		GENERATION_WEIGHTING = 1;
		GENERATION_POINTS = 3;
		
		BOOK_DESCRIPTION = "Fire is your friend with Lumen. Ignite whatever stands in your way, and bathe in flames.";
		
		AddLevel("Flammable hands", 12000);
		AddLevel("Fire and lava resistance", 16000);
		AddLevel("Fireballs", 20000);
		AddLevel("Fire wave", 22000);
		
		legacyAbilities.put(new LegacyAbility("Fireball", 3), NAME);
		legacyAbilities.put(new LegacyAbility("Firewave", 4), NAME);
		
		MinecraftForge.EVENT_BUS.register(this); // Need to receive events
	}

	@SubscribeEvent
	public void OnLivingAttackEvent(LivingAttackEvent event)
	{
		// Check side is server-side
		if (event.getEntity().world.isRemote) return;
		
		if (event.getEntity() instanceof PlayerEntity == false) return;
		
		// If player does not have Lumen, return
		if (GetLegacyLevel((PlayerEntity)event.getEntity()) == 0) return;
		
		int level = GetLegacyLevel((PlayerEntity)event.getEntity());
		
		// Fire resistance
		if (level >= 2 && event.getSource().equals(DamageSource.IN_FIRE) || event.getSource().equals(DamageSource.ON_FIRE))  event.setCanceled(true);
		
		// Magma blocks
		if (level >= 2 && event.getSource().equals(DamageSource.HOT_FLOOR)) event.setCanceled(true);
		
		// Lava resistance
		if (level >= 2 && event.getSource().equals(DamageSource.LAVA)) event.setCanceled(true);
		
		// Explosions
		if (level >= 3 && event.getSource().isExplosion()) event.setCanceled(true);
	}
	
	@SubscribeEvent
	public void OnPlayerInteractEvent(PlayerInteractEvent event)
	{
		// Check side is server-side
		if (event.getEntity().world.isRemote) return;
		
		// Check it was a right click
		if (event instanceof PlayerInteractEvent.RightClickBlock == false) return;
		
		if (event.getEntity() instanceof PlayerEntity == false) return;
		
		// If player does not have Lumen, return
		if (GetLegacyLevel((PlayerEntity)event.getEntity()) == 0) return;
		
		// Check the hand is empty
		if (event.getPlayer().getHeldItem(event.getHand()).getTranslationKey() != Items.AIR.getTranslationKey()) return;
		
		// Check the player is crouched
		if (event.getPlayer().isCrouching() == false) return;
		
		// Get the block the player is looking at
		PlayerEntity player = (PlayerEntity)event.getEntity();
		Vector3d startVec = player.getEyePosition(1.0f);
		Vector3d endVec = startVec.add(player.getLookVec().scale(10.0f));
		BlockRayTraceResult rayResult = event.getWorld().rayTraceBlocks(new RayTraceContext(startVec, endVec, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.ANY, player));
		
		if (rayResult == null || rayResult.getPos() == null || event.getWorld().getBlockState(rayResult.getPos()) == null) return;
		
		// If block is a door, ignore
		BlockState blockState = player.world.getBlockState(rayResult.getPos());
		if (blockState.getBlock() instanceof DoorBlock) return;
		
		IgniteBlock(player, rayResult.getPos(), blockState, event.getHand(), event.getFace());
		event.getFace();
		
		event.setCanceled(true);
	}
	
	// Partially lifted from FlintAndSteelItem#onItemUse, ignites block
	private void IgniteBlock(PlayerEntity player, BlockPos block, BlockState blockState, Hand hand, Direction direction)
	{
		if (CampfireBlock.canBeLit(blockState))
		{
			// Play sound audible to everyone by making a new entity at the position of player and making it play the sound
			SnowballEntity entity = new SnowballEntity(player.world, player.getPosX(), player.getPosY(), player.getPosZ());
			entity.playSound(SoundEvents.ITEM_FLINTANDSTEEL_USE, 1.0f, random.nextFloat() * 0.4f + 0.8f);
			entity.onKillCommand();
			
			player.world.setBlockState(block, blockState.with(BlockStateProperties.LIT, Boolean.valueOf(true)), 11); // Ignite block
		}
		else
		{
			BlockPos offsetBlockPos = block.offset(direction); // Find adjacent block
			
			if (AbstractFireBlock.canLightBlock(player.world, offsetBlockPos, player.getHorizontalFacing()))
			{
				// Play sound audible to everyone by making a new entity at the position of player and making it play the sound
				SnowballEntity entity = new SnowballEntity(player.world, player.getPosX(), player.getPosY(), player.getPosZ());
				entity.playSound(SoundEvents.ITEM_FLINTANDSTEEL_USE, 1.0f, random.nextFloat() * 0.4f + 0.8f);
				entity.onKillCommand();
				
				// Set alight
				BlockState offsetBlockState = AbstractFireBlock.getFireForPlacement(player.world, offsetBlockPos);
				player.world.setBlockState(offsetBlockPos, offsetBlockState, 11);
			}
		}
	}

	@Override
	public float GetAbilityStamina(String ability)
	{
		return ability == "Fireball" ? 25 : ConfigLorienLegacies.legacyStamina.maxStamina;
	}
	
	@Override
	public void OnAbility(String ability, PlayerEntity player)
	{
		if (ability == "Fireball")
		{
			// Fireball
			ShootFireProjectile(player, 0.0f, false);		
		}
		else
		{
			// Firewave
			for (int i = 0; i < FIREWAVE_PROJECTILES; ++i)
				ShootFireProjectile(player, (float) Math.toRadians(i * (360/FIREWAVE_PROJECTILES)), true);
		}
	}
	
	private void ShootFireProjectile(PlayerEntity player, float rotateAmount, boolean wave)
	{
		Vector3d velocity = wave ?  new Vector3d(Math.sin(rotateAmount), FIREWAVE_FALL_AMOUNT, Math.cos(rotateAmount)) : player.getLookVec();
		
		FireballEntity fireball = new FireballEntity(player.world, player, velocity.x, velocity.y, velocity.z)
		{
			@Override
			protected void onImpact(RayTraceResult result)
			{
				// Avoid colliding with player
				if (result instanceof EntityRayTraceResult)
				{
					if (((EntityRayTraceResult)result).getEntity().getUniqueID() == player.getUniqueID()) return;
				}
				
				this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
				if (this.world.isRemote == false)
				{
					this.world.createExplosion(this, this.getPosX(), this.getPosY(), this.getPosZ(), FIREBALL_EXPLOSION_RADIUS, true, Mode.DESTROY);
					this.onKillCommand();
				}
			}
		};
		
		player.world.addEntity(fireball);
		fireball.playSound(SoundEvents.ENTITY_GHAST_SHOOT, 1.0f, 1.0f);
	}
	
	@Override
	protected void OnLegacyTick(PlayerEntity player) {}

}
