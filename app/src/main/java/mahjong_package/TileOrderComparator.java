package mahjong_package;
import java.util.Comparator;


public class TileOrderComparator implements Comparator<Tile> {
    @Override
    public int compare(Tile s1, Tile s2) {
        int c;
        // sort by child class first
        c = s1.child_class.compareTo(s2.child_class);
        // sort by type next
        if (c == 0) {
            c = s1.type - s2.type;
            // sort by rank next
            if (c == 0) {
                c = s1.rank - s2.rank;
                // finally sort by tile ID
                if (c==0) {
                    c = s1.ID - s2.ID;
                }
            }
        }
        return c;
    }
}
