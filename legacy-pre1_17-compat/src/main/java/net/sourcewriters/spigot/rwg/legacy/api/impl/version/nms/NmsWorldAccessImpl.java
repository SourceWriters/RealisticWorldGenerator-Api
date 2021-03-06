package net.sourcewriters.spigot.rwg.legacy.api.impl.version.nms;

import java.util.Optional;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Skull;
import org.bukkit.entity.EntityType;

import com.mojang.authlib.GameProfile;
import com.syntaxphoenix.syntaxapi.nbt.NbtCompound;

import net.sourcewriters.spigot.rwg.legacy.api.util.java.reflect.Accessor;
import net.sourcewriters.spigot.rwg.legacy.api.util.java.reflect.AccessorProvider;
import net.sourcewriters.spigot.rwg.legacy.api.util.minecraft.ProfileCache;
import net.sourcewriters.spigot.rwg.legacy.api.util.rwg.RWGEntityType;
import net.sourcewriters.spigot.rwg.legacy.api.version.nms.INmsNbtAccess;
import net.sourcewriters.spigot.rwg.legacy.api.version.nms.INmsWorldAccess;
import net.sourcewriters.spigot.rwg.legacy.api.version.util.ServerVersion;
import net.sourcewriters.spigot.rwg.legacy.api.version.util.Versions;

public final class NmsWorldAccessImpl implements INmsWorldAccess {

    private final AccessorProvider provider;
    private final INmsNbtAccess access;

    public NmsWorldAccessImpl(final AccessorProvider provider, final INmsNbtAccess access) {
        this.provider = provider;
        this.access = access;
    }

    @Override
    public void setSpawner(final Block block, final EntityType type, final short spawnCount, final short spawnRange, final short delay,
        final short maxDelay, final short minDelay, final short maxNearbyEntities, final short requiredPlayerRange) {

        final ServerVersion version = Versions.getServer();

        final Optional<Accessor> option0 = provider.get("cb_block_entityState");
        final Optional<Accessor> option1 = provider.get("nms_entity_spawner");
        final Optional<Accessor> option2 = provider.get("nms_nbt_compound");
        final Optional<Accessor> option3 = provider.get("nms_blockdata");
        if (!option0.isPresent() || !option1.isPresent() || !option2.isPresent() || version.getMinor() >= 16 && !option3.isPresent()) {
            return;
        }

        final Accessor state = option0.get();
        final Accessor spawner = option1.get();
        final Accessor nbt = option2.get();
        final Accessor blockData = option3.orElse(null);

        final BlockState blockState = block.getState();
        if (!(blockState instanceof CreatureSpawner)) {
            return;
        }

        final Object spawnerObject = state.invoke(blockState, "entity");

        Object nbtCompound = nbt.initialize();
        nbtCompound = spawner.invoke(spawnerObject, "save", nbtCompound);

        final NbtCompound compound = (NbtCompound) access.fromMinecraftTag(nbtCompound);

        compound.remove("SpawnPotentials");

        compound.set("SpawnCount", spawnCount);
        compound.set("SpawnRange", spawnRange);
        compound.set("Delay", delay);
        compound.set("MinSpawnDelay", minDelay);
        compound.set("MaxSpawnDelay", maxDelay);
        compound.set("MaxNearbyEntities", maxNearbyEntities);
        compound.set("RequiredPlayerRange", requiredPlayerRange);

        final NbtCompound dataCompound = new NbtCompound();
        dataCompound.set("id", "minecraft:" + RWGEntityType.toMinecraft(type));

        compound.set("SpawnData", dataCompound);

        final Object spawnerCompound = access.toMinecraftTag(compound);

        if (version.getMinor() >= 16) {
            spawner.invoke(spawnerObject, "load", blockData.getOwner().cast(null), spawnerCompound);
        } else {
            spawner.invoke(spawnerObject, "load", spawnerCompound);
        }

    }

    @Override
    public void setHeadTexture(final Block block, final String texture) {

        if (texture == null) {
            return;
        }

        final Optional<Accessor> option0 = provider.get("nms_entity_skull");
        final Optional<Accessor> option1 = provider.get("cb_block_entityState");
        if (!option0.isPresent() || !option1.isPresent()) {
            return;
        }

        final Accessor nmsSkull = option0.get();
        final Accessor cbEntityState = option1.get();

        final BlockState state = block.getState();
        if (!(state instanceof Skull)) {
            return;
        }

        final GameProfile gameProfile = ProfileCache.asProfile(texture);

        final Object tileEntity = cbEntityState.invoke(state, "entity");
        nmsSkull.invoke(tileEntity, "profile", gameProfile);

    }

    @Override
    public String getHeadTexture(final Block block) {

        final Optional<Accessor> option0 = provider.get("cb_block_skull");
        if (!option0.isPresent()) {
            return null;
        }

        final Accessor skull = option0.get();

        final BlockState state = block.getState();
        if (!(state instanceof Skull)) {
            return null;
        }

        return ProfileCache.asTexture((GameProfile) skull.getValue(state, "profile"));
    }

}
