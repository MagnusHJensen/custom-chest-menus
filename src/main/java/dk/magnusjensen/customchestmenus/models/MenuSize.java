package dk.magnusjensen.customchestmenus.models;

import com.mojang.serialization.Codec;

import java.util.Locale;

/**
 * Enum describing the size of the menu based on chest size, either SINGLE (3 rows) or DOUBLE (6 rows).
 */
public enum MenuSize {
    SINGLE(27),
    DOUBLE(54);

    public static final Codec<MenuSize> CODEC = Codec.STRING.xmap(
        s -> MenuSize.valueOf(s.toUpperCase(Locale.ROOT)),
        MenuSize::name
    );

    private final int slots;
    MenuSize(int slots) {
        this.slots = slots;
    }

    public int getSlots() {
        return slots;
    }
}
