package de.dataflair.netty5;



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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty5.channel.EventLoop;
import io.netty5.channel.MultithreadEventLoopGroup;
import io.netty5.channel.ServerChannel;
import io.netty5.channel.ServerChannelFactory;
import io.netty5.channel.epoll.Epoll;
import io.netty5.channel.epoll.EpollHandler;
import io.netty5.channel.epoll.EpollServerSocketChannel;
import io.netty5.channel.epoll.EpollSocketChannel;
import io.netty5.channel.nio.NioHandler;
import io.netty5.channel.socket.SocketChannel;
import io.netty5.channel.socket.nio.NioServerSocketChannel;
import io.netty5.channel.socket.nio.NioSocketChannel;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@UtilityClass
public class Netty5ChannelUtils {
    public final Gson JSON = new GsonBuilder().disableHtmlEscaping().create();
    public final UUID SYSTEM_UUID = UUID.fromString("0f0f0f0f-0f0f-0f0f-f0f0-0f0f0f0f0f0f");

    public static @NotNull MultithreadEventLoopGroup createEventLoopGroup(int threads) {
        return new MultithreadEventLoopGroup(threads, Epoll.isAvailable() ? EpollHandler.newFactory() : NioHandler.newFactory());
    }

    public static @NotNull SocketChannel createChannelFactory(EventLoop loop) {
        return Epoll.isAvailable() ? new EpollSocketChannel(loop) : new NioSocketChannel(loop);
    }

    public static ServerChannelFactory<? extends ServerChannel> buildChannelFactory() {
        return Epoll.isAvailable() ? EpollServerSocketChannel::new : NioServerSocketChannel::new;
    }

}
