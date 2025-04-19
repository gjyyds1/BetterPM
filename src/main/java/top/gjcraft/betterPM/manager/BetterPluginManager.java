package top.gjcraft.betterPM.manager;

import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class BetterPluginManager {
    private final org.bukkit.plugin.PluginManager bukkitPluginManager;
    private final File pluginsFolder;

    public BetterPluginManager(PluginManager bukkitPluginManager, File pluginsFolder) {
        this.bukkitPluginManager = bukkitPluginManager;
        this.pluginsFolder = pluginsFolder;
    }

    public boolean loadPlugin(String fileName) {
        File pluginFile = new File(pluginsFolder, fileName + ".jar");
        if (!pluginFile.exists()) {
            return false;
        }

        try {
            Plugin plugin = bukkitPluginManager.loadPlugin(pluginFile);
            if (plugin != null) {
                plugin.onLoad();
                bukkitPluginManager.enablePlugin(plugin);
                return true;
            }
        } catch (InvalidPluginException | InvalidDescriptionException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean unloadPlugin(String pluginName) {
        Plugin plugin = bukkitPluginManager.getPlugin(pluginName);
        if (plugin == null) {
            return false;
        }

        bukkitPluginManager.disablePlugin(plugin);
        return true;
    }

    public boolean disablePlugin(String pluginName) {
        Plugin plugin = bukkitPluginManager.getPlugin(pluginName);
        if (plugin == null) {
            return false;
        }

        // 如果插件正在运行，先卸载它
        if (plugin.isEnabled()) {
            bukkitPluginManager.disablePlugin(plugin);
        }

        // 获取插件文件并添加.disabled后缀
        File pluginFile = new File(pluginsFolder, pluginName + ".jar");
        File disabledFile = new File(pluginFile + ".disabled");

        try {
            Files.move(pluginFile.toPath(), disabledFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean enablePlugin(String fileName) {
        File disabledFile = new File(pluginsFolder, fileName + ".jar.disabled");
        if (!disabledFile.exists()) {
            return false;
        }

        File enabledFile = new File(pluginsFolder, fileName + ".jar");
        try {
            Files.move(disabledFile.toPath(), enabledFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return loadPlugin(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}