package com.github.draylar.modid.mixin;

import com.github.draylar.modid.util.ItemEntityRotator;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin extends EntityRenderer {

    protected ItemEntityRendererMixin(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Shadow protected abstract int getRenderedAmount(ItemStack itemStack_1);

    @Shadow @Final private Random random;

    @Shadow protected abstract Identifier method_3999(ItemEntity itemEntity_1);

    @Shadow @Final private ItemRenderer itemRenderer;

    @Shadow protected abstract int method_3997(ItemEntity itemEntity_1, double double_1, double double_2, double double_3, float float_1, BakedModel bakedModel_1);

    @Inject(at = @At("RETURN"), method = "<init>")
    private void onConstructor(EntityRenderDispatcher entityRenderDispatcher_1, ItemRenderer itemRenderer_1, CallbackInfo ci) {
        this.field_4673 = 0;
    }

    @Inject(at = @At("HEAD"), method = "method_3996", cancellable = true)
    private void render(ItemEntity itemEntity, double x, double y, double z, float float_1, float float_2, CallbackInfo info) {
        ItemStack itemStack = itemEntity.getStack();

        int seed = itemStack.isEmpty() ? 187 : Item.getRawId(itemStack.getItem()) + itemStack.getDamage();
        this.random.setSeed((long) seed);

        boolean boolean_1 = false;
        if (this.bindEntityTexture(itemEntity)) {
            this.renderManager.textureManager.getTexture(this.method_3999(itemEntity)).pushFilter(false, false);
            boolean_1 = true;
        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GuiLighting.enable();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.pushMatrix();
        BakedModel model = this.itemRenderer.getModel(itemStack, itemEntity.world, null);

        int getRenderCountAndBob = this.method_3997(itemEntity, x, y, z, float_2, model);

        float scaleX = model.getTransformation().ground.scale.getX();
        float scaleY = model.getTransformation().ground.scale.getY();
        float scaleZ = model.getTransformation().ground.scale.getZ();

        boolean hasDepthInGui = model.hasDepthInGui();

        float float_9;
        float float_10;
        if (!hasDepthInGui) {
            float float_6 = -0.0F * (float)(getRenderCountAndBob - 1) * 0.5F * scaleX;
            float_9 = -0.0F * (float)(getRenderCountAndBob - 1) * 0.5F * scaleY;
            float_10 = -0.09375F * (float)(getRenderCountAndBob - 1) * 0.5F * scaleZ;
            GlStateManager.translatef(float_6, float_9, float_10);
        }

        if (this.renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.setupSolidRenderingTextureCombine(this.getOutlineColor(itemEntity));
        }

        for(int int_3 = 0; int_3 < getRenderCountAndBob; ++int_3) {
            if (hasDepthInGui) {
                GlStateManager.pushMatrix();
                if (int_3 > 0) {
                    float_9 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float_10 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float float_11 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    GlStateManager.translatef(float_9, float_10, float_11);
                }

                model.getTransformation().applyGl(ModelTransformation.Type.GROUND);
                this.itemRenderer.renderItemAndGlow(itemStack, model);
                GlStateManager.popMatrix();
            } else {
                GlStateManager.pushMatrix();
                if (int_3 > 0) {
                    float_9 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    float_10 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    GlStateManager.translatef(float_9, float_10, 0.0F);
                }

                model.getTransformation().applyGl(ModelTransformation.Type.GROUND);
                this.itemRenderer.renderItemAndGlow(itemStack, model);
                GlStateManager.popMatrix();
                GlStateManager.translatef(0.0F * scaleX, 0.0F * scaleY, 0.09375F * scaleZ);
            }
        }

        if (this.renderOutlines) {
            GlStateManager.tearDownSolidRenderingTextureCombine();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        this.bindEntityTexture(itemEntity);
        if (boolean_1) {
            this.renderManager.textureManager.getTexture(this.method_3999(itemEntity)).popFilter();
        }

        super.render(itemEntity, x, y, z, float_1, float_2);
        info.cancel();
    }

    @Inject(at = @At("HEAD"), method = "method_3997", cancellable = true)
    private void renderItem(ItemEntity itemEntity, double x, double y, double z, float partialTicks, BakedModel model, CallbackInfoReturnable<Integer> info) {
        ItemStack itemstack = itemEntity.getStack();
        Item item = itemstack.getItem();

        if (item == null) {
            info.setReturnValue(0);
        } else {
            GlStateManager.translatef((float) x, (float) y, (float) z);
            GlStateManager.translatef(0, .01f, -.15f);

            if(itemEntity.getStack().getItem() instanceof BlockItem) {
                GlStateManager.translatef(0, .1f, 0);
            }


            ItemEntityRotator rotator = (ItemEntityRotator) itemEntity;

            if(!itemEntity.onGround && !itemEntity.isInsideWater()) {
                if (model.hasDepthInGui() || this.renderManager.gameOptions != null) {
                    float rotation = (((float) itemEntity.getAge() + partialTicks) / 20.0F + itemEntity.hoverHeight) * 57.295776F;
                    GlStateManager.rotatef(rotation, 0.0F, 1.0F, 0.0F);
                    rotator.setRotation(new Vec3d(0, rotation, 0));
                }
            } else {
                GlStateManager.rotated(rotator.getRotation().y, 0.0F, 1.0F, 0.0F);
            }

            GlStateManager.rotatef(90, 1, 0, 0);

            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            info.setReturnValue(getRenderedAmount(itemstack));
        }

        info.cancel();
    }
}
