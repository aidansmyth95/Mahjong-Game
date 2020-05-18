package mahjong_package;


public class Suits extends Tile {

    // rank and type
    private final int type;

    // Type of suits
    private final static int BAMBOO = 1;
    private final static int DOTS = 2;
    private final static int CHARS = 3;

    // rank numbers 1-9 inclusive
    private final static int ONE = 1;
    private final static int TWO = 2;
    private final static int THREE = 3;
    private final static int FOUR = 4;
    private final static int FIVE = 5;
    private final static int SIX = 6;
    private final static int SEVEN = 7;
    private final static int EIGHT = 8;
    private final static int NINE = 9;

    Suits(int suit_type, int suit_rank, int suit_ID) {

        super(suit_ID);

        // assert validity
        isValid(suit_type, suit_rank, ID);

        super.rank = suit_rank;
        super.type = suit_type;
        this.type = suit_type;
        super.descriptor = describeSuit();
    }


    int getRank()
    {
        return this.rank;
    }

    int getType()
    {
        return this.type;
    }

    public int getID() { return super.ID; }

    public int getVisibility() { return super.hidden; }

    private static boolean isValid(int type, int rank, int ID) {

        if (type == BAMBOO || type == DOTS || type == CHARS) {
            if (rank >= ONE && rank <= NINE) {
                if (ID >= 0 && ID < 4) {
                    return true;
                }
            }
        }

        System.out.printf("Warning, incorrect ID (%d), type (%d) or rank (%d) for bonus tile\n", ID, type, rank);
        return false;
    }


    private String describeSuit() {

        String type = "";
        String rank = "";
        String total;

        switch(this.type) {
            case BAMBOO:
                type = "Bamboo";
                break;
            case DOTS:
                type = "Dots";
                break;
            case CHARS:
                type = "Characters";
                break;
            default:
                total = "Warning, this type of suits does not exist!";
                System.out.println(total);
                break;
        }

        switch (this.rank) {
            case ONE:
                rank = "One";
                break;
            case TWO:
                rank = "Two";
                break;
            case THREE:
                rank = "Three";
                break;
            case FOUR:
                rank = "Four";
                break;
            case FIVE:
                rank = "Five";
                break;
            case SIX:
                rank = "Six";
                break;
            case SEVEN:
                rank = "Seven";
                break;
            case EIGHT:
                rank = "Eight";
                break;
            case NINE:
                rank = "Nine";
                break;
            default:
                total = "Warning, rank of " + rank + " does not exist!";
                System.out.println(total);
                break;
        }

        total = type + " " + rank;
        return total;
    }

    public boolean isMatch(Suits cmp) {
        return (cmp.rank == super.rank && cmp.type == this.type);
    }
}
