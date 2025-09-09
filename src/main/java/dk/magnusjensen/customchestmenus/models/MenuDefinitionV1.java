package dk.magnusjensen.customchestmenus.models;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record MenuDefinitionV1(int formatVersion,
                               String id,
                               String name,
                               MenuSize size,
                               Optional<MenuItem> filler,
                               List<MenuPage> pages) {

    public static final Codec<MenuDefinitionV1> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("format_version").forGetter(v -> v.formatVersion),
        Codec.STRING.fieldOf("id").forGetter(v -> v.id),
        Codec.STRING.fieldOf("name").forGetter(v -> v.name),
        MenuSize.CODEC.fieldOf("size").forGetter(v -> v.size),
        MenuItem.CODEC.optionalFieldOf("filler").forGetter(v -> v.filler),
        MenuPage.CODEC.listOf().fieldOf("pages").forGetter(v -> v.pages)
    ).apply(instance, MenuDefinitionV1::new));

    public MenuDefinition toMenuDefinition() {
        return new MenuDefinition(id, name, size, filler, pages);
    }
}
