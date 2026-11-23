package com.allowance.menus;

import com.allowance.ModRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PackageEditMenu extends AbstractContainerMenu {
    public static final int CONTAINER_SLOTS = 27;

    // 与编辑界面共享的槽位布局
    public static final int GRID_X = 8;
    public static final int GRID_Y = 52;
    public static final int INV_X = 8;
    public static final int INV_Y = 122;
    public static final int HOTBAR_Y = 180;
    public static final int IMAGE_WIDTH = 176;
    public static final int IMAGE_HEIGHT = 204;

    public final SimpleContainer container = new SimpleContainer(CONTAINER_SLOTS);
    public int packageId;
    public String packageName;
    public int mode;
    public boolean enabled;

    /** 服务端构造：由 MenuProvider.createMenu 调用 */
    public PackageEditMenu(int windowId, Inventory playerInventory, int pkgId, String name, int mode, boolean enabled, List<ItemStack> items) {
        this(ModRegistry.PACKAGE_EDIT_MENU.get(), windowId, playerInventory, pkgId, name, mode, enabled);
        for (int i = 0; i < CONTAINER_SLOTS; i++) {
            this.container.setItem(i, i < items.size() ? items.get(i).copy() : ItemStack.EMPTY);
        }
    }

    /** 客户端构造：由 IContainerFactory 调用 */
    public static PackageEditMenu networkCreate(int windowId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        int id = buf.readInt();
        String name = buf.readUtf();
        int mode = buf.readVarInt();
        boolean enabled = buf.readBoolean();
        int count = buf.readVarInt();
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            items.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
        }
        return new PackageEditMenu(windowId, playerInventory, id, name, mode, enabled, items);
    }

    private PackageEditMenu(net.minecraft.world.inventory.MenuType<PackageEditMenu> type, int windowId, Inventory playerInventory, int pkgId, String name, int mode, boolean enabled) {
        super(type, windowId);
        this.packageId = pkgId;
        this.packageName = name;
        this.mode = mode;
        this.enabled = enabled;

        // 奖励物品栏 3x9
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(this.container, row * 9 + col, GRID_X + col * 18, GRID_Y + row * 18));
            }
        }
        // 玩家背包 3x9
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, 9 + row * 9 + col, INV_X + col * 18, INV_Y + row * 18));
            }
        }
        // 快捷栏
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, INV_X + col * 18, HOTBAR_Y));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        if (index < CONTAINER_SLOTS) {
            if (!this.moveItemStackTo(stack, CONTAINER_SLOTS, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.moveItemStackTo(stack, 0, CONTAINER_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return slot.hasItem() ? ItemStack.EMPTY : stack.copy();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    /** 关闭界面时，未保存的物品退回玩家背包（服务端） */
    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            for (int i = 0; i < this.container.getContainerSize(); i++) {
                ItemStack stack = this.container.removeItemNoUpdate(i);
                if (!stack.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(stack);
                }
            }
        }
    }

}
