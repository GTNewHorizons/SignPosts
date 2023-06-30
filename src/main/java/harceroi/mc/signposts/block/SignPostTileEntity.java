package harceroi.mc.signposts.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class SignPostTileEntity extends TileEntity {

  private boolean isBottom;

  public String text = null;

  public SignPostTileEntity(int meta){
    isBottom = (meta != 4);
  }

  // Jump Locations

  private double jumpX, jumpY, jumpZ;
  private int markerId;

  @Override
  public void writeToNBT(NBTTagCompound data) {
    super.writeToNBT(data);
    data.setBoolean("isBottom", isBottom);
    // TODO move to master only
    if (isBottom) {
      data.setDouble("jumpX", jumpX);
      data.setDouble("jumpY", jumpY);
      data.setDouble("jumpZ", jumpZ);
      data.setInteger("markerId", markerId);
    }
  }

  @Override
  public void readFromNBT(NBTTagCompound data) {
    super.readFromNBT(data);
    isBottom = data.getBoolean("isBottom");
    // TODO move to master only
    if (isBottom) {
      jumpX = data.getDouble("jumpX");
      jumpY = data.getDouble("jumpY");
      jumpZ = data.getDouble("jumpZ");
      markerId = data.getInteger("markerId");
    }
  }

  public double getJumpX() {
    return getBottom().jumpX;
  }

  public double getJumpY() {
    return getBottom().jumpY;
  }

  public double getJumpZ() {
    return getBottom().jumpZ;
  }

  public int getMarkerId() {
    return getBottom().markerId;
  }

  public boolean isBottom(){
    return isBottom;
  }

  public void setAsJumpLocation(double x, double y, double z, int markerId) {
    SignPostTileEntity te = getBottom();
    te.jumpX = x;
    te.jumpY = y;
    te.jumpZ = z;
    te.markerId = markerId;
    worldObj.markBlockForUpdate(te.xCoord, te.yCoord, te.zCoord);
  }

  @Override
  public Packet getDescriptionPacket() {
    NBTTagCompound tagCompound = new NBTTagCompound();
    this.writeToNBT(tagCompound);
    return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, tagCompound);
  }

  @Override
  public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
    readFromNBT(pkt.func_148857_g());
    worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
  }

  private SignPostTileEntity getBottom(){
    if(isBottom) return this;
    return (SignPostTileEntity) worldObj.getTileEntity(xCoord, yCoord - 1, zCoord);
  }

}
