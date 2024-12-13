package de.dataflair.netty5.common.packet;



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

import de.dataflair.netty5.common.codec.CodecBuffer;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Setter
@Getter
public abstract class RespondPacket extends Packet implements CodecBuffer.WriteReadStream {

    protected UUID queryId;

    public RespondPacket() {
        super();
    }

    public RespondPacket(@NotNull CodecBuffer buffer) {
        super(buffer);
        this.queryId = buffer.readUniqueId();
        this.readBuffer(buffer);
    }
}
