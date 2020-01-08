package com.github.draylar.modid.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import com.github.draylar.modid.util.ItemEntityRotator;

@Mixin(ItemEntityRenderer.class)
public abstract class MixinItemEntityRenderer extends EntityRenderer<ItemEntity> {
    @Shadow @Final private Random random;
    @Shadow @Final private ItemRenderer itemRenderer;
    @Shadow protected abstract int getRenderedAmount(ItemStack stack);

    protected MixinItemEntityRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Inject(at = @At("RETURN"), method = "<init>")
    private void onConstructor(EntityRenderDispatcher dispatcher, ItemRenderer renderer, CallbackInfo callback) {
        this.shadowSize = 0;
    }

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    private void render(ItemEntity itemEntity, float f, float partialTicks, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo callback) {
        ItemStack itemStack = itemEntity.getStack();

        int seed = itemStack.isEmpty() ? 187 : Item.getRawId(itemStack.getItem()) + itemStack.getDamage();
        this.random.setSeed((long) seed);

        matrixStack.push();
        BakedModel bakedModel = this.itemRenderer.getHeldItemModel(itemStack, itemEntity.world, (LivingEntity)null);
        boolean hasDepthInGui = bakedModel.hasDepthInGui();

        int renderCount = this.getRenderedAmount(itemStack);

        matrixStack.multiply(Vector3f.POSITIVE_X.getRadialQuaternion(1.571F));

        ItemEntityRotator rotator = (ItemEntityRotator) itemEntity;
        if(!itemEntity.onGround && !itemEntity.isInsideWater()) {
            float rotation = ((float)itemEntity.getAge() + partialTicks) / 20.0F + itemEntity.hoverHeight;
            matrixStack.multiply(Vector3f.POSITIVE_Z.getRadialQuaternion(rotation));
            rotator.setRotation(new Vec3d(0, 0, rotation));
        }
        else {
            matrixStack.multiply(Vector3f.POSITIVE_Z.getRadialQuaternion((float) rotator.getRotation().z));
        }
        
        matrixStack.translate(-0.07f, 0, -0.01f);
        if(itemEntity.getStack().getItem() instanceof BlockItem) {
            matrixStack.translate(0.23f, 0, -0.14f);
        }

        float scaleX = bakedModel.getTransformation().ground.scale.getX();
        float scaleY = bakedModel.getTransformation().ground.scale.getY();
        float scaleZ = bakedModel.getTransformation().ground.scale.getZ();

        float v;
        float w;
        if (!hasDepthInGui) {
            float r = -0.0F * (float)(renderCount - 1) * 0.5F * scaleX;
            v = -0.0F * (float)(renderCount - 1) * 0.5F * scaleY;
            w = -0.09375F * (float)(renderCount - 1) * 0.5F * scaleZ;
            matrixStack.translate((double)r, (double)v, (double)w);
        }

        for(int u = 0; u < renderCount; ++u) {
            matrixStack.push();
            if (u > 0) {
                if (hasDepthInGui) {
                    v = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    w = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float x = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    matrixStack.translate((double)v, (double)w, (double)x);
                } else {
                    v = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    w = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    matrixStack.translate((double)v, (double)w, 0.0D);
                }
            }

            this.itemRenderer.renderItem(itemStack, ModelTransformation.Mode.GROUND, false, matrixStack, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV, bakedModel);
            matrixStack.pop();
            if (!hasDepthInGui) {
                matrixStack.translate((double)(0.0F * scaleX), (double)(0.0F * scaleY), (double)(0.09375F * scaleZ));
            }
        }

        matrixStack.pop();
        
        super.render(itemEntity, f, partialTicks, matrixStack, vertexConsumerProvider, i);
        callback.cancel();
    }
}
