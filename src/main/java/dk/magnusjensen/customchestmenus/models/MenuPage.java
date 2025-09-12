package dk.magnusjensen.customchestmenus.models;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Represents a single page within a custom menu.
 */
public record MenuPage(
    String title,
    List<MenuItem> items
) {
    public static final Codec<MenuPage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("title").forGetter(MenuPage::title),
        MenuItem.CODEC.listOf().fieldOf("items").forGetter(MenuPage::items)
    ).apply(instance, MenuPage::new));

    public Component titleAsComponent() {
        return Component.literal(this.title);
    }
}
