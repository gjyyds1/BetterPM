package top.gjcraft.betterPM;

import org.bukkit.plugin.java.JavaPlugin;
import top.gjcraft.betterPM.commands.BPMCommand;

public final class BetterPM extends JavaPlugin {

    @Override
    public void onEnable() {
        // 保存默认配置
        saveDefaultConfig();

        // 注册命令
        getCommand("bpm").setExecutor(new BPMCommand(getConfig(), getServer().getPluginManager(), getDataFolder().getParentFile()));

        getLogger().info("BetterPM插件已启动");
        getLogger().info("作者: gjyyds1");
    }

    @Override
    public void onDisable() {
        getLogger().info("BetterPM插件已关闭");
    }
}
