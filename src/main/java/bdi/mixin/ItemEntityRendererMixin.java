package bdi.mixin;

import java.util.Random;

import bdi.util.ItemEntityRotator;
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
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin extends EntityRenderer<ItemEntity> {
    @Shadow @Final private Random random;
    @Shadow @Final private ItemRenderer itemRenderer;
    @Shadow protected abstract int getRenderedAmount(ItemStack stack);

    protected ItemEntityRendererMixin(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Inject(at = @At("RETURN"), method = "<init>")
    private void onConstructor(EntityRenderDispatcher dispatcher, ItemRenderer renderer, CallbackInfo callback) {
        this.shadowSize = 0;
    }

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    private void render(ItemEntity itemEntity, float f, float partialTicks, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo callback) {
        ItemStack itemStack = itemEntity.getStack();

        // setup seed for random rotation
        int seed = itemStack.isEmpty() ? 187 : Item.getRawId(itemStack.getItem()) + itemStack.getDamage();
        this.random.setSeed(seed);

        matrixStack.push();
        BakedModel bakedModel = this.itemRenderer.getHeldItemModel(itemStack, itemEntity.world, null);
        boolean hasDepthInGui = bakedModel.hasDepth();

        // decide how many item layers to render
        int renderCount = this.getRenderedAmount(itemStack);

        // make item lie flat on ground
        matrixStack.multiply(Vector3f.POSITIVE_X.getRadialQuaternion(1.571F));

        // rotate item
        ItemEntityRotator rotator = (ItemEntityRotator) itemEntity;
        if(!itemEntity.onGround && !itemEntity.isSubmergedInWater()) {
            float rotation = ((float)itemEntity.getAge() + partialTicks) / 20.0F + itemEntity.hoverHeight;
            matrixStack.multiply(Vector3f.POSITIVE_Z.getRadialQuaternion(rotation));
            rotator.setRotation(new Vec3d(0, 0, rotation));
        }
        else {
            matrixStack.multiply(Vector3f.POSITIVE_Z.getRadialQuaternion((float) rotator.getRotation().z));
        }

        // offset rendered stacks vertically
        // 0.01 barely brings flat items flush to the ground
        matrixStack.translate(0, 0, -0.01f);
        if(itemEntity.getStack().getItem() instanceof BlockItem) {
            // make blocks flush with the ground
            matrixStack.translate(0, 0, -0.12f);
        }

        float scaleX = bakedModel.getTransformation().ground.scale.getX();
        float scaleY = bakedModel.getTransformation().ground.scale.getY();
        float scaleZ = bakedModel.getTransformation().ground.scale.getZ();

        float x;
        float y;
        if (!hasDepthInGui) {
            float r = -0.0F * (float)(renderCount) * 0.5F * scaleX;
            x = -0.0F * (float)(renderCount) * 0.5F * scaleY;
            y = -0.09375F * (float)(renderCount) * 0.5F * scaleZ;
            matrixStack.translate(r, x, y);
        }

        // render each item in the stack on the ground (higher stack count == more items displayed)
        for(int u = 0; u < renderCount; ++u) {
            matrixStack.push();

            // random positioning for rendered items, is especially seen in 64 block stacks on the ground
            if (u > 0) {
                if (hasDepthInGui) {
                    x = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    y = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float z = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    matrixStack.translate(x, y, z);
                } else {
                    x = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    y = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    matrixStack.translate(x, y, 0.0D);
                    matrixStack.multiply(Vector3f.POSITIVE_Z.getRadialQuaternion(this.random.nextFloat()));
                }
            }

            // render item
            this.itemRenderer.renderItem(itemStack, ModelTransformation.Mode.GROUND, false, matrixStack, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV, bakedModel);

            // end
            matrixStack.pop();

            // translate based on scale, which gies vertical layering to high stack count items
            if (!hasDepthInGui) {
                matrixStack.translate(0.0F * scaleX, 0.0F * scaleY, 0.0625F * scaleZ);
            }
        }

        // end
        matrixStack.pop();
        super.render(itemEntity, f, partialTicks, matrixStack, vertexConsumerProvider, i);
        callback.cancel();
    }
}
