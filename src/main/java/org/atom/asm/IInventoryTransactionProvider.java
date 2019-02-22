package org.atom.asm;

import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;

import java.util.List;

/**
 * This interface is a kind of bridge that helps BukkitAPI to acquire viewers list in all inventories.
 * As it is implemented through ASM class transformation, this interface provides access to all inventories, even if
 * they are Forge inventories.
 *
 * @see org.atom.asm.transformers.ModInventoryTransformer
 */

public interface IInventoryTransactionProvider {

    void onOpen(CraftHumanEntity who);

    void onClose(CraftHumanEntity who);

    List<HumanEntity> getViewers();
}
