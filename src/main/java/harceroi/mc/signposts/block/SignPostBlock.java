package harceroi.mc.signposts.block;

import harceroi.mc.signposts.SignPostsMod;
import harceroi.mc.signposts.item.SignPostMarkerItem;
import hunternif.mc.atlas.item.ItemAtlas;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class SignPostBlock extends Block implements ITileEntityProvider {

  public SignPostBlock() {
    super(Material.wood);
    this.setHardness(2.0f);
    this.setResistance(6.0f);
    this.setHarvestLevel("axe", 2);
    this.isBlockContainer = true;
  }

  @Override
  public TileEntity createNewTileEntity(World world, int meta) {
    return new SignPostTileEntity(meta);
  }

  @Override
  public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int metadata, float sideX, float sideY, float sideZ) {
    if (world.isRemote) {
      // Do we actually have a signPost here?
      TileEntity te = world.getTileEntity(x, y, z);
      if (!(te instanceof SignPostTileEntity))
        return false;

      // what are we holding?
      ItemStack atlasStack = null;
      ItemStack currentItemStack = player.getCurrentEquippedItem();

      SignPostTileEntity signPost = (SignPostTileEntity) te;
      // is SignPost already activated, i.e. has a corresponding marker and is
      // the player holding a magic marker?
      if (currentItemStack != null && currentItemStack.getItem() instanceof SignPostMarkerItem) {
        if (signPost.getMarkerId() == 0) {
          SignPostsMod.proxy.openSignPostLabelGui(x, y, z, player.posX, Math.floor(player.posY - 1), player.posZ);
        }
        return false;
      }
      // else check if player has Atlas in hand or hotbar and open custom
      // AtlasGui
      if (currentItemStack != null && currentItemStack.getItem() instanceof ItemAtlas) {
        atlasStack = currentItemStack;
      } else {
        for (int j = 8; j > 0; j--) {
          ItemStack stack = player.inventory.getStackInSlot(j);

          if (stack != null && stack.getItem() instanceof ItemAtlas) {
            atlasStack = stack;
          }
        }

      }

      if (atlasStack != null && signPost.getMarkerId() != 0) {
        SignPostsMod.proxy.openAtlasGui(atlasStack, SignPostsMod.ID);
      }
    }

    return false;
  }

  /**
   * Called when a block is placed using its ItemBlock. Args: World, X, Y, Z,
   * side, hitX, hitY, hitZ, block metadata
   */
  @Override
  public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
    if (meta == 4) {
      return 4;
    }

    if (hitX > 0.5 && hitZ > 0.5)
      meta = 0;
    if (hitX <= 0.5 && hitZ > 0.5)
      meta = 1;
    if (hitX <= 0.5 && hitZ <= 0.5)
      meta = 2;
    if (hitX > 0.5 && hitZ <= 0.5)
      meta = 3;

    world.setBlock(x, y + 1, z, this, 4, 2);
    return meta;
  }

  @Override
  public boolean canPlaceBlockAt(World world, int x, int y, int z) {
    boolean amIBottom = this.bottomBlock(world, x, y, z);
    boolean myPlaceEmpty = world.getBlock(x, y, z).isReplaceable(world, x, y, z);

    if (amIBottom) {
      // check if one block above is empty as well
      boolean aboveMeEmpty = world.getBlock(x, y + 1, z).isReplaceable(world, x, y + 1, z);
      return (myPlaceEmpty && aboveMeEmpty);
    }

    return super.canPlaceBlockAt(world, x, y, z);
  }

  @Override
  public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
    if (meta == 4) {
      world.setBlockToAir(x, y - 1, z);
    } else {
      SignPostsMod.removeJumpTarget(x, y, z, world);
      world.setBlockToAir(x, y + 1, z);
    }
    world.removeTileEntity(x, y, z);
    super.breakBlock(world, x, y, z, block, meta);
  }

  @Override
  public boolean onBlockEventReceived(World p_149696_1_, int p_149696_2_, int p_149696_3_, int p_149696_4_, int p_149696_5_, int p_149696_6_) {
    super.onBlockEventReceived(p_149696_1_, p_149696_2_, p_149696_3_, p_149696_4_, p_149696_5_, p_149696_6_);
    TileEntity tileentity = p_149696_1_.getTileEntity(p_149696_2_, p_149696_3_, p_149696_4_);
    return tileentity != null ? tileentity.receiveClientEvent(p_149696_5_, p_149696_6_) : false;
  }

  @Override
  public int getRenderType() {
    return -1;
  }

  @Override
  public boolean isOpaqueCube() {
    return false;
  }

  @Override
  public boolean renderAsNormalBlock() {
    return false;
  }

  private boolean bottomBlock(World world, int x, int y, int z) {
    // I will be top if there is a bottom below me.
    boolean signPostAbove = world.getBlock(x, y + 1, z) instanceof SignPostBlock;
    if (signPostAbove) {
      if (world.getBlockMetadata(x, y + 1, z) == 4) {
        return true;
      }
    }
    return false;
  }

}
