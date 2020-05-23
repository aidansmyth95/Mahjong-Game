package mahjong_package;

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

        super("bonus", bonus_ID, bonus_rank, bonus_type);

        // assert valid
        isValid(bonus_type, bonus_rank, bonus_ID);

        super.descriptor = describeBonus();

    }


    int getRank()
    {
        return super.rank;
    }


    int getType()
    {
        return super.type;
    }

    //TODO: boolean never used? Why?
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

        System.out.printf("Warning, incorrect ID (%d), type (%d) or rank (%d) for bonus tile\n", ID, type, rank);
        return false;
    }


    private String describeBonus() {

        String type = "";
        String rank = "";
        String total;

        switch(super.type) {
            case SEASONS:
                type = "Seasons";
                // another switch here
                switch(super.rank) {
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
                        total = "Warning, " + super.rank + " rank of bonus does not exist!";
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
                        total = "Warning, " + super.rank + " rank of bonus does not exist!";
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

}
