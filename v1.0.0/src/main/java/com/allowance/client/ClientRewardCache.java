package com.allowance.client;

import com.allowance.network.SyncDataPayload;

import java.util.List;

/** 客户端数据缓存，由服务端同步刷新 */
public final class ClientRewardCache {
    public static volatile boolean checkedInToday = false;
    public static volatile int streak = 0;
    public static volatile int total = 0;
    public static volatile List<SyncDataPayload.PackageEntry> packages = List.of();

    private ClientRewardCache() {}

    public static void update(SyncDataPayload payload) {
        checkedInToday = payload.checkedInToday();
        streak = payload.streak();
        total = payload.total();
        packages = payload.packages();
    }
}
