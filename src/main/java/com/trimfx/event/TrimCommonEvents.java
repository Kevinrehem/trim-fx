package com.trimfx.event;

import com.trimfx.TrimFxMod;
import com.trimfx.config.TrimFxConfig;
import com.trimfx.domain.PatternBonusCatalog;
import com.trimfx.domain.TrimSetSummary;
import com.trimfx.domain.TrimTier;
import com.trimfx.service.MaterialAttributeApplier;
import com.trimfx.service.PatternEffectService;
import com.trimfx.service.TrimEquipmentExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = TrimFxMod.MOD_ID)
public final class TrimCommonEvents {

    private static final int VANILLA_PEARL_COOLDOWN_TICKS = 20;

    private TrimCommonEvents() {
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || !isHumanoidArmorSlot(event.getSlot())) {
            return;
        }
        TrimSetSummary summary = TrimEquipmentExtractor.evaluateEntity(entity);
        MaterialAttributeApplier.refreshEntityAttributes(entity, summary);
    }

    @SubscribeEvent
    public static void onPlayerTickPost(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        long gameTime = player.level().getGameTime();
        if (gameTime % 5L != 0L) {
            return;
        }
        TrimSetSummary summary = TrimEquipmentExtractor.evaluateEntity(player);
        PatternEffectService.applyPeriodicTickEffects(player, summary);
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) {
            return;
        }
        TrimSetSummary victimSummary = TrimEquipmentExtractor.evaluateEntity(victim);
        if (PatternEffectService.shouldCancelIncomingDamage(victim, event.getSource(), victimSummary)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) {
            return;
        }
        TrimSetSummary victimSummary = TrimEquipmentExtractor.evaluateEntity(victim);
        TrimSetSummary attackerSummary = null;
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            attackerSummary = TrimEquipmentExtractor.evaluateEntity(attacker);
        }
        float modified = PatternEffectService.computeModifiedDamage(
                victim,
                event.getSource(),
                event.getNewDamage(),
                victimSummary,
                attackerSummary
        );
        event.setNewDamage(modified);
    }

    @SubscribeEvent
    public static void onLivingDamagePost(LivingDamageEvent.Post event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) {
            return;
        }
        TrimSetSummary victimSummary = TrimEquipmentExtractor.evaluateEntity(victim);
        TrimSetSummary attackerSummary = null;
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            attackerSummary = TrimEquipmentExtractor.evaluateEntity(attacker);
        }
        PatternEffectService.handlePostDamageProcs(
                victim,
                event.getSource(),
                event.getInflictedDamage(),
                victimSummary,
                attackerSummary
        );
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) {
            return;
        }
        if (event.getSource().getEntity() instanceof LivingEntity killer) {
            TrimSetSummary killerSummary = TrimEquipmentExtractor.evaluateEntity(killer);
            PatternEffectService.handleEntityKill(killer, victim, killerSummary);
        }
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        TrimSetSummary summary = TrimEquipmentExtractor.evaluateEntity(entity);
        float modified = PatternEffectService.computeModifiedHealing(event.getAmount(), summary);
        event.setAmount(modified);
    }

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        TrimSetSummary summary = TrimEquipmentExtractor.evaluateEntity(player);
        PatternEffectService.handleFoodConsumed(player, event.getItem(), summary);
    }

    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.level().isClientSide()) {
            return;
        }
        TrimSetSummary summary = TrimEquipmentExtractor.evaluateEntity(player);
        PatternEffectService.tryConserveToolDurability(player, summary);
        applyEyePearlCooldownReductionIfEquipped(player, summary);
    }

    @SubscribeEvent
    public static void onPickupXp(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        ExperienceOrb orb = event.getOrb();
        int baseValue = orb.getValue();
        TrimSetSummary summary = TrimEquipmentExtractor.evaluateEntity(player);
        int modifiedValue = PatternEffectService.computeModifiedXpValue(baseValue, summary);
        int bonusXp = modifiedValue - baseValue;
        if (bonusXp > 0) {
            player.giveExperiencePoints(bonusXp);
        }
    }

    @SubscribeEvent
    public static void onLivingVisibility(LivingEvent.LivingVisibilityEvent event) {
        LivingEntity entity = event.getEntity();
        TrimSetSummary summary = TrimEquipmentExtractor.evaluateEntity(entity);
        double multiplier = PatternEffectService.computeVisibilityMultiplier(summary);
        if (multiplier != 1.0D) {
            event.modifyVisibility(multiplier);
        }
    }

    @SubscribeEvent
    public static void onVanillaGameEvent(VanillaGameEvent event) {
        if (!TrimFxConfig.arePatternBonusesEnabled()) {
            return;
        }
        if (!(event.getCause() instanceof LivingEntity entity)) {
            return;
        }
        if (!isMovementVibration(event)) {
            return;
        }
        TrimSetSummary summary = TrimEquipmentExtractor.evaluateEntity(entity);
        if (summary.getPatternTier(PatternBonusCatalog.SILENCE).isAtLeast(TrimTier.TIER_3)) {
            event.setCanceled(true);
        }
    }

    private static boolean isMovementVibration(VanillaGameEvent event) {
        return event.getVanillaEvent().is(GameEvent.STEP)
                || event.getVanillaEvent().is(GameEvent.HIT_GROUND);
    }

    private static void applyEyePearlCooldownReductionIfEquipped(Player player, TrimSetSummary summary) {
        TrimTier eyeTier = summary.getPatternTier(PatternBonusCatalog.EYE);
        if (!eyeTier.isActive()) {
            return;
        }
        ItemStack pearlStack = new ItemStack(Items.ENDER_PEARL);
        if (player.getCooldowns().isOnCooldown(pearlStack)) {
            double reduction = PatternBonusCatalog.find(PatternBonusCatalog.EYE)
                    .map(spec -> spec.getPrimaryValue(eyeTier, TrimFxConfig.getPatternMultiplier()))
                    .orElse(0.25D);
            int reducedTicks = Math.max(2, (int) Math.round(VANILLA_PEARL_COOLDOWN_TICKS * (1.0D - reduction)));
            player.getCooldowns().addCooldown(pearlStack, reducedTicks);
        }
    }

    private static boolean isHumanoidArmorSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD
                || slot == EquipmentSlot.CHEST
                || slot == EquipmentSlot.LEGS
                || slot == EquipmentSlot.FEET;
    }
}
