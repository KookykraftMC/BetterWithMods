package betterwithmods;

import betterwithmods.api.capabilities.MechanicalCapability;
import betterwithmods.api.tile.IMechanicalPower;
import betterwithmods.client.container.BWGuiHandler;
import betterwithmods.config.BWConfig;
import betterwithmods.config.ConfigSyncHandler;
import betterwithmods.entity.EntityDynamite;
import betterwithmods.entity.EntityExtendingRope;
import betterwithmods.entity.EntityMiningCharge;
import betterwithmods.entity.EntityShearedCreeper;
import betterwithmods.entity.item.EntityItemBuoy;
import betterwithmods.event.*;
import betterwithmods.integration.ModIntegration;
import betterwithmods.network.BWNetwork;
import betterwithmods.proxy.CommonProxy;
import betterwithmods.util.ColorUtils;
import betterwithmods.util.InvUtils;
import betterwithmods.util.RecipeUtils;
import betterwithmods.util.item.ItemExt;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = BWMod.MODID, name = BWMod.NAME, version = BWMod.VERSION, dependencies = "required-after:Forge@[12.18.1.2076,);before:survivalist;after:tconstruct;after:minechem;after:natura;after:terrafirmacraft;after:immersiveengineering", guiFactory = "betterwithmods.client.gui.BWGuiFactory")
public class BWMod {
    public static final String MODID = "betterwithmods";
    public static final String VERSION = "0.13.1 Beta hotfix 1";
    public static final String NAME = "Better With Mods";
    public static final Logger logger = LogManager.getLogger(BWMod.MODID);
    @SidedProxy(serverSide = "betterwithmods.proxy.CommonProxy", clientSide = "betterwithmods.proxy.ClientProxy")
    public static CommonProxy proxy;
    @Mod.Instance(BWMod.MODID)
    public static BWMod instance;

    private static void registerEventHandlers() {
        MinecraftForge.EVENT_BUS.register(new BWConfig());
        MinecraftForge.EVENT_BUS.register(new HungerEventHandler());
        MinecraftForge.EVENT_BUS.register(new BuoyancyEventHandler());
        MinecraftForge.EVENT_BUS.register(new RespawnEventHandler());
        MinecraftForge.EVENT_BUS.register(new MobDropEvent());
        MinecraftForge.EVENT_BUS.register(new BucketEvent());
        MinecraftForge.EVENT_BUS.register(new NetherSpawnEvent());
        MinecraftForge.EVENT_BUS.register(new LogHarvestEvent());
        MinecraftForge.EVENT_BUS.register(new PotionEventHandler());
        MinecraftForge.EVENT_BUS.register(new MobAIEvent());
    }

    private static void registerEntities() {
        BWRegistry.registerEntity(EntityExtendingRope.class, "ExtendingRope", 64, 20, true);
        BWRegistry.registerEntity(EntityDynamite.class, "BWMDynamite", 10, 50, true);
        BWRegistry.registerEntity(EntityMiningCharge.class, "BWMMiningCharge", 10, 50, true);
        BWRegistry.registerEntity(EntityItemBuoy.class, "entityItemBuoy", 64, 20, true);
        BWRegistry.registerEntity(EntityShearedCreeper.class, "entityShearedCreeper", 64, 1, true);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        //BWConfig.init(new File(evt.getModConfigurationDirectory() + "/betterwithmods.cfg"));
        BWConfig.init(evt.getSuggestedConfigurationFile());

        BWMBlocks.registerBlocks();
        BWMItems.registerItems();
        BWMBlocks.registerTileEntities();

        BWRegistry.init();
        ModIntegration.loadPreInit();
        BWCrafting.init();
        registerEntities();
        proxy.registerRenderInformation();
        proxy.initRenderers();
        CapabilityManager.INSTANCE.register(IMechanicalPower.class, new MechanicalCapability.CapabilityMechanicalPower(), MechanicalCapability.DefaultMechanicalPower.class);
        BWNetwork.INSTANCE.init();
    }

    @EventHandler
    public void init(FMLInitializationEvent evt) {
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new BWGuiHandler());
        proxy.registerColors();
        BWRegistry.registerHeatSources();
        GameRegistry.registerFuelHandler(new BWFuelHandler());
        BWRegistry.registerNetherWhitelist();
        ModIntegration.loadInit();
        BWSounds.registerSounds();
        ItemExt.initBuoyancy();
        ItemExt.initDesserts();
        ItemExt.initWeights();
    }

    @EventHandler
    public void processIMCMessages(IMCEvent evt) {
        BWIMCHandler.processIMC(evt.getMessages());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent evt) {
        RecipeUtils.gatherCookableFood();
        BWRegistry.registerWood();
        InvUtils.initOreDictGathering();
        ColorUtils.initColors();
        registerEventHandlers();
        RecipeUtils.refreshRecipes();
        ModIntegration.loadPostInit();
        BucketEvent.editModdedFluidDispenseBehavior();
        if(evt.getSide().isServer())
            MinecraftForge.EVENT_BUS.register(new ConfigSyncHandler());
    }
}
