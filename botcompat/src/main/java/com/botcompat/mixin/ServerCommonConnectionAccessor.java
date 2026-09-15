package com.botcompat.mixin;

import net.minecraft.network.Connection;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Accesses the protected connection field of the common packet listener. */
@Mixin(ServerCommonPacketListenerImpl.class)
public interface ServerCommonConnectionAccessor {

    @Accessor("connection")
    Connection botcompat$connection();
}
