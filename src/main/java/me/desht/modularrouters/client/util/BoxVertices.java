package me.desht.modularrouters.client.util;

import net.minecraft.core.Direction;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

/**
 * Used for rendering module targets and camo'd router highlighting
 */
public class BoxVertices {
    private static final float BOX_SIZE = 0.5f;
    public static final Map<Direction, float[][]> FACE_VERTICES = Util.make(new EnumMap<>(Direction.class), map -> {
        map.put(Direction.DOWN, new float[][]{{0, 0, 0}, {BOX_SIZE, 0, 0}, {BOX_SIZE, 0, BOX_SIZE}, {0, 0, BOX_SIZE}});
        map.put(Direction.UP, new float[][]{{0, BOX_SIZE, BOX_SIZE}, {BOX_SIZE, BOX_SIZE, BOX_SIZE}, {BOX_SIZE, BOX_SIZE, 0}, {0, BOX_SIZE, 0}});
        map.put(Direction.NORTH, new float[][]{{0, 0, 0}, {0, BOX_SIZE, 0}, {BOX_SIZE, BOX_SIZE, 0}, {BOX_SIZE, 0, 0}});
        map.put(Direction.SOUTH, new float[][]{{BOX_SIZE, 0, BOX_SIZE}, {BOX_SIZE, BOX_SIZE, BOX_SIZE}, {0, BOX_SIZE, BOX_SIZE}, {0, 0, BOX_SIZE}});
        map.put(Direction.WEST, new float[][]{{0, 0, 0}, {0, 0, BOX_SIZE}, {0, BOX_SIZE, BOX_SIZE}, {0, BOX_SIZE, 0}});
        map.put(Direction.EAST, new float[][]{{BOX_SIZE, BOX_SIZE, 0}, {BOX_SIZE, BOX_SIZE, BOX_SIZE}, {BOX_SIZE, 0, BOX_SIZE}, {BOX_SIZE, 0, 0}});
    });
    public static final float BOX_START = (1f - BOX_SIZE) / 2f;
    public static final VoxelShape SHAPE = Block.box(
            BOX_START * 16, BOX_START * 16, BOX_START * 16,
            16 - BOX_START * 16, 16 - BOX_START * 16, 16 - BOX_START * 16
    );
}
