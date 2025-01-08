package harceroi.mc.signposts.data;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

import harceroi.mc.signposts.SignPostsMod;

public class MarkerToTileMap extends WorldSavedData {

    public static final String DATA_NAME = SignPostsMod.ID + "_MarkerToTileMap";

    public Map<Integer, int[]> tileMap = new ConcurrentHashMap<Integer, int[]>();

    public MarkerToTileMap() {
        super(DATA_NAME);
    }

    public MarkerToTileMap(String s) {
        super(s);
    }

    public static MarkerToTileMap get(World world) {
        // MarkerToTileMap data = (MarkerToTileMap)
        // world.loadItemData(MarkerToTileMap.class, DATA_NAME);

        MapStorage storage = world.perWorldStorage;
        MarkerToTileMap data = (MarkerToTileMap) storage.loadData(MarkerToTileMap.class, DATA_NAME);

        if (data == null) {
            data = new MarkerToTileMap();
            storage.setData(DATA_NAME, data);
        }

        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        NBTTagList nbtList = compound.getTagList("arrayList", 10);
        for (int i = 0; i < nbtList.tagCount(); i++) {
            NBTTagCompound nbt = nbtList.getCompoundTagAt(i);
            int markerId = nbt.getInteger("markerId");
            int x = nbt.getInteger("x");
            int y = nbt.getInteger("y");
            int z = nbt.getInteger("z");

            tileMap.put(new Integer(markerId), new int[] { x, y, z });
        }

    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        NBTTagList nbtList = new NBTTagList();

        for (Map.Entry<Integer, int[]> entry : tileMap.entrySet()) {
            Integer markerId = entry.getKey();
            int[] tile = entry.getValue();

            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("markerId", markerId.intValue());
            nbt.setInteger("x", tile[0]);
            nbt.setInteger("y", tile[1]);
            nbt.setInteger("z", tile[2]);

            nbtList.appendTag(nbt);
        }
        compound.setTag("arrayList", nbtList);

    }

    public void setTileForMarker(int x, int y, int z, int markerId) {
        tileMap.put(new Integer(markerId), new int[] { x, y, z });
        setDirty(true);
    }

    public void removeMarker(int markerId) {
        tileMap.remove(new Integer(markerId));
        setDirty(true);
    }

    public int[] getTileForMarker(int markerId) {
        return tileMap.get(new Integer(markerId));
    }

    public int getMarkerForTile(int x, int y, int z) {
        Set<Entry<Integer, int[]>> set = tileMap.entrySet();
        for (Entry<Integer, int[]> entry : set) {
            int[] mapCoords = entry.getValue();
            if (mapCoords[0] == x && mapCoords[1] == y && mapCoords[2] == z) {
                return entry.getKey();
            }
        }
        return -1;
    }
}
