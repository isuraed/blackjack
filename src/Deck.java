import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public final class Deck {
    private static char[] ranks = {'2','3','4','5','6','7','8','9','T','J','Q','K','A'};
    private static char[] suits = {'c','d','h','s'};

    private ArrayList<Card> cards;
    private Random randomizer;
    private int topIndex;

    public Deck() {
        cards = new ArrayList<Card>(52);
        for (char r : ranks) {
            for (char s : suits) {
                Card c = new Card(r, s);
                cards.add(c);
            }
        }

        randomizer = new Random();
        topIndex = 0;
    }

    public void shuffle() {
        topIndex = 0;
        Collections.shuffle(cards, randomizer);
    }

    public Card dealNextCard() {
        Card topCard = cards.get(topIndex);
        topIndex++;
        return topCard;
    }
}
