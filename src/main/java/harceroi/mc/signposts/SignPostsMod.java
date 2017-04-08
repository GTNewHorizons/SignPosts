package harceroi.mc.signposts;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import harceroi.mc.signposts.block.SignPostBlock;
import harceroi.mc.signposts.block.SignPostTileEntity;
import harceroi.mc.signposts.data.MarkerToTileMap;
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
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

@Mod(modid = SignPostsMod.ID, name = SignPostsMod.NAME, version = SignPostsMod.VERSION, dependencies = "required-after:antiqueatlas")
public class SignPostsMod {
  public static final String ID = "signposts";
  public static final String NAME = "Sign-Posts";
  public static final String CHANNEL = ID;
  public static final String VERSION = "1.0.0";

  public static final String aaModName = "antiqueatlas";
  public static final String aaItemName = "antiqueAtlas";

  public static final String MARKERTYPE = "signPost";
  
  private SignPostMarkerItem markerItem;
  private SignPostBlock signPostBlock;

  @Instance(SignPostsMod.ID)
  public static SignPostsMod instance;

  @SidedProxy(clientSide = "harceroi.mc.signposts.ClientProxy", serverSide = "harceroi.mc.signposts.ServerProxy")
  public static CommonProxy proxy;

  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    proxy.preInit(event);

    signPostBlock = new SignPostBlock();
    signPostBlock.setBlockName("signPost");
    signPostBlock.setBlockTextureName(SignPostsMod.ID + ":signPost");
    signPostBlock.setCreativeTab(CreativeTabs.tabBlock);
    GameRegistry.registerBlock(signPostBlock, "signPost");
    GameRegistry.registerTileEntity(SignPostTileEntity.class, "signPostTileEntity");

    markerItem = new SignPostMarkerItem();
    markerItem.setUnlocalizedName("signPostMarker");
    markerItem.setCreativeTab(CreativeTabs.tabTools);
    markerItem.setTextureName(SignPostsMod.ID + ":emeraldSignPostMarker");
    GameRegistry.registerItem(markerItem, "signPostMarker");
  }

  @EventHandler
  public void init(FMLInitializationEvent event) {
    proxy.init(event);
    SignPostsNetworkHelper.registerMessageHandlers();
    
    GameRegistry.addRecipe(new ItemStack(markerItem), new Object[]{
        ".E.",
        ".E.",
        ".E.",
        'E',
        Items.emerald
    });
    
    IRecipe blockRecipe = new ShapedOreRecipe(new ItemStack(signPostBlock), new Object[]{
        ".WS",
        ".WS",
        ".W.",
        'W', "plankWood",
        'S', Items.sign
    });
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
    System.out.println("Try to add marker at " + signX + ", " + signZ + " to be transported to " + jumpX + ", " + jumpY + ", " + jumpZ + ".");
    TileEntity tileEntity = player.worldObj.getTileEntity(signX, signY, signZ);
    if (tileEntity instanceof SignPostTileEntity) {
      SignPostTileEntity signPost = (SignPostTileEntity) tileEntity;
      int markerId = addGlobalMarker(signX, signZ, label, player.worldObj);
      signPost.setAsJumpLocation(jumpX, jumpY, jumpZ, markerId);
      MarkerToTileMap.get(player.worldObj).setTileForMarker(signX, signY, signZ, markerId);
      ItemStack currentItemStack =  player.getCurrentEquippedItem();
      if(currentItemStack != null && currentItemStack.getItem() instanceof SignPostMarkerItem){
        currentItemStack.damageItem(1, player);
        if (currentItemStack.getItemDamage() == currentItemStack.getMaxDamage()){
          player.destroyCurrentEquippedItem();
        }
      }
      
    }
  }

  public static void removeJumpTarget(int signX, int signY, int signZ, World world) {
    System.out.println("Try to remove marker at " + signX + ", " + signY + ", " + signZ + ".");
    TileEntity tileEntity = world.getTileEntity(signX, signY, signZ);
    if (tileEntity instanceof SignPostTileEntity) {
      SignPostTileEntity signPost = (SignPostTileEntity) tileEntity;
      int markerId = signPost.getMarkerId();
      if(markerId != 0){
        removeGlobalMarker(markerId, world);
        MarkerToTileMap.get(world).removeMarker(markerId);        
      }
    }
  }

  private static int addGlobalMarker(int x, int z, String label, World world) {
    MarkersData data = AntiqueAtlasMod.globalMarkersData.getData();
    Marker marker = data.createAndSaveMarker(MARKERTYPE, label, world.provider.dimensionId, x, z, false);
    PacketDispatcher.sendToAll(new MarkersPacket(world.provider.dimensionId, marker));
    return marker.getId();
  }
  
  private static void removeGlobalMarker(int markerId, World world){
    AtlasAPI.markers.deleteGlobalMarker(world, markerId);
  }

  public static void playerJump(int markerId, EntityPlayerMP player) {

    int[] coords = MarkerToTileMap.get(player.worldObj).getTileForMarker(markerId);
    System.out.println("I got coords:");
    System.out.println(coords);
    // Get tile Entity
    if (coords != null) {
      TileEntity tileEntity = player.worldObj.getTileEntity(coords[0], coords[1], coords[2]);
      if (tileEntity instanceof SignPostTileEntity) {
        SignPostTileEntity tile = (SignPostTileEntity) tileEntity;
        double jumpX = tile.getJumpX();
        double jumpY = tile.getJumpY();
        double jumpZ = tile.getJumpZ();
        
        System.out.println("Jump Data? " + jumpX);

        if (jumpX != 0 && jumpY != 0 && jumpZ != 0) {
          // jumping is exhausting!
          double distance = Math.pow(Math.abs(player.posX - (int) jumpX), 2) + Math.pow(Math.abs(player.posY - (int) jumpY), 2) + Math.pow(Math.abs(player.posZ - (int) jumpZ), 2);
          int exhaustion = Math.min((Math.floorDiv((int) distance, 10000)), 10);
          player.getFoodStats().addStats(-1*exhaustion, 0.0F);
          // jump!
          player.setPositionAndUpdate(jumpX, jumpY, jumpZ);
        }

      }
    }
  }
}
