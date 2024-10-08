package de.lumesolutions.netty5.server;

/*
 * Copyright 2023-2024 netty5-api contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import de.lumesolutions.netty5.Netty5ChannelInitializer;
import de.lumesolutions.netty5.Netty5ChannelUtils;
import de.lumesolutions.netty5.Netty5ClientChannel;
import de.lumesolutions.netty5.Netty5Component;
import io.netty5.bootstrap.ServerBootstrap;
import io.netty5.channel.ChannelOption;
import io.netty5.channel.EventLoopGroup;
import io.netty5.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
public final class Netty5Server extends Netty5Component {

    private final EventLoopGroup workerGroup = Netty5ChannelUtils.createEventLoopGroup(0);
    private final List<Predicate<Netty5ClientChannel>> authPredicates = new ArrayList<>();
    private final List<Consumer<Netty5ClientChannel>> authenticationActions = new ArrayList<>();
    private final List<Consumer<Netty5ClientChannel>> inactiveActions = new ArrayList<>();
    private final Netty5ServerPacketTransmitter packetTransmitter;
    @Setter
    private List<Netty5ClientChannel> connections = new ArrayList<>();
    @Setter
    private Netty5ClientChannel.Identity serverIdentity;

    public Netty5Server(@NotNull String hostname, int port) {
        super(1, hostname, port);
        this.packetTransmitter = new Netty5ServerPacketTransmitter(bossGroup(), packet -> {

        }, (queryPacket, packetClass, consumer) -> {

        });
    }

    public Netty5Server addAuthenticationPredicate(@NotNull Predicate<Netty5ClientChannel> predicate) {
        this.authPredicates.add(predicate);
        return this;
    }

    @Override
    public void initialize() throws Exception {
        new ServerBootstrap()
                .group(bossGroup(), workerGroup)
                .channelFactory(Netty5ChannelUtils.buildChannelFactory())
                .childHandler(new Netty5ChannelInitializer() {
                    @Override
                    public SimpleChannelInboundHandler<?> handler() {
                        return new Netty5ServerHandler(Netty5Server.this);
                    }
                })
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.IP_TOS, 24)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .bind(this.hostname(), this.port())
                .addListener(deployFuture())
                .addListener(future -> {
                    if (!future.isSuccess()) {
                        throw new RuntimeException(future.cause());
                    }
                });
    }

    @Override
    public void shutdownGracefully() {
        this.workerGroup.shutdownGracefully();
        super.shutdownGracefully();
    }

    public List<Netty5ClientChannel> clientChannel(@NotNull String name) {
        return this.connections.stream()
                .filter(clientChannel -> clientChannel.identity().name().equalsIgnoreCase(name))
                .toList();
    }

    public Netty5ClientChannel firstChannel(@NotNull String name) {
        return this.clientChannel(name).stream().findFirst().orElse(null);
    }

    public Netty5ClientChannel clientChannel(@NotNull UUID uuid) {
        return this.connections.stream()
                .filter(clientChannel -> clientChannel.identity().uuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }

}
