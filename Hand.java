import java.util.ArrayList;

public final class Hand {
    private ArrayList<Card> cards;
    private int hardValue;
    private boolean hasAce;

    public Hand() {
        cards = new ArrayList<Card>();
        hardValue = 0;
        hasAce = false;
    }

    public void addCard(Card card) {
        cards.add(card);
        if (card.isAce()) {
            hasAce = true;
        }
        // Note: getValue() returns 1 for an ace.
        hardValue += card.getValue();
    }

    public int getHardValue() {
        return hardValue;
    }

    public int getSoftValue() {
        int softValue = hardValue + 10;
        if (hasAce && softValue <= 21)
            return softValue;
        else
            return hardValue;
    }

    public boolean isBlackjack() {
        return cards.size() == 2 && getSoftValue() == 21;
    }

    public boolean isBusted() {
        return hardValue > 21;
    }

    public String toString() {
        return showHand();
    }

    public String showHand() {
        String str = "[ ";
        for (Card c : cards) {
            str += c.toString() + " ";
        }
        str += "]";
        return str;
    }

    public String showUpCard() {
        // Needed to show dealer's starting hand.
        assert cards.size() == 2;
        Card upCard = cards.get(0);
        return "[ " + upCard.toString() + " XX" + " ]";
    }
}
