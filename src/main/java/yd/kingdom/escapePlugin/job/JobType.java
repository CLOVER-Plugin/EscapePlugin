package yd.kingdom.escapePlugin.job;


public enum JobType {
    WATERMELON("농부"),
    FLOWER   ("정원사"),
    TURTLE   ("사육사"),
    FISHING  ("낚시꾼"),
    WOOD     ("나무꾼"),
    MINERAL  ("광부"),
    FREEMAN  ("자유민"),
    ADMIN    ("관리자");

    private final String displayName;
    JobType(String name) { this.displayName = name; }
    public String getDisplayName() { return displayName; }

    public static JobType fromDisplayName(String name) {
        for (JobType j : values())
            if (j.displayName.equals(name)) return j;
        return null;
    }
}