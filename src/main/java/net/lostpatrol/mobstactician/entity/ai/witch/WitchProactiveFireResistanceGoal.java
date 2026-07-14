package net.lostpatrol.mobstactician.entity.ai.witch;

import java.util.EnumSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class WitchProactiveFireResistanceGoal extends Goal {
    private static final int REFRESH_THRESHOLD_TICKS = 1200;

    private final Witch witch;

    public WitchProactiveFireResistanceGoal(Witch witch) {
        this.witch = witch;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.witch.getTarget() != null
                || this.witch.isDrinkingPotion()
                || !this.witch.getMainHandItem().isEmpty()) {
            return false;
        }

        MobEffectInstance fireResistance = this.witch.getEffect(MobEffects.FIRE_RESISTANCE);
        return fireResistance == null || fireResistance.getDuration() <= REFRESH_THRESHOLD_TICKS;
    }

    @Override
    public boolean canContinueToUse() {
        return this.witch.getTarget() == null && this.witch.isDrinkingPotion();
    }

    @Override
    public void start() {
        this.witch.getNavigation().stop();
        ItemStack potion = WitchTacticalPotions.create(
                Items.POTION,
                MobEffects.FIRE_RESISTANCE,
                "fire_resistance",
                this.witch.getRandom()
        );
        this.witch.setItemSlot(EquipmentSlot.MAINHAND, potion);
        this.witch.usingTime = potion.getUseDuration(this.witch);
        this.witch.setUsingItem(true);

        if (!this.witch.isSilent()) {
            this.witch.level().playSound(
                    null,
                    this.witch.getX(),
                    this.witch.getY(),
                    this.witch.getZ(),
                    SoundEvents.WITCH_DRINK,
                    this.witch.getSoundSource(),
                    1.0F,
                    0.8F + this.witch.getRandom().nextFloat() * 0.4F
            );
        }

        AttributeInstance movementSpeed = this.witch.getAttribute(Attributes.MOVEMENT_SPEED);
        movementSpeed.removeModifier(Witch.SPEED_MODIFIER_DRINKING_ID);
        movementSpeed.addTransientModifier(Witch.SPEED_MODIFIER_DRINKING);
    }
}
