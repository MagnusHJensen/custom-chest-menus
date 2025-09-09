package dk.magnusjensen.customchestmenus.models;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dk.magnusjensen.customchestmenus.models.actions.MenuAction;
import dk.magnusjensen.customchestmenus.models.actions.NoopAction;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

/**
 * Represents an item definition with a custom menu.
 */
public record MenuItem(
    int slot,
    ResourceLocation item,
    String name,
    Optional<List<String>> lore,
    MenuAction action
) {
    public static final Codec<MenuItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("slot").forGetter(MenuItem::slot),
        ResourceLocation.CODEC.fieldOf("item").forGetter(MenuItem::item),
        Codec.STRING.fieldOf("name").forGetter(MenuItem::name),
        Codec.STRING.listOf().optionalFieldOf("lore").forGetter(MenuItem::lore),
        MenuAction.CODEC.optionalFieldOf("action", new NoopAction()).forGetter(MenuItem::action)
    ).apply(instance, MenuItem::new));
}
