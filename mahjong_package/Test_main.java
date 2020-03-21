package mahjong_package;

public class Test_main {
/*
	public static void main(String[] args) {
		
		int n = 60;
		
		// Create Tiles structure
		Tiles tiles = new Tiles();
		
		// Shuffle and start dealing until empty
		System.out.println("Shuffling tiles\n");
		tiles.shuffleTiles();
		
		// reveal n tiles
		int i = 0;
		do {
			tiles.revealTile();
			i++;
			} while (i<n && tiles.win()!=true);
		
		tiles.listTiles(tiles.uncovered_tiles);
		
		// check for quadruples
		tiles.quadMatch();
		
		// check for triples
		tiles.tripleMatch();
		
		// check for pairs
		tiles.doubleMatch();

		//TODO: function to print only uncovered tiles
		//TODO: function to print last uncovered tile
		
		System.out.println("End of tests\n");
	}
	*/
}

//Fixes: break was missing in constructors for H and B, tmp_list is new list and not pointing to real list