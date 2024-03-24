package org.polyfrost.vanillahud.mixin;

import cc.polyfrost.oneconfig.gui.elements.ModCard;
import cc.polyfrost.oneconfig.gui.pages.ModsPage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.ArrayList;

@Mixin(ModsPage.class)
public interface ModsPageAccessor {

    @Accessor
    ArrayList<ModCard> getModCards();

    @Accessor
    void setSize(int size);

}
