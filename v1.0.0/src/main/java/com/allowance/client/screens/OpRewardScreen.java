package com.allowance.client.screens;

import com.allowance.ClaimMode;
import com.allowance.client.ClientRewardCache;
import com.allowance.network.DeletePackagePayload;
import com.allowance.network.OpenEditorPayload;
import com.allowance.network.SyncDataPayload;
import com.allowance.network.TogglePackagePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/** OP 管理面板：奖励包列表 + 新建/编辑/删除/启停 */
public class OpRewardScreen extends Screen {
    private static final int WIDTH = 236;
    private static final int HEIGHT = 200;
    private static final int ROW_HEIGHT = 30;
    private static final int VISIBLE_ROWS = 4;
    private static final int LIST_TOP = 26;

    private int scroll = 0;
    private int pendingDeleteId = -1;

    public OpRewardScreen() {
        super(Component.literal("奖励管理面板"));
    }

    @Override
    protected void init() {
        int left = (this.width - WIDTH) / 2;
        int top = (this.height - HEIGHT) / 2;

        List<SyncDataPayload.PackageEntry> packages = ClientRewardCache.packages;
        int maxScroll = Math.max(0, packages.size() - VISIBLE_ROWS);
        scroll = Mth.clamp(scroll, 0, maxScroll);

        // 新建按钮
        this.addRenderableWidget(Button.builder(Component.literal("＋ 新建奖励包"), b ->
                        PacketDistributor.sendToServer(new OpenEditorPayload(-1)))
                .bounds(left + 8, top + HEIGHT - 24, 90, 18).build());

        // 关闭按钮
        this.addRenderableWidget(Button.builder(Component.literal("关 闭"), b -> onClose())
                .bounds(left + WIDTH - 60, top + HEIGHT - 24, 52, 18).build());

        // 每行按钮：编辑 / 停用启用 / 删除
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int idx = scroll + row;
            if (idx >= packages.size()) break;
            SyncDataPayload.PackageEntry pkg = packages.get(idx);
            int rowY = top + LIST_TOP + row * ROW_HEIGHT;
            int id = pkg.id();

            this.addRenderableWidget(Button.builder(Component.literal("编辑"), b ->
                            PacketDistributor.sendToServer(new OpenEditorPayload(id)))
                    .bounds(left + WIDTH - 108, rowY + 6, 30, 16).build());

            this.addRenderableWidget(Button.builder(
                            Component.literal(pkg.enabled() ? "停用" : "启用"), b ->
                            PacketDistributor.sendToServer(new TogglePackagePayload(id)))
                    .bounds(left + WIDTH - 76, rowY + 6, 30, 16).build());

            String deleteLabel = pendingDeleteId == id ? "确认?" : "删除";
            this.addRenderableWidget(Button.builder(Component.literal(deleteLabel), b -> {
                        if (pendingDeleteId == id) {
                            PacketDistributor.sendToServer(new DeletePackagePayload(id));
                            pendingDeleteId = -1;
                        } else {
                            pendingDeleteId = id;
                            rebuildWidgets();
                        }
                    }).bounds(left + WIDTH - 44, rowY + 6, 30, 16).build());
        }
    }

    public void refreshData() {
        pendingDeleteId = -1;
        this.rebuildWidgets();
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gg, mouseX, mouseY, partialTick);
        int left = (this.width - WIDTH) / 2;
        int top = (this.height - HEIGHT) / 2;

        gg.fill(left, top, left + WIDTH, top + HEIGHT, 0xF5101A2A);
        gg.renderOutline(left, top, WIDTH, HEIGHT, 0xFF3A4A60);
        gg.fill(left, top, left + WIDTH, top + 20, 0xFF1E2C3E);
        gg.drawCenteredString(this.font, "奖 励 管 理 面 板", left + WIDTH / 2, top + 6, 0xFFFFFF);

        List<SyncDataPayload.PackageEntry> packages = ClientRewardCache.packages;
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int idx = scroll + row;
            if (idx >= packages.size()) break;
            SyncDataPayload.PackageEntry pkg = packages.get(idx);
            int rowY = top + LIST_TOP + row * ROW_HEIGHT;

            gg.fill(left + 4, rowY, left + WIDTH - 4, rowY + ROW_HEIGHT - 2, 0xFF182338);
            gg.renderOutline(left + 4, rowY, WIDTH - 8, ROW_HEIGHT - 2, 0xFF2A3A50);

            if (!pkg.items().isEmpty()) {
                gg.renderItem(pkg.items().get(0), left + 10, rowY + 6);
            }
            String name = pkg.name();
            if (name.length() > 10) name = name.substring(0, 10) + "…";
            gg.drawString(this.font, "§f" + name + (pkg.enabled() ? "" : " §8(已停用)"), left + 32, rowY + 4, -1);
            gg.drawString(this.font,
                    "§8" + ClaimMode.byIndex(pkg.mode()).display + "  §7" + pkg.items().size() + " 种物品",
                    left + 32, rowY + 15, -1);
        }

        if (packages.isEmpty()) {
            gg.drawCenteredString(this.font, "§7还没有奖励包，点击左下角「新建奖励包」开始", left + WIDTH / 2, top + LIST_TOP + 40, -1);
        }

        // 滚动条
        if (packages.size() > VISIBLE_ROWS) {
            int barX = left + WIDTH - 3;
            int barH = HEIGHT - LIST_TOP - 32;
            int thumbH = Math.max(10, barH * VISIBLE_ROWS / packages.size());
            int thumbY = top + LIST_TOP + 2
                    + (barH - thumbH) * scroll / (packages.size() - VISIBLE_ROWS);
            gg.fill(barX, top + LIST_TOP + 2, barX + 2, top + HEIGHT - 30, 0xFF22304A);
            gg.fill(barX, thumbY, barX + 2, thumbY + thumbH, 0xFF6FA8DC);
        }

        gg.drawString(this.font, "§8/allowance 打开 · /allowance admin 管理", left + 8, top + HEIGHT - 34, -1);

        super.render(gg, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int size = ClientRewardCache.packages.size();
        if (size > VISIBLE_ROWS) {
            int maxScroll = size - VISIBLE_ROWS;
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
