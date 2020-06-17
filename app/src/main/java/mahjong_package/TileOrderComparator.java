package mahjong_package;
import java.util.Comparator;


public class TileOrderComparator implements Comparator<Tile> {
    @Override
    public int compare(Tile s1, Tile s2) {
        int c;
        // sort by child class first
        c = s1.getChildClass().compareTo(s2.getChildClass());
        // sort by type next
        if (c == 0) {
            c = s1.getType() - s2.getType();
            // sort by rank next
            if (c == 0) {
                c = s1.getRank() - s2.getRank();
                // finally sort by tile ID
                if (c==0) {
                    c = s1.getID() - s2.getID();
                }
            }
        }
        return c;
    }
}
