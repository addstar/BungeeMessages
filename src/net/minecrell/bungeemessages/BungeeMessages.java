package net.minecrell.bungeemessages;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.logging.Level;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeMessages extends Plugin {
    private static final String MESSAGE_FILE = "messages.properties";
    private static final Path messagePath = Paths.get(MESSAGE_FILE);

    @Override
    public void onEnable() {
        // Make sure the translation file is up to date
        try {
            this.getLogger().info("Checking default messages for new entries...");

            Properties latest = new Properties();
            try (InputStream in = BungeeCord.class.getClassLoader().getResourceAsStream(MESSAGE_FILE)) {
                latest.load(in);
            }

            if (Files.exists(messagePath)) {
                Properties custom = new Properties();

                try (InputStream in = Files.newInputStream(messagePath)) {
                    custom.load(in);
                }

                latest.putAll(custom);
            } else {
                this.getLogger().info("Creating new message file at: " + messagePath.toAbsolutePath());
                Files.createFile(messagePath);
            }

            try (OutputStream out = Files.newOutputStream(messagePath)) {
                latest.store(out, "BungeeCord messages, last updated for " + this.getProxy().getVersion());
            }
        } catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "An internal error occurred while updating the BungeeCord messages!");
            return;
        }

        this.reload();
        this.getProxy().getPluginManager().registerCommand(this, new ReloadCommand());
    }

    public final class ReloadCommand extends Command {
        private ReloadCommand() {
            super("BungeeMessages", "BungeeMessages.Reload");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            if (reload())
                sender.sendMessage(ChatColor.GREEN + "BungeeCord messages successfully reloaded!");
            else sender.sendMessage(ChatColor.RED + "An internal error occurred while reloading the BungeeCord " +
                    "messages!");
        }
    }

    public boolean reload() {
        this.getLogger().info("Reloading BungeeCord messages...");

        try (InputStream in = Files.newInputStream(messagePath)) {
            BungeeCord.getInstance().bundle = new PropertyResourceBundle(in);
        } catch (Exception e) {
            this.getLogger().log(Level.SEVERE, "Unable to reload BungeeCord messages from: " + messagePath
                    .toAbsolutePath()); return false;
        }

        this.getLogger().info("BungeeCord messages successfully reloaded."); return true;
    }
}
