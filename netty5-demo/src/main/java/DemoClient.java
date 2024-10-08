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
import de.lumesolutions.netty5.client.Netty5Client;

import java.util.UUID;

public class DemoClient {
    public static void main(String[] args) {
        var client = new Netty5Client("127.0.0.1", 8080,
                new Netty5ClientChannel.Identity("Client-1", UUID.randomUUID()),
                null);

        try {
            client.initialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        client.connectionFuture().thenAccept(_ -> {
            System.out.println("connected");

            // Call the send method
            send(client);
        });
    }

    public static void send(Netty5Client client) {
        new Thread(() -> {
            DemoRespondPacket packet = client.thisChannel().transmitter().queryPacketDirect(new DemoRequestPacket("asd"), DemoRespondPacket.class);
            System.out.println(packet.d());
        }).start();
    }
}
