package de.shadowsmp.totem;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class InfinityTotem extends JavaPlugin implements CommandExecutor, Listener {
    private final MiniMessage mm = MiniMessage.miniMessage();
    private NamespacedKey totemKey;

    @Override
    public void onEnable() {
        totemKey = new NamespacedKey(this, "infinity_totem");
        getCommand("infinitytotem").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    public ItemStack createInfinityTotem() {
        ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = totem.getItemMeta();
        meta.displayName(mm.deserialize("<gradient:#9d4edd:#e0aaff><bold>INFINITY TOTEM</bold></gradient>"));
        meta.lore(List.of(
                mm.deserialize("<gray>Regeneriert sich nach dem"),
                mm.deserialize("<gray>Auslösen automatisch wieder."),
                mm.deserialize("<dark_purple>» SHADOW SMP")
        ));
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.getPersistentDataContainer().set(totemKey, PersistentDataType.BYTE, (byte) 1);
        totem.setItemMeta(meta);
        return totem;
    }

    private boolean isInfinityTotem(ItemStack item) {
        if (item == null || item.getType() != Material.TOTEM_OF_UNDYING || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(totemKey, PersistentDataType.BYTE);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player target;
        if (args.length >= 1) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(mm.deserialize("<red>Spieler nicht online!"));
                return true;
            }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sender.sendMessage(mm.deserialize("<red>Nutze: /infinitytotem <Spieler>"));
            return true;
        }

        target.getInventory().addItem(createInfinityTotem());
        target.sendMessage(mm.deserialize("<light_purple>Du hast ein <bold>INFINITY TOTEM</bold> <light_purple>erhalten!"));
        if (!target.equals(sender)) {
            sender.sendMessage(mm.deserialize("<light_purple>Infinity Totem an <white>" + target.getName() + " <light_purple>gegeben."));
        }
        return true;
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTotemPop(EntityResurrectEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;

        EquipmentSlot hand = e.getHand();
        if (hand == null) return;

        ItemStack usedItem = (hand == EquipmentSlot.HAND)
                ? p.getInventory().getItemInMainHand()
                : p.getInventory().getItemInOffHand();

        if (!isInfinityTotem(usedItem)) return;

        ItemStack restored = usedItem.clone();
        Bukkit.getScheduler().runTask(this, () -> {
            if (hand == EquipmentSlot.HAND) {
                p.getInventory().setItemInMainHand(restored);
            } else {
                p.getInventory().setItemInOffHand(restored);
            }
            p.sendActionBar(mm.deserialize("<light_purple><bold>INFINITY TOTEM</bold> <gray>wurde regeneriert!"));
        });
    }
}
