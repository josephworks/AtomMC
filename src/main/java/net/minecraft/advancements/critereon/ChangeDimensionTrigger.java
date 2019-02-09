package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;

public class ChangeDimensionTrigger implements ICriterionTrigger<ChangeDimensionTrigger.Instance>
{
    private static final ResourceLocation ID = new ResourceLocation("changed_dimension");
    private final Map<PlayerAdvancements, Listeners> listeners = Maps.<PlayerAdvancements, Listeners>newHashMap();

    public ResourceLocation getId()
    {
        return ID;
    }

    public void addListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<Instance> listener)
    {
        Listeners changedimensiontrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (changedimensiontrigger$listeners == null)
        {
            changedimensiontrigger$listeners = new Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, changedimensiontrigger$listeners);
        }

        changedimensiontrigger$listeners.add(listener);
    }

    public void removeListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<Instance> listener)
    {
        Listeners changedimensiontrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (changedimensiontrigger$listeners != null)
        {
            changedimensiontrigger$listeners.remove(listener);

            if (changedimensiontrigger$listeners.isEmpty())
            {
                this.listeners.remove(playerAdvancementsIn);
            }
        }
    }

    public void removeAllListeners(PlayerAdvancements playerAdvancementsIn)
    {
        this.listeners.remove(playerAdvancementsIn);
    }

    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context)
    {
        DimensionType dimensiontype = json.has("from") ? DimensionType.byName(JsonUtils.getString(json, "from")) : null;
        DimensionType dimensiontype1 = json.has("to") ? DimensionType.byName(JsonUtils.getString(json, "to")) : null;
        return new Instance(dimensiontype, dimensiontype1);
    }

    public void trigger(EntityPlayerMP player, DimensionType from, DimensionType to)
    {
        Listeners changedimensiontrigger$listeners = this.listeners.get(player.getAdvancements());

        if (changedimensiontrigger$listeners != null)
        {
            changedimensiontrigger$listeners.trigger(from, to);
        }
    }

    public static class Instance extends AbstractCriterionInstance
        {
            @Nullable
            private final DimensionType from;
            @Nullable
            private final DimensionType to;

            public Instance(@Nullable DimensionType from, @Nullable DimensionType to)
            {
                super(ChangeDimensionTrigger.ID);
                this.from = from;
                this.to = to;
            }

            public boolean test(DimensionType from, DimensionType to)
            {
                if (this.from != null && this.from != from)
                {
                    return false;
                }
                else
                {
                    return this.to == null || this.to == to;
                }
            }
        }

    static class Listeners
        {
            private final PlayerAdvancements playerAdvancements;
            private final Set<ICriterionTrigger.Listener<Instance>> listeners = Sets.<ICriterionTrigger.Listener<Instance>>newHashSet();

            public Listeners(PlayerAdvancements playerAdvancementsIn)
            {
                this.playerAdvancements = playerAdvancementsIn;
            }

            public boolean isEmpty()
            {
                return this.listeners.isEmpty();
            }

            public void add(ICriterionTrigger.Listener<Instance> listener)
            {
                this.listeners.add(listener);
            }

            public void remove(ICriterionTrigger.Listener<Instance> listener)
            {
                this.listeners.remove(listener);
            }

            public void trigger(DimensionType from, DimensionType to)
            {
                List<ICriterionTrigger.Listener<Instance>> list = null;

                for (ICriterionTrigger.Listener<Instance> listener : this.listeners)
                {
                    if (((Instance)listener.getCriterionInstance()).test(from, to))
                    {
                        if (list == null)
                        {
                            list = Lists.<ICriterionTrigger.Listener<Instance>>newArrayList();
                        }

                        list.add(listener);
                    }
                }

                if (list != null)
                {
                    for (ICriterionTrigger.Listener<Instance> listener1 : list)
                    {
                        listener1.grantCriterion(this.playerAdvancements);
                    }
                }
            }
        }
}