package com.allowance.client.screens;

import com.allowance.ClaimMode;
import com.allowance.RewardData;
import com.allowance.client.ClientRewardCache;
import com.allowance.network.CheckInPayload;
import com.allowance.network.ClaimPayload;
import com.allowance.network.SyncDataPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/** 玩家奖励中心：签到 + 领取奖励 */
public class PlayerRewardScreen extends Screen {
    private static final int WIDTH = 236;
    private static final int HEIGHT = 200;
    private static final int CARD_HEIGHT = 44;
    private static final int CARD_GAP = 4;
    private static final int VISIBLE_CARDS = 3;
    private static final int LIST_TOP = 50;
    private static final int ICON_AREA_RIGHT = 168;

    private int scroll = 0;
    /** 用于悬浮提示的图标区域 [x1, y1, stack] */
    private final List<Object[]> iconTooltips = new java.util.ArrayList<>();

    public PlayerRewardScreen() {
        super(Component.literal("会员奖励中心"));
    }

    @Override
    protected void init() {
        int left = (this.width - WIDTH) / 2;
        int top = (this.height - HEIGHT) / 2;

        List<SyncDataPayload.PackageEntry> packages = ClientRewardCache.packages;
        int maxScroll = Math.max(0, packages.size() - VISIBLE_CARDS);
        scroll = Mth.clamp(scroll, 0, maxScroll);

        Button checkInBtn = Button.builder(
                        Component.literal(ClientRewardCache.checkedInToday ? "已签到" : "签  到"),
                        b -> PacketDistributor.sendToServer(CheckInPayload.INSTANCE))
                .bounds(left + WIDTH - 64, top + 26, 56, 18)
                .build();
        checkInBtn.active = !ClientRewardCache.checkedInToday;
        this.addRenderableWidget(checkInBtn);

        for (int row = 0; row < VISIBLE_CARDS; row++) {
            int idx = scroll + row;
            if (idx >= packages.size()) break;
            SyncDataPayload.PackageEntry pkg = packages.get(idx);
            int cardY = top + LIST_TOP + row * (CARD_HEIGHT + CARD_GAP);

            String label = switch (pkg.status()) {
                case RewardData.STATUS_CLAIMED -> "已领取";
                case RewardData.STATUS_NEED_CHECK_IN -> "需签到";
                case RewardData.STATUS_DISABLED -> "已停用";
                default -> "领  取";
            };
            int packageId = pkg.id();
            Button claimBtn = Button.builder(Component.literal(label), b ->
                            PacketDistributor.sendToServer(new ClaimPayload(packageId)))
                    .bounds(left + WIDTH - 64, cardY + 13, 56, 18)
                    .build();
            claimBtn.active = pkg.status() == RewardData.STATUS_CLAIMABLE;
            this.addRenderableWidget(claimBtn);
        }
    }

    public void refreshData() {
        this.rebuildWidgets();
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gg, mouseX, mouseY, partialTick);
        int left = (this.width - WIDTH) / 2;
        int top = (this.height - HEIGHT) / 2;

        // 主面板
        gg.fill(left, top, left + WIDTH, top + HEIGHT, 0xF5101A2A);
        gg.renderOutline(left, top, WIDTH, HEIGHT, 0xFF3A4A60);
        gg.fill(left, top, left + WIDTH, top + 22, 0xFF1E2C3E);
        gg.drawCenteredString(this.font, "会 员 奖 励 中 心", left + WIDTH / 2, top + 7, 0xFFFFFF);

        // 签到信息
        String status = ClientRewardCache.checkedInToday ? "§a✔ 今日已签到" : "§e○ 今日未签到";
        gg.drawString(this.font, status, left + 8, top + 26, -1);
        gg.drawString(this.font,
                "§7连续签到 §f" + ClientRewardCache.streak + " §7天  累计 §f" + ClientRewardCache.total + " §7天",
                left + 8, top + 38, 0xFFAAAAAA);

        // 奖励卡片
        iconTooltips.clear();
        List<SyncDataPayload.PackageEntry> packages = ClientRewardCache.packages;
        for (int row = 0; row < VISIBLE_CARDS; row++) {
            int idx = scroll + row;
            if (idx >= packages.size()) break;
            SyncDataPayload.PackageEntry pkg = packages.get(idx);
            int cardY = top + LIST_TOP + row * (CARD_HEIGHT + CARD_GAP);
            gg.fill(left + 4, cardY, left + WIDTH - 4, cardY + CARD_HEIGHT, 0xFF182338);
            gg.renderOutline(left + 4, cardY, WIDTH - 8, CARD_HEIGHT, 0xFF2A3A50);

            gg.drawString(this.font, "§f" + pkg.name(), left + 10, cardY + 4, -1);
            gg.drawString(this.font,
                    "§8" + ClaimMode.byIndex(pkg.mode()).display + "  §7共 §f" + pkg.items().size() + " §7种物品",
                    left + 10, cardY + 16, -1);

            // 物品图标预览（最多 8 个）
            int shown = Math.min(8, pkg.items().size());
            for (int k = 0; k < shown; k++) {
                ItemStack stack = pkg.items().get(k);
                int ix = left + 10 + k * 18;
                int iy = cardY + 26;
                if (ix > left + ICON_AREA_RIGHT) {
                    gg.drawString(this.font, "§7+", ix, iy + 4, -1);
                    break;
                }
                gg.renderItem(stack, ix, iy);
                iconTooltips.add(new Object[]{ix, iy, stack});
            }
        }

        // 空列表提示
        if (packages.isEmpty()) {
            gg.drawCenteredString(this.font, "§7暂时没有可领取的奖励，敬请期待", left + WIDTH / 2, top + LIST_TOP + 50, -1);
        }

        // 滚动条
        if (packages.size() > VISIBLE_CARDS) {
            int barX = left + WIDTH - 3;
            int barH = HEIGHT - LIST_TOP - 8;
            int thumbH = Math.max(10, barH * VISIBLE_CARDS / packages.size());
            int thumbY = top + LIST_TOP + 4
                    + (barH - thumbH) * scroll / (packages.size() - VISIBLE_CARDS);
            gg.fill(barX, top + LIST_TOP + 2, barX + 2, top + HEIGHT - 6, 0xFF22304A);
            gg.fill(barX, thumbY, barX + 2, thumbY + thumbH, 0xFF6FA8DC);
        }

        super.render(gg, mouseX, mouseY, partialTick);

        // 图标悬浮提示
        for (Object[] tip : iconTooltips) {
            int ix = (int) tip[0];
            int iy = (int) tip[1];
            if (mouseX >= ix && mouseX < ix + 16 && mouseY >= iy && mouseY < iy + 16) {
                gg.renderTooltip(this.font, (ItemStack) tip[2], mouseX, mouseY);
                break;
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int size = ClientRewardCache.packages.size();
        if (size > VISIBLE_CARDS) {
            int maxScroll = size - VISIBLE_CARDS;
            int delta = (int) Math.signum(scrollY);
            int newScroll = Mth.clamp(scroll + delta, 0, maxScroll);
            if (newScroll != scroll) {
                scroll = newScroll;
                this.rebuildWidgets();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
