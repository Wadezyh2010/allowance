package com.allowance;

public enum ClaimMode {
    FREE("每日限领一次"),
    DAILY("签到后每日限领"),
    ONCE("签到后限领一次");

    public final String display;

    ClaimMode(String display) {
        this.display = display;
    }

    public ClaimMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static ClaimMode byIndex(int index) {
        return values()[Math.floorMod(index, values().length)];
    }
}
