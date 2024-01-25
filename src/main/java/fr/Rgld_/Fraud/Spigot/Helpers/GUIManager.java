package fr.Rgld_.Fraud.Spigot.Helpers;

import fr.Rgld_.Fraud.Spigot.Fraud;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GUIManager implements Listener {

    private final Fraud fraud;
    private final HashMap<String, String> inv_names = new HashMap<>();
    private final HashMap<Player, String> looker = new HashMap<>();

    public GUIManager(Fraud fraud) {
        this.fraud = fraud;
        inv_names.put("main", "§cFraud §7- §cMenu");
        inv_names.put("alts", "§cFraud §7- §cAlts");
        inv_names.put("detail", "§cFraud §7- §cDetail §7- §c");
    }

    /**
     * Open the GUI of the plugin.
     * @param player The player who opens the GUI.
     */
    public void openMainGUI(final Player player) {
        Inventory inv = Bukkit.createInventory(null, 9*3, inv_names.get("main"));

        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta info_meta = info.getItemMeta();
        info_meta.setDisplayName(Messages.GUI_MAIN_INFOS.getMessage());
        info_meta.setLore(Arrays.asList("§7Click to see the §6infos §7of the plugin.",
                                        "§7Version: §6" + fraud.getDescription().getVersion(),
                                        "§7Author: §6Rgld_",
                                        "§7Spigot Page: §6" + Links.FRAUD_SPIGOT.getUrl()));
        info.setItemMeta(info_meta);


        ItemStack alts = createPHead("Rgld_", Messages.GUI_MAIN_ALTS.getMessage());

        inv.setItem(11, info);
        inv.setItem(13, alts);
        inv.setItem(15, getCloseItem());

        fillInv(inv);

        player.openInventory(inv);
    }

    /**
     * Open the GUI of the alts.
     * @param player The player who opens the GUI.
     */
    public void openAltsGUI(final Player player) {
        Inventory inv = Bukkit.createInventory(null, 9*3, inv_names.get("alts"));
        inv.setItem(inv.getSize()-1, getBackItem());

        ConcurrentHashMap<String, List<OfflinePlayer>> alts = fraud.getData().getAllPlayersWDA_OfflinePlayer();

        if(alts.isEmpty()) {
            ItemStack noAlts = new ItemStack(Material.BARRIER);
            ItemMeta noAlts_meta = noAlts.getItemMeta();
            noAlts_meta.setDisplayName("§cNo alts found");
            noAlts.setItemMeta(noAlts_meta);
            inv.setItem(13, noAlts);
        } else {
            for (Map.Entry<String, List<OfflinePlayer>> entry : alts.entrySet()) {
                if(inv.firstEmpty() == 26) {
                    ItemStack moreAlts = new ItemStack(Material.PAPER);
                    ItemMeta moreAltsMeta = moreAlts.getItemMeta();
                    moreAltsMeta.setDisplayName(Messages.GUI_DETAIL_MORE_ALTS.format(alts.size()-26));
                    moreAlts.setItemMeta(moreAltsMeta);
                    inv.setItem(26, moreAlts);
                    break;
                }
                String ipAddress = entry.getKey();
                List<OfflinePlayer> altPlayers = entry.getValue();
                List<String> altNames = new ArrayList<>();
                for (OfflinePlayer alt : altPlayers) {
                    String vl = "§7- " + alt.getName();
                    if(!altNames.contains(vl))
                        altNames.add(vl);
                }
                inv.addItem(createPHead(altNames.get(0), Messages.GUI_ALTS_IP.format(ipAddress), altNames.toArray(new String[0])));
            }
        }

        fillInv(inv);
        player.openInventory(inv);
    }

    /**
     * Open the GUI of the details of an ip.
     * @param player The player who opens the GUI.
     * @param ipadress The ip to display.
     */
    private void openDetailGUI(final Player player, String ipadress) {
        Inventory inv = Bukkit.createInventory(null, 9*4, inv_names.get("detail") + ipadress);
        inv.setItem(inv.getSize()-1, getBackItem());

        List<String> dc_pseudo = fraud.getData().getList(ipadress);
        if(dc_pseudo.size() <= 9 && !dc_pseudo.isEmpty()) {
            int count = 0;
            for(int i : transform(dc_pseudo.size())) {
                inv.setItem(9+i,
                            generateInfoPlayer(dc_pseudo.get(count)));
                count++;
            }
        } else {
            for (int i = 0; i < 8; i++) {
                inv.setItem(9+i, generateInfoPlayer(dc_pseudo.get(i)));
            }
            ItemStack moreAlts = new ItemStack(Material.PAPER);
            ItemMeta moreAltsMeta = moreAlts.getItemMeta();
            moreAltsMeta.setDisplayName(Messages.GUI_DETAIL_MORE_ALTS.format(dc_pseudo.size()-8));
            moreAlts.setItemMeta(moreAltsMeta);
            inv.setItem(17, moreAlts);
        }

        ItemStack geo = new ItemStack(Material.EMPTY_MAP);
        ItemMeta geo_meta = geo.getItemMeta();
        geo_meta.setDisplayName(Messages.GUI_DETAIL_IP_DETAILS.getMessage());
        List<String> lore = new ArrayList<>();
        lore.add(Messages.GUI_DETAIL_GET_MORE_INFO.getMessage());
        geo_meta.setLore(lore);
        geo.setItemMeta(geo_meta);
        inv.setItem(22, geo);


        fillInv(inv);
        player.openInventory(inv);
    }

    /**
     * Generate the item of a player.
     * @param playerName the name of the player.
     * @return the item of the player.
     */
    private ItemStack generateInfoPlayer(final String playerName) {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner(playerName);
        meta.setDisplayName(playerName);
        List<String> lore = new ArrayList<>();
        lore.add(Messages.GUI_DETAIL_FIRST_CONNECTION.format(Utils.formatDate(fraud.getData().getFirstJoin(playerName))));
        lore.add(Messages.GUI_DETAIL_LAST_CONNECTION.format(Utils.formatDate(fraud.getData().getLastJoin(playerName))));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * When a player click on an item in an inventory.
     * Used in 3 inventories:
     * <ul>
     *     <li>main</li>
     *     <li>alts</li>
     *     <li>detail</li>
     * <ul>
     * @param e the event.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        if (title.equals(inv_names.get("main"))) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
            Player p = (Player) e.getWhoClicked();

            switch (e.getCurrentItem().getType()) {
                case BOOK:
                    p.closeInventory();
                    p.sendMessage(Messages.GUI_MAIN_INFOS.getMessage());
                    p.sendMessage("§7Version: §6" + fraud.getDescription().getVersion());
                    p.sendMessage("§7Author: §6Rgld_");
                    p.sendMessage("§7Spigot Page: §6" + Links.FRAUD_SPIGOT.getUrl());
                    p.sendMessage("§7Rest-API Status Page: §6" + Links.RGLD_API_STATUS_PAGE.getUrl());
                    break;
                case SKULL_ITEM:
                    p.closeInventory();
                    openAltsGUI(p);
                    break;
                case BARRIER:
                    p.closeInventory();
                    p.sendMessage(Messages.GUI_GENERAL_CLOSE.getMessage());
                    break;
            }
        } else if(title.equals(inv_names.get("alts"))) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
            Player p = (Player) e.getWhoClicked();
            switch (e.getCurrentItem().getType()) {
                case SKULL_ITEM:
                    p.closeInventory();
                    String ip = Utils.extractFirstIPV4(e.getCurrentItem().getItemMeta().getDisplayName());
                    if(ip == null || ip.isEmpty() || !Utils.isIPv4Address(ip)) {
                        p.sendMessage("§cAn error occur during the extraction of the ip. Please call an administrator or the developer of this plugin via /fraud contact. (Check Console)");
                        System.err.println("GUIManager.java#onInventoryClick(InventoryClickEvent e)");
                        System.err.println("ip == null -> " + ip == null);
                        System.err.println("ip.isEmpty() -> " + ip.isEmpty());
                        System.err.println("Utils.isIPv4Address(ip) -> " + Utils.isIPv4Address(ip));
                    } else {
                        openDetailGUI(p, ip);
                        looker.remove(p);
                        looker.put(p, ip);
                    }
                    break;
                case ARROW:
                    p.closeInventory();
                    openMainGUI(p);
                    break;
            }
        } else if(title.startsWith(inv_names.get("detail"))) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
            Player p = (Player) e.getWhoClicked();
            switch(e.getCurrentItem().getType()) {
                case EMPTY_MAP:
                    IPInfoManager ipMan = fraud.getIpInfoManager();
                    String ip = looker.get(p);
                    IPInfo ipInfo;
                    if(ipMan.getIpInfoMap().containsKey(ip)) ipInfo = ipMan.getIpInfoMap().get(ip);
                    else ipInfo = ipMan.getIpInfo(ip, p.hasPermission("fraud.geoip"));
                    if(Objects.equals(e.getCurrentItem().getItemMeta().getLore().get(0), Messages.GUI_DETAIL_GET_MORE_INFO.getMessage())) {

                        ItemStack i = e.getCurrentItem();
                        ItemMeta m = i.getItemMeta();
                        List<String> lore = new ArrayList<>();

                        if(ipInfo.getCity() != null && !ipInfo.getCity().isEmpty())
                            lore.add(Messages.GUI_DETAIL_GEO_CITY.format(ipInfo.getCity()));
                        if(ipInfo.getPostalCode() != null && !ipInfo.getPostalCode().isEmpty())
                            lore.add(Messages.GUI_DETAIL_GEO_POSTAL_CODE.format(ipInfo.getPostalCode()));
                        if(ipInfo.getSubDivision() != null && !ipInfo.getSubDivision().isEmpty())
                            lore.add(Messages.GUI_DETAIL_GEO_SUB_DIVISION.format(ipInfo.getSubDivision()));
                        if(ipInfo.getCountryName() != null && !ipInfo.getCountryName().isEmpty())
                            lore.add(Messages.GUI_DETAIL_GEO_COUNTRY.format(ipInfo.getCountryName(), ipInfo.getCountryCode()));
                        if(ipInfo.getContinent() != null && !ipInfo.getContinent().isEmpty())
                            lore.add(Messages.GUI_DETAIL_GEO_CONTINENT.format(ipInfo.getContinent()));
                        if(ipInfo.getLatitude() != null && !ipInfo.getLatitude().isEmpty() && ipInfo.getLongitude() != null && !ipInfo.getLongitude().isEmpty())
                            lore.add(Messages.GUI_DETAIL_GEO_COORDINATES.format(ipInfo.getLatitude(), ipInfo.getLongitude()));
                        if(ipInfo.getNetname() != null && !ipInfo.getNetname().isEmpty()) {
                            lore.add(Messages.GUI_DETAIL_OTHERS.getMessage());
                            for (String s : ipInfo.getDesc()) lore.add("§7- " + s);
                        }
                        if(lore.isEmpty()) {
                            lore.add(Messages.GUI_DETAIL_NO_INFORMATION.getMessage());
                        }
                        m.setLore(lore);
                        i.setItemMeta(m);
                    } else {
                        p.closeInventory();
                        p.sendMessage(ChatColor.GOLD + " → URL Google Maps →" + Utils.generateURLGmap(ipInfo.getLatitude(), ipInfo.getLongitude()));
                    }
                    break;
                case ARROW:
                    p.closeInventory();
                    openAltsGUI(p);
                    break;
            }
        }
    }


    /**
     * Create a player head.
     * @param player_name the name of the player.
     * @param item_name the name of the item.
     * @return the player head.
     */
    private ItemStack createPHead(final String player_name, final String item_name) {
        return createPHead(player_name, item_name, new String[0]);
    }

    /**
     * Create a player head.
     * @param player_name the name of the player.
     * @param item_name the name of the item.
     * @param lore the lore of the item.
     * @return the player head.
     */
    private ItemStack createPHead(final String player_name, final String item_name, final String... lore) {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner(player_name);
        meta.setDisplayName(item_name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Fill the inventory with glass pane.
     * @param inv the inventory to fill.
     */
    private void fillInv(final Inventory inv) {
        ItemStack filler = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta filler_meta = filler.getItemMeta();
        filler_meta.setDisplayName(" ");
        filler.setItemMeta(filler_meta);
        for (int i = 0; i < inv.getSize(); i++) if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) inv.setItem(i, filler);
    }

    /**
     * Get the close item.
     * @return the close item.
     */
    private ItemStack getCloseItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Messages.GUI_GENERAL_CLOSE.getMessage());
        meta.setLore(Collections.singletonList("§7Click to close the GUI."));
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Get the back item.
     * @return the back item.
     */
    private ItemStack getBackItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Messages.GUI_GENERAL_BACK.getMessage());
        item.setItemMeta(meta);
        return item;
    }

    /**
     * The index where put the items in the inventory.
     * <br>
     * 1 item, it is in the middle
     * <br>
     * 2 items, it is in the middle of the left and the right
     * <br>
     * 3 items, it is in the middle of the left, the middle and the right
     * <br>
     * …
     * @param n the number of items
     * @return the index where put the items in the inventory.
     */
    private int[] transform(int n) {
        switch(n) {
            case 1:
                return new int[] { 4 };
            case 2:
                return new int[] { 3, 5 };
            case 3:
                return new int[] { 3,4,5 };
            case 4:
                return new int[] { 2,3,5,6 };
            case 5:
                return new int[] { 2,3,4,5,6 };
            case 6:
                return new int[] { 1,2,3,5,6,7 };
            case 7:
                return new int[] { 1,2,3,4,5,6,7 };
            case 8:
                return new int[] { 0,1,2,3,5,6,7,8 };
            case 9:
                return new int[] { 0,1,2,3,4,5,6,7,8,9 };
            default:
                throw new IllegalArgumentException("n must be between 1 and 9");
        }
    }
}
