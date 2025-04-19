package top.gjcraft.betterPM;

import org.bukkit.plugin.java.JavaPlugin;
import top.gjcraft.betterPM.commands.BPMCommand;
import top.gjcraft.betterPM.listeners.MenuListener;
import top.gjcraft.betterPM.manager.BetterPluginManager;
import top.gjcraft.betterPM.manager.MenuManager;

public final class BetterPM extends JavaPlugin {

    @Override
    public void onEnable() {
        // 保存默认配置
        saveDefaultConfig();

        // 初始化插件管理器
        BetterPluginManager pluginManager = new BetterPluginManager(getServer().getPluginManager(), getDataFolder().getParentFile());
        MenuManager menuManager = new MenuManager(pluginManager, getServer().getPluginManager());
        
        // 注册命令
        getCommand("bpm").setExecutor(new BPMCommand(getConfig(), getServer().getPluginManager(), getDataFolder().getParentFile()));
        
        // 注册监听器
        getServer().getPluginManager().registerEvents(new MenuListener(menuManager, pluginManager), this);

        getLogger().info("BetterPM插件已启动");
        getLogger().info("作者: gjyyds1");
    }

    @Override
    public void onDisable() {
        getLogger().info("BetterPM插件已关闭");
    }
}
