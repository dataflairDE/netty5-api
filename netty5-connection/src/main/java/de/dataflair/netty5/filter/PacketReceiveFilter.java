package de.dataflair.netty5.filter;

import de.dataflair.netty5.Netty5ClientChannel;
import de.dataflair.netty5.common.packet.Packet;
import org.jetbrains.annotations.NotNull;

public abstract class PacketReceiveFilter extends Filter<PacketReceiveFilter.FilterValue> {
    public record FilterValue(@NotNull Packet recievedPacket, @NotNull Netty5ClientChannel senderChannel) {
    }
}
