package mahjong_package;

import android.util.Log;

public class Honors extends Tile {

    // enums for wind and dragon
    private final static int WIND = 1;
    private final static int DRAGON = 2;

    // wind values
    private final static int NORTH = 1;
    private final static int EAST = 2;
    private final static int SOUTH = 3;
    private final static int WEST = 4;

    // dragons
    private final static int RED = 1;
    private final static int GREEN = 2;
    private final static int WHITE = 3;

    Honors(int honor_type, int honor_rank, int honor_ID)
    {
        super("Honors", honor_type, honor_rank, honor_rank);
        // assert valid
        if (!isValid(honor_type, honor_rank, honor_ID)) {
            Log.e("Honors", "Honors is not valid");
        }
        super.setDescriptor(describeHonor());
    }

    Honors() {
        super();
        super.setChildClass("Honors");
    }

    public int getRank() { return super.getRank(); }
    public int getType()
    {
        return super.getType();
    }
    public int getID() { return super.getID(); }
    public String getDescriptor() { return super.getDescriptor(); }
    public void setRank(int rank) { super.setRank(rank); }
    public void setType(int type) { super.setType(type); }
    public void setID(int id) { super.setID(id); }
    public void setDescriptor(String descriptor) { super.setDescriptor(descriptor); }

    private static boolean isValid(int type, int rank, int ID) {
        if (ID >= 0 && ID < 4) {
            if (type == WIND) {
                return true;
            } else if (type == DRAGON) {
                return true;
            }
        }
        Log.e("Honors", "Warning, incorrect ID, type or rank for honor tile");
        return false;
    }

    private String describeHonor() {
        String type = "";
        String rank = "";
        String total;
        switch(super.getType()) {
            case WIND:
                type = "Wind";
                // another switch here
                switch(super.getRank()) {
                    case NORTH:
                        rank = "North";
                        break;
                    case EAST:
                        rank = "East";
                        break;
                    case SOUTH:
                        rank = "South";
                        break;
                    case WEST:
                        rank = "West";
                        break;
                    default:
                        total = "Warning, " + super.getRank() + " rank of honor does not exist!";
                        Log.e("Honors", total);
                }
                break;
            case DRAGON:
                type = "Dragon";
                // another switch
                switch(super.getRank()) {
                    case RED:
                        rank = "Red";
                        break;
                    case GREEN:
                        rank = "Green";
                        break;
                    case WHITE:
                        rank = "White";
                        break;
                    default:
                        total = "Warning, " + super.getRank() + " rank of honor does not exist!";
                        Log.e("Honors", total);
                }
                break;
            default:
                total = "Warning, " + super.getType() + " type of honor does not exist!";
                Log.e("Honors", total);
        }
        total = type + " " + rank;
        return total;
    }

}
