package mahjong_package;


import android.util.Log;

public class Suits extends Tile {

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
        super("Suits", suit_type, suit_rank, suit_ID);
        // assert validity
        isValid(suit_type, suit_rank, suit_ID);
        super.setDescriptor(describeSuit());
    }

    public Suits() {
        super();
        super.setChildClass("Suits");
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

        switch(super.getType()) {
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
                total = "Warning, " + super.getType() + " type of suits does not exist!";
                System.out.println(total);
                break;
        }

        switch (super.getRank()) {
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
                total = "Warning, rank of " + super.getRank() + " does not exist!";
                System.out.println(total);
                break;
        }

        total = type + " " + rank;
        return total;
    }

}
