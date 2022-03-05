package bdi.mixin;

import bdi.util.ItemEntityRotator;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.*;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin extends EntityRenderer<ItemEntity> {

    @Shadow @Final private Random random;
    @Shadow @Final private ItemRenderer itemRenderer;
    @Shadow protected abstract int getRenderedAmount(ItemStack stack);

    private ItemEntityRendererMixin(EntityRendererFactory.Context dispatcher) {
        super(dispatcher);
    }

    @Inject(at = @At("RETURN"), method = "<init>")
    private void onConstructor(EntityRendererFactory.Context context, CallbackInfo ci) {
        this.shadowRadius = 0;
    }

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    private void render(ItemEntity dropped, float f, float partialTicks, MatrixStack matrix, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo callback) {
        ItemStack itemStack = dropped.getStack();

        // setup seed for random rotation
        int seed = itemStack.isEmpty() ? 187 : Item.getRawId(itemStack.getItem()) + itemStack.getDamage();
        this.random.setSeed(seed);

        matrix.push();
        BakedModel bakedModel = this.itemRenderer.getModel(itemStack, dropped.world, null, seed);
        boolean hasDepthInGui = bakedModel.hasDepth();

        // decide how many item layers to render
        int renderCount = this.getRenderedAmount(itemStack);

        // Helper for manipulating data on the current ItemEntity
        ItemEntityRotator rotator = (ItemEntityRotator) dropped;

        // Certain BlockItems (Grass Block, Jukebox, Dirt, Ladders) are fine being rotated 180 degrees like standard items.
        // Other BlockItems (Carpet, Slab) do not like being rotated and should stay flat.
        // To determine whether a block should be flat or rotated, we check the collision box height.
        // Anything that takes up more than half a block vertically is rotated.
        boolean renderBlockFlat = false;
        if(dropped.getStack().getItem() instanceof BlockItem && !(dropped.getStack().getItem() instanceof AliasedBlockItem)) {
            Block b = ((BlockItem) dropped.getStack().getItem()).getBlock();
            VoxelShape shape = b.getOutlineShape(b.getDefaultState(), dropped.world, dropped.getBlockPos(), ShapeContext.absent());

            // Only blocks with a collision box of <.5 should be rendered flat
            if(shape.getMax(Direction.Axis.Y) <= .5) {
                renderBlockFlat = true;
            }
        }

        // Make full blocks flush with ground
        Item item = dropped.getStack().getItem();
        if(item instanceof BlockItem && !(item instanceof AliasedBlockItem) && !renderBlockFlat) {
            // make blocks flush with the ground
             matrix.translate(0, -0.06, 0);
        }

        // Give all non-flat items a 90* spin
        if(!renderBlockFlat) {
            matrix.translate(0, .185, .0);
            matrix.multiply(Vec3f.POSITIVE_X.getRadialQuaternion(1.571F));
            matrix.translate(0, -.185, -.0);
        }

        // Item is flying through air
        boolean isAboveWater = dropped.world.getBlockState(dropped.getBlockPos()).getFluidState().getFluid().isIn(FluidTags.WATER);
        if(!dropped.isOnGround() && (!dropped.isSubmergedInWater() && !isAboveWater)) {
            float rotation = ((float) dropped.getItemAge() + partialTicks) / 20.0F + dropped.getHeight(); // calculate rotation based on age and ticks

            // 90* items/blocks (non-flat) get spin on Z axis, flat items/blocks get spin on Y axis
            if(!renderBlockFlat) {
                // rotate renderer
                matrix.translate(0, .185, .0);
                matrix.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion(rotation));
                matrix.translate(0, -.185, .0);

                // save rotation in entity
                rotator.setRotation(new Vec3d(0, 0, rotation));
            } else {
                // rotate renderer
                matrix.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(rotation));

                // save rotation in entity
                rotator.setRotation(new Vec3d(0, rotation, 0));

                // Translate down to become flush with floor
                matrix.translate(0, -.065, 0);
            }

            // Carrots/Potatoes/Redstone/other crops in air need vertical offset
            if(dropped.getStack().getItem() instanceof AliasedBlockItem) {
                matrix.translate(0, 0, .195);
            }

            else if (!(dropped.getStack().getItem() instanceof BlockItem)) {
                // Translate down to become flush with floor
                matrix.translate(0, 0, .195);
            }
        }

        // Carrots/Potatoes/Redstone/other crops on ground
        else if(dropped.getStack().getItem() instanceof AliasedBlockItem){
            matrix.translate(0, .185, .0);
            matrix.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion((float) rotator.getRotation().z));
            matrix.translate(0, -.185, .0);

            // Translate down to become flush with floor
            matrix.translate(0, 0, .195);
        }

        // Ladders/Slabs/Carpet and other short blocks on ground
        else if(renderBlockFlat) {
            matrix.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion((float) rotator.getRotation().y));

            // Translate down to become flush with floor
            matrix.translate(0, -.065, 0);
        }


        // Normal blocks/items on ground
        else {
            // Translate normal items down to become flush with floor
            if (!(dropped.getStack().getItem() instanceof BlockItem)) {
                matrix.translate(0, 0, .195);
            }

            matrix.translate(0, .185, .0);
            matrix.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion((float) rotator.getRotation().z));
            matrix.translate(0, -.185, .0);
        }

        // special-case soul sand
        if(dropped.world.getBlockState(dropped.getBlockPos()).getBlock().equals(Blocks.SOUL_SAND)) {
            matrix.translate(0, 0, -.1);
        }

        // special-case skulls
        if(dropped.getStack().getItem() instanceof BlockItem) {
            if(((BlockItem) dropped.getStack().getItem()).getBlock() instanceof SkullBlock) {
                matrix.translate(0, .11, 0);
            }
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
            matrix.translate(r, x, y);
        }

        // render each item in the stack on the ground (higher stack count == more items displayed)
        for(int u = 0; u < renderCount; ++u) {
            matrix.push();

            // random positioning for rendered items, is especially seen in 64 block stacks on the ground
            if (u > 0) {
                if (hasDepthInGui) {
                    x = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    y = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float z = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    matrix.translate(x, y, z);
                } else {
                    x = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    y = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    matrix.translate(x, y, 0.0D);
                    matrix.multiply(Vec3f.POSITIVE_Z.getRadialQuaternion(this.random.nextFloat()));
                }
            }

            // render item
            this.itemRenderer.renderItem(itemStack, ModelTransformation.Mode.GROUND, false, matrix, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV, bakedModel);

            // end
            matrix.pop();

            // translate based on scale, which gies vertical layering to high stack count items
            if (!hasDepthInGui) {
                matrix.translate(0.0F * scaleX, 0.0F * scaleY, 0.0625F * scaleZ);
            }
        }

        // end
        matrix.pop();
        super.render(dropped, f, partialTicks, matrix, vertexConsumerProvider, i);
        callback.cancel();
    }
}
