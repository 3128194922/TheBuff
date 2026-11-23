package com.thebuff.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.thebuff.api.BuffApi;
import com.thebuff.api.BuffInstance;
import com.thebuff.api.BuffType;
import com.thebuff.registry.BuffRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collection;

public final class TheBuffCommand {
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_BUFFS = (context, builder) -> {
        for (BuffType type : BuffRegistry.all()) {
            builder.suggest(type.getId().toString());
        }
        return builder.buildFuture();
    };

    private TheBuffCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("thebuff")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("apply")
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .then(Commands.argument("buff", ResourceLocationArgument.id())
                                        .suggests(SUGGEST_BUFFS)
                                        .executes(context -> apply(context, 200, 0))
                                        .then(Commands.argument("duration", IntegerArgumentType.integer(-1))
                                                .executes(context -> apply(context,
                                                        IntegerArgumentType.getInteger(context, "duration"), 0))
                                                .then(Commands.argument("amplifier", IntegerArgumentType.integer(0))
                                                        .executes(context -> apply(context,
                                                                IntegerArgumentType.getInteger(context, "duration"),
                                                                IntegerArgumentType.getInteger(context, "amplifier"))))))))
                .then(Commands.literal("applyPermanent")
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .then(Commands.argument("buff", ResourceLocationArgument.id())
                                        .suggests(SUGGEST_BUFFS)
                                        .executes(context -> apply(context, -1, 0))
                                        .then(Commands.argument("amplifier", IntegerArgumentType.integer(0))
                                                .executes(context -> apply(context, -1,
                                                        IntegerArgumentType.getInteger(context, "amplifier")))))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .then(Commands.argument("buff", ResourceLocationArgument.id())
                                        .suggests(SUGGEST_BUFFS)
                                        .executes(TheBuffCommand::remove))))
                .then(Commands.literal("clear")
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .executes(TheBuffCommand::clear)))
                .then(Commands.literal("list")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .executes(TheBuffCommand::list))));
    }

    private static int apply(CommandContext<CommandSourceStack> context, int duration, int amplifier) throws CommandSyntaxException {
        ResourceLocation id = ResourceLocationArgument.getId(context, "buff");
        BuffType type = BuffRegistry.get(id);
        if (type == null) {
            context.getSource().sendFailure(Component.translatable("thebuff.command.unknown_buff", id.toString()));
            return 0;
        }

        Collection<? extends Entity> targets = EntityArgument.getEntities(context, "targets");
        Entity source = context.getSource().getEntity();
        int applied = 0;
        for (Entity target : targets) {
            if (target instanceof LivingEntity living && BuffApi.apply(living, id, duration, amplifier, source)) {
                applied++;
            }
        }

        int count = applied;
        context.getSource().sendSuccess(
                () -> Component.translatable("thebuff.command.applied", id.toString(), count),
                true
        );
        return applied;
    }

    private static int remove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ResourceLocation id = ResourceLocationArgument.getId(context, "buff");
        int removed = 0;
        for (Entity target : EntityArgument.getEntities(context, "targets")) {
            if (target instanceof LivingEntity living && BuffApi.remove(living, id)) {
                removed++;
            }
        }
        int count = removed;
        context.getSource().sendSuccess(
                () -> Component.translatable("thebuff.command.removed", id.toString(), count),
                true
        );
        return removed;
    }

    private static int clear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int cleared = 0;
        for (Entity target : EntityArgument.getEntities(context, "targets")) {
            if (target instanceof LivingEntity living) {
                cleared += BuffApi.removeAll(living);
            }
        }
        int count = cleared;
        context.getSource().sendSuccess(
                () -> Component.translatable("thebuff.command.cleared", count),
                true
        );
        return cleared;
    }

    private static int list(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity target = EntityArgument.getEntity(context, "target");
        if (!(target instanceof LivingEntity living)) {
            context.getSource().sendFailure(Component.translatable("thebuff.command.not_living"));
            return 0;
        }

        var buffs = BuffApi.getAll(living);
        if (buffs.isEmpty()) {
            context.getSource().sendSuccess(
                    () -> Component.translatable("thebuff.command.list.empty", living.getDisplayName()),
                    false
            );
            return 0;
        }

        for (BuffInstance instance : buffs) {
            Component level = Component.translatable("thebuff.hud.level", instance.getAmplifier() + 1);
            Component duration = instance.isPermanent()
                    ? Component.translatable("thebuff.duration.permanent")
                    : Component.literal(StringUtil.formatTickDuration(instance.getDuration()));
            Component source = instance.getSourceName() != null
                    ? Component.literal(instance.getSourceName())
                    : Component.literal("-");
            context.getSource().sendSuccess(
                    () -> Component.translatable("thebuff.command.list.line", living.getDisplayName(),
                            instance.getId().toString(), level, duration, source),
                    false
            );
        }
        return buffs.size();
    }
}
