package harceroi.mc.signposts.integration.gollorum;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gollorum.signpost.event.UpdateWaystoneEvent;
import gollorum.signpost.event.UseSignpostEvent;
import harceroi.mc.signposts.SignPostsMod;
import harceroi.mc.signposts.block.SignPostTileEntity;
import harceroi.mc.signposts.data.MarkerToTileMap;
import harceroi.mc.signposts.item.SignPostMarkerItem;
import hunternif.mc.atlas.item.ItemAtlas;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GollorumEventHandler {

  @SubscribeEvent
  public void handleWaystoneEvent(UpdateWaystoneEvent event) {
    switch (event.type) {
    case PLACED:
      addGollorumWaystone(event.world, event.x, event.y, event.z, event.name);
      break;
    case DESTROYED:
      removeGollorumWaystone(event.world, event.x, event.y, event.z);
      break;
    case NAMECHANGED:
      removeGollorumWaystone(event.world, event.x, event.y, event.z);
      addGollorumWaystone(event.world, event.x, event.y, event.z, event.name);
      break;
    default:
      break;
    }
  }

  @SubscribeEvent
  public void handleSignPostEvent(UseSignpostEvent event) {
    ItemStack atlasStack = null;
    ItemStack currentItemStack = event.player.getCurrentEquippedItem();
    if (currentItemStack != null && currentItemStack.getItem() instanceof ItemAtlas) {
      if (event.world.isRemote) {
        // else check if player has Atlas in hand or hotbar and open custom
        // AtlasGui
        atlasStack = currentItemStack;
        SignPostsMod.proxy.openAtlasGui(atlasStack, SignPostsMod.GOLLORUM_SIGNPOST_MOD_ID);
      }
      event.setCanceled(true);
    }
  }

  private static void addGollorumWaystone(World world, int x, int y, int z, String name) {
    int markerId = SignPostsMod.addGlobalMarker(x, z, name, world);
    MarkerToTileMap.get(world).setTileForMarker(x, y, z, markerId);
  }

  private static void removeGollorumWaystone(World world, int x, int y, int z) {
    MarkerToTileMap map = MarkerToTileMap.get(world);
    int markerId = map.getMarkerForTile(x, y, z);
    if (markerId == -1)
      return;
    SignPostsMod.removeGlobalMarker(markerId, world);
    map.removeMarker(markerId);
  }

}
