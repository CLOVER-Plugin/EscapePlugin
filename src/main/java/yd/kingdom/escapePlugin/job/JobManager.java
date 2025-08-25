package yd.kingdom.escapePlugin.job;

import org.bukkit.entity.Player;

import java.util.*;

public class JobManager {
    private final Map<UUID, JobType> playerJobs = new HashMap<>();
    private final Set<UUID> admins = new HashSet<>();
    private final List<JobType> pool = Arrays.asList(
            JobType.WATERMELON, JobType.FLOWER, JobType.TURTLE,
            JobType.FISHING, JobType.WOOD, JobType.MINERAL
    );

    /** 직업별 도우미(Helper) 목록 */
    private final Map<JobType, Set<UUID>> helpers = new HashMap<>();

    /**
     * 특정 플레이어를 해당 직업 영역의 도우미로 등록
     */
    public void addHelper(UUID uuid, JobType job) {
        helpers.computeIfAbsent(job, k -> new HashSet<>()).add(uuid);
    }

    /**
     * 플레이어가 해당 직업 영역의 도우미인지 확인
     */
    public boolean isHelper(UUID uuid, JobType job) {
        return helpers.getOrDefault(job, Collections.emptySet()).contains(uuid);
    }
    
    /**
     * 특정 플레이어의 해당 직업 영역 도우미 권한 제거
     */
    public void removeHelper(UUID uuid, JobType job) {
        Set<UUID> jobHelpers = helpers.get(job);
        if (jobHelpers != null) {
            jobHelpers.remove(uuid);
        }
    }
    
    /**
     * FREEMAN을 모든 직업의 도우미로 설정
     */
    public void setFreemanAsHelper(UUID uuid) {
        for (JobType jobType : pool) {
            helpers.computeIfAbsent(jobType, k -> new HashSet<>()).add(uuid);
        }
    }
    
    /**
     * FREEMAN의 모든 도우미 권한 제거
     */
    public void removeFreemanHelper(UUID uuid) {
        for (JobType jobType : pool) {
            Set<UUID> jobHelpers = helpers.get(jobType);
            if (jobHelpers != null) {
                jobHelpers.remove(uuid);
            }
        }
    }

    // 게임 시작 시 랜덤 직업 배분
    public void assignJobs(List<Player> players) {
        List<JobType> jobs = new ArrayList<>(pool);
        Collections.shuffle(jobs);
        int idx = 0;
        for (Player p : players) {
            if (admins.contains(p.getUniqueId())) {
                playerJobs.put(p.getUniqueId(), JobType.ADMIN);
            } else {
                playerJobs.put(p.getUniqueId(), jobs.get(idx++ % jobs.size()));
            }
        }
    }

    public void addAdmin(UUID uuid) {
        admins.add(uuid);
        playerJobs.put(uuid, JobType.ADMIN);
    }
    public JobType getJob(UUID uuid) {
        return playerJobs.getOrDefault(uuid, JobType.FREEMAN);
    }
    public void setJob(UUID uuid, JobType job) {
        // 기존 직업이 FREEMAN이었다면 모든 도우미 권한 제거
        JobType oldJob = playerJobs.get(uuid);
        if (oldJob == JobType.FREEMAN) {
            removeFreemanHelper(uuid);
        }
        
        // 새 직업 설정
        playerJobs.put(uuid, job);
        
        // FREEMAN으로 설정하면 모든 직업의 도우미가 됨
        if (job == JobType.FREEMAN) {
            setFreemanAsHelper(uuid);
        }
    }
}