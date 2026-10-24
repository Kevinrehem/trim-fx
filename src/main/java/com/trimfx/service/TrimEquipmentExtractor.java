package com.trimfx.service;

import com.trimfx.domain.ArmorSlotTrim;
import com.trimfx.domain.SetBonusCalculator;
import com.trimfx.domain.TrimSetSummary;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

public final class TrimEquipmentExtractor {

    private static final String UNREGISTERED_HOLDER = "[unregistered]";
    private static final String SMITHING_TEMPLATE_SUFFIX = "_armor_trim_smithing_template";

    private static final List<EquipmentSlot> ARMOR_SLOTS = List.of(
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    );

    private TrimEquipmentExtractor() {
    }

    public static TrimSetSummary evaluateEntity(LivingEntity entity) {
        if (entity == null) {
            return TrimSetSummary.EMPTY;
        }
        List<ArmorSlotTrim> trims = new ArrayList<>(ARMOR_SLOTS.size());
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = entity.getItemBySlot(slot);
            extractFromArmorStack(stack).ifPresent(trims::add);
        }
        return SetBonusCalculator.calculate(trims);
    }

    public static Optional<ArmorSlotTrim> extractFromArmorStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        ArmorTrim armorTrim = stack.get(DataComponents.TRIM);
        if (armorTrim == null) {
            return Optional.empty();
        }
        String patternId = sanitizeRegisteredName(armorTrim.pattern().getRegisteredName());
        String materialId = sanitizeRegisteredName(armorTrim.material().getRegisteredName());
        if (patternId.isEmpty() && materialId.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ArmorSlotTrim(patternId, materialId));
    }

    public static Optional<String> extractPatternFromSmithingTemplate(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        String itemName = sanitizeRegisteredName(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        if (!itemName.endsWith(SMITHING_TEMPLATE_SUFFIX)) {
            return Optional.empty();
        }
        String patternId = itemName.substring(0, itemName.length() - SMITHING_TEMPLATE_SUFFIX.length());
        return Optional.of(ArmorSlotTrim.normalizeId(patternId));
    }

    private static String sanitizeRegisteredName(String name) {
        if (name == null || name.isBlank() || UNREGISTERED_HOLDER.equals(name)) {
            return "";
        }
        return ArmorSlotTrim.normalizeId(name);
    }
}
