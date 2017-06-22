package me.desht.modularrouters.util;

import me.desht.modularrouters.ModularRouters;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import org.apache.commons.lang3.text.WordUtils;

import java.util.List;

public class MiscUtil {
    public static void appendMultiline(List<String> result, String key, Object... args) {
        String raw = translate(key, args);
        int n = 0;
        for (String s : raw.split("\\\\n")) {
            result.add((n++ > 0 ? "\u00a77" : "") + s);
        }
    }

    public static String[] splitLong(String key, int len, Object... args) {
        return WordUtils.wrap(I18n.format(key, args), len, "=CUT", false).split("=CUT");
    }

    public static String locToString(World world, BlockPos pos) {
        return locToString(world.provider.getDimension(), pos);
    }

    public static String locToString(int dim, BlockPos pos) {
        return String.format("DIM:%d X:%d Y:%d Z:%d", dim, pos.getX(), pos.getY(), pos.getZ());
    }

    public static String translate(String key, Object... args) {
        return new TextComponentTranslation(key, args).getUnformattedText();
    }

    public static ResourceLocation RL(String name) {
        return new ResourceLocation(ModularRouters.MODID, name);
    }
}
