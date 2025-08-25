package yd.kingdom.escapePlugin.service;

import org.bukkit.Bukkit;
import org.bukkit.profile.PlayerProfile;
import yd.kingdom.escapePlugin.EscapePlugin;

import java.lang.reflect.Method;
import java.util.UUID;

public class ProfileResolver {
    
    /**
     * 플레이어 이름으로 Profile을 생성하고 완성(텍스처까지 채움)
     */
    public static PlayerProfile resolveProfileBlocking(String name) {
        PlayerProfile profile = Bukkit.createProfile(name);
        try {
            // 1) 최신 Paper: update() 존재
            //    (버전에 따라 complete(true) 인 경우도 있습니다. 둘 다 시도)
            try {
                profile.update(); // 최신
            } catch (NoSuchMethodError ignored) {
                // 구버전 Paper/Bukkit
                var m = profile.getClass().getMethod("complete", boolean.class);
                m.invoke(profile, true); // textures까지 채움
            }
        } catch (Throwable t) {
            EscapePlugin.getInstance().getLogger().warning("Profile 채우기 실패: " + name + " - " + t.getMessage());
        }
        return profile;
    }
    
    /**
     * UUID와 이름으로 Profile을 생성하고 완성(텍스처까지 채움)
     */
    public static PlayerProfile resolveProfileBlocking(UUID uuid, String name) {
        PlayerProfile profile = Bukkit.createProfile(uuid, name);
        try {
            // 1) 최신 Paper: update() 존재
            try {
                profile.update(); // 최신
            } catch (NoSuchMethodError ignored) {
                // 구버전 Paper/Bukkit
                var m = profile.getClass().getMethod("complete", boolean.class);
                m.invoke(profile, true); // textures까지 채움
            }
        } catch (Throwable t) {
            EscapePlugin.getInstance().getLogger().warning("Profile 채우기 실패: " + name + " - " + t.getMessage());
        }
        return profile;
    }
    
    /**
     * Profile이 텍스처를 가지고 있는지 확인
     */
    public static boolean hasTextures(PlayerProfile profile) {
        try {
            var m = profile.getClass().getMethod("hasTextures");
            return (boolean) m.invoke(profile);
        } catch (NoSuchMethodError | NoSuchMethodException e) {
            return true; // 확인 불가 시 성공 처리
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Profile 완성 및 텍스처 확인
     */
    public static boolean completeProfile(PlayerProfile profile, String playerName) {
        try {
            try {
                profile.update();
            } catch (NoSuchMethodError ignored) {
                var m = profile.getClass().getMethod("complete", boolean.class);
                m.invoke(profile, true);
            }
            
            // 텍스처가 있는지까지 확인
            boolean hasTextures = hasTextures(profile);
            
            EscapePlugin.getInstance().getLogger().info(
                hasTextures ? "Profile API로 " + playerName + " 텍스처까지 로드됨"
                           : "Profile API 시도 -> 텍스처 미확인/미완성: " + playerName
            );
            
            return hasTextures;
        } catch (Throwable t) {
            EscapePlugin.getInstance().getLogger().warning("Profile 완성 실패: " + playerName + " - " + t.getMessage());
            return false;
        }
    }
}
