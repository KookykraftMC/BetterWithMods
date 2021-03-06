package betterwithmods.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.Vec3d;

/**
 * The Entity will flee from threats in the opposite direction.
 *
 * @author Koward
 */
public class EntityAIFlee extends EntityAIBase {
    private final EntityCreature theEntityCreature;
    protected double speed;
    private double randPosX;
    private double randPosY;
    private double randPosZ;

    public EntityAIFlee(EntityCreature creature, double speedIn) {
        this.theEntityCreature = creature;
        this.speed = speedIn;
        this.setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute() {
        Vec3d vec3d = null;

        if (this.theEntityCreature.isBurning()) {
            vec3d = RandomPositionGenerator.findRandomTarget(this.theEntityCreature, 5, 4);
        } else if (this.theEntityCreature.getAITarget() != null) {
            vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.theEntityCreature, 5, 4,
                    new Vec3d(this.theEntityCreature.getAITarget().posX, this.theEntityCreature.getAITarget().posY, this.theEntityCreature.getAITarget().posZ));
        }

        if (vec3d != null) {
            this.randPosX = vec3d.xCoord;
            this.randPosY = vec3d.yCoord;
            this.randPosZ = vec3d.zCoord;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting() {
        this.theEntityCreature.getNavigator().tryMoveToXYZ(this.randPosX, this.randPosY, this.randPosZ, this.speed);
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean continueExecuting() {
        if (!this.theEntityCreature.getNavigator().noPath() && this.theEntityCreature.getAITarget() != null) {
            EntityLivingBase target = this.theEntityCreature.getAITarget();

            if (target == null) {
                return true;
            }

            double sqDistToPos = this.theEntityCreature.getDistanceSq(this.randPosX, this.randPosY, this.randPosZ);

            if (sqDistToPos > 2.0D) {
                double sqDistToTarget = this.theEntityCreature.getDistanceSqToEntity(target);
                double sqDistOfTargetToPos = target.getDistanceSq(this.randPosX, this.randPosY, this.randPosZ);

                if (sqDistToTarget < sqDistOfTargetToPos) {
                    return true;
                }
            }
        }

        return false;
    }
}