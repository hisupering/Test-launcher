package com.simpleengine.game;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class MinecraftCommandLine {
    private MinecraftCommandLine() {}

    public static List<String> resolve(
            List<String> arguments,
            MinecraftArguments variables) {
        List<String> result = new ArrayList<>();
        if (arguments == null) return result;
        for (String argument : arguments) {
            if (argument == null) continue;
            result.add(variables.resolve(argument));
        }
        return result;
    }

    public static MinecraftArguments defaults(
            String username,
            File gameDirectory,
            String version,
            String classpath) {
        return new MinecraftArguments()
                .put("auth_player_name", username == null ? "Player" : username)
                .put("game_directory", gameDirectory.getAbsolutePath())
                .put("version_name", version)
                .put("classpath", classpath);
    }
}
