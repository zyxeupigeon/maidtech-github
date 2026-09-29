package com.botcompat.mixin;

import com.owlmaddie.chat.EntityChatData;
import com.owlmaddie.commands.ConfigurationHandler;
import com.owlmaddie.network.PacketHelper;
import com.owlmaddie.network.ServerPackets;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * CreatureChat's ServerPackets broadcasts reuse ONE PacketByteBuf for every
 * recipient: fabric's encoder consumes the buffer's readerIndex on the first
 * send, so the 2nd..Nth players receive a corrupt payload
 * (IndexOutOfBoundsException readerIndex > writerIndex) and are disconnected.
 * Multibyte (Chinese) messages make the corruption deterministic.
 *
 * Fix: cancel the three broken broadcasts and reimplement them with a fresh
 * buffer per player (identical wire format).
 */
@Mixin(ServerPackets.class)
public abstract class ServerPacketsMixin {

    @Shadow(remap = false)
    private static MinecraftServer serverInstance;
    @Shadow(remap = false)
    public static net.minecraft.resources.ResourceLocation PACKET_S2C_PLAYER_MESSAGE;
    @Shadow(remap = false)
    public static net.minecraft.resources.ResourceLocation PACKET_S2C_PLAYER_STATUS;
    @Shadow(remap = false)
    public static net.minecraft.resources.ResourceLocation PACKET_S2C_WHITELIST;

    @Inject(method = "BroadcastPlayerMessage", at = @At("HEAD"), cancellable = true, remap = false)
    private static void botcompat$fixPlayerMessage(EntityChatData chatData, ServerPlayer sender, CallbackInfo ci) {
        for (ServerPlayer sp : serverInstance.getPlayerList().getPlayers()) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeUtf(sender.getUUID().toString());
            buf.writeUtf(sender.getGameProfile().getName());
            buf.writeUtf(chatData.currentMessage);
            PacketHelper.send(sp, PACKET_S2C_PLAYER_MESSAGE, buf);
        }
        ci.cancel();
    }

    @Inject(method = "BroadcastPlayerStatus", at = @At("HEAD"), cancellable = true, remap = false)
    private static void botcompat$fixPlayerStatus(net.minecraft.world.entity.player.Player player, boolean isChatOpen, CallbackInfo ci) {
        for (ServerPlayer sp : serverInstance.getPlayerList().getPlayers()) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeUtf(player.getUUID().toString());
            buf.writeBoolean(isChatOpen);
            PacketHelper.send(sp, PACKET_S2C_PLAYER_STATUS, buf);
        }
        ci.cancel();
    }

    @Inject(method = "send_whitelist_blacklist", at = @At("HEAD"), cancellable = true, remap = false)
    private static void botcompat$fixWhitelist(ServerPlayer player, CallbackInfo ci) {
        ConfigurationHandler.Config config = new ConfigurationHandler(serverInstance).loadConfig();
        for (ServerPlayer sp : serverInstance.getPlayerList().getPlayers()) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            List<String> whitelist = config.getWhitelist();
            buf.writeVarInt(whitelist.size());
            for (String entry : whitelist) {
                buf.writeUtf(entry);
            }
            List<String> blacklist = config.getBlacklist();
            buf.writeVarInt(blacklist.size());
            for (String entry : blacklist) {
                buf.writeUtf(entry);
            }
            PacketHelper.send(sp, PACKET_S2C_WHITELIST, buf);
        }
        ci.cancel();
    }
}
