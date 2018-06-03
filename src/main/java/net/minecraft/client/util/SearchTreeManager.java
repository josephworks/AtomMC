package net.minecraft.client.util;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SearchTreeManager implements IResourceManagerReloadListener
{
    public static final Key<ItemStack> ITEMS = new Key<ItemStack>();
    public static final Key<RecipeList> RECIPES = new Key<RecipeList>();
    private final Map < Key<?>, SearchTree<? >> trees = Maps. < Key<?>, SearchTree<? >> newHashMap();

    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        for (SearchTree<?> searchtree : this.trees.values())
        {
            searchtree.recalculate();
        }
    }

    public <T> void register(Key<T> key, SearchTree<T> searchTreeIn)
    {
        this.trees.put(key, searchTreeIn);
    }

    public <T> ISearchTree<T> get(Key<T> key)
    {
        return (ISearchTree)this.trees.get(key);
    }

    @SideOnly(Side.CLIENT)
    public static class Key<T>
        {
        }
}