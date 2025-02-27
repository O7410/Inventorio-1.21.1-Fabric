package de.rubixdev.inventorio.client.ui

import com.mojang.blaze3d.systems.RenderSystem
import de.rubixdev.inventorio.config.PlayerSettings
import de.rubixdev.inventorio.player.PlayerInventoryAddon
import de.rubixdev.inventorio.util.*
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.DiffuseLighting
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Arm
import net.minecraft.util.Identifier
import net.minecraft.util.crash.CrashException
import net.minecraft.util.crash.CrashReport
import net.minecraft.util.math.MathHelper
import net.minecraft.world.GameMode
import org.joml.Matrix4f

@Environment(EnvType.CLIENT)
object HotbarHUDRenderer {
    private val WIDGETS_TEXTURE = Identifier.of("inventorio", "textures/gui/widgets.png")
    private val WIDGETS_TEXTURE_DARK = Identifier.of("inventorio", "textures/gui/widgets_dark.png")
    private val client = MinecraftClient.getInstance()

    fun renderSegmentedHotbar(drawContext: DrawContext): Boolean {
        if (PlayerSettings.segmentedHotbar.value == SegmentedHotbar.OFF
            || PlayerSettings.segmentedHotbar.value == SegmentedHotbar.ONLY_FUNCTION
            || isHidden()
        ) {
            return false
        }

        val playerEntity = client.cameraEntity as? PlayerEntity ?: return false
        val inventory = playerEntity.inventory
        val scaledWidthHalved = client.window.scaledWidth / 2 - 30
        val scaledHeight = client.window.scaledHeight
        val selectedSection = PlayerInventoryAddon.Client.selectedHotbarSection

        val texture = if (PlayerSettings.darkTheme.boolValue) {
            WIDGETS_TEXTURE_DARK
        } else {
            WIDGETS_TEXTURE
        }

        // Draw the hotbar itself
        drawContext.drawTexture(
            texture,
            scaledWidthHalved - HUD_SEGMENTED_HOTBAR.x,
            scaledHeight - HUD_SEGMENTED_HOTBAR.y,
            CANVAS_SEGMENTED_HOTBAR.x,
            CANVAS_SEGMENTED_HOTBAR.y,
            HUD_SEGMENTED_HOTBAR.width,
            HUD_SEGMENTED_HOTBAR.height,
            CANVAS_WIDGETS_TEXTURE_SIZE.x, CANVAS_WIDGETS_TEXTURE_SIZE.y,
        )

        if (selectedSection == -1) {
            // Draw the regular vanilla selection box
            drawContext.drawTexture(
                texture,
                scaledWidthHalved - HUD_SECTION_SELECTION.x - HUD_SEGMENTED_HOTBAR_GAP +
                    (inventory.selectedSlot * SLOT_HOTBAR_SIZE.width) + (HUD_SEGMENTED_HOTBAR_GAP * (inventory.selectedSlot / 3)),
                scaledHeight - HUD_SECTION_SELECTION.y,
                CANVAS_VANILLA_SELECTION_FRAME_POS.x,
                CANVAS_VANILLA_SELECTION_FRAME_POS.y,
                CANVAS_VANILLA_SELECTION_FRAME_SIZE.width,
                CANVAS_VANILLA_SELECTION_FRAME_SIZE.height,
                CANVAS_WIDGETS_TEXTURE_SIZE.x, CANVAS_WIDGETS_TEXTURE_SIZE.y,
            )
        } else {
            // Draw the section-wide selection box
            drawContext.drawTexture(
                texture,
                scaledWidthHalved - HUD_SECTION_SELECTION.x + (HUD_SECTION_SELECTION.width * selectedSection) - HUD_SEGMENTED_HOTBAR_GAP,
                scaledHeight - HUD_SECTION_SELECTION.y,
                CANVAS_SECTION_SELECTION_FRAME.x,
                CANVAS_SECTION_SELECTION_FRAME.y,
                HUD_SECTION_SELECTION.width,
                HUD_SECTION_SELECTION.height,
                CANVAS_WIDGETS_TEXTURE_SIZE.x, CANVAS_WIDGETS_TEXTURE_SIZE.y,
            )
        }

        // Draw hotbar items
        for (slotNum in 0 until VANILLA_ROW_LENGTH) {
            val x = scaledWidthHalved - HUD_SECTION_SELECTION.x + (slotNum * SLOT_HOTBAR_SIZE.width) + (HUD_SEGMENTED_HOTBAR_GAP * (slotNum / 3))
            val y = scaledHeight - SLOT_HOTBAR_SIZE.height
            val itemStack = inventory.getStack(slotNum)

            drawContext.drawItem(itemStack, x, y)
            drawContext.drawItemInSlot(client.textRenderer, itemStack, x, y)
        }
        return true
    }

    private fun isHidden(): Boolean {
        if (client.interactionManager == null
            || client.interactionManager?.currentGameMode == GameMode.SPECTATOR
            || client.options.hudHidden
        ) {
            return true
        }

        val player = client.cameraEntity as? PlayerEntity
        return player == null || !player.isAlive || player.playerScreenHandler == null
    }

    fun renderHotbarAddons(drawContext: DrawContext) {
        if (isHidden()) {
            return
        }

        val inventoryAddon = PlayerInventoryAddon.Client.local ?: return
        val player = inventoryAddon.player

        val utilBeltDisplay = inventoryAddon.getDisplayedUtilities()
        val selectedHotbarItem = inventoryAddon.getSelectedHotbarStack()

        val scaledWidthHalved = client.window.scaledWidth / 2 - 30
        val scaledHeight = client.window.scaledHeight

        val drawSegmentedHotbar = PlayerSettings.segmentedHotbar.value == SegmentedHotbar.ONLY_VISUAL
            || PlayerSettings.segmentedHotbar.value == SegmentedHotbar.ON
        val segmentedModeOffset = if (drawSegmentedHotbar) HUD_SEGMENTED_HOTBAR_GAP else 0

        var rightHanded = player.mainArm == Arm.RIGHT
        if (inventoryAddon.swappedHands) {
            rightHanded = !rightHanded
        }

        val leftHandedUtilityBeltOffset = if (rightHanded) 0 else (LEFT_HANDED_UTILITY_BELT_OFFSET + segmentedModeOffset * 2)
        val leftHandedDisplayToolOffset = if (rightHanded) 0 else (LEFT_HANDED_DISPLAY_TOOL_OFFSET - segmentedModeOffset * 2)

        var texture = if (PlayerSettings.darkTheme.boolValue) {
            WIDGETS_TEXTURE_DARK
        } else {
            WIDGETS_TEXTURE
        }

        // Draw the frame of a tool currently in use (one on the opposite side from the offhand)

        RenderSystem.disableDepthTest()
        RenderSystem.enableBlend()
        if (inventoryAddon.displayTool.isNotEmpty && inventoryAddon.displayTool != selectedHotbarItem) {
            drawContext.drawTexture(
                texture,
                scaledWidthHalved + leftHandedDisplayToolOffset + HUD_ACTIVE_TOOL_FRAME.x + segmentedModeOffset,
                scaledHeight - HUD_ACTIVE_TOOL_FRAME.y,
                CANVAS_ACTIVE_TOOL_FRAME.x,
                CANVAS_ACTIVE_TOOL_FRAME.y,
                HUD_ACTIVE_TOOL_FRAME.width,
                HUD_ACTIVE_TOOL_FRAME.height,
                CANVAS_WIDGETS_TEXTURE_SIZE.x, CANVAS_WIDGETS_TEXTURE_SIZE.y,
            )
        }

        // Draw utility belt (both the frame and the items)
        if (!PlayerSettings.skipEmptyUtilitySlots.boolValue || utilBeltDisplay.any { it.isNotEmpty }) {
            // Draw the semi-transparent background (needed to paint next and prev utility belt items dimmed,
            //   while keeping the resulting slot opacity akin to other hotbar slots)
            drawContext.drawTexture(
                texture,
                scaledWidthHalved + leftHandedUtilityBeltOffset - HUD_UTILITY_BELT.x - segmentedModeOffset,
                scaledHeight - HUD_UTILITY_BELT.y,
                CANVAS_UTILITY_BELT_BCG.x,
                CANVAS_UTILITY_BELT_BCG.y,
                HUD_UTILITY_BELT.width,
                HUD_UTILITY_BELT.height,
                CANVAS_WIDGETS_TEXTURE_SIZE.x, CANVAS_WIDGETS_TEXTURE_SIZE.y,
            )

            // Draw next and prev utility belt items
            drawSmallItem(
                drawContext,
                utilBeltDisplay[0],
                (scaledWidthHalved + leftHandedUtilityBeltOffset - segmentedModeOffset) * 10 / 8 - SLOT_UTILITY_BELT_1.x,
                MathHelper.ceil((scaledHeight - SLOT_UTILITY_BELT_1.y) / 0.8),
            )

            drawSmallItem(
                drawContext,
                utilBeltDisplay[2],
                (scaledWidthHalved + leftHandedUtilityBeltOffset - segmentedModeOffset) * 10 / 8 - SLOT_UTILITY_BELT_2.x,
                MathHelper.ceil((scaledHeight - SLOT_UTILITY_BELT_2.y) / 0.8),
            )

            // Reverse the scaling. Also, rendering an item disables the blending for some reason and changes the binded texture
            RenderSystem.applyModelViewMatrix()
            RenderSystem.disableDepthTest()
            RenderSystem.enableBlend()
            texture = if (PlayerSettings.darkTheme.boolValue) {
                WIDGETS_TEXTURE_DARK
            } else {
                WIDGETS_TEXTURE
            }

            // Draw the utility belt frame
            drawContext.drawTexture(
                texture,
                scaledWidthHalved + leftHandedUtilityBeltOffset - HUD_UTILITY_BELT.x - segmentedModeOffset,
                scaledHeight - HUD_UTILITY_BELT.y,
                CANVAS_UTILITY_BELT.x,
                CANVAS_UTILITY_BELT.y,
                HUD_UTILITY_BELT.width,
                HUD_UTILITY_BELT.height,
                CANVAS_WIDGETS_TEXTURE_SIZE.x, CANVAS_WIDGETS_TEXTURE_SIZE.y,
            )

            // Draw the active utility item
            renderItem(
                drawContext,
                utilBeltDisplay[1],
                scaledWidthHalved + leftHandedUtilityBeltOffset - SLOT_UTILITY_BELT_3.x - segmentedModeOffset,
                scaledHeight - SLOT_UTILITY_BELT_3.y,
            )
        }

        // Draw the active tool item itself
        if (inventoryAddon.displayTool.isNotEmpty && inventoryAddon.displayTool != selectedHotbarItem) {
            renderItem(
                drawContext,
                inventoryAddon.displayTool,
                scaledWidthHalved + leftHandedDisplayToolOffset + SLOT_ACTIVE_TOOL_FRAME.x + segmentedModeOffset,
                scaledHeight - SLOT_ACTIVE_TOOL_FRAME.y,
            )
        }
        RenderSystem.enableBlend()
    }

    private fun renderItem(drawContext: DrawContext, stack: ItemStack, x: Int, y: Int) {
        if (stack.isNotEmpty) {
            drawContext.drawItem(stack, x, y)
            drawContext.drawItemInSlot(client.textRenderer, stack, x, y)
        }
    }

    fun renderFunctionOnlySelector(drawContext: DrawContext) {
        val selectedSection = PlayerInventoryAddon.Client.selectedHotbarSection
        if (selectedSection != -1 && PlayerSettings.segmentedHotbar.value == SegmentedHotbar.ONLY_FUNCTION) {
            val scaledWidthHalved = client.window.scaledWidth / 2 - 30
            val scaledHeight = client.window.scaledHeight

            val texture = if (PlayerSettings.darkTheme.boolValue) {
                WIDGETS_TEXTURE_DARK
            } else {
                WIDGETS_TEXTURE
            }
            drawContext.drawTexture(
                texture,
                scaledWidthHalved - HUD_SECTION_SELECTION.x + ((HUD_SECTION_SELECTION.width - HUD_SEGMENTED_HOTBAR_GAP) * selectedSection),
                scaledHeight - HUD_SECTION_SELECTION.y,
                CANVAS_SECTION_SELECTION_FRAME.x,
                CANVAS_SECTION_SELECTION_FRAME.y,
                HUD_SECTION_SELECTION.width,
                HUD_SECTION_SELECTION.height,
                CANVAS_WIDGETS_TEXTURE_SIZE.x, CANVAS_WIDGETS_TEXTURE_SIZE.y,
            )
        }
    }

    private fun drawSmallItem(drawContext: DrawContext, stack: ItemStack, x: Int, y: Int) {
        if (!stack.isEmpty) {
            val bakedModel = client.itemRenderer.getModel(stack, null, null, 0)
            drawContext.matrices.push()
            drawContext.matrices.scale(0.8f, 0.8f, 0.8f)
            drawContext.matrices.translate((x + 8).toFloat(), (y + 8).toFloat(), 150f)
            try {
                drawContext.matrices.multiplyPositionMatrix(Matrix4f().scaling(1.0f, -1.0f, 1.0f))
                drawContext.matrices.scale(16.0f, 16.0f, 16.0f)
                val bl = !bakedModel.isSideLit
                if (bl) {
                    DiffuseLighting.disableGuiDepthLighting()
                }
                client.itemRenderer.renderItem(stack, ModelTransformationMode.GUI, false, drawContext.matrices, drawContext.vertexConsumers, 15728880, OverlayTexture.DEFAULT_UV, bakedModel)
                drawContext.draw()
                if (bl) {
                    DiffuseLighting.enableGuiDepthLighting()
                }
            } catch (var12: Throwable) {
                val crashReport = CrashReport.create(var12, "Rendering item")
                val crashReportSection = crashReport.addElement("Item being rendered")
                crashReportSection.add("Item Type") { stack.item.toString() }
                crashReportSection.add("Item Damage") { stack.damage.toString() }
                //#if MC >= 12100
                crashReportSection.add("Item Components") { stack.components.toString() }
                //#else
                //$$ crashReportSection.add("Item NBT") { stack.nbt.toString() }
                //#endif
                crashReportSection.add("Item Foil") { stack.hasGlint().toString() }
                throw CrashException(crashReport)
            }
            drawContext.matrices.pop()
        }
    }
}
