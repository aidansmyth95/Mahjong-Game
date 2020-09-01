package mahjong_package;

import android.util.Log;

public class Bonus extends Tile {

    // Types of bonus
    private final static int SEASONS = 1;
    private final static int FLOWERS = 2;

    // Ranks for seasons
    private final static int SPRING = 1;
    private final static int SUMMER = 2;
    private final static int AUTUMN = 3;
    private final static int WINTER = 4;

    // Ranks for flowers
    private final static int PLUM = 1;
    private final static int ORCHID = 2;
    private final static int CHRYSANTHEMUM = 3;
    private final static int BAMBOO = 4;

    Bonus(int bonus_type, int bonus_rank, int bonus_ID) {
        super("Bonus", bonus_type, bonus_rank, bonus_ID);
        // assert valid
        if (!isValid(bonus_type, bonus_rank, bonus_ID)) {
            Log.e("Bonus", "Bonus is not valid");
        }
        super.setDescriptor(describeBonus());
    }

    Bonus() {
        super();
        super.setChildClass("Bonus");
    }

    public int getRank() { return super.getRank(); }
    public int getType() { return super.getType(); }
    public int getID() { return super.getID(); }
    public String getDescriptor() { return super.getDescriptor(); }
    public void setRank(int rank) { super.setRank(rank); }
    public void setType(int type) { super.setType(type); }
    public void setID(int id) { super.setID(id); }
    public void setDescriptor(String descriptor) { super.setDescriptor(descriptor); }

    private static boolean isValid(int type, int rank, int ID) {
        if (type == SEASONS) {
            if (ID < 4 && ID >= 0) {
                return true;
            }
        } else if (type == FLOWERS) {
            if (ID < 2 && ID >= 0) {
                return true;
            }
        }
        Log.e("Suits", "Warning, incorrect ID, type or rank for bonus tile\n");
        return false;
    }

    private String describeBonus() {

        String type = "";
        String rank = "";
        String total;

        switch(super.getType()) {
            case SEASONS:
                type = "Seasons";
                // another switch here
                switch(super.getRank()) {
                    case SPRING:
                        rank = "Spring";
                        break;
                    case SUMMER:
                        rank = "Summer";
                        break;
                    case AUTUMN:
                        rank = "Autumn";
                        break;
                    case WINTER:
                        rank = "Winter";
                        break;
                    default:
                        total = "Warning, " + super.getRank() + " rank of bonus does not exist!";
                        Log.e("Bonus", total);
                }
                break;
            case FLOWERS:
                type = "Flowers";
                // another switch
                switch(super.getRank()) {
                    case PLUM:
                        rank = "Plum";
                        break;
                    case ORCHID:
                        rank = "Orchid";
                        break;
                    case CHRYSANTHEMUM:
                        rank = "Chrysanthemum";
                        break;
                    case BAMBOO:
                        rank = "Bamboo";
                        break;
                    default:
                        total = "Warning, " + super.getRank() + " rank of bonus does not exist!";
                        Log.e("Bonus", total);
                }
                break;
            default:
                total = "Warning, " + super.getType() + " type of bonus does not exist!";
                Log.e("Bonus", total);
        }

        total = type + " " + rank;
        return total;
    }

}
