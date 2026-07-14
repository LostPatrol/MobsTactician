package net.lostpatrol.mobstactician.entity.ai.witch;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

public final class WitchTacticalPotions {
    private static final int LONG_DURATION_TICKS = 9600;
    private static final float LEVEL_TWO_CHANCE = 0.15F;

    private WitchTacticalPotions() {
    }

    public static ItemStack create(Item item, Holder<MobEffect> effect, String potionName, RandomSource random) {
        int amplifier = random.nextFloat() < LEVEL_TWO_CHANCE ? 1 : 0;
        PotionContents contents = new PotionContents(
                Optional.empty(),
                Optional.empty(),
                List.of(new MobEffectInstance(effect, LONG_DURATION_TICKS, amplifier)),
                Optional.of(potionName)
        );
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.POTION_CONTENTS, contents);
        return stack;
    }
}
