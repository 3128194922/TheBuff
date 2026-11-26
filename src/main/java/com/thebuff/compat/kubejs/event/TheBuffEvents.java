package com.thebuff.compat.kubejs.event;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

/**
 * KubeJS event group for The Buff. Listen from {@code server_scripts}:
 *
 * <pre>{@code
 * TheBuffEvents.removed(event => {
 *     console.info(`${event.entity} lost ${event.buffId} (${event.reasonName})`)
 * })
 *
 * TheBuffEvents.expired(event => {
 *     console.info(`${event.buffId} expired on ${event.entity}`)
 * })
 * }</pre>
 */
public interface TheBuffEvents {
    EventGroup GROUP = EventGroup.of("TheBuffEvents");

    /** Fires on every removal: manual, clear, death, replacement and expiry. */
    EventHandler REMOVED = GROUP.server("removed", () -> BuffRemovedEventJS.class);

    /** Fires only when a timed buff runs out (also triggers {@code removed}). */
    EventHandler EXPIRED = GROUP.server("expired", () -> BuffRemovedEventJS.class);
}
