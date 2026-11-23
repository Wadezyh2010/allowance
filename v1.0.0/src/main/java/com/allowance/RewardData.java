package com.allowance;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.item.ItemStack;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RewardData extends SavedData {
    public static final String DATA_NAME = "allowance_rewards";

    /** 领取状态：0 可领取 1 已领取 2 需先签到 3 已停用 */
    public static final int STATUS_CLAIMABLE = 0;
    public static final int STATUS_CLAIMED = 1;
    public static final int STATUS_NEED_CHECK_IN = 2;
    public static final int STATUS_DISABLED = 3;

    public static class PlayerEntry {
        public long lastCheckIn = 0L;
        public int streak = 0;
        public int total = 0;
        /** packageId -> 领取日 (ONCE 模式存 -1 表示永久) */
        public final Map<Integer, Long> claims = new HashMap<>();
    }

    private final List<RewardPackage> packages = new ArrayList<>();
    private int nextId = 1;
    private final Map<UUID, PlayerEntry> players = new HashMap<>();

    public static RewardData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(RewardData::new, RewardData::load, null), DATA_NAME);
    }

    public RewardData() {}

    public static RewardData load(CompoundTag tag, HolderLookup.Provider provider) {
        RewardData data = new RewardData();
        data.nextId = Math.max(1, tag.getInt("NextId"));
        ListTag pkgList = tag.getList("Packages", Tag.TAG_COMPOUND);
        for (int i = 0; i < pkgList.size(); i++) {
            CompoundTag t = pkgList.getCompound(i);
            ClaimMode mode;
            try {
                mode = ClaimMode.valueOf(t.getString("Mode"));
            } catch (IllegalArgumentException e) {
                mode = ClaimMode.FREE;
            }
            List<ItemStack> items = new ArrayList<>();
            ListTag itemTags = t.getList("Items", Tag.TAG_COMPOUND);
            for (int j = 0; j < itemTags.size(); j++) {
                items.add(ItemStack.parseOptional(provider, itemTags.getCompound(j)));
            }
            data.packages.add(new RewardPackage(t.getInt("Id"), t.getString("Name"), mode, t.getBoolean("Enabled"), items));
        }
        ListTag playerList = tag.getList("Players", Tag.TAG_COMPOUND);
        for (int i = 0; i < playerList.size(); i++) {
            CompoundTag t = playerList.getCompound(i);
            PlayerEntry e = new PlayerEntry();
            e.lastCheckIn = t.getLong("LastDay");
            e.streak = t.getInt("Streak");
            e.total = t.getInt("Total");
            ListTag claimTags = t.getList("Claims", Tag.TAG_COMPOUND);
            for (int j = 0; j < claimTags.size(); j++) {
                CompoundTag c = claimTags.getCompound(j);
                e.claims.put(c.getInt("Pkg"), c.getLong("Day"));
            }
            data.players.put(t.getUUID("Uuid"), e);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putInt("NextId", nextId);
        ListTag pkgList = new ListTag();
        for (RewardPackage p : packages) {
            CompoundTag t = new CompoundTag();
            t.putInt("Id", p.id());
            t.putString("Name", p.name());
            t.putString("Mode", p.mode().name());
            t.putBoolean("Enabled", p.enabled());
            ListTag itemTags = new ListTag();
            for (ItemStack stack : p.items()) {
                itemTags.add(stack.save(provider));
            }
            t.put("Items", itemTags);
            pkgList.add(t);
        }
        tag.put("Packages", pkgList);
        ListTag playerList = new ListTag();
        for (Map.Entry<UUID, PlayerEntry> entry : players.entrySet()) {
            CompoundTag t = new CompoundTag();
            t.putUUID("Uuid", entry.getKey());
            t.putLong("LastDay", entry.getValue().lastCheckIn);
            t.putInt("Streak", entry.getValue().streak);
            t.putInt("Total", entry.getValue().total);
            ListTag claimTags = new ListTag();
            for (Map.Entry<Integer, Long> c : entry.getValue().claims.entrySet()) {
                CompoundTag c2 = new CompoundTag();
                c2.putInt("Pkg", c.getKey());
                c2.putLong("Day", c.getValue());
                claimTags.add(c2);
            }
            t.put("Claims", claimTags);
            playerList.add(t);
        }
        tag.put("Players", playerList);
        return tag;
    }

    // ---------------- player operations ----------------

    public PlayerEntry entry(UUID uuid) {
        return players.computeIfAbsent(uuid, u -> new PlayerEntry());
    }

    public String checkIn(ServerPlayer player) {
        long today = LocalDate.now().toEpochDay();
        PlayerEntry e = entry(player.getUUID());
        if (e.lastCheckIn == today) {
            return ChatFormatting.YELLOW + "你今天已经签到过了，明天再来吧！";
        }
        e.streak = (e.lastCheckIn == today - 1) ? e.streak + 1 : 1;
        e.lastCheckIn = today;
        e.total++;
        setDirty();
        player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8f, 1.2f);
        return ChatFormatting.GREEN + "签到成功！" + ChatFormatting.GRAY
                + "连续签到 " + e.streak + " 天，累计签到 " + e.total + " 天。";
    }

    public int statusFor(PlayerEntry e, RewardPackage pkg, long today) {
        if (!pkg.enabled()) return STATUS_DISABLED;
        Long last = e.claims.get(pkg.id());
        return switch (pkg.mode()) {
            case FREE -> (last != null && last == today) ? STATUS_CLAIMED : STATUS_CLAIMABLE;
            case DAILY -> {
                if (last != null && last == today) yield STATUS_CLAIMED;
                if (e.lastCheckIn != today) yield STATUS_NEED_CHECK_IN;
                yield STATUS_CLAIMABLE;
            }
            case ONCE -> {
                if (last != null) yield STATUS_CLAIMED;
                if (e.lastCheckIn != today) yield STATUS_NEED_CHECK_IN;
                yield STATUS_CLAIMABLE;
            }
        };
    }

    public String claim(ServerPlayer player, int packageId) {
        long today = LocalDate.now().toEpochDay();
        PlayerEntry e = entry(player.getUUID());
        RewardPackage pkg = byId(packageId);
        if (pkg == null) return ChatFormatting.RED + "该奖励不存在。";
        if (!pkg.enabled()) return ChatFormatting.RED + "该奖励已停用。";
        int status = statusFor(e, pkg, today);
        switch (status) {
            case STATUS_CLAIMED -> {
                return ChatFormatting.YELLOW + "这个奖励你已经领取过了。";
            }
            case STATUS_NEED_CHECK_IN -> {
                return ChatFormatting.RED + "请先完成今日签到，再领取该奖励。";
            }
            case STATUS_DISABLED -> {
                return ChatFormatting.RED + "该奖励已停用。";
            }
            default -> {}
        }
        if (pkg.items().isEmpty()) return ChatFormatting.RED + "该奖励是空的，请联系管理员。";
        int totalCount = 0;
        for (ItemStack stack : pkg.items()) {
            if (stack.isEmpty()) continue;
            player.getInventory().placeItemBackInInventory(stack.copy());
            totalCount += stack.getCount();
        }
        e.claims.put(pkg.id(), pkg.mode() == ClaimMode.ONCE ? -1L : today);
        setDirty();
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6f, 1.0f);
        return ChatFormatting.GREEN + "领取成功！" + ChatFormatting.GRAY
                + "「" + pkg.name() + "」 共 " + totalCount + " 件物品已放入你的背包。";
    }

    // ---------------- OP operations ----------------

    public String updatePackage(int id, String name, int modeIndex, boolean enabled, List<ItemStack> items) {
        if (items.size() > 27) items = items.subList(0, 27);
        String safeName = (name == null || name.isBlank()) ? "未命名奖励包" : name.trim();
        if (safeName.length() > 24) safeName = safeName.substring(0, 24);
        ClaimMode mode = ClaimMode.byIndex(modeIndex);
        if (id < 0) {
            RewardPackage pkg = new RewardPackage(nextId++, safeName, mode, enabled, new ArrayList<>(items));
            packages.add(pkg);
            setDirty();
            return ChatFormatting.GREEN + "已创建奖励包：「" + safeName + "」";
        }
        RewardPackage existing = byId(id);
        if (existing == null) return ChatFormatting.RED + "目标奖励包不存在。";
        existing.set(safeName, mode, enabled, items);
        setDirty();
        return ChatFormatting.GREEN + "已保存奖励包：「" + safeName + "」";
    }

    public boolean deletePackage(int id) {
        boolean removed = packages.removeIf(p -> p.id() == id);
        if (removed) setDirty();
        return removed;
    }

    public boolean togglePackage(int id) {
        for (RewardPackage p : packages) {
            if (p.id() == id) {
                p.set(p.name(), p.mode(), !p.enabled(), p.items());
                setDirty();
                return p.enabled();
            }
        }
        return false;
    }

    public RewardPackage byId(int id) {
        for (RewardPackage p : packages) {
            if (p.id() == id) return p;
        }
        return null;
    }

    public List<RewardPackage> packages() {
        return packages;
    }
}
