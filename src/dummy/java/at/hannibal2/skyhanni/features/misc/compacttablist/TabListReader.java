package at.hannibal2.skyhanni.features.misc.compacttablist;

import java.util.ArrayList;
import java.util.List;

public class TabListReader {
    public static final TabListReader INSTANCE = new TabListReader();

    public final List<RenderColumn> getRenderColumns() {
        return new ArrayList<>();
    }
}
