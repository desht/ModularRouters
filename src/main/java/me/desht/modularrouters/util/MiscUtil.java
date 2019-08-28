package me.desht.modularrouters.util;

import me.desht.modularrouters.ModularRouters;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class MiscUtil {

    private static final int WRAP_LENGTH = 45;

    public static void appendMultiline(List<String> result, String key, Object... args) {
        ITextComponent raw = xlate(key, args);
        int n = 0;
        for (String s : raw.getString().split("\\\\n")) {
            for (String s1 : WordUtils.wrap(s, WRAP_LENGTH).split("\\\n")) {
                result.add((n++ > 0 ? "\u00a77" : "") + s1);
            }
        }
    }

    public static void appendMultilineText(List<ITextComponent> result, TextFormatting formatting, String key, Object... args) {
        for (String s : I18n.format(key, args).split("\\\\n")) {
            for (String s1 : WordUtils.wrap(s, WRAP_LENGTH).split("\\\n")) {
                ITextComponent textComponent = new StringTextComponent(s1);
                result.add(textComponent.applyTextStyle(formatting));
            }
        }
    }

    public static String[] splitLong(String key, int len, Object... args) {
        return WordUtils.wrap(I18n.format(key, args), len, "=CUT", false, "\\n").split("=CUT");
    }

    public static List<String> wrapString(String text) {
        return wrapString(text, WRAP_LENGTH);
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

    public static String locToString(int dim, BlockPos pos) {
        return String.format("DIM:%d [%d,%d,%d]", dim, pos.getX(), pos.getY(), pos.getZ());
    }

    public static ITextComponent xlate(String key, Object... args) {
        return new TranslationTextComponent(key, args);
    }

    public static ResourceLocation RL(String name) {
        return new ResourceLocation(ModularRouters.MODID, name);
    }

    public static int getYawFromFacing(Direction facing) {
        switch (facing) {
            case NORTH:
                return 180;
            case SOUTH:
                return 0;
            case WEST:
                return 90;
            case EAST:
                return -90;
            default:
                return 0;
        }
    }

    public static String getFluidName(ItemStack stack) {
        return FluidUtil.getFluidContained(stack)
                .map(fluidStack -> fluidStack.getFluid().getLocalizedName(fluidStack))
                .orElse(stack.getDisplayName().getString());
    }

    public static ITextComponent settingsStr(String prefix, ITextComponent c) {
        return new StringTextComponent(prefix).appendSibling(c);
    }

    public static CompoundNBT serializeGlobalPos(GlobalPos globalPos) {
        CompoundNBT tag = new CompoundNBT();
        tag.put("pos", net.minecraft.nbt.NBTUtil.writeBlockPos(globalPos.getPos()));
        tag.putString("dim", DimensionType.getKey(globalPos.getDimension()).toString());
        return tag;
    }

    public static GlobalPos deserializeGlobalPos(CompoundNBT tag) {
        return GlobalPos.of(
                DimensionType.byName(new ResourceLocation(tag.getString("dim"))),
                NBTUtil.readBlockPos(tag.getCompound("pos"))
        );
    }

    public static ServerWorld getWorldForGlobalPos(GlobalPos pos) {
        return DimensionManager.getWorld(ServerLifecycleHooks.getCurrentServer(), pos.getDimension(), false, false);
    }
}
