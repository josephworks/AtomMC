package org.bukkit;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;
import net.minecraft.world.WorldType;

public class WorldTypeTest {
    @Test
    public void testTypes() {
        for (WorldType type : WorldType.WORLD_TYPES) {
            if (type == null) continue;
            if (type == WorldType.DEBUG_ALL_BLOCK_STATES) continue; // Doesn't work anyway

            assertThat(type.getName() + " has no Bukkit world", org.bukkit.WorldType.getByName(type.getName()), is(not(nullValue())));
        }
    }
}
