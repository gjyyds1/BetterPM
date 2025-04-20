package top.gjcraft.betterPM.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import top.gjcraft.betterPM.manager.MenuManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BPMCommand implements CommandExecutor {
    private final FileConfiguration config;
    private final PluginManager pluginManager;
    private final File pluginsFolder;
    private final int pluginsPerPage;
    private final top.gjcraft.betterPM.manager.BetterPluginManager pluginManagerUtil;
    private final MenuManager menuManager;

    public BPMCommand(FileConfiguration config, PluginManager pluginManager, File pluginsFolder) {
        this.config = config;
        this.pluginManager = pluginManager;
        this.pluginsFolder = pluginsFolder;
        this.pluginsPerPage = config.getInt("plugins-per-page", 10);
        this.pluginManagerUtil = new top.gjcraft.betterPM.manager.BetterPluginManager(pluginManager, pluginsFolder);
        this.menuManager = new MenuManager(pluginManagerUtil, pluginManager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return handleListCommand(sender, 1);
        }

        return switch (args[0].toLowerCase()) {
            case "dump" -> handleDumpCommand(sender);
            case "load" -> args.length > 1 && handleLoadCommand(sender, args[1]);
            case "unload" -> args.length > 1 && handleUnloadCommand(sender, args[1]);
            case "reload" -> args.length > 1 && handleReloadCommand(sender, args[1]);
            case "disable" -> args.length > 1 && handleDisableCommand(sender, args[1]);
            case "enable" -> args.length > 1 && handleEnableCommand(sender, args[1]);
            case "list" -> handleListCommand(sender, 1);
            case "page" -> args.length > 1 && handleListCommand(sender, Integer.parseInt(args[1]));
            case "menu" -> handleMenuCommand(sender);
            case "refresh" -> handleRefreshCommand(sender);
            default -> false;
        };
    }

    private boolean handleDumpCommand(CommandSender sender) {
        try {
            File dumpFile = new File(pluginsFolder.getParentFile(), "plugins_version_dump.txt");
            FileWriter writer = new FileWriter(dumpFile);
            for (Plugin plugin : pluginManager.getPlugins()) {
                writer.write(plugin.getName() + " v" + plugin.getDescription().getVersion() + "\n");
            }
            writer.close();
            sender.sendMessage(config.getString("operation.dump-success"));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean handleLoadCommand(CommandSender sender, String fileName) {
        boolean success = pluginManagerUtil.loadPlugin(fileName);
        sender.sendMessage(success ?
                config.getString("operation.load-success").replace("{plugin}", fileName) :
                config.getString("operation.load-failed").replace("{plugin}", fileName));
        return true;
    }

    private boolean handleUnloadCommand(CommandSender sender, String pluginName) {
        boolean success = pluginManagerUtil.unloadPlugin(pluginName);
        if (success) {
            sender.sendMessage(config.getString("operation.unload-success").replace("{plugin}", pluginName));
        } else {
            sender.sendMessage(config.getString("operation.plugin-not-found").replace("{plugin}", pluginName));
        }
        return true;
    }

    private boolean handleDisableCommand(CommandSender sender, String pluginName) {
        boolean success = pluginManagerUtil.disablePlugin(pluginName);
        if (success) {
            sender.sendMessage(config.getString("operation.disable-success").replace("{plugin}", pluginName));
        } else {
            sender.sendMessage(config.getString("operation.plugin-not-found").replace("{plugin}", pluginName));
        }
        return true;
    }

    private boolean handleEnableCommand(CommandSender sender, String fileName) {
        boolean success = pluginManagerUtil.enablePlugin(fileName);
        if (success) {
            sender.sendMessage(config.getString("operation.enable-success").replace("{plugin}", fileName));
        } else {
            sender.sendMessage(config.getString("operation.plugin-not-found").replace("{plugin}", fileName));
        }
        return true;
    }

    private boolean handleReloadCommand(CommandSender sender, String pluginName) {
        boolean success = pluginManagerUtil.reloadPlugin(pluginName);
        if (success) {
            sender.sendMessage(config.getString("operation.reload-success").replace("{plugin}", pluginName));
        } else {
            sender.sendMessage(config.getString("operation.plugin-not-found").replace("{plugin}", pluginName));
        }
        return true;
    }

    private boolean handleMenuCommand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c该命令只能由玩家执行！");
            return false;
        }
        menuManager.openPluginMenu(player);
        return true;
    }

    private boolean handleRefreshCommand(CommandSender sender) {
        pluginManagerUtil.scanPluginFiles();
        sender.sendMessage(config.getString("operation.refresh-success", "§a插件列表已刷新"));
        return true;
    }

    private boolean handleListCommand(CommandSender sender, int page) {
        Plugin[] plugins = pluginManager.getPlugins();
        List<String> pluginNames = new ArrayList<>();
        for (Plugin plugin : plugins) {
            pluginNames.add(plugin.getName());
        }
        Collections.sort(pluginNames, String.CASE_INSENSITIVE_ORDER);

        int totalPages = (int) Math.ceil(pluginNames.size() / (double) pluginsPerPage);
        if (page < 1 || page > totalPages) {
            sender.sendMessage(config.getString("operation.invalid-page"));
            return true;
        }

        String header = config.getString("messages.plugin-list-header")
                .replace("{page}", String.valueOf(page))
                .replace("{total}", String.valueOf(totalPages));
        sender.sendMessage(header);

        int startIndex = (page - 1) * pluginsPerPage;
        int endIndex = Math.min(startIndex + pluginsPerPage, pluginNames.size());

        for (int i = startIndex; i < endIndex; i++) {
            String pluginName = pluginNames.get(i);
            Plugin plugin = pluginManager.getPlugin(pluginName);
            String status = plugin.isEnabled() ?
                    config.getString("messages.plugin-enabled") :
                    config.getString("messages.plugin-disabled");

            String format = config.getString("messages.plugin-list-format")
                    .replace("{plugin_name}", pluginName)
                    .replace("{status}", status);
            sender.sendMessage(format);
        }

        sender.sendMessage(config.getString("messages.plugin-list-footer"));
        return true;
    }
}