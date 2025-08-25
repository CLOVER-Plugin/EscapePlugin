package yd.kingdom.escapePlugin.service;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.service.ProfileResolver;
import java.util.UUID;

public class SimpleSkinLoader {
    
    /**
     * Mojang API를 우선적으로 사용하여 플레이어 머리 생성
     */
    public static ItemStack createPlayerHead(String playerName) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        
        if (meta != null) {
            try {
                // 1. Mojang API 우선 시도
                String uuid = getPlayerUUIDFromMojang(playerName);
                if (uuid != null) {
                    UUID playerUUID = java.util.UUID.fromString(uuid); // ★ 필수
                    PlayerProfile profile = Bukkit.createProfile(playerUUID, playerName);

                    // 텍스처까지 확실히 채우기
                    try {
                        profile.update(); // 최신
                    } catch (NoSuchMethodError ignored) {
                        var m = profile.getClass().getMethod("complete", boolean.class);
                        m.invoke(profile, true);
                    }

                    meta.setOwnerProfile(profile);
                    skull.setItemMeta(meta);
                    // EscapePlugin.getInstance().getLogger().info("Mojang API로 스킨 로드 성공: " + playerName + " -> " + uuid);
                    return skull;
                }
            } catch (Exception e) {
                // EscapePlugin.getInstance().getLogger().warning("Mojang API 스킨 로드 실패: " + playerName + " - " + e.getMessage());
            }
            
            // 2. Profile API 시도
            try {
                PlayerProfile profile = ProfileResolver.resolveProfileBlocking(playerName);
                meta.setOwnerProfile(profile);
                skull.setItemMeta(meta);
                // EscapePlugin.getInstance().getLogger().info("Profile API로 스킨 로드 성공: " + playerName);
                return skull;
            } catch (Exception e) {
                // EscapePlugin.getInstance().getLogger().warning("Profile API 실패: " + playerName + " - " + e.getMessage());
            }
            
            // 3. OfflinePlayer 폴백
            try {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
                meta.setOwningPlayer(offlinePlayer);
                skull.setItemMeta(meta);
                // EscapePlugin.getInstance().getLogger().info("OfflinePlayer로 스킨 로드 성패: " + playerName);
                return skull;
            } catch (Exception e) {
                // EscapePlugin.getInstance().getLogger().warning("OfflinePlayer로 스킨 로드 실패: " + playerName + " - " + e.getMessage());
            }
            
            // 모든 방법 실패 시 기본 머리
            meta.setDisplayName("§7" + playerName + "의 머리");
            skull.setItemMeta(meta);
        }
        
        return skull;
    }
    
    /**
     * Mojang API를 사용하여 강제로 스킨 로드
     */
    public static ItemStack createPlayerHeadWithMojangAPI(String playerName) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        
        if (meta != null) {
            try {
                // Mojang API를 통해 UUID 가져오기
                String uuid = getPlayerUUIDFromMojang(playerName);
                if (uuid != null) {
                    // UUID로 OfflinePlayer 생성
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                    meta.setOwningPlayer(offlinePlayer);
                    skull.setItemMeta(meta);
                    
                    EscapePlugin.getInstance().getLogger().info("Mojang API로 스킨 로드 성공: " + playerName + " -> " + uuid);
                    return skull;
                }
            } catch (Exception e) {
                EscapePlugin.getInstance().getLogger().warning("Mojang API 스킨 로드 실패: " + playerName + " - " + e.getMessage());
            }
            
            // 실패 시 기본 머리
            meta.setDisplayName("§7" + playerName + "의 머리");
            skull.setItemMeta(meta);
        }
        
        return skull;
    }
    
    private static String getPlayerUUIDFromMojang(String playerName) {
        try {
            java.net.URL url = new java.net.URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            if (connection.getResponseCode() == 200) {
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(connection.getInputStream())
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                // JSON 파싱
                String json = response.toString();
                if (json.contains("\"id\":")) {
                    String uuid = json.split("\"id\":\"")[1].split("\"")[0];
                    return uuid.replaceAll("(.{8})(.{4})(.{4})(.{4})(.{12})", "$1-$2-$3-$4-$5");
                }
            }
        } catch (Exception e) {
            EscapePlugin.getInstance().getLogger().warning("Mojang API 호출 실패: " + playerName + " - " + e.getMessage());
        }
        return null;
    }
}
