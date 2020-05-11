package mahjong_package;

import java.util.Comparator;

//TODO: verify this works
public class RankComparator implements Comparator<Tile> {
	   @Override
	    public int compare(Tile s1, Tile s2) {
	        return s1.rank - s2.rank;
	    }
}
