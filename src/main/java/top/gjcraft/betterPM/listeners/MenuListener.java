package top.gjcraft.betterPM.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import top.gjcraft.betterPM.manager.BetterPluginManager;
import top.gjcraft.betterPM.manager.MenuManager;

public class MenuListener implements Listener {
    private final MenuManager menuManager;
    private final BetterPluginManager pluginManager;

    public MenuListener(MenuManager menuManager, BetterPluginManager pluginManager) {
        this.menuManager = menuManager;
        this.pluginManager = pluginManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().startsWith("插件管理")) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null) {
            return;
        }

        int currentPage = menuManager.getCurrentPage(player);

        // 处理翻页按钮
        if (event.getSlot() == 45 && currentPage > 1) {
            menuManager.openPluginMenu(player, currentPage - 1);
            return;
        }

        if (event.getSlot() == 53) {
            menuManager.openPluginMenu(player, currentPage + 1);
            return;
        }

        // 处理插件选择
        if (event.getSlot() < 45) {
            String pluginFileName = clickedItem.getItemMeta().getDisplayName().substring(2);
            if (pluginFileName.endsWith(".jar") || pluginFileName.endsWith(".jar.disabled")) {
                menuManager.setSelectedPlugin(player.getName(), pluginFileName + ".jar");
                menuManager.openPluginMenu(player, currentPage);
            }
            return;
        }

        // 处理操作按钮
        String selectedPlugin = menuManager.getSelectedPlugin(player.getName());
        if (selectedPlugin == null) {
            player.sendMessage("§c请先选择一个插件");
            return;
        }

        String pluginName = selectedPlugin;
        if (pluginName.endsWith(".jar")) {
            pluginName = pluginName.substring(0, pluginName.length() - 4);
        } else if (pluginName.endsWith(".jar.disabled")) {
            pluginName = pluginName.substring(0, pluginName.length() - 13);
        }

        switch (event.getSlot()) {
            case 47: // 加载插件
                pluginManager.loadPlugin(pluginName);
                break;
            case 48: // 卸载插件
                pluginManager.unloadPlugin(pluginName);
                break;
            case 50: // 启用插件
                pluginManager.enablePlugin(pluginName);
                break;
            case 51: // 禁用插件
                pluginManager.disablePlugin(pluginName);
                break;
        }

        menuManager.openPluginMenu(player, currentPage);
    }
}