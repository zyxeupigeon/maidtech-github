package com.botcompat;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.Connection;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared filter logic for BotCompat.
 *
 * Purpose: the private server runs heavy client-required mods (BetterCombat,
 * TechReborn, ...) whose server side kicks players that did not register their
 * protocol channels, with messages like
 *   "Network configuration task not supported: bettercombat:config"
 *   "The TechReborn mod must be installed to play on this server."
 *
 * Those mods are only interested in syncing optional client-side data. For
 * headless bot accounts (name prefix builder_*) that sync is skipped anyway,
 * so the kicks are pointless and only block vanilla-protocol bots. We swallow
 * exactly those technical kicks for exactly those names - nothing else.
 */
public final class BotCompat {
    private static final Logger LOGGER = LoggerFactory.getLogger("botcompat");

    /** Player names that are treated as headless bots (configurable later if ever needed). */
    private static final String[] BOT_PREFIXES = { "builder_", "mindcraft" };

    /** Kick-message fragments that are safe to swallow: "client feature not synced" kicks. */
    private static final String[] SUPPRESS_MARKERS = {
            "Network configuration task not supported",
            "The TechReborn mod must be installed to play on this server.",
            "This server requires Cardinal Components API"
    };

    /** Connections that registered protocol channels (real fabric clients). */
    private static final Set<Connection> MODDED_CLIENTS = ConcurrentHashMap.newKeySet();

    public static boolean isModded(Connection connection) {
        return connection != null && MODDED_CLIENTS.contains(connection);
    }

    public static void markModdedClient(Connection connection) {
        if (connection != null) {
            MODDED_CLIENTS.add(connection);
        }
    }

    public static void logModded(String player) {
        LOGGER.info("[botcompat] Connection registered channels -> treated as modded client, no packet adaptation ({}).", player);
    }

    public static void logModdedClientUntouched(String player, String what) {
        LOGGER.info("[botcompat] '{}' is a modded client - {} not suppressed/adapted.", player, what);
    }

    private BotCompat() {
    }

    public static boolean isBotName(String name) {
        if (name == null) {
            return false;
        }
        for (String prefix : BOT_PREFIXES) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSuppressibleKick(String message) {
        if (message == null) {
            return false;
        }
        for (String marker : SUPPRESS_MARKERS) {
            if (message.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    public static void logSuppressed(String player, String message) {
        LOGGER.info("[botcompat] Suppressed client-requirement kick for '{}': {}", player, message);
    }

    public static void logNotBot(String player, String message) {
        LOGGER.info("[botcompat] Not suppressing kick for '{}' (not a bot account): {}", player, message);
    }

    public static void logReplaced(String player, String what) {
        LOGGER.info("[botcompat] Bot '{}': replaced {} (modded item components unparseable by vanilla-protocol clients)", player, what);
    }

    /**
     * Vanilla-protocol parsers (minecraft-protocol) only know the vanilla data
     * component types (ids 0..56). A stack carrying any other (modded)
     * component type cannot be parsed and would kill the connection.
     */
    public static boolean isUnsafeForVanilla(ItemStack stack, Registry<net.minecraft.core.component.DataComponentType<?>> componentRegistry) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        DataComponentMap map = stack.getComponents();
        for (TypedDataComponent<?> comp : map) {
            int id = componentRegistry.getId(comp.type());
            if (id < 0 || id >= 57) {
                return true;
            }
        }
        return false;
    }
}
