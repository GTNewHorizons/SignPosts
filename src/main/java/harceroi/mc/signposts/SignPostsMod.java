package harceroi.mc.signposts;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import harceroi.mc.signposts.block.SignPostBlock;
import harceroi.mc.signposts.block.SignPostTileEntity;
import harceroi.mc.signposts.configuration.ConfigurationHandler;
import harceroi.mc.signposts.data.MarkerToTileMap;
import harceroi.mc.signposts.integration.IPaymentHandler;
import harceroi.mc.signposts.integration.gollorum.GollorumPaymentHandler;
import harceroi.mc.signposts.item.SignPostMarkerItem;
import harceroi.mc.signposts.network.SignPostsNetworkHelper;
import hunternif.mc.atlas.AntiqueAtlasMod;
import hunternif.mc.atlas.api.AtlasAPI;
import hunternif.mc.atlas.marker.Marker;
import hunternif.mc.atlas.marker.MarkersData;
import hunternif.mc.atlas.network.PacketDispatcher;
import hunternif.mc.atlas.network.client.MarkersPacket;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.ShapedOreRecipe;

@Mod(modid = SignPostsMod.ID, name = SignPostsMod.NAME, version = SignPostsMod.VERSION, dependencies = "required-after:antiqueatlas")
public class SignPostsMod {
  public static final String ID = "signposts";
  public static final String NAME = "Sign-Posts";
  public static final String CHANNEL = ID;
  public static final String VERSION = "GRADLETOKEN_VERSION";

  public static final String aaModName = "antiqueatlas";
  public static final String aaItemName = "antiqueAtlas";

  public static final String MARKERTYPE = "signPost";

  // Integration

  private static HashMap<String, IPaymentHandler> paymentHandlers = new HashMap<String, IPaymentHandler>();

  // Gollorum integration
  public static final String GOLLORUM_SIGNPOST_MOD_ID = "signpost";

  private SignPostMarkerItem markerItem;
  private SignPostBlock signPostBlock;

  @Instance(SignPostsMod.ID)
  public static SignPostsMod instance;

  @SidedProxy(clientSide = "harceroi.mc.signposts.ClientProxy", serverSide = "harceroi.mc.signposts.ServerProxy")
  public static CommonProxy proxy;

  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {

    File configDir = new File(event.getModConfigurationDirectory() + File.separator + ID);
    configDir.mkdirs();
    ConfigurationHandler.init(new File(configDir.getAbsolutePath(), "settings.cfg"));

    proxy.preInit(event);

    signPostBlock = new SignPostBlock();
    signPostBlock.setBlockName("signPost");
    signPostBlock.setBlockTextureName(SignPostsMod.ID + ":sign_post_top");
    signPostBlock.setCreativeTab(CreativeTabs.tabBlock);
    GameRegistry.registerBlock(signPostBlock, "signPost");
    GameRegistry.registerTileEntity(SignPostTileEntity.class, "signPostTileEntity");

    markerItem = new SignPostMarkerItem();
    markerItem.setUnlocalizedName("signPostMarker");
    markerItem.setCreativeTab(CreativeTabs.tabTools);
    markerItem.setTextureName(SignPostsMod.ID + ":emeraldSignPostMarker");
    GameRegistry.registerItem(markerItem, "signPostMarker");
    SignPostsMod.addPaymentHandler(new PaymentHandler(), ID);

    if (Loader.isModLoaded(GOLLORUM_SIGNPOST_MOD_ID)) {
      MinecraftForge.EVENT_BUS.register(new harceroi.mc.signposts.integration.gollorum.GollorumEventHandler());
      SignPostsMod.addPaymentHandler(new GollorumPaymentHandler(), GOLLORUM_SIGNPOST_MOD_ID);
    }
  }

  @EventHandler
  public void init(FMLInitializationEvent event) {
    proxy.init(event);
    SignPostsNetworkHelper.registerMessageHandlers();

    GameRegistry.addRecipe(new ItemStack(markerItem), new Object[] { ".E.", ".E.", ".E.", 'E', Items.emerald });

    IRecipe blockRecipe = new ShapedOreRecipe(new ItemStack(signPostBlock), new Object[] { ".WS", ".WS", ".W.", 'W', "plankWood", 'S', Items.sign });
    GameRegistry.addRecipe(blockRecipe);
  }

  static void setFinalStatic(Field field, Object newValue) throws Exception {
    field.setAccessible(true);

    Field modifiersField = Field.class.getDeclaredField("modifiers");
    modifiersField.setAccessible(true);
    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

    field.set(null, newValue);
  }

  public static void addJumpTarget(int signX, int signY, int signZ, double jumpX, double jumpY, double jumpZ, String label, EntityPlayerMP player) {
    TileEntity tileEntity = player.worldObj.getTileEntity(signX, signY, signZ);
    if (tileEntity instanceof SignPostTileEntity) {
      SignPostTileEntity signPost = (SignPostTileEntity) tileEntity;
      int markerId = addGlobalMarker(signX, signZ, label, player.worldObj);
      signPost.setAsJumpLocation(jumpX, jumpY, jumpZ, markerId);
      MarkerToTileMap.get(player.worldObj).setTileForMarker(signX, signY, signZ, markerId);
      ItemStack currentItemStack = player.getCurrentEquippedItem();
      if (currentItemStack != null && currentItemStack.getItem() instanceof SignPostMarkerItem) {
        int maxItemUsages = ConfigurationHandler.getMarkerMaxUsage();
        if (maxItemUsages != -1) {
          currentItemStack.damageItem(1, player);
          if (currentItemStack.getItemDamage() == currentItemStack.getMaxDamage()) {
            player.destroyCurrentEquippedItem();
          }
        }
      }
    }
  }

  public static void removeJumpTarget(int signX, int signY, int signZ, World world) {
    TileEntity tileEntity = world.getTileEntity(signX, signY, signZ);
    if (tileEntity instanceof SignPostTileEntity) {
      SignPostTileEntity signPost = (SignPostTileEntity) tileEntity;
      int markerId = signPost.getMarkerId();
      if (markerId != 0) {
        removeGlobalMarker(markerId, world);
        MarkerToTileMap.get(world).removeMarker(markerId);
      }
    }
  }

  public static int addGlobalMarker(int x, int z, String label, World world) {
    MarkersData data = AntiqueAtlasMod.globalMarkersData.getData();
    Marker marker = data.createAndSaveMarker(MARKERTYPE, label, world.provider.dimensionId, x, z, false);
    PacketDispatcher.sendToAll(new MarkersPacket(world.provider.dimensionId, marker));
    return marker.getId();
  }

  public static void removeGlobalMarker(int markerId, World world) {
    AtlasAPI.markers.deleteGlobalMarker(world, markerId);
  }

  public static void playerJump(int markerId, EntityPlayerMP player, String paymentHandlerKey) {
    IPaymentHandler paymentHandler = paymentHandlers.get(paymentHandlerKey);
    int[] coords = MarkerToTileMap.get(player.worldObj).getTileForMarker(markerId);
    // Get tile Entity
    if (coords != null) {
      double jumpX = (double) (coords[0]) + 0.5;
      double jumpY = (double) (coords[1]) + 1;
      double jumpZ = (double) (coords[2]) + 0.5;
      TileEntity tileEntity = player.worldObj.getTileEntity(coords[0], coords[1], coords[2]);
      if (tileEntity instanceof SignPostTileEntity) {
        SignPostTileEntity tile = (SignPostTileEntity) tileEntity;
        jumpX = tile.getJumpX();
        jumpY = tile.getJumpY();
        jumpZ = tile.getJumpZ();
      }

      if (jumpX != 0 && jumpY != 0 && jumpZ != 0) {
        // PAY
        if (player.capabilities.isCreativeMode || paymentHandler.pay(player, (int) jumpX, (int) jumpY, (int) jumpZ)) {
          // jump!
          player.setPositionAndUpdate(jumpX, jumpY, jumpZ);
        }
      }
    }
  }

  public static void addPaymentHandler(IPaymentHandler paymentHandler, String name) {
    paymentHandlers.put(name, paymentHandler);
  }
}
