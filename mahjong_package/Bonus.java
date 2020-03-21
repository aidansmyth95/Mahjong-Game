package mahjong_package;

public class Bonus extends Tile {

    // rank and type
    private final int rank;
    private final int type;

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

    public Bonus(int bonus_type, int bonus_rank, int bonus_ID) {
        super(bonus_ID);

        // assert valid
        isValid(bonus_type, bonus_rank, bonus_ID);

        this.rank = bonus_rank;
        this.type = bonus_type;

        super.descriptor = describeBonus();
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

    //TODO: booolean for hidden?
    public int getVisibility() { return super.hidden; }

    public static boolean isValid(int type, int rank, int ID) {

        if (type == SEASONS) {
            if (ID < 4 && ID >= 0) {
                return type >= SPRING && type <= WINTER;
            }
        } else if (type == FLOWERS) {
            if (ID < 2 && ID >= 0) {
                return type >= PLUM && type <= BAMBOO;
            }
        }

        System.out.println("Warning, incorrect ID, type or rank for honor tile");
        return false;
    }


    public String describeBonus() {

        String type = new String();
        String rank = new String();
        String total = new String();

        switch(this.type) {
            case SEASONS:
                type = "Seasons";
                // another switch here
                switch(this.rank) {
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
                        total = "Warning, " + this.rank + " rank of bonus does not exist!";
                        System.out.println(total);
                }
                break;
            case FLOWERS:
                type = "Flowers";
                // another switch
                switch(this.rank) {
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
                        total = "Warning, " + this.rank + " rank of bonus does not exist!";
                        System.out.println(total);
                }
                break;
            default:
                total = "Warning, this type of bonus does not exist!";
                System.out.println(total);
        }

        total = type + " " + rank;
        return total;
    }

    public boolean isMatch(Bonus cmp) {
        return (cmp.rank == this.rank && cmp.type == this.type);
    }

}
