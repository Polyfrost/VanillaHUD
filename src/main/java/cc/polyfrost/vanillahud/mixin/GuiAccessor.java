package cc.polyfrost.vanillahud.mixin;

import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Gui.class)
public interface GuiAccessor {
    @Accessor("zLevel")
    float getZLevel();

    @Accessor("zLevel")
    void setZLevel(float zLevel);
}
