package yd.kingdom.escapePlugin.item;

public enum GameItem {
    ENTRY("입장권"),
    ESCAPE("탈출권"),
    STEAL("강탈권");

    private final String display;
    GameItem(String display) { this.display = display; }
    public String getDisplay() { return display; }

    /**
     * 문자열(명칭 또는 enum 이름)로부터 GameItem 얻기
     */
    public static GameItem fromString(String s) {
        for (GameItem it : values()) {
            if (it.name().equalsIgnoreCase(s) || it.getDisplay().equals(s)) {
                return it;
            }
        }
        return null;
    }
}