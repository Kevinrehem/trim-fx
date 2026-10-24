package com.trimfx.client;

import com.trimfx.TrimFxMod;
import com.trimfx.config.TrimFxConfig;
import com.trimfx.domain.ArmorSlotTrim;
import com.trimfx.domain.MaterialBonusCatalog;
import com.trimfx.domain.PatternBonusCatalog;
import com.trimfx.domain.TrimSetSummary;
import com.trimfx.domain.TrimTier;
import com.trimfx.service.TrimEquipmentExtractor;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = TrimFxMod.MOD_ID, value = Dist.CLIENT)
public final class TrimClientTooltipEvents {

    private TrimClientTooltipEvents() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack == null || stack.isEmpty()) {
            return;
        }
        List<Component> tooltip = event.getToolTip();
        Player player = event.getEntity();
        TrimSetSummary summary = player != null ? TrimEquipmentExtractor.evaluateEntity(player) : TrimSetSummary.EMPTY;
        boolean shiftDown = event.getFlags().hasShiftDown();

        Optional<ArmorSlotTrim> armorTrim = TrimEquipmentExtractor.extractFromArmorStack(stack);
        if (armorTrim.isPresent()) {
            appendTrimmedArmorTooltip(tooltip, armorTrim.get(), summary, shiftDown);
            return;
        }

        if (TrimFxConfig.areTemplateTooltipsEnabled()) {
            Optional<String> templatePattern = TrimEquipmentExtractor.extractPatternFromSmithingTemplate(stack);
            templatePattern.ifPresent(patternId -> appendTemplateTooltip(tooltip, patternId, summary, shiftDown));
        }
    }

    private static void appendTrimmedArmorTooltip(
            List<Component> tooltip,
            ArmorSlotTrim slotTrim,
            TrimSetSummary summary,
            boolean shiftDown
    ) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.trimfx.header").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        if (TrimFxConfig.arePatternBonusesEnabled() && slotTrim.hasPattern()) {
            appendPatternSection(tooltip, slotTrim.patternId(), summary, shiftDown);
        }
        if (TrimFxConfig.areMaterialBonusesEnabled() && slotTrim.hasMaterial()) {
            appendMaterialSection(tooltip, slotTrim.materialId(), summary, shiftDown);
        }
        if (!shiftDown) {
            tooltip.add(Component.translatable("tooltip.trimfx.hold_shift").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    private static void appendTemplateTooltip(
            List<Component> tooltip,
            String patternId,
            TrimSetSummary summary,
            boolean shiftDown
    ) {
        if (PatternBonusCatalog.find(patternId).isEmpty()) {
            return;
        }
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.trimfx.template_header").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        appendPatternSection(tooltip, patternId, summary, shiftDown);
        if (!shiftDown) {
            tooltip.add(Component.translatable("tooltip.trimfx.hold_shift").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    private static void appendPatternSection(
            List<Component> tooltip,
            String patternId,
            TrimSetSummary summary,
            boolean shiftDown
    ) {
        Optional<PatternBonusCatalog.PatternBonusSpec> specOpt = PatternBonusCatalog.find(patternId);
        if (specOpt.isEmpty()) {
            return;
        }
        PatternBonusCatalog.PatternBonusSpec spec = specOpt.get();
        int count = summary.getPatternCount(patternId);
        TrimTier activeTier = summary.getPatternTier(patternId);
        ChatFormatting statusColor = activeTier.isActive() ? ChatFormatting.AQUA : ChatFormatting.GRAY;

        String tierLabel = activeTier.isActive()
                ? Component.translatable("tooltip.trimfx.tier_active", activeTier.romanNumeral()).getString()
                : Component.translatable("tooltip.trimfx.tier_inactive").getString();

        tooltip.add(Component.translatable(
                "tooltip.trimfx.pattern_line",
                Component.translatable("trimfx.pattern." + spec.translationKeySuffix() + ".name"),
                count,
                TrimTier.MAX_ARMOR_SLOTS,
                tierLabel
        ).withStyle(statusColor));

        TrimTier previewTier = activeTier.isActive() ? activeTier : TrimTier.TIER_1;
        tooltip.add(Component.translatable(
                "trimfx.pattern." + spec.translationKeySuffix() + ".tier." + previewTier.level()
        ).withStyle(activeTier.isActive() ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));

        if (shiftDown) {
            appendAllTierLines(tooltip, "trimfx.pattern." + spec.translationKeySuffix() + ".tier.", activeTier);
        }
    }

    private static void appendMaterialSection(
            List<Component> tooltip,
            String materialId,
            TrimSetSummary summary,
            boolean shiftDown
    ) {
        Optional<MaterialBonusCatalog.MaterialBonusSpec> specOpt = MaterialBonusCatalog.find(materialId);
        if (specOpt.isEmpty()) {
            return;
        }
        MaterialBonusCatalog.MaterialBonusSpec spec = specOpt.get();
        int count = summary.getMaterialCount(materialId);
        TrimTier activeTier = summary.getMaterialTier(materialId);
        ChatFormatting statusColor = activeTier.isActive() ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GRAY;

        String tierLabel = activeTier.isActive()
                ? Component.translatable("tooltip.trimfx.tier_active", activeTier.romanNumeral()).getString()
                : Component.translatable("tooltip.trimfx.tier_inactive").getString();

        tooltip.add(Component.translatable(
                "tooltip.trimfx.material_line",
                Component.translatable("trimfx.material." + spec.translationKeySuffix() + ".name"),
                count,
                TrimTier.MAX_ARMOR_SLOTS,
                tierLabel
        ).withStyle(statusColor));

        TrimTier previewTier = activeTier.isActive() ? activeTier : TrimTier.TIER_1;
        tooltip.add(Component.translatable(
                "trimfx.material." + spec.translationKeySuffix() + ".tier." + previewTier.level()
        ).withStyle(activeTier.isActive() ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));

        if (shiftDown) {
            appendAllTierLines(tooltip, "trimfx.material." + spec.translationKeySuffix() + ".tier.", activeTier);
        }
    }

    private static void appendAllTierLines(List<Component> tooltip, String keyPrefix, TrimTier activeTier) {
        for (TrimTier tier : List.of(TrimTier.TIER_1, TrimTier.TIER_2, TrimTier.TIER_3)) {
            boolean isCurrent = activeTier == tier;
            ChatFormatting color = isCurrent ? ChatFormatting.YELLOW : ChatFormatting.GRAY;
            tooltip.add(Component.translatable(
                    "tooltip.trimfx.tier_detail",
                    tier.requiredPieces(),
                    tier.romanNumeral(),
                    Component.translatable(keyPrefix + tier.level())
            ).withStyle(color));
        }
    }
}
