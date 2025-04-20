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
        String title = event.getView().getTitle();
        if (!(title.startsWith("插件管理") || title.startsWith("插件操作"))) {
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

        // 处理主菜单操作
        if (title.startsWith("插件管理")) {
            // 处理插件项点击
            if (event.getSlot() < 45 && clickedItem.getItemMeta() != null) {
                String pluginFileName = clickedItem.getItemMeta().getDisplayName().substring(2);
                menuManager.openPluginSubMenu(player, pluginFileName);
                return;
            }

            // 处理刷新按钮
            if (event.getSlot() == 49) {
                menuManager.openPluginMenu(player, currentPage);
                return;
            }
            return;
        }

        // 处理子菜单操作
        if (title.startsWith("插件操作")) {
            String selectedPlugin = menuManager.getSelectedPlugin(player.getName());
            if (selectedPlugin == null) {
                player.sendMessage("§c发生错误，请重新选择插件");
                menuManager.openPluginMenu(player, currentPage);
                return;
            }

            String pluginName = selectedPlugin;
            if (pluginName.endsWith(".jar.disabled")) {
                pluginName = pluginName.substring(0, pluginName.length() - 13);
            } else if (pluginName.endsWith(".jar")) {
                pluginName = pluginName.substring(0, pluginName.length() - 4);
            }

            switch (event.getSlot()) {
                case 0: // 加载插件
                    pluginManager.loadPlugin(pluginName);
                    break;
                case 2: // 卸载插件
                    pluginManager.unloadPlugin(pluginName);
                    break;
                case 4: // 启用插件
                    pluginManager.enablePlugin(pluginName);
                    break;
                case 6: // 禁用插件
                    pluginManager.disablePlugin(pluginName);
                    break;
                case 8: // 返回主菜单
                    menuManager.openPluginMenu(player, currentPage);
                    return;
            }

            // 执行操作后返回主菜单
            menuManager.openPluginMenu(player, currentPage);
        }
    }
}