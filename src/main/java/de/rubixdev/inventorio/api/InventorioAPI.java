package de.rubixdev.inventorio.api;

import com.google.common.collect.ImmutableList;
import de.rubixdev.inventorio.client.ui.InventorioScreen;
import de.rubixdev.inventorio.config.GlobalSettings;
import de.rubixdev.inventorio.player.InventorioScreenHandler;
import de.rubixdev.inventorio.player.PlayerInventoryAddon;
import de.rubixdev.inventorio.util.ToolBeltMode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public final class InventorioAPI {
    public static final String SLOT_PICKAXE = "pickaxe";
    public static final String SLOT_SWORD = "sword";
    public static final String SLOT_AXE = "axe";
    public static final String SLOT_SHOVEL = "shovel";
    public static final String SLOT_HOE = "hoe";

    static {
        if (GlobalSettings.toolBeltMode.getValue() == ToolBeltMode.ENABLED) {
            InventorioAPI
                .registerToolBeltSlotIfNotExists(
                    InventorioAPI.SLOT_PICKAXE,
                    Identifier.of("inventorio", "textures/gui/empty/pickaxe.png")
                )
                .addAllowingCondition((itemStack, addon) -> itemStack.getItem() instanceof PickaxeItem)
                .addAllowingTag(Identifier.of("inventorio", "pickaxes"))
                .addDenyingTag(Identifier.of("inventorio", "pickaxes_blacklist"));

            InventorioAPI
                .registerToolBeltSlotIfNotExists(
                    InventorioAPI.SLOT_SWORD,
                    Identifier.of("inventorio", "textures/gui/empty/sword.png")
                )
                .addAllowingCondition(
                    ((itemStack, addon) -> itemStack.getItem() instanceof SwordItem
                        || itemStack.getItem() instanceof TridentItem)
                )
                .addAllowingTag(Identifier.of("inventorio", "swords"))
                .addDenyingTag(Identifier.of("inventorio", "swords_blacklist"));

            InventorioAPI
                .registerToolBeltSlotIfNotExists(
                    InventorioAPI.SLOT_AXE,
                    Identifier.of("inventorio", "textures/gui/empty/axe.png")
                )
                .addAllowingCondition(((itemStack, addon) -> itemStack.getItem() instanceof AxeItem))
                .addAllowingTag(Identifier.of("inventorio", "axes"))
                .addDenyingTag(Identifier.of("inventorio", "axes_blacklist"));

            InventorioAPI
                .registerToolBeltSlotIfNotExists(
                    InventorioAPI.SLOT_SHOVEL,
                    Identifier.of("inventorio", "textures/gui/empty/shovel.png")
                )
                .addAllowingCondition(((itemStack, addon) -> itemStack.getItem() instanceof ShovelItem))
                .addAllowingTag(Identifier.of("inventorio", "shovels"))
                .addDenyingTag(Identifier.of("inventorio", "shovels_blacklist"));

            InventorioAPI
                .registerToolBeltSlotIfNotExists(
                    InventorioAPI.SLOT_HOE,
                    Identifier.of("inventorio", "textures/gui/empty/hoe.png")
                )
                .addAllowingCondition(
                    ((itemStack, addon) -> itemStack.getItem() instanceof HoeItem
                        || itemStack.getItem() instanceof ShearsItem)
                )
                .addAllowingTag(Identifier.of("inventorio", "hoes"))
                .addDenyingTag(Identifier.of("inventorio", "hoes_blacklist"));
        }
    }

    private InventorioAPI() {}

    /**
     * Items within vanilla Player Inventory can tick
     * ({@link ItemStack#inventoryTick}).<br>
     * Inventorio will by default call that method with a slot of -2 for added
     * inventory slots. If you require to know which exact slot of the addon the
     * stack is in, you can register a custom tick handler here, which will be
     * called additionally.<br>
     * Note: each tick handler gets ran within its own try-catch block.<br>
     */
    public static void registerInventoryTickHandler(
        @NotNull Identifier customIdentifier,
        @NotNull InventorioTickHandler tickHandler
    ) {
        PlayerInventoryAddon.registerTickHandler(customIdentifier, tickHandler);
    }

    /** Note: each consumer get ran within its own try-catch block */
    public static void registerScreenHandlerOpenConsumer(
        @NotNull Identifier customIdentifier,
        Consumer<InventorioScreenHandler> screenHandlerConsumer
    ) {
        InventorioScreenHandler.registerOpenConsumer(customIdentifier, screenHandlerConsumer);
    }

    /** Note: each consumer get ran within its own try-catch block */
    @Environment(EnvType.CLIENT)
    public static void registerInventoryUIInitConsumer(
        @NotNull Identifier customIdentifier,
        Consumer<InventorioScreen> uiConsumer
    ) {
        InventorioScreen.registerInitConsumer(customIdentifier, uiConsumer);
    }

    /**
     * @param slotName  Unique string id for the slot. Default slots available
     *                  at {@link InventorioAPI} constants.
     * @param emptyIcon Identifier/ResourceLocation that leads to an icon, e.g.
     *                  <code>Identifier.of("your_mod", "textures/gui/empty/your_tool_slot.png")</code>
     * @return If <code>slotName</code> has already been taken, returns the
     *         existing {@link ToolBeltSlotTemplate}.<br>
     *         Creates a new one and returns it otherwise.<br>
     *         Returns NULL if {@link GlobalSettings#toolBeltMode} is set to
     *         <code>DISABLED</code>
     * @throws IllegalStateException when attempted to add a new Template post
     *                               initialization.<br>
     *                               No new ToolBelt slots can be added after
     *                               the first player has been spawned.
     */
    @Nullable public static ToolBeltSlotTemplate registerToolBeltSlotIfNotExists(
        @NotNull String slotName,
        @NotNull Identifier emptyIcon
    ) {
        return PlayerInventoryAddon
            .registerToolBeltTemplateIfNotExists(slotName, new ToolBeltSlotTemplate(slotName, emptyIcon));
    }

    /**
     * Please consider the existence of {@link GlobalSettings#toolBeltMode} when
     * calling this method.<br>
     * When set to anything but <code>ENABLED</code>, the standard tool belt
     * slots might be missing.
     * 
     * @param slotName Unique string id for the slot. Default slots available at
     *                 {@link InventorioAPI} constants.
     */
    @Nullable public static ToolBeltSlotTemplate getToolBeltSlotTemplate(@NotNull String slotName) {
        return PlayerInventoryAddon.getToolBeltTemplate(slotName);
    }

    /**
     * Please consider the existence of {@link GlobalSettings#toolBeltMode} when
     * calling this method.<br>
     * When set to anything but <code>ENABLED</code>, the standard tool belt
     * slots might be missing.
     * 
     * @return ItemStack.Empty if no fitting slot was found
     */
    @NotNull public static ItemStack findFittingToolBeltStack(
        @NotNull PlayerInventoryAddon playerInventoryAddon,
        @NotNull ItemStack sampleStack
    ) {
        return playerInventoryAddon.findFittingToolBeltStack(sampleStack);
    }

    /**
     * Please consider the existence of {@link GlobalSettings#toolBeltMode} when
     * calling this method.<br>
     * When set to anything but <code>ENABLED</code>, the standard tool belt
     * slots might be missing.
     * 
     * @return -1 if no fitting slot was found
     */
    public static int findFittingToolBeltIndex(
        @NotNull PlayerInventoryAddon playerInventoryAddon,
        @NotNull ItemStack sampleStack
    ) {
        return playerInventoryAddon.findFittingToolBeltIndex(sampleStack);
    }

    /**
     * @see InventorioAPI#registerToolBeltSlotIfNotExists
     */
    @NotNull public static ImmutableList<ToolBeltSlotTemplate> getToolBeltTemplates() {
        return PlayerInventoryAddon.getToolBeltTemplates();
    }

    @Nullable public static PlayerInventoryAddon getInventoryAddon(@NotNull PlayerEntity playerEntity) {
        return PlayerInventoryAddon.getInventoryAddon(playerEntity);
    }
}
