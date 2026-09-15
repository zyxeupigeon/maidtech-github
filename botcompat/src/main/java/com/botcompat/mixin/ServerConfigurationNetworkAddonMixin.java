package com.botcompat.mixin;

import com.botcompat.BotCompat;
import net.fabricmc.fabric.impl.networking.RegistrationPayload;
import net.fabricmc.fabric.impl.networking.server.ServerConfigurationNetworkAddon;
import net.minecraft.network.Connection;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric clients announce their protocol channels to the server during the
 * configuration phase; this arrives here as a RegistrationPayload with a
 * non-empty channel list. Vanilla-protocol bots (mineflayer) never send one.
 *
 * Connections that registered channels are real modded clients (e.g. the
 * regular bot instance used by the owner under the name builder_0) and must
 * receive ALL packets untouched - BotCompat only adapts the protocol for true
 * vanilla-protocol bots.
 */
@Mixin(ServerConfigurationNetworkAddon.class)
public abstract class ServerConfigurationNetworkAddonMixin {

    @Shadow
    private ServerConfigurationPacketListenerImpl handler;

    @Inject(method = "receiveRegistration", at = @At("HEAD"))
    private void botcompat$markModdedClient(boolean register, RegistrationPayload payload, CallbackInfo ci) {
        if (register && payload != null && payload.channels() != null && !payload.channels().isEmpty()) {
            Connection conn = ((ServerCommonConnectionAccessor) (Object) this.handler).botcompat$connection();
            BotCompat.markModdedClient(conn);
            String name = this.handler.getOwner() == null ? null : this.handler.getOwner().getName();
            BotCompat.logModded(name);
        }
    }
}
