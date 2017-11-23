package me.desht.modularrouters.util;

import me.desht.modularrouters.ModularRouters;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class MiscUtil {
    public static void appendMultiline(List<String> result, String key, Object... args) {
        String raw = translate(key, args);
        int n = 0;
        for (String s : raw.split("\\\\n")) {
            result.add((n++ > 0 ? "\u00a77" : "") + s);
        }
    }

    public static String[] splitLong(String key, int len, Object... args) {
        return WordUtils.wrap(I18n.format(key, args), len, "=CUT", false, "\\n").split("=CUT");
    }

    public static List<String> wrapString(String text, int maxCharPerLine) {
        StringTokenizer tok = new StringTokenizer(text, " ");
        StringBuilder output = new StringBuilder(text.length());
        List<String> textList = new ArrayList<>();
        String color = "";
        int lineLen = 0;
        while (tok.hasMoreTokens()) {
            String word = tok.nextToken();
            if (word.contains("\u00a7")) {
                // save text formatting information so it can be continued on the next line
                for (int i = 0; i < word.length() - 1; i++) {
                    if (word.substring(i, i + 2).contains("\u00a7"))
                        color = word.substring(i, i + 2);
                }
                lineLen -= 2; // formatting doesn't count toward line length
            }
            if (lineLen + word.length() > maxCharPerLine || word.contains("\\n")) {
                word = word.replace("\\n", "");
                textList.add(output.toString());
                output.delete(0, output.length());
                output.append(color);
                lineLen = 0;
            } else if (lineLen > 0) {
                output.append(" ");
                lineLen++;
            }
            output.append(word);
            lineLen += word.length();
        }
        textList.add(output.toString());
        return textList;
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

    public static TileEntity getTileEntitySafely(IBlockAccess world, BlockPos pos) {
        return world instanceof ChunkCache ?
                ((ChunkCache) world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) :
                world.getTileEntity(pos);
    }
}
