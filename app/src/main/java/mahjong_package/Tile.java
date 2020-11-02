package mahjong_package;


public class Tile {
    // Hidden
    private int ID;
    private int rank;
    private int type;
    private String descriptor;
    private String childClass;


    // Constructor for tile (for one of kind tiles with no need of ID)
    public Tile() {
        this.ID = -1;
        this.descriptor = "No tile";
        this.rank = -1;
        this.type = -1;
        this.childClass = "None";
    }

    public Tile(String child_class, int type, int rank, int ID) {
        this.childClass = child_class;
        this.ID = ID;
        this.rank = rank;
        this.type = type;
        this.descriptor = "No tile";
    }

    public Tile(int type, int rank, int ID) {
        this.ID = ID;
        this.rank = rank;
        this.type = type;
        this.descriptor = "No tile";
        this.childClass = "None";
    }

    public Boolean checkRealTile() {
        // return true if not  & if not
        return (!this.getDescriptor().equals("No tile")) && (!this.getChildClass().equals("None"));
    }

    public void setID(int ID) { this.ID = ID; }
    public void setRank(int rank) { this.rank = rank; }
    public void setType(int type) { this.type = type; }
    public void setDescriptor(String descriptor) { this.descriptor = descriptor; }
    public void setChildClass(String c) { this.childClass = c; }
    public int getID() { return this.ID; }
    public int getRank() { return this.rank; }
    public int getType() { return this.type; }
    public String getDescriptor() { return this.descriptor; }
    public String getChildClass() { return this.childClass; }
}
