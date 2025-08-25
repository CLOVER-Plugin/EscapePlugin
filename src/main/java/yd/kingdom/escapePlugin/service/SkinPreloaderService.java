package yd.kingdom.escapePlugin.service;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import yd.kingdom.escapePlugin.EscapePlugin;
import yd.kingdom.escapePlugin.job.JobType;
import yd.kingdom.escapePlugin.service.ProfileResolver;

import java.util.HashSet;
import java.util.Set;

public class SkinPreloaderService {
    
    /**
     * 플러그인 시작 시 모든 직업 플레이어의 스킨을 미리 로드
     */
    public static void preloadAllSkins() {
        Set<String> playerNames = new HashSet<>();
        
        // config에서 모든 직업의 owner 정보를 가져와서 스킨 로드
        for (JobType jobType : JobType.values()) {
            if (jobType == JobType.FREEMAN || jobType == JobType.ADMIN) continue;
            
            String owner = getHeadOwner(jobType);
            if (owner != null && !owner.isEmpty()) {
                playerNames.add(owner);
            }
        }
        
        // 각 플레이어의 스킨을 비동기로 로드
        for (String playerName : playerNames) {
            preloadPlayerSkin(playerName);
        }
        
        EscapePlugin.getInstance().getLogger().info("총 " + playerNames.size() + "명의 플레이어 스킨을 미리 로드합니다.");
    }
    
    private static void preloadPlayerSkin(String playerName) {
        Bukkit.getScheduler().runTaskAsynchronously(EscapePlugin.getInstance(), () -> {
            try {
                // 1. 먼저 Profile API로 시도
                try {
                    var profile = Bukkit.createProfile(playerName);
                    if (profile != null) {
                        boolean ok = ProfileResolver.completeProfile(profile, playerName);
                        if (ok) {
                            EscapePlugin.getInstance().getLogger().info("Profile API로 " + playerName + " 텍스처까지 로드됨");
                        } else {
                            EscapePlugin.getInstance().getLogger().info("Profile API 시도 -> 텍스처 미확인/미완성: " + playerName);
                        }
                        return;
                    }
                } catch (Exception e) {
                    EscapePlugin.getInstance().getLogger().warning("Profile API 실패: " + playerName + " - " + e.getMessage());
                }
                
                // 2. Mojang API로 UUID 가져오기 시도
                try {
                    String uuid = getPlayerUUIDFromMojang(playerName);
                    if (uuid != null) {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                        EscapePlugin.getInstance().getLogger().info("Mojang API로 " + playerName + "의 스킨을 로드했습니다. (UUID: " + uuid + ")");
                        return;
                    }
                } catch (Exception e) {
                    EscapePlugin.getInstance().getLogger().warning("Mojang API 실패: " + playerName + " - " + e.getMessage());
                }
                
                // 3. 기본 OfflinePlayer 방식 시도
                try {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
                    if (offlinePlayer.hasPlayedBefore()) {
                        EscapePlugin.getInstance().getLogger().info("OfflinePlayer로 " + playerName + "의 스킨을 로드했습니다.");
                    } else {
                        EscapePlugin.getInstance().getLogger().warning("플레이어 " + playerName + "의 스킨을 로드할 수 없습니다. (hasPlayedBefore: false)");
                    }
                } catch (Exception e) {
                    EscapePlugin.getInstance().getLogger().warning("OfflinePlayer 방식 실패: " + playerName + " - " + e.getMessage());
                }
                
            } catch (Exception e) {
                EscapePlugin.getInstance().getLogger().warning("플레이어 " + playerName + "의 스킨 로드 중 예외 발생: " + e.getMessage());
            }
        });
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
            // 오류 무시
        }
        return null;
    }
    
    private static String getHeadOwner(JobType jobType) {
        // config에서 해당 직업의 owner 정보를 가져오기
        String configKey = jobType.name().toLowerCase();
        return EscapePlugin.getInstance().getConfig()
                .getString("hidden." + configKey + ".owner", "");
    }
}
