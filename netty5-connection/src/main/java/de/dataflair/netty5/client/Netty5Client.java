package de.dataflair.netty5.client;



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

import de.dataflair.netty5.Netty5ChannelInitializer;
import de.dataflair.netty5.Netty5ChannelUtils;
import de.dataflair.netty5.Netty5ClientChannel;
import de.dataflair.netty5.Netty5Component;
import io.netty5.bootstrap.Bootstrap;
import io.netty5.channel.ChannelOption;
import io.netty5.channel.SimpleChannelInboundHandler;
import io.netty5.channel.epoll.Epoll;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Getter
public final class Netty5Client extends Netty5Component {

    private final Netty5ClientChannel.Identity identity;
    private final Map<String, String> authProperty;
    @Setter
    private Netty5ClientChannel thisChannel;

    public Netty5Client(@NotNull String hostname,
                        int port,
                        @NotNull Netty5ClientChannel.Identity identity,
                        @Nullable Map<String, String> authProperty) {
        super(0, hostname, port);
        this.identity = identity;
        this.authProperty = authProperty == null ? new HashMap<>() : authProperty;
    }

    @Override
    public void initialize() throws Exception {
        var bootstrap = new Bootstrap()
                .group(bossGroup())
                .channelFactory(Netty5ChannelUtils::createChannelFactory)
                .handler(new Netty5ChannelInitializer(this.identity) {
                    @Override
                    public SimpleChannelInboundHandler<?> handler() {
                        return new Netty5ClientHandler(Netty5Client.this);
                    }
                })
                .option(ChannelOption.AUTO_READ, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.IP_TOS, 24)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000);

        if (Epoll.isTcpFastOpenClientSideAvailable()) {
            bootstrap.option(ChannelOption.TCP_FASTOPEN_CONNECT, true);
        }

        bootstrap.connect(hostname(), port()).addListener(future -> {
            if (future.isSuccess()) {
                return;
            }
            this.connectionFuture(null);
        });
    }
}
