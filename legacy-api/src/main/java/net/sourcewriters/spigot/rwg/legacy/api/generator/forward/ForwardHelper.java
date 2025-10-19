package net.sourcewriters.spigot.rwg.legacy.api.generator.forward;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import net.sourcewriters.spigot.rwg.legacy.api.generator.IRwgGenerator;
import net.sourcewriters.spigot.rwg.legacy.api.util.java.reflect.Accessor;

public final class ForwardHelper {

    private static final ConcurrentHashMap<String, Accessor> ACCESSORS = new ConcurrentHashMap<>();
    private static final ArrayList<String> LIST = new ArrayList<>();

    private ForwardHelper() {}
    
    public static boolean isForward(final World world) {
        return world == null ? false : isForward(world.getGenerator());
    }

    public static boolean isForward(final ChunkGenerator generator) {
        if (generator == null) {
            return false;
        }
        final Class<?> clazz = generator.getClass();
        final String name = clazz.getName();
        if (LIST.contains(name)) {
            return ACCESSORS.containsKey(name);
        }
        final Accessor access = Accessor.of(clazz);
        try {
            access.findMethod("id", "getIdentifier");
        } catch(NoClassDefFoundError err) {
            LIST.add(name);
            return false;
        }
        final Object object = access.invoke(generator, "id");
        final boolean valid = object != null && (long) object == 345679324062398605L;
        LIST.add(name);
        if (!valid) {
            return false;
        }
        ACCESSORS.put(name, access);
        access.findMethod("set", "setGenerator", ChunkGenerator.class).findMethod("get", "getGenerator").findMethod("populators",
            "setPopulators", BlockPopulator[].class);
        return valid;
    }

    public static IRwgGenerator get(final World world) {
        return world != null ? get(world.getGenerator()) : null;
    }

    public static IRwgGenerator get(final ChunkGenerator generator) {
        if (!isForward(generator)) {
            return null;
        }
        final Object found = ACCESSORS.get(generator.getClass().getName()).invoke(generator, "get");
        return found instanceof IRwgGenerator ? (IRwgGenerator) found : null;
    }

    public static ForwardState set(final World world, final Function<World, ChunkGenerator> builder) {
        if (world == null || world.getGenerator() == null) {
            return ForwardState.UNKNOWN;
        }
        final ChunkGenerator current = world.getGenerator();
        if (current == null || !isForward(current)) {
            return ForwardState.UNKNOWN;
        }
        String genName = current.getClass().getName();
        if (!ACCESSORS.containsKey(genName)) {
            return ForwardState.FAILED;
        }
        final ChunkGenerator generator = builder.apply(world);
        final Accessor access = ACCESSORS.get(genName);
        access.invoke(current, "set", generator);
        final List<BlockPopulator> list = generator.getDefaultPopulators(world);
        access.invoke(current, "populators",
            (Object) (list == null ? new BlockPopulator[0] : list.stream().filter(obj -> obj != null).toArray(BlockPopulator[]::new)));
        return ForwardState.FINE;
    }

    public static void clear() {
        LIST.clear();
    }

}
