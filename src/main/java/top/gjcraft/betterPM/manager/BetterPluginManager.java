package top.gjcraft.betterPM.manager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BetterPluginManager {
    private final org.bukkit.plugin.PluginManager bukkitPluginManager;
    private final File pluginsFolder;
    private boolean isPaperServer;
    private Method paperPluginUnloadMethod;
    private Method paperPluginLoadMethod;

    public BetterPluginManager(PluginManager bukkitPluginManager, File pluginsFolder) {
        this.bukkitPluginManager = bukkitPluginManager;
        this.pluginsFolder = pluginsFolder;
        
        // 检测是否为Paper服务端
        this.isPaperServer = checkPaperServer();
        if (isPaperServer) {
            initPaperMethods();
        }
    }

    private boolean checkPaperServer() {
        try {
            // 使用ServerBuildInfo API检测Paper服务端
            Class<?> serverBuildInfoClass = Class.forName("io.papermc.paper.ServerBuildInfo");
            Object buildInfo = serverBuildInfoClass.getMethod("buildInfo").invoke(null);
            Object brandId = serverBuildInfoClass.getMethod("brandId").invoke(buildInfo);
            return brandId.toString().equals("papermc:paper");
        } catch (Exception e) {
            return false;
        }
    }

    private void initPaperMethods() {
        try {
            Class<?> simplePluginManagerClass = bukkitPluginManager.getClass();
            try {
                // 尝试获取Paper特定的插件管理方法
                paperPluginUnloadMethod = simplePluginManagerClass.getDeclaredMethod("unloadPlugin", Plugin.class);
                paperPluginLoadMethod = simplePluginManagerClass.getDeclaredMethod("loadPlugin", File.class);
                paperPluginUnloadMethod.setAccessible(true);
                paperPluginLoadMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                // 尝试获取通用插件管理方法
                paperPluginUnloadMethod = simplePluginManagerClass.getMethod("unloadPlugin", Plugin.class);
                paperPluginLoadMethod = simplePluginManagerClass.getMethod("loadPlugin", File.class);
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("无法初始化Paper插件管理方法：" + e.getMessage());
            Bukkit.getLogger().warning("将使用标准Bukkit API作为备选方案");
            isPaperServer = false;
        }
    }

    public boolean loadPlugin(String fileName) {
        // 首先检查插件是否已经加载
        Plugin existingPlugin = bukkitPluginManager.getPlugin(fileName);
        if (existingPlugin != null) {
            // 如果插件已存在但未启用，直接启用它
            if (!existingPlugin.isEnabled()) {
                bukkitPluginManager.enablePlugin(existingPlugin);
            }
            return true;
        }

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

        if (isPaperServer && paperPluginUnloadMethod != null) {
            try {
                paperPluginUnloadMethod.invoke(bukkitPluginManager, plugin);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        bukkitPluginManager.disablePlugin(plugin);
        return true;
    }

    public boolean reloadPlugin(String pluginName) {
        Plugin plugin = bukkitPluginManager.getPlugin(pluginName);
        if (plugin == null) {
            return loadPlugin(pluginName);
        }

        // 先完全卸载插件
        if (isPaperServer && paperPluginUnloadMethod != null) {
            try {
                paperPluginUnloadMethod.invoke(bukkitPluginManager, plugin);
            } catch (Exception e) {
                e.printStackTrace();
                // 如果Paper方法失败，使用标准方法
                bukkitPluginManager.disablePlugin(plugin);
            }
        } else {
            bukkitPluginManager.disablePlugin(plugin);
        }

        // 重新加载插件
        return loadPlugin(pluginName);
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

    public List<File> scanPluginFiles() {
        List<File> pluginFiles = new ArrayList<>();
        File[] files = pluginsFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                String name = file.getName().toLowerCase();
                if (name.endsWith(".jar") || name.endsWith(".jar.disabled")) {
                    pluginFiles.add(file);
                }
            }
        }
        return pluginFiles;
    }

    public boolean isPluginLoaded(String fileName) {
        String pluginName = fileName;
        if (pluginName.endsWith(".jar")) {
            pluginName = pluginName.substring(0, pluginName.length() - 4);
        } else if (pluginName.endsWith(".jar.disabled")) {
            pluginName = pluginName.substring(0, pluginName.length() - 13);
        }
        return bukkitPluginManager.getPlugin(pluginName) != null;
    }
}