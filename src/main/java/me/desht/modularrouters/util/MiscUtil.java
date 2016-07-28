package me.desht.modularrouters.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import java.util.List;

public class MiscUtil {
    public static void processTooltip(List<String> result, String key, Object... args) {
        String raw = I18n.translateToLocalFormatted(key, args);
        int n = 0;
        for (String s : raw.split("//")) {
            result.add((n++ > 0 ? "\u00a77" : "") + s);
        }
    }

    public static String locToString(World world, BlockPos pos) {
        return String.format("[%d %d,%d,%d]", world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ());
    }
}
