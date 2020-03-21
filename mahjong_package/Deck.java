package mahjong_package;


public class Deck {

    int draw_index;
    int n = 52;
    // ArrayList?
    Card[] cards = new Card[n];

    public Deck(){

        int c=0;

        // for all suits
        for (int i=1; i<5; i++){
            // for all rank
            for (int j=1; j<14; j++){
                // initialize card
                this.cards[c] = new Card(i,j);
                c++;
            }
        }

        this.draw_index = 0;
    }


    public void shuffleDeck(){
        // shuffle all cards not yet drawn
        for (int i = this.draw_index; i < n; i++) {
            int r = i + (int) (Math.random() * (n-i));
            Card temp = cards[r];
            cards[r] = cards[i];
            cards[i] = temp;
        }
        return;
    }

    public void restartDeck(){
        this.draw_index = 0;
        shuffleDeck();
        return;
    }

    public Card drawCard(){
        // check to see if draw from deck is possible
        if (this.draw_index == this.n)
        {
            System.out.println("Warning: no more cards to draw. Auto resetting deck\n");
            restartDeck();
        }

        // Draw card and increment draw index
        Card r = this.cards[this.draw_index];
        // increment draw index
        this.draw_index++;

        System.out.println("Card drawn: " + r.rankToString(r.getRank())
                + " of " + r.suitToString(r.getSuit()) + ".\n");
        return r;
    }

    public String deckStatus(){
        String r = "Number of cards left is " + (this.n - this.draw_index) + ".\n";
        return r;
    }
}
