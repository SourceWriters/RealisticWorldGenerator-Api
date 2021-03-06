package net.sourcewriters.spigot.rwg.legacy.api.block;

import java.util.function.Supplier;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import net.sourcewriters.spigot.rwg.legacy.api.util.annotation.source.NonNull;

public interface IBlockDataLoaderManager {

    boolean register(@NonNull BlockDataLoader loader);

    default boolean register(final boolean expression, @NonNull final Supplier<BlockDataLoader> loader) {
        if (!expression) {
            return false;
        }
        return register(loader.get());
    }

    boolean unregister(long id);

    boolean has(long id);

    BlockDataLoader get(long id);

    int getPosition(long id);

    IBlockData load(@NonNull Location location);

    IBlockData load(@NonNull Block block);
    
    IBlockData load(@NonNull BlockState state);

}
