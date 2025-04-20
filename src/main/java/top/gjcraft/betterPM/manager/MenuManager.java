package top.gjcraft.betterPM.manager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuManager {
    private final BetterPluginManager pluginManager;
    private final PluginManager bukkitPluginManager;
    private final Map<String, Integer> playerPages;
    private final Map<String, String> selectedPlugins;
    private final int ITEMS_PER_PAGE = 45; // 5行9列的箱子界面，最后一行放控制按钮
    private final int SUB_MENU_SIZE = 9; // 子菜单大小

    public MenuManager(BetterPluginManager pluginManager, PluginManager bukkitPluginManager) {
        this.pluginManager = pluginManager;
        this.bukkitPluginManager = bukkitPluginManager;
        this.playerPages = new HashMap<>();
        this.selectedPlugins = new HashMap<>();
    }

    public void openPluginMenu(Player player) {
        openPluginMenu(player, 1);
    }

    public void openPluginMenu(Player player, int page) {
        List<File> pluginFiles = pluginManager.scanPluginFiles();
        int totalPages = (int) Math.ceil((double) pluginFiles.size() / ITEMS_PER_PAGE);
        page = Math.max(1, Math.min(page, totalPages));
        playerPages.put(player.getName(), page);

        Inventory inventory = Bukkit.createInventory(null, 54, "插件管理 - 第 " + page + " 页");

        // 填充插件项
        int startIndex = (page - 1) * ITEMS_PER_PAGE;
        for (int i = 0; i < ITEMS_PER_PAGE && startIndex + i < pluginFiles.size(); i++) {
            File file = pluginFiles.get(startIndex + i);
            ItemStack item = createPluginItem(file, player.getName());
            inventory.setItem(i, item);
        }

        // 添加控制按钮
        if (page > 1) {
            inventory.setItem(45, createNavigationItem(Material.ARROW, "上一页"));
        }
        if (page < totalPages) {
            inventory.setItem(53, createNavigationItem(Material.ARROW, "下一页"));
        }

        // 添加刷新按钮
        inventory.setItem(49, createActionButton(Material.CLOCK, "刷新"));

        player.openInventory(inventory);
    }

    private ItemStack createPluginItem(File file, String playerName) {
        String fileName = file.getName();
        boolean isDisabled = fileName.endsWith(".disabled");
        boolean isLoaded = pluginManager.isPluginLoaded(fileName);

        Material material = isLoaded ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = fileName;
            if (displayName.endsWith(".jar.disabled")) {
                displayName = displayName.substring(0, displayName.length() - 13);
            } else if (displayName.endsWith(".jar")) {
                displayName = displayName.substring(0, displayName.length() - 4);
            }
            meta.setDisplayName("§f" + displayName);

            List<String> lore = new ArrayList<>();
            Plugin plugin = bukkitPluginManager.getPlugin(displayName);
            boolean isEnabled = plugin != null && plugin.isEnabled();
            lore.add("§7状态: " + (isEnabled ? "§a已启用" : "§c未启用"));
            if (isDisabled) {
                lore.add("§7文件状态: §c已禁用");
            }
            lore.add("");
            lore.add("§e点击进入操作菜单");
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

    private ItemStack createActionButton(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§f" + name);
            List<String> lore = new ArrayList<>();
            lore.add("§7点击执行操作");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void openPluginSubMenu(Player player, String pluginFileName) {
        Inventory inventory = Bukkit.createInventory(null, SUB_MENU_SIZE, "插件操作 - " + pluginFileName);
        
        // 添加操作按钮
        inventory.setItem(0, createActionButton(Material.HOPPER, "加载插件"));
        inventory.setItem(2, createActionButton(Material.BARRIER, "卸载插件"));
        inventory.setItem(4, createActionButton(Material.LIME_DYE, "启用插件"));
        inventory.setItem(6, createActionButton(Material.GRAY_DYE, "禁用插件"));
        inventory.setItem(8, createActionButton(Material.ARROW, "返回"));

        player.openInventory(inventory);
        selectedPlugins.put(player.getName(), pluginFileName);
    }

    public String getSelectedPlugin(String playerName) {
        return selectedPlugins.get(playerName);
    }

    public int getCurrentPage(Player player) {
        return playerPages.getOrDefault(player.getName(), 1);
    }
}