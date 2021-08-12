package net.sourcewriters.spigot.rwg.legacy.api.impl.version.biome;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;

@SuppressWarnings("deprecation")
public final class BiomeProvider1_13 extends BiomeProvider {
    
    BiomeProvider1_13() {}

    @Override
    public Biome getBiome(BiomeGrid grid, int x, int y, int z) {
        return grid.getBiome(x, z);
    }

    @Override
    public void setBiome(BiomeGrid grid, int x, int y, int z, Biome biome) {
        grid.setBiome(x, z, biome);
    }

    @Override
    public Biome getBiome(World world, int x, int y, int z) {
        return world.getBiome(x, z);
    }

    @Override
    public void setBiome(World world, int x, int y, int z, Biome biome) {
        world.setBiome(x, z, biome);
    }

}