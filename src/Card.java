import java.util.Objects;

public final class Card {
    private char rank;
    private char suit;

    public Card(char rank, char suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public int getValue() {
        if (isAce()) {
            return 1;
        }
        else if (isFaceCard()) {
            return 10;
        }
        else {
            return rank - '0';
        }
    }

    public boolean isAce() {
        return rank == 'A';
    }

    private boolean isFaceCard() {
        return rank == 'T' || rank == 'J' || rank == 'Q' || rank == 'K';
    }

    public boolean equals(Object other) {
        if (other instanceof Card) {
            return rank == ((Card)other).rank && suit == ((Card)other).suit;
        }
        else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(rank, suit);
    }

    public String toString() {
        return rank + "" + suit;
    }
}
