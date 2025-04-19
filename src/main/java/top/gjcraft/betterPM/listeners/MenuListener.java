package top.gjcraft.betterPM.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
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
        if (!event.getView().getTitle().startsWith("插件管理")) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String itemName = clickedItem.getItemMeta().getDisplayName();
        if (itemName.equals("§f上一页")) {
            int currentPage = menuManager.getCurrentPage(player);
            menuManager.openPluginMenu(player, currentPage - 1);
            return;
        }
        if (itemName.equals("§f下一页")) {
            int currentPage = menuManager.getCurrentPage(player);
            menuManager.openPluginMenu(player, currentPage + 1);
            return;
        }

        // 处理插件操作
        String pluginName = itemName.substring(2); // 移除颜色代码
        Plugin targetPlugin = player.getServer().getPluginManager().getPlugin(pluginName);
        if (targetPlugin == null) return;

        if (event.isLeftClick()) {
            // 左键点击：启用/禁用插件
            if (targetPlugin.isEnabled()) {
                pluginManager.unloadPlugin(pluginName);
            } else {
                pluginManager.loadPlugin(pluginName);
            }
        } else if (event.isRightClick()) {
            // 右键点击：重载插件
            pluginManager.reloadPlugin(pluginName);
        }

        // 更新菜单
        menuManager.openPluginMenu(player, menuManager.getCurrentPage(player));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().startsWith("插件管理") && event.getPlayer() instanceof Player player) {
            menuManager.getCurrentPage(player);
        }
    }
}