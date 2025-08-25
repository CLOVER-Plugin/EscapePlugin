package yd.kingdom.escapePlugin.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.job.JobType;

public class FishingListener implements Listener {

    @EventHandler
    public void onFish(PlayerFishEvent e) {
        // 실제로 물고기를 잡았을 때만
        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;

        Player player = e.getPlayer();
        JobType job = EscapePlugin.getInstance()
                .getJobManager()
                .getJob(player.getUniqueId());
        if (job != JobType.FISHING && job != JobType.FREEMAN) return;

        if (e.getCaught() instanceof Item caught) {
            // 기존 스택의 수량만 가져와서
            int amount = caught.getItemStack().getAmount();
            // 메타가 초기화된 새로운 Raw Cod 스택으로 교체
            ItemStack codStack = new ItemStack(Material.COD, amount);
            caught.setItemStack(codStack);
        }

        ConfigurationSection sec = EscapePlugin.getInstance()
                .getConfig()
                .getConfigurationSection("hidden." + job.name().toLowerCase());
        if (sec == null) return;

        double chance = sec.getDouble("chance");
        String owner = sec.getString("owner");

        if (Math.random() < chance) {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            OfflinePlayer op = Bukkit.getOfflinePlayer(owner);
            meta.setOwningPlayer(op);
            skull.setItemMeta(meta);

            // 낚싯바늘 위치에서 자연스럽게 플레이어를 향해 날아오도록
            Vector hookLoc = e.getHook().getLocation().toVector();
            Vector playerLoc = player.getLocation().toVector();
            Vector velocity = playerLoc
                    .subtract(hookLoc)
                    .normalize()
                    .multiply(0.3);

            Item droppedHead = player.getWorld().dropItem(e.getHook().getLocation(), skull);
            droppedHead.setVelocity(velocity);
        }
    }
}