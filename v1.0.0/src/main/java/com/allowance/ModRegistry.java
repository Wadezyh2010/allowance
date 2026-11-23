package com.allowance;

import com.allowance.menus.PackageEditMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModRegistry {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, AllowanceMod.MODID);

    public static final Supplier<MenuType<PackageEditMenu>> PACKAGE_EDIT_MENU = MENUS.register("package_edit",
            () -> IMenuTypeExtension.create(PackageEditMenu::networkCreate));

    private ModRegistry() {}
}
