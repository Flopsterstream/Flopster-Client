package net.minecraft.world.level.block.sounds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public class AmbientDesertBlockSoundsPlayer {
    private static final int IDLE_SOUND_CHANCE = 1600;
    private static final int WIND_SOUND_CHANCE = 10000;
    private static final int SURROUNDING_BLOCKS_PLAY_SOUND_THRESHOLD = 3;
    private static final int SURROUNDING_BLOCKS_DISTANCE_CHECK = 8;

    public static void playAmbientBlockSounds(BlockState p_396471_, Level p_396963_, BlockPos p_397306_, RandomSource p_392626_) {
        if (p_396471_.is(BlockTags.PLAYS_AMBIENT_DESERT_BLOCK_SOUNDS) && p_396963_.canSeeSky(p_397306_.above())) {
            if (p_392626_.nextInt(1600) == 0 && shouldPlayAmbientSound(p_396963_, p_397306_)) {
                p_396963_.playLocalSound(
                    p_397306_.getX(), p_397306_.getY(), p_397306_.getZ(), SoundEvents.SAND_IDLE, SoundSource.AMBIENT, 1.0F, 1.0F, false
                );
            }

            if (p_392626_.nextInt(10000) == 0 && isInAmbientSoundBiome(p_396963_.getBiome(p_397306_)) && shouldPlayAmbientSound(p_396963_, p_397306_)) {
                p_396963_.playPlayerSound(SoundEvents.SAND_WIND, SoundSource.AMBIENT, 1.0F, 1.0F);
            }
        }
    }

    private static boolean isInAmbientSoundBiome(Holder<Biome> p_391489_) {
        return p_391489_.is(Biomes.DESERT) || p_391489_.is(BiomeTags.IS_BADLANDS);
    }

    private static boolean shouldPlayAmbientSound(Level p_396249_, BlockPos p_396261_) {
        int i = 0;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos = p_396261_.relative(direction, 8);
            BlockState blockstate = p_396249_.getBlockState(blockpos.atY(p_396249_.getHeight(Heightmap.Types.WORLD_SURFACE, blockpos) - 1));
            if (blockstate.is(BlockTags.PLAYS_AMBIENT_DESERT_BLOCK_SOUNDS)) {
                if (++i >= 3) {
                    return true;
                }
            }
        }

        return false;
    }
}