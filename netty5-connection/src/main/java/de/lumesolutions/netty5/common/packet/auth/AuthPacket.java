package de.lumesolutions.netty5.common.packet.auth;



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

import de.lumesolutions.netty5.Netty5ClientChannel;
import de.lumesolutions.netty5.common.codec.CodecBuffer;
import de.lumesolutions.netty5.common.packet.Packet;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Getter
public class AuthPacket extends Packet {

    protected final Netty5ClientChannel.Identity identity;
    protected final Map<String, String> properties;

    public AuthPacket(@NotNull Netty5ClientChannel.Identity identity, @NotNull Map<String, String> properties) {
        this.identity = identity;
        this.properties = properties;
        buffer.writeStream(this.identity)
                .writeMap(this.properties, CodecBuffer::writeString, CodecBuffer::writeString);
    }

    public AuthPacket(@NotNull CodecBuffer buffer) {
        super(buffer);
        this.identity = buffer.readStream(new Netty5ClientChannel.Identity());
        this.properties = buffer.readMap(new HashMap<>(), buffer::readString, buffer::readString);
    }
}
