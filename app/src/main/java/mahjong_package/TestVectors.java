package mahjong_package;

import android.util.Log;

import java.util.ArrayList;

public class TestVectors {

    // Game object
    private Game testGame = new Game(1);
    private final int numTestVectors = 13;
    private final String TAG = "TestVectors";

    // ArrayList of test vector hands
    private ArrayList<ArrayList<Tile>> tilesList = new ArrayList<ArrayList<Tile>>();
    private ArrayList<String> valueList = new ArrayList<>();  // string value is pong, kong etc

    public TestVectors() {
        testGame.addPlayer("dummy", "dummy");
        addTestVectors();
    }

    private void addTestVectors() {
        // in a for loop, add the hands to the tilesList, and add the string expected results
        for (int i=0; i<numTestVectors; i++) {
            tilesList.add(createTestVectorHand(i));
            valueList.add(createTestVectorHandResult(i));
        }
    }

    private ArrayList<Boolean> getTestVectorResults() {
        ArrayList<Boolean> results = new ArrayList<>();
        for (int i=0; i<numTestVectors; i++) {
            results.add(false);
        }
        // for loop through arraylists.
        if (tilesList.size() != valueList.size() || tilesList.size() != numTestVectors) {
            Log.e(TAG, TAG+": test vector sizes not the same: " + tilesList.size() + " " + valueList.size()+ " " + numTestVectors);
            return results;
        } else {
            for (int i=0; i<numTestVectors; i++) {
                Player p = testGame.getPlayer().get(0);
                // clear hand
                p.clearHand();
                // add tiles to hand
                ArrayList<Tile> all_tiles = tilesList.get(i);
                Tile disc_tile = all_tiles.remove(all_tiles.size()-1);
                p.createHand(all_tiles);
                boolean bool_val;
                // check hand for the corresponding string's desired value
                switch (valueList.get(i)) {
                    case "none":
                        bool_val = !(p.checkHandChow(disc_tile) || p.checkHandPong(disc_tile) || p.checkHandKong(disc_tile) || p.checkHandMahjong(disc_tile));
                        break;
                    case "chow":
                        bool_val = p.checkHandChow(disc_tile);
                        break;
                    case "pong":
                        bool_val = p.checkHandPong(disc_tile);
                        break;
                    case "kong":
                        bool_val = p.checkHandKong(disc_tile);
                        break;
                    case "mahjong":
                        bool_val = p.checkHandMahjong(disc_tile);
                        break;
                    default:
                        bool_val = false;
                        break;
                }
                Log.d(TAG, TAG+": Test vector " + i + " pass? - " + bool_val);
                if (!bool_val) {
                    System.out.println("Failed test " + i);
                }
                results.set(i, bool_val);
            }
        }
        // PASS if all are true
        return results;
    }

    public Boolean testVectorPass() {
        ArrayList<Boolean> results = this.getTestVectorResults();
        // return false if there is a false
        return !results.contains(false);
    }


    /* Test vector values */
    private String createTestVectorHandResult(int test_num) {
        switch (test_num) {
            case 0:
                return "chow";
            case 1:
                return "chow";
            case 2:
                return "none";
            case 3:
                return "pong";
            case 4:
                return "pong";
            case 5:
                return "pong";
            case 6:
                return "none";
            case 7:
                return "kong";
            case 8:
                return "kong";
            case 9:
                return "none";
            case 10:
                return "none";
            case 11:
                return "mahjong";
            case 12:
                return "mahjong";
            default:
                return "none";
        }
    }

    //TODO: a more optimized structure for this - a dict, map. etc...
	/*

		Test vectors - Important that last tile is part of the pong/kong/chow/mahjong

	 */

    private ArrayList<Tile> createTestVectorHand(int test_num) {
        ArrayList<Tile> test_vector = new ArrayList<>();
        switch (test_num) {
            // Chow
            case 0:
                test_vector.add(0,new Bonus(1,1,1));
                test_vector.add(1, new Bonus(1,2,2));
                test_vector.add(2, new Suits(1,1,1)); // Bamboo 1 #1
                test_vector.add(3, new Suits(2,1,1));
                test_vector.add(4, new Suits(3,1,1));
                test_vector.add(5, new Suits(1,7,1));
                test_vector.add(6, new Suits(1,2,1)); // Bamboo 2 #1
                test_vector.add(7, new Suits(2,3,1));
                test_vector.add(8, new Suits(3,3,1));
                test_vector.add(9, new Suits(1,5,1));
                test_vector.add(10, new Suits(1,9,1));
                test_vector.add(11, new Suits(2,9,1));
                test_vector.add(12, new Suits(3,9,1));
                test_vector.add(13, new Suits(1,3,1)); // Bamboo 3 #1
                break;
            // Chow
            case 1:
                test_vector.add(0, new Bonus(1,1,1));
                test_vector.add(1, new Bonus(1,2,2));
                test_vector.add(2, new Suits(3,9,1));
                test_vector.add(3, new Suits(2,1,1));
                test_vector.add(4, new Suits(3,1,0));
                test_vector.add(5, new Suits(1,7,1));
                test_vector.add(6, new Suits(2,7,1)); // Dots 7 #1
                test_vector.add(7, new Suits(2,3,1));
                test_vector.add(8, new Suits(3,3,1));
                test_vector.add(9, new Suits(1,5,1));
                test_vector.add(10, new Suits(1,9,1));
                test_vector.add(11, new Suits(2,9,1));
                test_vector.add(12, new Suits(2,6,1)); // Dots 6 #1
                test_vector.add(13, new Suits(2,8,1)); // Dots 8 #1
                break;
            // None
            case 2:
                test_vector.add(0, new Bonus(1,1,1)); // Spring #1      //FC
                test_vector.add(1, new Bonus(1,1,2)); // Spring #2
                test_vector.add(2, new Bonus(1,1,3)); // Summer #3
                test_vector.add(3, new Bonus(1,1,0)); // Summer #0
                test_vector.add(4, new Suits(3,1,1));
                test_vector.add(5, new Suits(1,7,1));
                test_vector.add(6, new Suits(1,3,1));
                test_vector.add(7, new Suits(2,3,1));
                test_vector.add(8, new Suits(3,3,1));
                test_vector.add(9, new Suits(1,5,1));
                test_vector.add(10, new Suits(1,9,1));
                test_vector.add(11, new Suits(2,9,1));
                test_vector.add(12, new Suits(3,9,1));
                test_vector.add(13, new Suits(1,1,1));
                break;
            // Pong
            case 3:
                test_vector.add(0, new Bonus(1,1,1));
                test_vector.add(1, new Bonus(1,2,2));
                test_vector.add(2, new Suits(1,1,1)); // Bamboo 1 #1
                test_vector.add(3, new Suits(2,1,1));
                test_vector.add(4, new Suits(3,1,1));
                test_vector.add(5, new Suits(1,7,1));
                test_vector.add(6, new Suits(1,1,2)); // Bamboo 1 #2
                test_vector.add(7, new Suits(2,3,1));
                test_vector.add(8, new Suits(3,3,1));
                test_vector.add(9, new Suits(1,5,1));
                test_vector.add(10, new Suits(1,9,1));
                test_vector.add(11, new Suits(2,9,1));
                test_vector.add(12, new Suits(3,9,1));
                test_vector.add(13, new Suits(1,1,3)); // Bamboo 1 #3
                break;
            // Pong
            case 4:
                test_vector.add(0, new Bonus(1,1,1)); //Spring #1       //TP
                test_vector.add(1, new Bonus(1,1,2)); //Spring #2
                test_vector.add(2, new Suits(1,1,1));
                test_vector.add(3, new Suits(2,1,1));
                test_vector.add(4, new Suits(3,1,1));
                test_vector.add(5, new Suits(1,7,1));
                test_vector.add(6, new Suits(1,3,1));
                test_vector.add(7, new Suits(2,3,1));
                test_vector.add(8, new Suits(3,3,1));
                test_vector.add(9, new Suits(1,5,1));
                test_vector.add(10, new Suits(1,9,1));
                test_vector.add(11, new Suits(2,9,1));
                test_vector.add(12, new Suits(3,9,1));
                test_vector.add(13, new Bonus(1,1,3)); //Spring #3
                break;
            // Pong
            case 5:
                test_vector.add(0, new Bonus(1,1,1)); // Spring #1
                test_vector.add(1, new Bonus(1,1,2)); // Spring #2
                test_vector.add(2, new Bonus(1,1,3)); // Spring #3
                test_vector.add(3, new Suits(2,1,1));
                test_vector.add(4, new Suits(3,1,1));
                test_vector.add(5, new Suits(1,7,1));
                test_vector.add(6, new Suits(1,3,1));
                test_vector.add(7, new Suits(2,3,1));
                test_vector.add(8, new Suits(3,3,1));
                test_vector.add(9, new Suits(1,5,1));
                test_vector.add(10, new Suits(1,9,1));
                test_vector.add(11, new Suits(1,1,3)); // Bamboo 1 - #3
                test_vector.add(12, new Suits(1,1,2)); // Bamboo 1 - #2
                test_vector.add(13, new Suits(1,1,1)); // Bamboo 1 - #1
                break;
            // Chow
            case 6:
                test_vector.add(0, new Bonus(1,1,1));
                test_vector.add(1, new Bonus(1,2,2));
                test_vector.add(2, new Suits(1,1,1)); // Bamboo 1 #1
                test_vector.add(3, new Suits(2,1,1));
                test_vector.add(4, new Suits(3,1,1));
                test_vector.add(5, new Suits(1,7,1));
                test_vector.add(6, new Suits(1,2,1)); // Bamboo 2 #1
                test_vector.add(7, new Suits(2,3,1));
                test_vector.add(8, new Suits(3,3,1));
                test_vector.add(9, new Suits(1,5,1));
                test_vector.add(10, new Suits(1,9,1));
                test_vector.add(11, new Suits(2,9,1));
                test_vector.add(12, new Suits(1,3,1)); // Bamboo 3 #1
                test_vector.add(13, new Suits(3,9,1));
                break;
            // Kong
            case 7:
                test_vector.add(0, new Bonus(1,1,1)); //Spring
                test_vector.add(1, new Bonus(1,1,2)); //Spring
                test_vector.add(2, new Bonus(1,1,3)); //Spring
                test_vector.add(3, new Suits(2,1,1));
                test_vector.add(4, new Suits(3,1,1));
                test_vector.add(5, new Suits(1,7,1));
                test_vector.add(6, new Suits(1,3,1));
                test_vector.add(7, new Suits(2,3,1));
                test_vector.add(8, new Suits(3,3,1));
                test_vector.add(9, new Suits(1,5,1));
                test_vector.add(10, new Suits(1,9,1));
                test_vector.add(11, new Suits(2,9,1));
                test_vector.add(12, new Suits(3,9,1));
                test_vector.add(13, new Bonus(1,1,0)); //Spring
                break;
            // Kong
            case 8:
                test_vector.add(0, new Bonus(1,2,1)); //Summer
                test_vector.add(1, new Bonus(1,2,2)); //Summer
                test_vector.add(2, new Bonus(1,2,3)); //Summer
                test_vector.add(3, new Suits(3,9,2));
                test_vector.add(4, new Suits(3,1,1));
                test_vector.add(5, new Suits(1,7,1));
                test_vector.add(6, new Suits(1,3,1));
                test_vector.add(7, new Suits(2,3,1));
                test_vector.add(8, new Suits(3,3,1));
                test_vector.add(9, new Suits(1,5,1));
                test_vector.add(10, new Suits(1,9,1));
                test_vector.add(11, new Suits(2,9,1));
                test_vector.add(12, new Suits(3,9,1));
                test_vector.add(13, new Bonus(1,2,0)); //Summer
                break;
            // None
            case 9:
                test_vector.add(0, new Bonus(1,1,1));               //FK
                test_vector.add(1, new Bonus(1,2,2));
                test_vector.add(2, new Suits(1,1,1)); // Bamboo 1 #1
                test_vector.add(3, new Suits(1,1,2)); // Bamboo 1 #2
                test_vector.add(4, new Suits(1,1,0)); // Bamboo 1 #4
                test_vector.add(5, new Suits(1,7,1));
                test_vector.add(6, new Suits(1,2,1));
                test_vector.add(7, new Suits(2,3,1));
                test_vector.add(8, new Suits(3,3,1));
                test_vector.add(9, new Suits(1,5,1));
                test_vector.add(10, new Suits(1,9,1));
                test_vector.add(11, new Suits(2,9,1));
                test_vector.add(12, new Suits(1,1,3)); // Bamboo 1 #3
                test_vector.add(13, new Suits(3,1,1)); // last tile is not part of kong, so no kong
                break;
            // same as above, none
            case 10:
                test_vector.add(0, new Bonus(1,1,1)); //1
                test_vector.add(1, new Bonus(1,2,2));
                test_vector.add(2, new Suits(1,1,1));
                test_vector.add(3, new Bonus(1,1,2)); //2
                test_vector.add(4, new Bonus(1,1,0)); //0
                test_vector.add(5, new Suits(1,7,1));
                test_vector.add(6, new Suits(1,2,1));
                test_vector.add(7, new Suits(2,3,1));
                test_vector.add(8, new Suits(3,3,1));
                test_vector.add(9, new Suits(1,5,1));
                test_vector.add(10, new Suits(1,9,1));
                test_vector.add(11, new Suits(2,9,1));
                test_vector.add(12, new Bonus(1,1,3)); //3
                test_vector.add(13, new Suits(3,1,1));
                break;
            case 11:
                test_vector.add(0, new Bonus(1,1,1)); //Spring          //MJ
                test_vector.add(1, new Bonus(1,1,2)); //Spring
                test_vector.add(2, new Bonus(1,2,0)); //Summer
                test_vector.add(3, new Bonus(1,2,1)); //Summer
                test_vector.add(4, new Bonus(1,2,2)); //Summer
                test_vector.add(5, new Bonus(1,3,0)); //Autumn
                test_vector.add(6, new Bonus(1,3,1)); //Autumn
                test_vector.add(7, new Bonus(1,3,2)); //Autumn
                test_vector.add(8, new Suits(3,3,1)); //3
                test_vector.add(9, new Suits(3,4,1)); //4
                test_vector.add(10, new Suits(3,5,1)); //5
                test_vector.add(11, new Suits(2,7,1)); //7
                test_vector.add(12, new Bonus(1,1,0)); //Spring
                test_vector.add(13, new Suits(2,7,1)); //7
                break;
            case 12:
                test_vector.add(0, new Bonus(1,1,1)); //Spring
                test_vector.add(1, new Bonus(1,1,2)); //Spring
                test_vector.add(2, new Bonus(1,2,0)); //Summer
                test_vector.add(3, new Bonus(1,2,1)); //Summer
                test_vector.add(4, new Bonus(1,2,2)); //Summer
                test_vector.add(5, new Bonus(1,3,0)); //Autumn
                test_vector.add(6, new Bonus(1,3,1)); //Autumn
                test_vector.add(7, new Bonus(1,3,2)); //Autumn
                test_vector.add(8, new Suits(3,1,1)); //1
                test_vector.add(9, new Suits(3,2,1)); //2
                test_vector.add(10, new Suits(3,3,1)); //3
                test_vector.add(11, new Suits(2,7,1)); //7
                test_vector.add(12, new Bonus(1,1,0)); //Spring
                test_vector.add(13, new Suits(2,7,1)); //7
                break;
            default:
                createTestVectorHand(0);
                break;
        }
        return test_vector;
    }

}
