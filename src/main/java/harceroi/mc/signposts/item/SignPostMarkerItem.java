package harceroi.mc.signposts.item;

import harceroi.mc.signposts.configuration.ConfigurationHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class SignPostMarkerItem extends Item {

  public SignPostMarkerItem() {
    super();
    int maxDamage = ConfigurationHandler.getMarkerMaxUsage();
    if (maxDamage != -1){
      this.setMaxDamage(maxDamage);
    }
    
    this.maxStackSize = 1;
  }

  /**
   * Callback for item usage. If the item does something special on right
   * clicking, he will have one of those. Return True if something happen and
   * false if it don't. This is for ITEMS, not BLOCKS
   */
  public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World p_77648_3_, int x, int y, int z, int side, float p_77648_8_, float p_77648_9_, float p_77648_10_) {
    return true;
  }
  
  @Override
  public int getDamage(ItemStack stack) {
    if(ConfigurationHandler.getMarkerMaxUsage() == -1){
      return 0;
    }
    return super.getDamage(stack);
  }

}