package harceroi.mc.signposts.gui;

import harceroi.mc.signposts.network.CreateJumpTargetMessage;
import harceroi.mc.signposts.network.SignPostsNetworkHelper;
import hunternif.mc.atlas.client.gui.core.GuiComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

public class SignPostLabelGui extends GuiComponent {

  private static final int BUTTON_WIDTH = 100;
  private static final int BUTTON_SPACING = 4;


  private GuiButton btnDone;
  private GuiButton btnCancel;
  private GuiTextField textField;
  private int signX;
  private int signY;
  private int signZ;
  private double jumpY;
  private double jumpX;
  private double jumpZ;

  @SuppressWarnings("unchecked")
  @Override
  public void initGui() {
    buttonList.add(btnDone = new GuiButton(0, this.width / 2 - BUTTON_WIDTH - BUTTON_SPACING / 2, this.height / 2 + 40, BUTTON_WIDTH, 20, I18n.format("gui.done")));
    buttonList.add(btnCancel = new GuiButton(0, this.width / 2 + BUTTON_SPACING / 2, this.height / 2 + 40, BUTTON_WIDTH, 20, I18n.format("gui.cancel")));
    textField = new GuiTextField(Minecraft.getMinecraft().fontRenderer, (this.width - 200) / 2, this.height / 2 - 81, 200, 20);
    textField.setFocused(true);
    textField.setText("");
  }

  public SignPostLabelGui setJumpTargetValues(int signX, int signY, int signZ, double jumpX, double jumpY, double jumpZ) {
    this.signX = signX;
    this.signY = signY;
    this.signZ = signZ;
    this.jumpX = jumpX;
    this.jumpY = jumpY;
    this.jumpZ = jumpZ;
    return this;
  }

  @Override
  protected void mouseClicked(int par1, int par2, int par3) {
    super.mouseClicked(par1, par2, par3);
    textField.mouseClicked(par1, par2, par3);
  }

  @Override
  protected void keyTyped(char par1, int par2) {
    super.keyTyped(par1, par2);
    textField.textboxKeyTyped(par1, par2);
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTick) {
    drawDefaultBackground();
    drawCenteredString(I18n.format("gui.signposts.marker.label"), this.height / 2 - 97, 0xffffff, true);
    textField.drawTextBox();

    super.drawScreen(mouseX, mouseY, partialTick);
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button == btnDone) {
      CreateJumpTargetMessage msg = new CreateJumpTargetMessage(signX, signY, signZ, jumpX, jumpY, jumpZ, textField.getText());
      SignPostsNetworkHelper.sendCreateJumpTargetMessage(msg);
      close();
    } else if (button == btnCancel) {
      close();
    }
  }

  @Override
  public boolean doesGuiPauseGame() {
    return false;
  }
  
  

}
