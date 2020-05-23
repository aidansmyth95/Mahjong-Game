package mahjong_package;


class Tile {
    // Hidden
    int hidden;
    int ID;
    int rank;
    int type;
    String descriptor;
    //TODO: use this instead of isMember checks throuhout package
    String child_class;

    
    // Constructor for tile
    Tile(int ID) {
        this.hidden = 1;
        this.ID = ID;
        this.descriptor = "No tile";
        this.rank = -1;
        this.type = -1;
    }
    
    
    // Constructor for tile (for one of kind tiles with no need of ID)
    Tile() {
        this.hidden = 1;
        this.ID = -1;
        this.descriptor = "No tile";
        this.rank = -1;
        this.type = -1;
    }

    Tile(String child_class, int ID, int rank, int type) {
        this.child_class = child_class;
        this.ID = ID;
        this.rank = rank;
        this.type = type;
    }

}
