package me.td.mythicalstaves;

import me.td.mythicalstaves.interfaces.KeyBindingDisabledAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {
    public static BlockHitResult raycastForBlock(PlayerEntity player, double maxDistance) {
        // Start position (player's eye position)
        Vec3d start = player.getCameraPosVec(1.0F);

        // Direction the player is facing
        Vec3d direction = player.getRotationVec(1.0F);

        // End position (start + direction * maxDistance)
        Vec3d end = start.add(direction.multiply(maxDistance));

        RaycastContext context = new RaycastContext(
                start, end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                player
        );

        return player.getEntityWorld().raycast(context);
    }

    public static ArrayList<BlockPos> drawSphere(BlockPos center, int radius) {
        ArrayList<BlockPos> blocks = new ArrayList<>();
        for(int x = -radius; x < radius; ++x) {
            for(int y = -radius; y < radius; ++y) {
                for(int z = -radius; z < radius; ++z) {
                    Vec3d pos1 = new Vec3d(center.getX(), center.getY(), center.getZ());
                    BlockPos _pos2 = center.add(x, y, z);
                    Vec3d pos2 = new Vec3d(_pos2.getX(), _pos2.getY(), _pos2.getZ());
                    if(pos1.distanceTo(pos2) <= radius) {
                        blocks.add(_pos2);
                    }
                }
            }
        }

        return blocks;
    }

    public static ArrayList<BlockPos> drawHollowSphere(BlockPos center, int radius) {
        ArrayList<BlockPos> blocks = new ArrayList<>();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos currentPos = center.add(x, y, z);
                    Vec3d pos1 = new Vec3d(center.getX(), center.getY(), center.getZ());
                    Vec3d pos2 = new Vec3d(currentPos.getX(), currentPos.getY(), currentPos.getZ());

                    // Distance from center
                    double distance = pos1.distanceTo(pos2);

                    // Check if the position is on the shell of the sphere
                    if (distance <= radius && distance >= radius - 1) {
                        blocks.add(currentPos);
                    }
                }
            }
        }

        return blocks;
    }

    public static List<Entity> findEntitiesOfTypeInSphere(World world, BlockPos center, double radius, Entity except) {
        // Define the bounding box
        Box box = new Box(
                center.getX() - radius, center.getY() - radius, center.getZ() - radius,
                center.getX() + radius, center.getY() + radius, center.getZ() + radius
        );

        // Retrieve all entities of the specified type within the bounding box
        List<Entity> entitiesInBox = world.getOtherEntities(except, box, entity -> true);

        // Filter entities to include only those within the specified radius
        return entitiesInBox.stream()
                .filter(entity -> entity.squaredDistanceTo(center.getX(), center.getY(), center.getZ()) <= radius * radius)
                .collect(Collectors.toList());
    }

    public static void disablePlayerMovement(boolean disabled) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ((KeyBindingDisabledAccessor) mc.options.forwardKey).setDisabled(disabled);
        ((KeyBindingDisabledAccessor) mc.options.backKey).setDisabled(disabled);
        ((KeyBindingDisabledAccessor) mc.options.rightKey).setDisabled(disabled);
        ((KeyBindingDisabledAccessor) mc.options.leftKey).setDisabled(disabled);
        ((KeyBindingDisabledAccessor) mc.options.jumpKey).setDisabled(disabled);
        ((KeyBindingDisabledAccessor) mc.options.sprintKey).setDisabled(disabled);
        ((KeyBindingDisabledAccessor) mc.options.sneakKey).setDisabled(disabled);
    }

    public static boolean isPlayerMovementDisabled() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return ((KeyBindingDisabledAccessor) mc.options.forwardKey).isDisabled();
    }

    public static Vec3d vec3dAbs(Vec3d vec) {
        double x = vec.x;
        double y = vec.y;
        double z = vec.z;
        if(x < 0) x*=-1;
        if(y < 0) y*=-1;
        if(z < 0) z*=-1;
        return new Vec3d(x, y, z);
    }
}
