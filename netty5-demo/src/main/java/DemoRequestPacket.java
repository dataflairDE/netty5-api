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
import de.dataflair.netty5.common.packet.QueryPacket;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class DemoRequestPacket extends QueryPacket {

    private String s;

    public DemoRequestPacket(String s) {
        this.s = s;
    }

    public DemoRequestPacket(@NotNull CodecBuffer buffer) {
        super(buffer);
    }

    @Override
    public void writeBuffer(@NotNull CodecBuffer codecBuffer) {
        codecBuffer.writeString(this.s);
    }

    @Override
    public void readBuffer(@NotNull CodecBuffer codecBuffer) {
        this.s = codecBuffer.readString();
    }
}
