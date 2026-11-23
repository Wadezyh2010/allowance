package com.allowance;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RewardPackage {
    private final int id;
    private String name;
    private ClaimMode mode;
    private boolean enabled;
    private final List<ItemStack> items = new ArrayList<>();

    public RewardPackage(int id, String name, ClaimMode mode, boolean enabled, List<ItemStack> items) {
        this.id = id;
        set(name, mode, enabled, items);
    }

    public void set(String name, ClaimMode mode, boolean enabled, List<ItemStack> items) {
        this.name = name;
        this.mode = mode;
        this.enabled = enabled;
        this.items.clear();
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) this.items.add(stack);
        }
    }

    public int id() { return id; }
    public String name() { return name; }
    public ClaimMode mode() { return mode; }
    public boolean enabled() { return enabled; }
    public List<ItemStack> items() { return Collections.unmodifiableList(items); }
}
