package mahjong_package;


public class Tile {
    // Hidden
    private int hidden;
    private int ID;
    private int rank;
    private int type;
    private String descriptor;
    //TODO: use this instead of isMember checks throuhout package
    private String childClass;

    
    // Constructor for tile
    Tile (int ID) {
        this.hidden = 1;
        this.ID = ID;
        this.descriptor = "No tile";
        this.rank = -1;
        this.type = -1;
    }
    
    
    // Constructor for tile (for one of kind tiles with no need of ID)
    public Tile() {
        this.hidden = 1;
        this.ID = -1;
        this.descriptor = "No tile";
        this.rank = -1;
        this.type = -1;
        this.childClass = "None";
    }

    public Tile(String child_class, int ID, int rank, int type) {
        this.childClass = child_class;
        this.ID = ID;
        this.rank = rank;
        this.type = type;
        this.descriptor = "No tile";
        this.childClass = "None";
    }

    public void setHidden(int hidden) {this.hidden = hidden; }
    public void setID(int ID) { this.ID = ID; }
    public void setRank(int rank) { this.rank = rank; }
    public void setType(int type) { this.type = type; }
    public void setDescriptor(String descriptor) { this.descriptor = descriptor; }
    public void setChildClass(String c) { this.childClass = c; }
    public int getHidden() { return this.hidden; }
    public int getID() { return this.ID; }
    public int getRank() { return this.rank; }
    public int getType() { return this.rank; }
    public String getDescriptor() { return this.descriptor; }
    public String getChildClass() { return this.childClass; }
}
