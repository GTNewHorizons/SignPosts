package harceroi.mc.signposts.atlas.gui;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import harceroi.mc.signposts.network.JumpMessage;
import harceroi.mc.signposts.network.SignPostsNetworkHelper;
import hunternif.mc.atlas.AntiqueAtlasMod;
import hunternif.mc.atlas.client.BiomeTextureMap;
import hunternif.mc.atlas.client.SubTile;
import hunternif.mc.atlas.client.SubTileQuartet;
import hunternif.mc.atlas.client.Textures;
import hunternif.mc.atlas.client.TileRenderIterator;
import hunternif.mc.atlas.client.gui.GuiArrowButton;
import hunternif.mc.atlas.client.gui.GuiPositionButton;
import hunternif.mc.atlas.client.gui.GuiScaleBar;
import hunternif.mc.atlas.client.gui.core.GuiComponent;
import hunternif.mc.atlas.client.gui.core.GuiComponentButton;
import hunternif.mc.atlas.client.gui.core.GuiStates;
import hunternif.mc.atlas.client.gui.core.GuiStates.IState;
import hunternif.mc.atlas.client.gui.core.GuiStates.SimpleState;
import hunternif.mc.atlas.client.gui.core.IButtonListener;
import hunternif.mc.atlas.core.DimensionData;
import hunternif.mc.atlas.marker.DimensionMarkersData;
import hunternif.mc.atlas.marker.Marker;
import hunternif.mc.atlas.marker.MarkerTextureMap;
import hunternif.mc.atlas.marker.MarkersData;
import hunternif.mc.atlas.network.PacketDispatcher;
import hunternif.mc.atlas.network.server.BrowsingPositionPacket;
import hunternif.mc.atlas.util.AtlasRenderHelper;
import hunternif.mc.atlas.util.MathUtil;
import hunternif.mc.atlas.util.Rect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class SignPostsAtlasGui extends GuiComponent {
  private static final int CONTENT_X = 17;
  private static final int CONTENT_Y = 11;
  
  public static final int WIDTH = 310;
  public static final int HEIGHT = 218;
  private static final int MAP_WIDTH = WIDTH - 17 * 2;
  private static final int MAP_HEIGHT = 194;
  private static final float PLAYER_ROTATION_STEPS = 16;
  private static final int PLAYER_ICON_WIDTH = 7;
  private static final int PLAYER_ICON_HEIGHT = 8;

  public static final int MARKER_SIZE = 32;
  /** The radius of the area in which the marker will display hovering label. */
  private static final int MARKER_RADIUS = 7;

  /**
   * If the map scale goes below this value, the tiles will not scale down
   * visually, but will instead span greater area.
   */
  public static final double MIN_SCALE_THRESHOLD = 0.5;

  private boolean DEBUG_RENDERING = false;
  private long[] renderTimes = new long[30];
  private int renderTimesIndex = 0;

  // States ==================================================================

  private final GuiStates state = new GuiStates();

  /** If on, navigate the map normally. */
  private final IState NORMAL = new SimpleState();

  // Buttons =================================================================

  /** Arrow buttons for navigating the map view via mouse clicks. */
  private final GuiArrowButton btnUp, btnDown, btnLeft, btnRight;

  /** Button for restoring player's position at the center of the Atlas. */
  private final GuiPositionButton btnPosition;

  // Navigation ==============================================================

  /**
   * Pause between after the arrow button is pressed and continuous navigation
   * starts, in ticks.
   */
  private static final int BUTTON_PAUSE = 8;

  /** How much the map view is offset, in blocks, per click (or per tick). */
  public static int navigateStep = 24;

  /**
   * The button which is currently being pressed. Used for continuous navigation
   * using the arrow buttons. Also used to prevent immediate canceling of
   * placing marker.
   */
  private GuiComponentButton selectedButton = null;

  /**
   * Time in world ticks when the button was pressed. Used to create a pause
   * before continuous navigation using the arrow buttons.
   */
  private long timeButtonPressed = 0;

  /** Set to true when dragging the map view. */
  private boolean isDragging = false;
  /** The starting cursor position when dragging. */
  private int dragMouseX, dragMouseY;
  /** Map offset at the beginning of drag. */
  private int dragMapOffsetX, dragMapOffsetY;

  /**
   * Offset to the top left corner of the tile at (0, 0) from the center of the
   * map drawing area, in pixels.
   */
  private int mapOffsetX, mapOffsetY;
  /**
   * If true, the player's icon will be in the center of the GUI, and the offset
   * of the tiles will be calculated accordingly. Otherwise it's the position of
   * the player that will be calculated with respect to the offset.
   */
  private boolean followPlayer = true;

  private GuiScaleBar scaleBar = new GuiScaleBar();
  /** Pixel-to-block ratio. */
  private double mapScale;
  /** The visual size of a tile in pixels. */
  private int tileHalfSize;
  /** The number of chunks a tile spans. */
  private int tile2ChunkScale;

  // Markers =================================================================

  /** Local markers in the current dimension */
  private DimensionMarkersData localMarkersData;
  /** Global markers in the current dimension */
  private DimensionMarkersData globalMarkersData;

  private Marker toJump;

  // Misc stuff ==============================================================

  private EntityPlayer player;
  private ItemStack stack;
  private DimensionData biomeData;

  /** Coordinate scale factor relative to the actual screen size. */
  private int screenScale;
  
  private String paymentHandlerKey;

  @SuppressWarnings("rawtypes")
  public SignPostsAtlasGui() {
    setSize(WIDTH, HEIGHT);
    setMapScale(0.5);
    followPlayer = true;
    setInterceptKeyboard(false);

    btnUp = GuiArrowButton.up();
    addChild(btnUp).offsetGuiCoords(148, 10);
    btnDown = GuiArrowButton.down();
    addChild(btnDown).offsetGuiCoords(148, 194);
    btnLeft = GuiArrowButton.left();
    addChild(btnLeft).offsetGuiCoords(15, 100);
    btnRight = GuiArrowButton.right();
    addChild(btnRight).offsetGuiCoords(283, 100);
    btnPosition = new GuiPositionButton();
    btnPosition.setEnabled(!followPlayer);
    addChild(btnPosition).offsetGuiCoords(283, 194);
    IButtonListener positionListener = new IButtonListener() {
      @Override
      public void onClick(GuiComponentButton button) {
        selectedButton = button;
        if (button.equals(btnPosition)) {
          followPlayer = true;
          btnPosition.setEnabled(false);
        } else {
          // Navigate once, before enabling pause:
          navigateByButton(selectedButton);
          timeButtonPressed = player.worldObj.getTotalWorldTime();
        }
      }
    };
    btnUp.addListener(positionListener);
    btnDown.addListener(positionListener);
    btnLeft.addListener(positionListener);
    btnRight.addListener(positionListener);
    btnPosition.addListener(positionListener);

    addChild(scaleBar).offsetGuiCoords(20, 198);
    scaleBar.setMapScale(1);
  }

  public SignPostsAtlasGui setAtlasItemStack(ItemStack stack) {
    this.player = Minecraft.getMinecraft().thePlayer;
    this.stack = stack;
    updateAtlasData();
    if (!followPlayer && AntiqueAtlasMod.settings.doSaveBrowsingPos) {
      loadSavedBrowsingPosition();
    }
    return this;
  }
  
  
  public SignPostsAtlasGui setPaymentHandlerKey(String paymentHandlerKey){
    this.paymentHandlerKey = paymentHandlerKey;
    return this;
  }

  public void loadSavedBrowsingPosition() {
    // Apply zoom first, because browsing position depends on it:
    setMapScale(biomeData.getBrowsingZoom());
    mapOffsetX = biomeData.getBrowsingX();
    mapOffsetY = biomeData.getBrowsingY();
    isDragging = false;
  }

  @Override
  public void initGui() {
    super.initGui();
    state.switchTo(NORMAL);
    Keyboard.enableRepeatEvents(true);
    screenScale = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight).getScaleFactor();
    setCentered();
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseState) {
    super.mouseClicked(mouseX, mouseY, mouseState);

    // If clicked on the map, start dragging
    int mapX = (width - MAP_WIDTH) / 2;
    int mapY = (height - MAP_HEIGHT) / 2;
    boolean isMouseOverMap = mouseX >= mapX && mouseX <= mapX + MAP_WIDTH && mouseY >= mapY && mouseY <= mapY + MAP_HEIGHT;
    
    if (isMouseOverMap && toJump != null){
      JumpMessage msg = new JumpMessage(toJump.getId(), paymentHandlerKey); 
      SignPostsNetworkHelper.sendJumpMessage(msg);
      close();
    } else if (isMouseOverMap && selectedButton == null) {
      isDragging = true;
      dragMouseX = mouseX;
      dragMouseY = mouseY;
      dragMapOffsetX = mapOffsetX;
      dragMapOffsetY = mapOffsetY;
    }
  }

  @Override
  public void handleKeyboardInput() {
    super.handleKeyboardInput();
    if (Keyboard.getEventKeyState()) {
      int key = Keyboard.getEventKey();
      if (key == Keyboard.KEY_UP) {
        navigateMap(0, navigateStep);
      } else if (key == Keyboard.KEY_DOWN) {
        navigateMap(0, -navigateStep);
      } else if (key == Keyboard.KEY_LEFT) {
        navigateMap(navigateStep, 0);
      } else if (key == Keyboard.KEY_RIGHT) {
        navigateMap(-navigateStep, 0);
      } else if (key == Keyboard.KEY_ADD || key == Keyboard.KEY_EQUALS) {
        setMapScale(mapScale * 2);
      } else if (key == Keyboard.KEY_SUBTRACT || key == Keyboard.KEY_MINUS) {
        setMapScale(mapScale / 2);
      }
      // Close the GUI if a hotbar key is pressed
      else {
        KeyBinding[] hotbarKeys = mc.gameSettings.keyBindsHotbar;
        for (KeyBinding bind : hotbarKeys) {
          if (key == bind.getKeyCode()) {
            close();
            break;
          }
        }
      }
    }
  }

  @Override
  public void handleMouseInput() {
    super.handleMouseInput();
    int wheelMove = Mouse.getEventDWheel();
    if (wheelMove != 0) {
      wheelMove = wheelMove > 0 ? 1 : -1;
      if (AntiqueAtlasMod.settings.doReverseWheelZoom)
        wheelMove *= -1;
      setMapScale(mapScale * Math.pow(2, wheelMove));
    }
  }

  @Override
  protected void mouseMovedOrUp(int mouseX, int mouseY, int mouseState) {
    super.mouseMovedOrUp(mouseX, mouseY, mouseState);
    if (mouseState != -1) {
      selectedButton = null;
      isDragging = false;
    }
  }

  @Override
  protected void mouseClickMove(int mouseX, int mouseY, int lastMouseButton, long timeSinceMouseClick) {
    super.mouseClickMove(mouseX, mouseY, lastMouseButton, timeSinceMouseClick);
    if (isDragging) {
      followPlayer = false;
      btnPosition.setEnabled(true);
      mapOffsetX = dragMapOffsetX + mouseX - dragMouseX;
      mapOffsetY = dragMapOffsetY + mouseY - dragMouseY;
    }
  }

  @Override
  public void updateScreen() {
    super.updateScreen();
    if (player == null)
      return;
    if (followPlayer) {
      mapOffsetX = (int) (-player.posX * mapScale);
      mapOffsetY = (int) (-player.posZ * mapScale);
    }
    if (player.worldObj.getTotalWorldTime() > timeButtonPressed + BUTTON_PAUSE) {
      navigateByButton(selectedButton);
    }
    updateAtlasData();
  }

  /**
   * Update {@link #biomeData}, {@link #localMarkersData},
   * {@link #globalMarkersData}
   */
  private void updateAtlasData() {
    biomeData = AntiqueAtlasMod.atlasData.getAtlasData(stack, player.worldObj).getDimensionData(player.dimension);
    globalMarkersData = AntiqueAtlasMod.globalMarkersData.getData().getMarkersDataInDimension(player.dimension);
    MarkersData markersData = AntiqueAtlasMod.markersData.getMarkersData(stack, player.worldObj);
    if (markersData != null) {
      localMarkersData = markersData.getMarkersDataInDimension(player.dimension);
    } else {
      localMarkersData = null;
    }
  }

  /** Offset the map view depending on which button was pressed. */
  private void navigateByButton(GuiComponentButton btn) {
    if (btn == null)
      return;
    if (btn.equals(btnUp)) {
      navigateMap(0, navigateStep);
    } else if (btn.equals(btnDown)) {
      navigateMap(0, -navigateStep);
    } else if (btn.equals(btnLeft)) {
      navigateMap(navigateStep, 0);
    } else if (btn.equals(btnRight)) {
      navigateMap(-navigateStep, 0);
    }
  }

  /** Offset the map view by given values, in blocks. */
  public void navigateMap(int dx, int dy) {
    mapOffsetX += dx;
    mapOffsetY += dy;
    followPlayer = false;
    btnPosition.setEnabled(true);
  }

  /**
   * Set the pixel-to-block ratio, maintaining the current center of the screen.
   */
  public void setMapScale(double scale) {
    double oldScale = mapScale;
    mapScale = scale;
    if (mapScale < AntiqueAtlasMod.settings.minScale)
      mapScale = AntiqueAtlasMod.settings.minScale;
    if (mapScale > AntiqueAtlasMod.settings.maxScale)
      mapScale = AntiqueAtlasMod.settings.maxScale;
    if (mapScale >= MIN_SCALE_THRESHOLD) {
      tileHalfSize = (int) Math.round(8 * mapScale);
      tile2ChunkScale = 1;
    } else {
      tileHalfSize = (int) Math.round(8 * MIN_SCALE_THRESHOLD);
      tile2ChunkScale = (int) Math.round(MIN_SCALE_THRESHOLD / mapScale);
    }
    // Times 2 because the contents of the Atlas are rendered at resolution 2
    // times smaller:
    scaleBar.setMapScale(mapScale * 2);
    mapOffsetX *= mapScale / oldScale;
    mapOffsetY *= mapScale / oldScale;
    dragMapOffsetX *= mapScale / oldScale;
    dragMapOffsetY *= mapScale / oldScale;
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float par3) {
    if (DEBUG_RENDERING) {
      renderTimes[renderTimesIndex++] = System.currentTimeMillis();
      if (renderTimesIndex == renderTimes.length) {
        renderTimesIndex = 0;
        double elapsed = 0;
        for (int i = 0; i < renderTimes.length - 1; i++) {
          elapsed += renderTimes[i + 1] - renderTimes[i];
        }
      }
    }

    GL11.glColor4f(1, 1, 1, 1);
    GL11.glAlphaFunc(GL11.GL_GREATER, 0); // So light detail on tiles is visible
    AtlasRenderHelper.drawFullTexture(Textures.BOOK, getGuiX(), getGuiY(), WIDTH, HEIGHT);

    if (stack == null || biomeData == null)
      return;

    GL11.glEnable(GL11.GL_SCISSOR_TEST);
    GL11.glScissor((getGuiX() + CONTENT_X) * screenScale, mc.displayHeight - (getGuiY() + CONTENT_Y + MAP_HEIGHT) * screenScale, MAP_WIDTH * screenScale, MAP_HEIGHT * screenScale);
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    // Find chunk coordinates of the top left corner of the map.
    // The 'roundToBase' is required so that when the map scales below the
    // threshold the tiles don't change when map position changes slightly.
    // The +-2 at the end provide margin so that tiles at the edges of
    // the page have their stitched texture correct.
    int mapStartX = MathUtil.roundToBase((int) Math.floor(-((double) MAP_WIDTH / 2d + mapOffsetX + 2 * tileHalfSize) / mapScale / 16d), tile2ChunkScale);
    int mapStartZ = MathUtil.roundToBase((int) Math.floor(-((double) MAP_HEIGHT / 2d + mapOffsetY + 2 * tileHalfSize) / mapScale / 16d), tile2ChunkScale);
    int mapEndX = MathUtil.roundToBase((int) Math.ceil(((double) MAP_WIDTH / 2d - mapOffsetX + 2 * tileHalfSize) / mapScale / 16d), tile2ChunkScale);
    int mapEndZ = MathUtil.roundToBase((int) Math.ceil(((double) MAP_HEIGHT / 2d - mapOffsetY + 2 * tileHalfSize) / mapScale / 16d), tile2ChunkScale);
    int mapStartScreenX = getGuiX() + WIDTH / 2 + (int) ((mapStartX << 4) * mapScale) + mapOffsetX;
    int mapStartScreenY = getGuiY() + HEIGHT / 2 + (int) ((mapStartZ << 4) * mapScale) + mapOffsetY;

    TileRenderIterator iter = new TileRenderIterator(biomeData);
    iter.setScope(new Rect().setOrigin(mapStartX, mapStartZ).set(mapStartX, mapStartZ, mapEndX, mapEndZ));
    iter.setStep(tile2ChunkScale);
    while (iter.hasNext()) {
      SubTileQuartet subtiles = iter.next();
      for (SubTile subtile : subtiles) {
        if (subtile == null || subtile.tile == null)
          continue;
        AtlasRenderHelper.drawAutotileCorner(BiomeTextureMap.instance().getTexture(subtile.tile), mapStartScreenX + subtile.x * tileHalfSize, mapStartScreenY + subtile.y * tileHalfSize, subtile.getTextureU(), subtile.getTextureV(), tileHalfSize);
      }
    }

    int markersStartX = MathUtil.roundToBase(mapStartX, MarkersData.CHUNK_STEP) / MarkersData.CHUNK_STEP - 1;
    int markersStartZ = MathUtil.roundToBase(mapStartZ, MarkersData.CHUNK_STEP) / MarkersData.CHUNK_STEP - 1;
    int markersEndX = MathUtil.roundToBase(mapEndX, MarkersData.CHUNK_STEP) / MarkersData.CHUNK_STEP + 1;
    int markersEndZ = MathUtil.roundToBase(mapEndZ, MarkersData.CHUNK_STEP) / MarkersData.CHUNK_STEP + 1;
    double iconScale = getIconScale();

    // Draw global markers:
    for (int x = markersStartX; x <= markersEndX; x++) {
      for (int z = markersStartZ; z <= markersEndZ; z++) {
        List<Marker> markers = globalMarkersData.getMarkersAtChunk(x, z);
        if (markers == null)
          continue;
        for (Marker marker : markers) {
          renderMarker(marker, iconScale, true);
        }
      }
    }

    // Draw local markers:
    if (localMarkersData != null) {
      for (int x = markersStartX; x <= markersEndX; x++) {
        for (int z = markersStartZ; z <= markersEndZ; z++) {
          List<Marker> markers = localMarkersData.getMarkersAtChunk(x, z);
          if (markers == null)
            continue;
          for (Marker marker : markers) {
            renderMarker(marker, iconScale, false);
          }
        }
      }
    }

    GL11.glDisable(GL11.GL_SCISSOR_TEST);

    // Overlay the frame so that edges of the map are smooth:
    GL11.glColor4f(1, 1, 1, 1);
    AtlasRenderHelper.drawFullTexture(Textures.BOOK_FRAME, getGuiX(), getGuiY(), WIDTH, HEIGHT);

    // Draw player icon:

    // How much the player has moved from the top left corner of the map, in
    // pixels:
    int playerOffsetX = (int) (player.posX * mapScale) + mapOffsetX;
    int playerOffsetZ = (int) (player.posZ * mapScale) + mapOffsetY;
    if (playerOffsetX < -MAP_WIDTH / 2)
      playerOffsetX = -MAP_WIDTH / 2;
    if (playerOffsetX > MAP_WIDTH / 2)
      playerOffsetX = MAP_WIDTH / 2;
    if (playerOffsetZ < -MAP_HEIGHT / 2)
      playerOffsetZ = -MAP_HEIGHT / 2;
    if (playerOffsetZ > MAP_HEIGHT / 2 - 2)
      playerOffsetZ = MAP_HEIGHT / 2 - 2;
    // Draw the icon:
    GL11.glColor4f(1, 1, 1, 1);
    GL11.glPushMatrix();
    GL11.glTranslated(getGuiX() + WIDTH / 2 + playerOffsetX, getGuiY() + HEIGHT / 2 + playerOffsetZ, 0);
    float playerRotation = (float) Math.round(player.rotationYaw / 360f * PLAYER_ROTATION_STEPS) / PLAYER_ROTATION_STEPS * 360f;
    GL11.glRotatef(180 + playerRotation, 0, 0, 1);
    GL11.glTranslated(-PLAYER_ICON_WIDTH / 2 * iconScale, -PLAYER_ICON_HEIGHT / 2 * iconScale, 0);
    AtlasRenderHelper.drawFullTexture(Textures.PLAYER, 0, 0, (int) Math.round(PLAYER_ICON_WIDTH * iconScale), (int) Math.round(PLAYER_ICON_HEIGHT * iconScale));
    GL11.glPopMatrix();
    GL11.glColor4f(1, 1, 1, 1);

    // Draw buttons:
    super.drawScreen(mouseX, mouseY, par3);

    // Draw the semi-transparent marker attached to the cursor when placing a
    // new marker:
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

  }

  private void renderMarker(Marker marker, double scale, boolean global) {
    int markerX = worldXToScreenX(marker.getX());
    int markerY = worldZToScreenY(marker.getZ());
    if (!marker.isVisibleAhead() && !biomeData.hasTileAt(marker.getChunkX(), marker.getChunkZ())) {
      return;
    }
    boolean mouseIsOverMarker = isMouseInRadius(markerX, markerY, (int) Math.ceil(MARKER_RADIUS * scale));
    
    if(global && mouseIsOverMarker && marker.getType().equals("signPost")){
      GL11.glColor4f(0.5f, 0.5f, 0.5f, 1);
      toJump = marker;
    }else {
      GL11.glColor4f(1, 1, 1, 1);
      if(toJump == marker){
        toJump = null;
      }
    }
    AtlasRenderHelper.drawFullTexture(MarkerTextureMap.instance().getTexture(marker.getType()), markerX - (double) MARKER_SIZE / 2 * scale, markerY - (double) MARKER_SIZE / 2 * scale, (int) Math.round(MARKER_SIZE * scale), (int) Math.round(MARKER_SIZE * scale));
    if (isMouseOver && mouseIsOverMarker && marker.getLabel().length() > 0) {
      drawTooltip(Arrays.asList(marker.getLocalizedLabel()), mc.fontRenderer);
    }
  }

  @Override
  public boolean doesGuiPauseGame() {
    return false;
  }

  @Override
  public void onGuiClosed() {
    super.onGuiClosed();
    Keyboard.enableRepeatEvents(false);
    biomeData.setBrowsingPosition(mapOffsetX, mapOffsetY, mapScale);
    PacketDispatcher.sendToServer(new BrowsingPositionPacket(stack.getItemDamage(), player.dimension, mapOffsetX, mapOffsetY, mapScale));
  }

  private int worldXToScreenX(int x) {
    return (int) Math.round((double) x * mapScale + this.width / 2 + mapOffsetX);
  }

  private int worldZToScreenY(int z) {
    return (int) Math.round((double) z * mapScale + this.height / 2 + mapOffsetY);
  }

  /** Returns the scale of markers and player icon at given mapScale. */
  private double getIconScale() {
    return AntiqueAtlasMod.settings.doScaleMarkers ? (mapScale < 0.5 ? 0.5 : mapScale > 1 ? 2 : 1) : 1;
  }
}
