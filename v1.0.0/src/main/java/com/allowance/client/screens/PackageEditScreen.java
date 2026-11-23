package com.allowance.client.screens;

import com.allowance.ClaimMode;
import com.allowance.menus.PackageEditMenu;
import com.allowance.network.SavePackagePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/** OP 奖励包编辑器：名称 / 领取方式 / 启用开关 / 物品槽位 */
public class PackageEditScreen extends AbstractContainerScreen<PackageEditMenu> {
    private EditBox nameBox;
    private Button modeButton;
    private Button enabledButton;

    public PackageEditScreen(PackageEditMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        this.imageWidth = PackageEditMenu.IMAGE_WIDTH;
        this.imageHeight = PackageEditMenu.IMAGE_HEIGHT;
        super.init();

        int left = this.leftPos;
        int top = this.topPos;

        nameBox = new EditBox(this.font, left + 8, top + 6, 100, 16, Component.literal("奖励包名称"));
        nameBox.setValue(menu.packageName == null ? "" : menu.packageName);
        nameBox.setMaxLength(24);
        nameBox.setBordered(true);
        this.addWidget(nameBox); // 不需要按键焦点被按钮抢走，用 addWidget 手动渲染

        modeButton = Button.builder(modeLabel(), b -> {
                    menu.mode = ClaimMode.byIndex(menu.mode).next().ordinal();
                    modeButton.setMessage(modeLabel());
                })
                .bounds(left + 112, top + 6, 56, 16).build();
        this.addRenderableWidget(modeButton);

        enabledButton = Button.builder(enabledLabel(), b -> {
                    menu.enabled = !menu.enabled;
                    enabledButton.setMessage(enabledLabel());
                })
                .bounds(left + 8, top + 28, 52, 16).build();
        this.addRenderableWidget(enabledButton);

        int newPackageId = menu.packageId;
        int newMode = menu.mode;
        boolean newEnabled = menu.enabled;
        this.addRenderableWidget(Button.builder(Component.literal("保 存"), b -> {
                    PacketDistributor.sendToServer(new SavePackagePayload(
                            newPackageId, nameBox.getValue(), newMode, newEnabled));
                    this.onClose();
                }).bounds(left + 64, top + 28, 52, 16).build());

        this.addRenderableWidget(Button.builder(Component.literal("取 消"), b -> this.onClose())
                .bounds(left + 120, top + 28, 48, 16).build());
    }

    private Component modeLabel() {
        return Component.literal("领取: " + ClaimMode.byIndex(menu.mode).display);
    }

    private Component enabledLabel() {
        return Component.literal(menu.enabled ? "已启用" : "已停用");
    }

    @Override
    protected void renderBg(GuiGraphics gg, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        gg.fill(left - 4, top - 4, left + imageWidth + 4, top + imageHeight + 4, 0xE0101A2A);
        gg.fill(left, top, left + imageWidth, top + imageHeight, 0xF5131C2C);
        gg.renderOutline(left, top, imageWidth, imageHeight, 0xFF3A4A60);
        gg.fill(left, top, left + imageWidth, top + 48, 0xFF1E2C3E);

        // 槽位底色
        for (net.minecraft.world.inventory.Slot slot : this.menu.slots) {
            gg.fill(left + slot.x - 1, top + slot.y - 1, left + slot.x + 17, top + slot.y + 17, 0xFF0B111C);
            gg.renderOutline(left + slot.x - 1, top + slot.y - 1, 18, 18, 0xFF2A3A50);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gg, int mouseX, int mouseY) {
        gg.drawString(this.font, "奖励物品（点击放入/取出）", 8, 40, 0xFF999999, false);
        gg.drawString(this.font, "你的背包", 8, PackageEditMenu.INV_Y - 10, 0xFF999999, false);
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        nameBox.render(gg, mouseX, mouseY, partialTick);
        super.render(gg, mouseX, mouseY, partialTick);
        this.renderTooltip(gg, mouseX, mouseY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
