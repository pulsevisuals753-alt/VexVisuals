package com.vexvisuals.mixin;

import com.vexvisuals.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true)
    private void vexvisuals$hasOutline(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.ESP != null && ModuleManager.ESP.enabled
                && entity instanceof PlayerEntity
                && entity != MinecraftClient.getInstance().player) {
            cir.setReturnValue(true);
        }
    }
}
