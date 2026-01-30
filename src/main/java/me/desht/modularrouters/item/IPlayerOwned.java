package me.desht.modularrouters.item;

import com.mojang.authlib.GameProfile;
import me.desht.modularrouters.core.ModDataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.Optional;

public interface IPlayerOwned {
    default Optional<GameProfile> getOwnerProfile(ItemStack stack) {
        //noinspection DataFlowIssue
        return stack.has(ModDataComponents.OWNER) ?
                Optional.of(stack.get(ModDataComponents.OWNER).partialProfile()) :
                Optional.empty();
    }

    default void setOwner(ItemStack stack, Player player) {
        stack.set(ModDataComponents.OWNER, ResolvableProfile.createResolved(player.getGameProfile()));
    }
}
