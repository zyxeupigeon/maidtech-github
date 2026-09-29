package com.botcompat.mixin;

import com.botcompat.BotCompat;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.NonNullList;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Both ServerConfigurationPacketListenerImpl and ServerGamePacketListenerImpl
 * inherit disconnect(Component) and send(Packet) from
 * ServerCommonPacketListenerImpl, so single HEAD injections here cover the
 * configuration phase (BetterCombat / TechReborn "client must be installed"
 * kicks) and the play phase.
 *
 * 1) disconnect: swallow exactly the technical client-requirement kicks for
 *    exactly the bot accounts (name prefix builder_*). Those mods only skip
 *    syncing optional client-side data for such players, which is harmless.
 *
 * 2) send: vanilla-protocol bots (mineflayer) cannot parse item stacks that
 *    carry modded data components (their component ids are not in the vanilla
 *    registry). For bot accounts we therefore replace item-bearing packets
 *    with empty equivalents:
 *      - ClientboundUpdateRecipesPacket      -> empty recipe list
 *      - ClientboundUpdateAdvancementsPacket -> empty advancement data
 *      - ClientboundSetEquipmentPacket       -> no equipment
 *    A bot does not need recipes/advancements, and equipment of other players
 *    is cosmetic for it.
 */
@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonNetworkHandlerMixin {

    /** Guards re-entrant send() of our replacement packets. */
    private boolean botcompat$replacing = false;

    @Shadow
    private Connection connection;

    @Shadow
    private net.minecraft.server.MinecraftServer server;

    @Inject(method = "disconnect", at = @At("HEAD"), cancellable = true)
    private void botcompat$suppressClientRequirementKick(Component reason, CallbackInfo ci) {
        String name = botcompat$playerName();
        String message = reason == null ? null : reason.getString();
        if (BotCompat.isBotName(name) && !BotCompat.isModded(this.connection) && BotCompat.isSuppressibleKick(message)) {
            BotCompat.logSuppressed(name, message);
            ci.cancel();
        } else if (BotCompat.isBotName(name) && BotCompat.isModded(this.connection)) {
            BotCompat.logModdedClientUntouched(name, "kick");
        } else {
            BotCompat.logNotBot(name, message);
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void botcompat$sanitizeItems(Packet<?> packet, CallbackInfo ci) {
        botcompat$handleSend(packet, ci);
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V", at = @At("HEAD"), cancellable = true)
    private void botcompat$sanitizeItemsWithListener(Packet<?> packet, PacketSendListener listener, CallbackInfo ci) {
        botcompat$handleSend(packet, ci);
    }

    private void botcompat$handleSend(Packet<?> packet, CallbackInfo ci) {
        if (botcompat$replacing) {
            return; // re-entrant send() of a replacement packet
        }
        String name = botcompat$playerName();
        if (!BotCompat.isBotName(name) || BotCompat.isModded(this.connection) || packet == null) {
            return;
        }
        if (packet instanceof ClientboundSetEntityMotionPacket) {
            // Entity velocity is cosmetic; NMP chokes on some of these frames
            // (massive log spam that slows bots down). Drop them for bots.
            ci.cancel();
            return;
        }
        ServerCommonPacketListenerImpl self = (ServerCommonPacketListenerImpl) (Object) this;
        if (packet instanceof ClientboundCommandsPacket) {
            ci.cancel();
            BotCompat.logReplaced(name, "command tree sync -> dropped (modded command parsers unparseable)");
        } else if (packet instanceof ClientboundUpdateTagsPacket) {
            ci.cancel();
            BotCompat.logReplaced(name, "tags sync -> dropped");
        } else if (packet instanceof ClientboundUpdateRecipesPacket) {
            ci.cancel();
            BotCompat.logReplaced(name, "recipes sync -> empty");
            botcompat$replacing = true;
            try {
                self.send(new ClientboundUpdateRecipesPacket(java.util.List.of()));
            } finally {
                botcompat$replacing = false;
            }
        } else if (packet instanceof ClientboundUpdateAdvancementsPacket) {
            ci.cancel();
            BotCompat.logReplaced(name, "advancements sync -> empty");
            botcompat$replacing = true;
            try {
                self.send(new ClientboundUpdateAdvancementsPacket(false, java.util.List.of(), java.util.Set.of(), java.util.Map.of()));
            } finally {
                botcompat$replacing = false;
            }
        } else if (packet instanceof ClientboundSetEquipmentPacket eq) {
            ci.cancel();
            BotCompat.logReplaced(name, "equipment of entity " + eq.getEntity() + " -> empty");
            botcompat$replacing = true;
            try {
                self.send(new ClientboundSetEquipmentPacket(eq.getEntity(), java.util.List.of()));
            } finally {
                botcompat$replacing = false;
            }
        } else if (packet instanceof ClientboundContainerSetContentPacket win) {
            ci.cancel();
            BotCompat.logReplaced(name, "inventory window " + win.getContainerId() + " -> modded-item slots emptied");
            botcompat$replacing = true;
            try {
                java.util.List<ItemStack> orig = win.getItems();
                NonNullList<ItemStack> safe = NonNullList.withSize(orig.size(), ItemStack.EMPTY);
                var reg = this.server.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE);
                for (int i = 0; i < orig.size(); i++) {
                    if (!BotCompat.isUnsafeForVanilla(orig.get(i), reg)) {
                        safe.set(i, orig.get(i));
                    }
                }
                self.send(new ClientboundContainerSetContentPacket(win.getContainerId(), win.getStateId(), safe, ItemStack.EMPTY));
            } finally {
                botcompat$replacing = false;
            }
        } else if (packet instanceof ClientboundCustomPayloadPacket payloadPkt) {
            var ch = payloadPkt.payload().type().id();
            String chStr = ch == null ? null : ch.toString();
            if (chStr == null || !chStr.startsWith("minecraft:")) {
                // Modded channels (carpet:hello, polymorph:*, ...) crash the vanilla
                // protocol encoder for bot connections; bots do not need them.
                ci.cancel();
                BotCompat.logReplaced(name, "custom_payload " + chStr + " -> dropped (modded channel)");
            }
        } else if (packet instanceof ClientboundContainerSetSlotPacket slot) {
            ci.cancel();
            BotCompat.logReplaced(name, "slot " + slot.getContainerId() + ":" + slot.getSlot() + " -> modded-item emptied");
            botcompat$replacing = true;
            try {
                var reg = this.server.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE);
                ItemStack item = BotCompat.isUnsafeForVanilla(slot.getItem(), reg) ? ItemStack.EMPTY : slot.getItem();
                self.send(new ClientboundContainerSetSlotPacket(slot.getContainerId(), slot.getStateId(), slot.getSlot(), item));
            } finally {
                botcompat$replacing = false;
            }
        }
    }

    private String botcompat$playerName() {
        Object self = this;
        if (self instanceof ServerConfigurationPacketListenerImpl cfg) {
            GameProfile profile = cfg.getOwner();
            return profile == null ? null : profile.getName();
        }
        if (self instanceof ServerGamePacketListenerImpl play) {
            ServerPlayer player = play.getPlayer();
            return player == null ? null : player.getGameProfile().getName();
        }
        return null;
    }
}
