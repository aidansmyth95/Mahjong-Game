package mahjong_package;


public class Tile {
    // Hidden
    public int hidden;
    public int ID;
    public int rank;
    public int type;
    public String descriptor;

    // Constructor for tile
    public Tile(int ID) {
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
    }

    public String describeTile() {
        return this.descriptor;
    }
}
