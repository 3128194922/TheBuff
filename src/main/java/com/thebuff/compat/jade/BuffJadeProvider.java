package com.thebuff.compat.jade;

import com.thebuff.TheBuff;
import com.thebuff.api.BuffApi;
import com.thebuff.api.BuffCategory;
import com.thebuff.api.BuffInstance;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;

public enum BuffJadeProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = new ResourceLocation(TheBuff.MODID, "buffs");
    private static final String KEY = "TheBuffBuffs";

    @Override
    public void appendServerData(CompoundTag tag, EntityAccessor accessor) {
        Entity entity = accessor.getEntity();
        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        var buffs = BuffApi.getAll(living);
        if (buffs.isEmpty()) {
            return;
        }

        ListTag list = new ListTag();
        for (BuffInstance instance : buffs) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Id", instance.getId().toString());
            entry.putString("Name", Component.Serializer.toJson(instance.getType().getDisplayName()));
            entry.putInt("Amplifier", instance.getAmplifier());
            entry.putInt("Duration", instance.getDuration());
            entry.putInt("Color", instance.getType().getColor());
            entry.putByte("Category", (byte) instance.getType().getCategory().ordinal());
            entry.putBoolean("Permanent", instance.isPermanent());
            list.add(entry);
        }
        tag.put(KEY, list);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains(KEY, 9)) {
            return;
        }

        ListTag list = data.getList(KEY, 10);
        if (list.isEmpty()) {
            return;
        }

        IElementHelper helper = IElementHelper.get();
        ITooltip box = helper.tooltip();
        IThemeHelper theme = IThemeHelper.get();
        box.add(theme.title(Component.translatable("thebuff.jade.header")));

        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            Component name = Component.Serializer.fromJsonLenient(entry.getString("Name"));
            if (name == null) {
                continue;
            }

            int amplifier = entry.getInt("Amplifier");
            Component level = Component.translatable("thebuff.hud.level", amplifier + 1);
            String duration = entry.getBoolean("Permanent")
                    ? I18n.get("thebuff.duration.permanent")
                    : StringUtil.formatTickDuration(entry.getInt("Duration"));
            Component line = Component.translatable("thebuff.jade.line", name, level, duration);

            int categoryIndex = entry.getByte("Category");
            BuffCategory[] categories = BuffCategory.values();
            BuffCategory category = categories[Math.max(0, Math.min(categoryIndex, categories.length - 1))];
            box.add(switch (category) {
                case HARMFUL -> theme.danger(line);
                case BENEFICIAL -> theme.success(line);
                case NEUTRAL -> theme.warning(line);
            });
        }

        tooltip.add(helper.box(box, BoxStyle.DEFAULT));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public int getDefaultPriority() {
        return 500;
    }
}
