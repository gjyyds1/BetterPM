package top.gjcraft.betterPM.manager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuManager {
    private final BetterPluginManager pluginManager;
    private final PluginManager bukkitPluginManager;
    private final Map<String, Integer> playerPages;
    private final int ITEMS_PER_PAGE = 45; // 5行9列的箱子界面，最后一行放控制按钮

    public MenuManager(BetterPluginManager pluginManager, PluginManager bukkitPluginManager) {
        this.pluginManager = pluginManager;
        this.bukkitPluginManager = bukkitPluginManager;
        this.playerPages = new HashMap<>();
    }

    public void openPluginMenu(Player player) {
        openPluginMenu(player, 1);
    }

    public void openPluginMenu(Player player, int page) {
        Plugin[] plugins = bukkitPluginManager.getPlugins();
        int totalPages = (int) Math.ceil((double) plugins.length / ITEMS_PER_PAGE);
        page = Math.max(1, Math.min(page, totalPages));
        playerPages.put(player.getName(), page);

        Inventory inventory = Bukkit.createInventory(null, 54, "插件管理 - 第 " + page + " 页");

        // 填充插件项
        int startIndex = (page - 1) * ITEMS_PER_PAGE;
        for (int i = 0; i < ITEMS_PER_PAGE && startIndex + i < plugins.length; i++) {
            Plugin plugin = plugins[startIndex + i];
            ItemStack item = createPluginItem(plugin);
            inventory.setItem(i, item);
        }

        // 添加控制按钮
        if (page > 1) {
            inventory.setItem(45, createNavigationItem(Material.ARROW, "上一页"));
        }
        if (page < totalPages) {
            inventory.setItem(53, createNavigationItem(Material.ARROW, "下一页"));
        }

        player.openInventory(inventory);
    }

    private ItemStack createPluginItem(Plugin plugin) {
        ItemStack item = new ItemStack(plugin.isEnabled() ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§f" + plugin.getName());
            List<String> lore = new ArrayList<>();
            lore.add("§7版本: " + plugin.getDescription().getVersion());
            lore.add("§7状态: " + (plugin.isEnabled() ? "§a已启用" : "§c已禁用"));
            lore.add("");
            lore.add("§e左键点击: " + (plugin.isEnabled() ? "禁用" : "启用"));
            lore.add("§e右键点击: 重载插件");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createNavigationItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§f" + name);
            item.setItemMeta(meta);
        }
        return item;
    }

    public int getCurrentPage(Player player) {
        return playerPages.getOrDefault(player.getName(), 1);
    }
}