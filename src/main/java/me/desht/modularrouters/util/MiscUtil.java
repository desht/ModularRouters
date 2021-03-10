package me.desht.modularrouters.util;

import me.desht.modularrouters.ModularRouters;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MiscUtil {

    private static final int WRAP_LENGTH = 45;

    public static void appendMultilineText(List<ITextComponent> result, TextFormatting formatting, String key, Object... args) {
        for (String s : I18n.get(key, args).split(Pattern.quote("${br}"))) {
            for (String s1 : WordUtils.wrap(s, WRAP_LENGTH).split("\n")) {
                result.add(new StringTextComponent(s1).withStyle(formatting));
            }
        }
    }

    public static IFormattableTextComponent asFormattable(ITextComponent component) {
        return component instanceof IFormattableTextComponent ? (IFormattableTextComponent) component : component.plainCopy();
    }

    public static List<String> wrapString(String text) {
        return wrapString(text, WRAP_LENGTH);
    }

    public static List<ITextComponent> wrapStringAsTextComponent(String text) {
        return wrapString(text, WRAP_LENGTH).stream().map(StringTextComponent::new).collect(Collectors.toList());
    }

    public static List<String> wrapString(String text, int maxCharPerLine) {
        List<String> result = new ArrayList<>();

        StringBuilder builder = new StringBuilder(text.length());
        String format = "";
        for (String para : text.split(Pattern.quote("${br}"))) {
            StringTokenizer tok = new StringTokenizer(para, " ");
            int lineLen = 0;
            while (tok.hasMoreTokens()) {
                String word = tok.nextToken();
                int idx = word.lastIndexOf("\u00a7");
                if (idx >= 0 && idx < word.length() - 1) {
                    // note the formatting sequence so we can apply to next line if any
                    format = word.substring(idx, idx + 2);
                    // formatting sequence does not contribute to line length
                    lineLen -= 2;
                }
                if (lineLen + word.length() > maxCharPerLine) {
                    result.add(builder.toString());
                    builder.delete(0, builder.length());
                    builder.append(format);
                    lineLen = 0;
                } else if (lineLen > 0) {
                    builder.append(" ");
                    lineLen++;
                }
                builder.append(word);
                lineLen += word.length();
            }
            result.add(builder.toString());
            builder.delete(0, builder.length());
            builder.append(format);
        }
        return result;
    }

    public static String locToString(ResourceLocation dim, BlockPos pos) {
        return String.format("%s [%d,%d,%d]", dim.toString(), pos.getX(), pos.getY(), pos.getZ());
    }

    public static String locToString(GlobalPos pos) {
        return locToString(pos.dimension().location(), pos.pos());
    }

    public static ResourceLocation RL(String name) {
        return new ResourceLocation(ModularRouters.MODID, name);
    }

    public static int getYawFromFacing(Direction facing) {
        switch (facing) {
            case NORTH:
                return 180;
            case WEST:
                return 90;
            case EAST:
                return -90;
            default: // including SOUTH
                return 0;
        }
    }

    public static ITextComponent settingsStr(String prefix, ITextComponent c) {
        return new StringTextComponent(prefix).append(c);  // appendSibling
    }

    public static CompoundNBT serializeGlobalPos(GlobalPos globalPos) {
        CompoundNBT tag = new CompoundNBT();
        tag.put("pos", net.minecraft.nbt.NBTUtil.writeBlockPos(globalPos.pos()));
        tag.putString("dim", globalPos.dimension().location().toString());
        return tag;
    }

    public static GlobalPos deserializeGlobalPos(CompoundNBT tag) {
        RegistryKey<World> worldKey = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString("dim")));
        return GlobalPos.of(worldKey, NBTUtil.readBlockPos(tag.getCompound("pos")));
    }

    public static ServerWorld getWorldForGlobalPos(GlobalPos pos) {
        return ServerLifecycleHooks.getCurrentServer().getLevel(pos.dimension());
    }

    public static GlobalPos makeGlobalPos(World w, BlockPos pos) {
        return GlobalPos.of(w.dimension(), pos);
    }

    // this method from Block went missing in 1.16.2
    public static boolean blockHasSolidSide(BlockState state, IBlockReader worldIn, BlockPos pos, Direction side) {
        return Block.isFaceFull(state.getBlockSupportShape(worldIn, pos), side);
    }
}
