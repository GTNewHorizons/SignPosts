package harceroi.mc.signposts.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class SignPostMarkerItem extends Item {

  public SignPostMarkerItem() {
    super();
    this.setMaxDamage(8);
    this.maxStackSize = 1;
  }
  
  /**
   * Callback for item usage. If the item does something special on right clicking, he will have one of those. Return
   * True if something happen and false if it don't. This is for ITEMS, not BLOCKS
   */
  public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World p_77648_3_, int x, int y, int z, int side, float p_77648_8_, float p_77648_9_, float p_77648_10_){
    return true;
  }

}