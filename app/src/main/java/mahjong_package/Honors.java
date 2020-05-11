package mahjong_package;

public class Honors extends Tile {

    // values
    private final int rank;
    private final int type;

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
        super(honor_ID);

        // assert valid
        isValid(honor_type, honor_rank, honor_ID);

        this.rank = honor_rank;
        this.type = honor_type;
        super.descriptor = describeHonor();
    }

    int getRank()
    {
        return this.rank;
    }


    int getType()
    {
        return this.type;
    }


    private static boolean isValid(int type, int rank, int ID) {

        if (ID >= 0 && ID < 4) {
            if (type == WIND) {
                return true;
            } else if (type == DRAGON) {
                return true;
            }
        }

        System.out.println("Warning, incorrect ID, type or rank for honor tile");
        return false;
    }

    private String describeHonor() {

        String type = "";
        String rank = "";
        String total;

        switch(this.type) {
            case WIND:
                type = "Wind";
                // another switch here
                switch(this.rank) {
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
                        total = "Warning, this rank of honor does not exist!";
                        System.out.println(total);
                }
                break;
            case DRAGON:
                type = "Dragon";
                // another switch
                switch(this.rank) {
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
                        total = "Warning, this rank of honor does not exist!";
                        System.out.println(total);
                }
                break;
            default:
                total = "Warning, this type of honor does not exist!";
                System.out.println(total);
        }

        total = type + " " + rank;
        return total;
    }

    public boolean isMatch(Honors cmp) {
        return (cmp.rank == this.rank && cmp.type == this.type);
    }
}
