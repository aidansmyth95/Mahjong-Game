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

    public Honors(int honor_type, int honor_rank, int honor_ID)
    {
        super(honor_ID);

        // assert valid
        isValid(honor_type, honor_rank, honor_ID);

        this.rank = honor_rank;
        this.type = honor_type;
        super.descriptor = describeHonor();
    }

    public int getRank()
    {
        return this.rank;
    }

    public int getType()
    {
        return this.type;
    }

    public int getID() { return super.ID; }

    public int getVisibility() { return super.hidden; }

    public static boolean isValid(int type, int rank, int ID) {

        if (ID >= 0 && ID < 4) {
            if (type == WIND) {
                return type >= NORTH && type <= WEST;
            } else if (type == DRAGON) {
                return type >= RED && type <= WHITE;
            }
        }

        System.out.println("Warning, incorrect ID, type or rank for honor tile");
        return false;
    }

    public String describeHonor() {

        String type = new String();
        String rank = new String();
        String total = new String();

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
