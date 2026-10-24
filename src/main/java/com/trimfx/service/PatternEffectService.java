package com.trimfx.service;

import com.trimfx.config.TrimFxConfig;
import com.trimfx.domain.MaterialBonusCatalog;
import com.trimfx.domain.PatternBonusCatalog;
import com.trimfx.domain.TrimSetSummary;
import com.trimfx.domain.TrimTier;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class PatternEffectService {

    private static final ThreadLocal<Boolean> PROCESSING_TRIM_DAMAGE = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static final Map<UUID, Long> DUNE_COOLDOWN_EXPIRY = new ConcurrentHashMap<>();

    private static final int PASSIVE_BUFF_DURATION_TICKS = 240;
    private static final int SHORT_BUFF_DURATION_TICKS = 80;
    private static final double BOLT_CHAIN_RADIUS = 4.5D;
    private static final int BOLT_MAX_TARGETS = 3;
    private static final double MAGNET_PULL_STRENGTH = 0.09D;
    private static final double PACIFY_SCAN_RADIUS = 14.0D;
    private static final Identifier ENDERMAN_ENTITY_ID = Identifier.withDefaultNamespace("enderman");
    private static final Identifier ENDER_PEARL_ENTITY_ID = Identifier.withDefaultNamespace("ender_pearl");

    private PatternEffectService() {
    }

    public static void applyPeriodicTickEffects(Player player, TrimSetSummary summary) {
        if (player == null || player.level().isClientSide() || !TrimFxConfig.arePatternBonusesEnabled()) {
            return;
        }
        long gameTime = player.level().getGameTime();
        if (gameTime % 5L == 0L) {
            applyFastPeriodicEffects(player, summary);
        }
        if (gameTime % 20L == 0L) {
            applyOneSecondPeriodicEffects(player, summary);
        }
    }

    private static void applyFastPeriodicEffects(Player player, TrimSetSummary summary) {
        TrimTier hostTier = summary.getPatternTier(PatternBonusCatalog.HOST);
        if (hostTier.isActive()) {
            pullNearbyItemsAndXp(player, hostTier);
        }

        TrimTier spireTier = summary.getPatternTier(PatternBonusCatalog.SPIRE);
        if (spireTier.isActive()) {
            handleSpireLevitationAndGlide(player);
        }
    }

    private static void applyOneSecondPeriodicEffects(Player player, TrimSetSummary summary) {
        applyCoastPassives(player, summary.getPatternTier(PatternBonusCatalog.COAST));
        applyTidePassives(player, summary.getPatternTier(PatternBonusCatalog.TIDE));
        applyFlowPassives(player, summary.getPatternTier(PatternBonusCatalog.FLOW));
        applyWardPassives(player, summary.getPatternTier(PatternBonusCatalog.WARD));
        applyWildAndRibImmunities(player, summary);
        applyWayfinderStamina(player, summary.getPatternTier(PatternBonusCatalog.WAYFINDER));
        applySnoutPiglinPacification(player, summary.getPatternTier(PatternBonusCatalog.SNOUT));
        applyEyeEndermanPacification(player, summary.getPatternTier(PatternBonusCatalog.EYE));
    }

    private static void applyCoastPassives(Player player, TrimTier tier) {
        if (!tier.isActive()) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, PASSIVE_BUFF_DURATION_TICKS, 0, true, false, true));
        if (tier.isAtLeast(TrimTier.TIER_3) && player.isInWaterOrRain()) {
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, SHORT_BUFF_DURATION_TICKS, 0, true, false, true));
        }
    }

    private static void applyTidePassives(Player player, TrimTier tier) {
        if (tier.isAtLeast(TrimTier.TIER_3) && player.isInWaterOrRain()) {
            player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, PASSIVE_BUFF_DURATION_TICKS, 0, true, false, true));
        }
    }

    private static void applyFlowPassives(Player player, TrimTier tier) {
        if (!tier.isActive()) {
            return;
        }
        int amplifier = Math.max(0, tier.level() - 1);
        player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, PASSIVE_BUFF_DURATION_TICKS, amplifier, true, false, true));
    }

    private static void applyWardPassives(Player player, TrimTier tier) {
        if (!tier.isActive()) {
            return;
        }
        if (player.hasEffect(MobEffects.DARKNESS)) {
            player.removeEffect(MobEffects.DARKNESS);
        }
        int light = player.level().getMaxLocalRawBrightness(player.blockPosition());
        if (tier.isAtLeast(TrimTier.TIER_3) && light <= PatternBonusCatalog.MAX_DARKNESS_LIGHT_LEVEL) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, PASSIVE_BUFF_DURATION_TICKS + 100, 0, true, false, true));
        }
    }

    private static void applyWildAndRibImmunities(Player player, TrimSetSummary summary) {
        if (summary.getPatternTier(PatternBonusCatalog.WILD).isAtLeast(TrimTier.TIER_3) && player.hasEffect(MobEffects.POISON)) {
            player.removeEffect(MobEffects.POISON);
        }
        if (summary.getPatternTier(PatternBonusCatalog.RIB).isAtLeast(TrimTier.TIER_3) && player.hasEffect(MobEffects.WITHER)) {
            player.removeEffect(MobEffects.WITHER);
        }
    }

    private static void applyWayfinderStamina(Player player, TrimTier tier) {
        if (!tier.isActive() || !player.isSprinting()) {
            return;
        }
        float currentSaturation = player.getFoodData().getSaturationLevel();
        float bonus = 0.05F * tier.level();
        player.getFoodData().setSaturation(Math.min(20.0F, currentSaturation + bonus));
    }

    private static void applySnoutPiglinPacification(Player player, TrimTier tier) {
        if (!tier.isActive()) {
            return;
        }
        AABB box = player.getBoundingBox().inflate(PACIFY_SCAN_RADIUS);
        List<Piglin> piglins = player.level().getEntitiesOfClass(Piglin.class, box, p -> p.getTarget() == player);
        for (Piglin piglin : piglins) {
            piglin.setTarget(null);
        }
    }

    private static void applyEyeEndermanPacification(Player player, TrimTier tier) {
        if (!tier.isActive()) {
            return;
        }
        AABB box = player.getBoundingBox().inflate(PACIFY_SCAN_RADIUS);
        List<Mob> endermen = player.level().getEntitiesOfClass(
                Mob.class,
                box,
                m -> ENDERMAN_ENTITY_ID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(m.getType())) && m.getTarget() == player
        );
        for (Mob enderMan : endermen) {
            if (enderMan.getLastHurtByMob() != player) {
                enderMan.setTarget(null);
            }
        }
    }

    private static void pullNearbyItemsAndXp(Player player, TrimTier hostTier) {
        double mult = TrimFxConfig.getPatternMultiplier();
        double radius = PatternBonusCatalog.find(PatternBonusCatalog.HOST)
                .map(spec -> spec.getSecondaryValue(hostTier, mult))
                .orElse(2.0D);
        AABB area = player.getBoundingBox().inflate(Math.min(8.0D, radius));
        Vec3 targetPos = player.position().add(0.0D, 0.5D, 0.0D);

        for (ItemEntity item : player.level().getEntitiesOfClass(ItemEntity.class, area, Entity::isAlive)) {
            if (!item.hasPickUpDelay()) {
                nudgeEntityToward(item, targetPos);
            }
        }
        for (ExperienceOrb orb : player.level().getEntitiesOfClass(ExperienceOrb.class, area, Entity::isAlive)) {
            nudgeEntityToward(orb, targetPos);
        }
    }

    private static void nudgeEntityToward(Entity entity, Vec3 targetPos) {
        Vec3 delta = targetPos.subtract(entity.position());
        double distSq = delta.lengthSqr();
        if (distSq > 0.25D) {
            Vec3 velocity = delta.normalize().scale(MAGNET_PULL_STRENGTH);
            entity.setDeltaMovement(entity.getDeltaMovement().add(velocity));
        }
    }

    private static void handleSpireLevitationAndGlide(Player player) {
        if (player.hasEffect(MobEffects.LEVITATION)) {
            player.removeEffect(MobEffects.LEVITATION);
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, SHORT_BUFF_DURATION_TICKS, 0, true, false, true));
        }
        if (player.isShiftKeyDown() && !player.onGround() && player.getDeltaMovement().y < -0.05D) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20, 0, true, false, true));
        }
    }

    public static boolean shouldCancelIncomingDamage(LivingEntity victim, DamageSource source, TrimSetSummary summary) {
        if (victim == null || victim.level().isClientSide() || !TrimFxConfig.arePatternBonusesEnabled()) {
            return false;
        }
        if (shouldCancelEyePearlDamage(source, summary.getPatternTier(PatternBonusCatalog.EYE))) {
            return true;
        }
        if (summary.getPatternTier(PatternBonusCatalog.FLOW).isAtLeast(TrimTier.TIER_3) && source.is(DamageTypes.FALL)) {
            return true;
        }
        if (summary.getPatternTier(PatternBonusCatalog.RIB).isAtLeast(TrimTier.TIER_3) && source.is(DamageTypes.WITHER)) {
            return true;
        }
        return tryTriggerVexDodge(victim, source, summary.getPatternTier(PatternBonusCatalog.VEX));
    }

    private static boolean shouldCancelEyePearlDamage(DamageSource source, TrimTier eyeTier) {
        if (!eyeTier.isActive()) {
            return false;
        }
        return source.is(DamageTypes.FALL)
                && source.getDirectEntity() != null
                && ENDER_PEARL_ENTITY_ID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(source.getDirectEntity().getType()));
    }

    private static boolean tryTriggerVexDodge(LivingEntity victim, DamageSource source, TrimTier vexTier) {
        if (!vexTier.isActive() || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        if (source.getEntity() == null && source.getDirectEntity() == null) {
            return false;
        }
        double chance = PatternBonusCatalog.find(PatternBonusCatalog.VEX)
                .map(spec -> spec.getPrimaryValue(vexTier, TrimFxConfig.getPatternMultiplier()))
                .orElse(0.0D);
        if (victim.getRandom().nextDouble() >= chance) {
            return false;
        }
        playFeedback(victim, ParticleTypes.PORTAL, 16, SoundEvents.ENDERMAN_TELEPORT, 0.7F, 1.4F);
        return true;
    }

    public static float computeModifiedDamage(
            LivingEntity victim,
            DamageSource source,
            float currentDamage,
            TrimSetSummary victimSummary,
            TrimSetSummary attackerSummary
    ) {
        if (currentDamage <= 0.0F || !TrimFxConfig.arePatternBonusesEnabled()) {
            return Math.max(0.0F, currentDamage);
        }
        float modified = currentDamage;
        if (attackerSummary != null && source.getEntity() instanceof LivingEntity attacker) {
            modified = applyOffensiveDamageBonuses(attacker, victim, source, modified, attackerSummary);
        }
        if (victimSummary != null && victim != null) {
            modified = applyDefensiveDamageReductions(victim, source, modified, victimSummary);
        }
        return Math.max(0.0F, modified);
    }

    private static float applyOffensiveDamageBonuses(
            LivingEntity attacker,
            LivingEntity victim,
            DamageSource source,
            float damage,
            TrimSetSummary summary
    ) {
        double mult = TrimFxConfig.getPatternMultiplier();
        float result = damage;

        TrimTier sentryTier = summary.getPatternTier(PatternBonusCatalog.SENTRY);
        if (sentryTier.isActive() && source.is(DamageTypeTags.IS_PROJECTILE)) {
            result = PatternBonusCatalog.applyDamageAmplification(result, sentryTier, PatternBonusCatalog.SENTRY, mult);
            if (sentryTier.isAtLeast(TrimTier.TIER_3) && victim != null) {
                victim.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
            }
        }

        TrimTier snoutTier = summary.getPatternTier(PatternBonusCatalog.SNOUT);
        if (snoutTier.isActive() && isSnoutDamageApplicable(attacker, victim)) {
            result = PatternBonusCatalog.applyDamageAmplification(result, snoutTier, PatternBonusCatalog.SNOUT, mult);
        }

        TrimTier eyeTier = summary.getPatternTier(PatternBonusCatalog.EYE);
        if (eyeTier.isActive() && isAirborneCriticalHit(attacker)) {
            double critBonus = PatternBonusCatalog.find(PatternBonusCatalog.EYE)
                    .map(spec -> spec.getSecondaryValue(eyeTier, mult))
                    .orElse(0.0D);
            result = (float) (result * (1.0D + critBonus));
        }

        return result;
    }

    private static boolean isSnoutDamageApplicable(LivingEntity attacker, LivingEntity victim) {
        if (attacker.level().dimension() == Level.NETHER) {
            return true;
        }
        return victim instanceof AbstractPiglin || victim instanceof Hoglin;
    }

    private static boolean isAirborneCriticalHit(LivingEntity attacker) {
        return !attacker.onGround() && attacker.fallDistance > 0.0F && !attacker.isInWater();
    }

    private static float applyDefensiveDamageReductions(
            LivingEntity victim,
            DamageSource source,
            float damage,
            TrimSetSummary summary
    ) {
        double mult = TrimFxConfig.getPatternMultiplier();
        float result = damage;

        TrimTier flowTier = summary.getPatternTier(PatternBonusCatalog.FLOW);
        if (flowTier.isActive() && source.is(DamageTypes.FALL)) {
            result = PatternBonusCatalog.applyDamageReduction(result, flowTier, PatternBonusCatalog.FLOW, mult);
        }

        TrimTier duneTier = summary.getPatternTier(PatternBonusCatalog.DUNE);
        if (duneTier.isActive()) {
            if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION)) {
                result = PatternBonusCatalog.applyDamageReduction(result, duneTier, PatternBonusCatalog.DUNE, mult);
            }
            tryTriggerDuneAbsorptionShield(victim, result, duneTier);
        }

        TrimTier wardTier = summary.getPatternTier(PatternBonusCatalog.WARD);
        if (wardTier.isActive()) {
            int light = victim.level().getMaxLocalRawBrightness(victim.blockPosition());
            if (light <= PatternBonusCatalog.MAX_DARKNESS_LIGHT_LEVEL) {
                result = PatternBonusCatalog.applyDamageReduction(result, wardTier, PatternBonusCatalog.WARD, mult);
            }
        }

        TrimTier ribTier = summary.getPatternTier(PatternBonusCatalog.RIB);
        if (ribTier.isActive() && source.is(DamageTypes.WITHER)) {
            result = PatternBonusCatalog.applyDamageReduction(result, ribTier, PatternBonusCatalog.RIB, mult);
        }

        return result;
    }

    private static void tryTriggerDuneAbsorptionShield(LivingEntity victim, float incomingDamage, TrimTier duneTier) {
        float projectedHealth = victim.getHealth() - incomingDamage;
        if (projectedHealth > victim.getMaxHealth() * PatternBonusCatalog.DUNE_LOW_HEALTH_RATIO) {
            return;
        }
        long now = victim.level().getGameTime();
        long readyTick = DUNE_COOLDOWN_EXPIRY.getOrDefault(victim.getUUID(), 0L);
        if (now < readyTick) {
            return;
        }
        DUNE_COOLDOWN_EXPIRY.put(victim.getUUID(), now + PatternBonusCatalog.DUNE_COOLDOWN_TICKS);
        int amplifier = Math.max(0, duneTier.level() - 1);
        victim.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, PatternBonusCatalog.DUNE_ABSORPTION_DURATION_TICKS, amplifier));
        playFeedback(victim, ParticleTypes.TOTEM_OF_UNDYING, 12, SoundEvents.SHIELD_BLOCK.value(), 0.8F, 1.1F);
    }

    public static void handlePostDamageProcs(
            LivingEntity victim,
            DamageSource source,
            float inflictedDamage,
            TrimSetSummary victimSummary,
            TrimSetSummary attackerSummary
    ) {
        if (PROCESSING_TRIM_DAMAGE.get() || victim == null || !(victim.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (inflictedDamage <= 0.0F) {
            return;
        }

        PROCESSING_TRIM_DAMAGE.set(Boolean.TRUE);
        try {
            if (TrimFxConfig.arePatternBonusesEnabled()) {
                if (victimSummary != null && source.getEntity() instanceof LivingEntity attacker && attacker != victim) {
                    triggerTideThorns(serverLevel, victim, attacker, inflictedDamage, victimSummary.getPatternTier(PatternBonusCatalog.TIDE));
                }
                if (attackerSummary != null && source.getEntity() instanceof LivingEntity attacker && !source.is(DamageTypeTags.IS_PROJECTILE)) {
                    triggerMeleeAttackerProcs(serverLevel, attacker, victim, attackerSummary);
                }
            }
            if (TrimFxConfig.areMaterialBonusesEnabled()
                    && attackerSummary != null
                    && source.getEntity() instanceof LivingEntity attacker
                    && !source.is(DamageTypeTags.IS_PROJECTILE)) {
                triggerRoseQuartzLifesteal(attacker, inflictedDamage, attackerSummary.getMaterialTier(MaterialBonusCatalog.ROSE_QUARTZ));
            }
        } finally {
            PROCESSING_TRIM_DAMAGE.set(Boolean.FALSE);
        }
    }

    private static void triggerRoseQuartzLifesteal(LivingEntity attacker, float inflictedDamage, TrimTier roseQuartzTier) {
        if (!roseQuartzTier.isActive()) {
            return;
        }
        double lifestealRatio = MaterialBonusCatalog.find(MaterialBonusCatalog.ROSE_QUARTZ)
                .map(spec -> spec.getSecondaryBonus(roseQuartzTier, TrimFxConfig.getMaterialMultiplier()))
                .orElse(0.0D);
        float totalRestore = (float) (inflictedDamage * Math.min(1.0D, Math.max(0.0D, lifestealRatio)));
        if (totalRestore <= 0.05F) {
            return;
        }
        float missingHealth = Math.max(0.0F, attacker.getMaxHealth() - attacker.getHealth());
        float directHeal = Math.min(missingHealth, totalRestore);
        if (directHeal > 0.0F) {
            attacker.heal(directHeal);
        }
        float overheal = totalRestore - directHeal;
        if (overheal > 0.0F && attacker.getMaxAbsorption() > 0.0F) {
            float newAbsorption = Math.min(attacker.getMaxAbsorption(), attacker.getAbsorptionAmount() + overheal);
            attacker.setAbsorptionAmount(newAbsorption);
        }
        playFeedback(attacker, ParticleTypes.CHERRY_LEAVES, 5, null, 0.0F, 1.0F);
    }

    private static void triggerTideThorns(
            ServerLevel serverLevel,
            LivingEntity defender,
            LivingEntity attacker,
            float inflictedDamage,
            TrimTier tideTier
    ) {
        if (!tideTier.isActive()) {
            return;
        }
        double ratio = PatternBonusCatalog.find(PatternBonusCatalog.TIDE)
                .map(spec -> spec.getPrimaryValue(tideTier, TrimFxConfig.getPatternMultiplier()))
                .orElse(0.0D);
        float reflected = (float) (inflictedDamage * ratio);
        if (reflected >= 0.5F) {
            attacker.hurtServer(serverLevel, defender.damageSources().thorns(defender), reflected);
            playFeedback(defender, ParticleTypes.SPLASH, 10, SoundEvents.THORNS_HIT, 0.7F, 1.2F);
        }
    }

    private static void triggerMeleeAttackerProcs(
            ServerLevel serverLevel,
            LivingEntity attacker,
            LivingEntity target,
            TrimSetSummary attackerSummary
    ) {
        double mult = TrimFxConfig.getPatternMultiplier();

        TrimTier boltTier = attackerSummary.getPatternTier(PatternBonusCatalog.BOLT);
        if (boltTier.isActive()) {
            tryTriggerBoltChainShock(serverLevel, attacker, target, boltTier, mult);
        }

        TrimTier ribTier = attackerSummary.getPatternTier(PatternBonusCatalog.RIB);
        if (ribTier.isActive()) {
            int duration = PatternBonusCatalog.find(PatternBonusCatalog.RIB)
                    .map(spec -> spec.getSecondaryValue(ribTier, 1.0D))
                    .orElse(60.0D)
                    .intValue();
            int amp = ribTier.isAtLeast(TrimTier.TIER_3) ? 1 : 0;
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, duration, amp));
            playFeedback(target, ParticleTypes.SOUL, 6, SoundEvents.WITHER_HURT, 0.4F, 1.5F);
        }

        TrimTier spireTier = attackerSummary.getPatternTier(PatternBonusCatalog.SPIRE);
        if (spireTier.isActive()) {
            tryTriggerSpireLevitation(attacker, target, spireTier, mult);
        }
    }

    private static void tryTriggerBoltChainShock(
            ServerLevel serverLevel,
            LivingEntity attacker,
            LivingEntity primaryTarget,
            TrimTier boltTier,
            double mult
    ) {
        PatternBonusCatalog.PatternBonusSpec spec = PatternBonusCatalog.find(PatternBonusCatalog.BOLT).orElse(null);
        if (spec == null) {
            return;
        }
        double chance = spec.getPrimaryValue(boltTier, mult);
        if (serverLevel.isThundering() || serverLevel.isRainingAt(primaryTarget.blockPosition())) {
            chance = Math.min(1.0D, chance * 2.0D);
        }
        if (attacker.getRandom().nextDouble() >= chance) {
            return;
        }

        float shockDamage = (float) spec.getSecondaryValue(boltTier, mult);
        primaryTarget.hurtServer(serverLevel, attacker.damageSources().lightningBolt(), shockDamage);
        playFeedback(primaryTarget, ParticleTypes.ELECTRIC_SPARK, 18, SoundEvents.LIGHTNING_BOLT_IMPACT, 0.5F, 1.6F);

        AABB chainBox = primaryTarget.getBoundingBox().inflate(BOLT_CHAIN_RADIUS);
        List<Mob> nearbyHostiles = serverLevel.getEntitiesOfClass(
                Mob.class,
                chainBox,
                mob -> mob != primaryTarget && mob.isAlive() && mob instanceof Enemy
        );
        int hits = 0;
        for (Mob hostile : nearbyHostiles) {
            if (hits >= BOLT_MAX_TARGETS) {
                break;
            }
            hostile.hurtServer(serverLevel, attacker.damageSources().lightningBolt(), shockDamage * 0.75F);
            playFeedback(hostile, ParticleTypes.ELECTRIC_SPARK, 10, null, 0.0F, 1.0F);
            hits++;
        }
    }

    private static void tryTriggerSpireLevitation(
            LivingEntity attacker,
            LivingEntity target,
            TrimTier spireTier,
            double mult
    ) {
        PatternBonusCatalog.PatternBonusSpec spec = PatternBonusCatalog.find(PatternBonusCatalog.SPIRE).orElse(null);
        if (spec == null) {
            return;
        }
        double chance = spec.getPrimaryValue(spireTier, mult);
        if (attacker.getRandom().nextDouble() < chance) {
            int duration = (int) spec.getSecondaryValue(spireTier, 1.0D);
            target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, duration, 0));
            playFeedback(target, ParticleTypes.END_ROD, 10, SoundEvents.SHULKER_BULLET_HIT, 0.5F, 1.4F);
        }
    }

    public static void handleEntityKill(LivingEntity killer, LivingEntity victim, TrimSetSummary killerSummary) {
        if (killer == null || killer.level().isClientSide() || !TrimFxConfig.arePatternBonusesEnabled()) {
            return;
        }
        TrimTier wildTier = killerSummary.getPatternTier(PatternBonusCatalog.WILD);
        if (wildTier.isActive()) {
            int duration = PatternBonusCatalog.find(PatternBonusCatalog.WILD)
                    .map(spec -> spec.getSecondaryValue(wildTier, 1.0D))
                    .orElse(60.0D)
                    .intValue();
            killer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, 0));
        }

        TrimTier ribTier = killerSummary.getPatternTier(PatternBonusCatalog.RIB);
        if (ribTier.isAtLeast(TrimTier.TIER_3) && victim != null && victim.hasEffect(MobEffects.WITHER)) {
            killer.heal(PatternBonusCatalog.RIB_KILL_HEAL_AMOUNT);
            playFeedback(killer, ParticleTypes.HEART, 4, SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F, 0.8F);
        }
    }

    public static float computeModifiedHealing(float baseHeal, TrimSetSummary summary) {
        if (baseHeal <= 0.0F || summary == null || !TrimFxConfig.arePatternBonusesEnabled()) {
            return Math.max(0.0F, baseHeal);
        }
        TrimTier raiserTier = summary.getPatternTier(PatternBonusCatalog.RAISER);
        if (!raiserTier.isActive()) {
            return baseHeal;
        }
        return PatternBonusCatalog.applyDamageAmplification(baseHeal, raiserTier, PatternBonusCatalog.RAISER, TrimFxConfig.getPatternMultiplier());
    }

    public static void handleFoodConsumed(Player player, ItemStack foodStack, TrimSetSummary summary) {
        if (player == null || player.level().isClientSide() || !TrimFxConfig.arePatternBonusesEnabled()) {
            return;
        }
        if (foodStack == null || foodStack.get(DataComponents.FOOD) == null) {
            return;
        }
        TrimTier raiserTier = summary.getPatternTier(PatternBonusCatalog.RAISER);
        if (!raiserTier.isActive()) {
            return;
        }
        int bonusFood = raiserTier.level();
        float bonusSaturation = 0.5F * raiserTier.level();
        player.getFoodData().eat(bonusFood, bonusSaturation);
    }

    public static void tryConserveToolDurability(Player player, TrimSetSummary summary) {
        if (player == null || player.level().isClientSide() || !TrimFxConfig.arePatternBonusesEnabled()) {
            return;
        }
        TrimTier shaperTier = summary.getPatternTier(PatternBonusCatalog.SHAPER);
        if (!shaperTier.isActive()) {
            return;
        }
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty() || !mainHand.isDamageableItem() || mainHand.getDamageValue() <= 0) {
            return;
        }
        double chance = PatternBonusCatalog.find(PatternBonusCatalog.SHAPER)
                .map(spec -> spec.getPrimaryValue(shaperTier, TrimFxConfig.getPatternMultiplier()))
                .orElse(0.0D);
        if (player.getRandom().nextDouble() < chance) {
            mainHand.setDamageValue(Math.max(0, mainHand.getDamageValue() - 1));
        }
    }

    public static int computeModifiedXpValue(int baseXp, TrimSetSummary summary) {
        if (baseXp <= 0 || summary == null || !TrimFxConfig.arePatternBonusesEnabled()) {
            return Math.max(0, baseXp);
        }
        TrimTier hostTier = summary.getPatternTier(PatternBonusCatalog.HOST);
        if (!hostTier.isActive()) {
            return baseXp;
        }
        double bonusRatio = PatternBonusCatalog.find(PatternBonusCatalog.HOST)
                .map(spec -> spec.getPrimaryValue(hostTier, TrimFxConfig.getPatternMultiplier()))
                .orElse(0.0D);
        return Math.max(baseXp, (int) Math.round(baseXp * (1.0D + bonusRatio)));
    }

    public static double computeVisibilityMultiplier(TrimSetSummary summary) {
        if (summary == null || !TrimFxConfig.arePatternBonusesEnabled()) {
            return 1.0D;
        }
        TrimTier silenceTier = summary.getPatternTier(PatternBonusCatalog.SILENCE);
        if (!silenceTier.isActive()) {
            return 1.0D;
        }
        double reduction = PatternBonusCatalog.find(PatternBonusCatalog.SILENCE)
                .map(spec -> spec.getSecondaryValue(silenceTier, TrimFxConfig.getPatternMultiplier()))
                .orElse(0.0D);
        return Math.max(0.15D, 1.0D - reduction);
    }

    private static void playFeedback(
            LivingEntity entity,
            ParticleOptions particle,
            int count,
            SoundEvent sound,
            float volume,
            float pitch
    ) {
        if (!TrimFxConfig.isProcFeedbackEnabled() || !(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (particle != null && count > 0) {
            serverLevel.sendParticles(
                    particle,
                    entity.getX(),
                    entity.getY(0.5D),
                    entity.getZ(),
                    count,
                    0.35D,
                    0.45D,
                    0.35D,
                    0.05D
            );
        }
        if (sound != null) {
            serverLevel.playSound(null, entity.blockPosition(), sound, SoundSource.PLAYERS, volume, pitch);
        }
    }
}
