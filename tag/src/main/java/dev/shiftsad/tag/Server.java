package dev.shiftsad.tag;

import dev.shiftsad.core.MinestomServer;
import dev.shiftsad.core.modules.impl.ConsulConfigModule;
import dev.shiftsad.core.modules.impl.RedisModule;
import dev.shiftsad.core.modules.impl.ServerPublisherModule;
import dev.shiftsad.core.util.NameGenerator;

import static dev.shiftsad.core.util.NameGenerator.WordType.*;

public class Server {

    public static void main(String[] args) {
        var name = NameGenerator.randomName(ANIMALS, COLORS, ADJECTIVE);
        var port = Integer.parseInt(System.getenv("PORT"));
        if (port == 0) port = 25565;
        var config = new ConsulConfigModule();

        var server = MinestomServer.builder()
                .game("tag")
                .name(name)
                .port(port)
                .modules(
                        config,
                        new RedisModule(config),
                        new ServerPublisherModule(config, name, String.valueOf(port))
                )
                .build();

        server.start();
    }
}
