package com.gmail.haloinverse.DynamicMarket;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Messaging {

    public Player player = null;
    private CommandSender sender = null;
    public static String colNormal = "&e"; // Normal text colour {}
    public static String colCmd = "&f"; // Command highlight colour {CMD}
    public static String colBracket = "&d"; // Highlighting of brackets around params/data {PBK}
    public static String colParam = "&b"; // Highlighting of parameters.
    public static String colError = "&c"; // Highlighting for errors. {ERR}
    /*     */

    public Messaging(CommandSender thisSender) {
        sender = thisSender;
        if (thisSender instanceof Player) {
            player = (Player) thisSender;
        }
    }

    public boolean isPlayer() {
        if (player == null) {
            return false;
        }
        return true;
    }

    @Deprecated
    public static String argument(String original, String[] arguments, String[] points) {
        for (int i = 0; i < arguments.length; ++i) {
            if (arguments[i].contains(",")) {
                for (String arg : arguments[i].split(",")) {
                    original = original.replace(arg, points[i]);
                }
            } else {
                original = original.replace(arguments[i], points[i]);
            }
        }

        return original;
    }

    public static String parseHighlights(String original) {
        return original.replace("{}", colNormal).replace("{CMD}", colCmd).replace("{BKT}", colBracket).replace("{ERR}", colError).replace("{PRM}", colParam);
    }

    public static String stripHighlights(String original) {
        return original.replace("{}", "").replace("{CMD}", "").replace("{BKT}", "").replace("{ERR}", "").replace("{PRM}", "");
    }

    public static String parse(String original) {
        return parseHighlights(original).replaceAll("(&([a-z0-9]))", "§$2").replace("&&", "&");
    }

    public static String colorize(String original) {
        return original.replace("<black>", "§0").replace("<navy>", "§1").replace("<green>", "§2").replace("<teal>", "§3").replace("<red>", "§4").replace("<purple>", "§5").replace("<gold>", "§6").replace("<silver>", "§7").replace("<gray>", "§8").replace("<blue>", "§9").replace("<lime>", "§a").replace("<aqua>", "§b").replace("<rose>", "§c").replace("<pink>", "§d").replace("<yellow>", "§e").replace("<white>", "§f");
    }

    public static String bracketize(String message) {
        return "[" + message + "]";
    }

    public void send(String message) {
        if (sender != null) {
            sender.sendMessage(parse(message));
        }
    }

    public static void broadcast(String message) {
        for (Player p : iListen.plugin.getServer().getOnlinePlayers()) {
            p.sendMessage(parse(message));
        }
    }
}
