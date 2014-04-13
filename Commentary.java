import java.io.Console;

public final class Commentary {
    private static final int DEALING_PAUSE_TIME = 2000;
    private static final int RESULT_PAUSE_TIME = 1500;
    private Console console;

    public Commentary(Console console) {
        assert console != null;
        this.console = console;
    }

    public void printWelcome() {
        printLine("Let's play some blackjack...Good luck!");
        printEmptyLine();
    }

    public void printGoodbye() {
        printEmptyLine();
        printLine("Thank you for playing. Goodbye...");
        pauseForEffect(2000);
    }

    public void printDealing() {
        printLine("Dealing...");
        pauseForEffect(DEALING_PAUSE_TIME);
    }

    public void printDoublingDown() {
        printLine("Doubling down.");
        pauseForEffect(DEALING_PAUSE_TIME);
    }

    public void printHitting() {
        printLine("Taking a card...");
        pauseForEffect(DEALING_PAUSE_TIME);
    }

    public void printStaying() {
        printLine("Staying pat. Dealing dealer's hand...");
        pauseForEffect(DEALING_PAUSE_TIME);
    }

    public void printDealerStartingHand(Hand dealerHand) {
        printLine("Dealer: " + dealerHand.showUpCard());
    }

    public void printDealerHand(Hand dealerHand) {
        printLine("Dealer: " + dealerHand.showHand());
    }

    public void printPlayerHand(Hand playerHand) {
        printLine("Player: " + playerHand.showHand());
        // Empty line so it's easier to see the current hands.
        printEmptyLine();
    }

    public void printBlackjackPush() {
        pauseForEffect(RESULT_PAUSE_TIME);
        printLine("Dealer and player both have blackjack! Push.");
    }

    public void printPush(int handValue) {
        pauseForEffect(RESULT_PAUSE_TIME);
        console.printf("You and dealer both have %d. Push.\n", handValue);
        printEmptyLine();
        printEmptyLine();
    }

    public void printDealerBlackjack() {
        pauseForEffect(RESULT_PAUSE_TIME);
        printLine("Dealer has blackjack...Dealer wins.");
        printEmptyLine();
        printEmptyLine();
    }

    public void printDealerBusted() {
        pauseForEffect(RESULT_PAUSE_TIME);
        printLine("Dealer busts! You win.");
        printEmptyLine();
        printEmptyLine();
    }

    public void printDealerWins(int handValue) {
        pauseForEffect(RESULT_PAUSE_TIME);
        console.printf("Dealer wins with %d...\n", handValue);
        printEmptyLine();
        printEmptyLine();
    }

    public void printPlayerBlackjack() {
        pauseForEffect(RESULT_PAUSE_TIME);
        printLine("You have blackjack!!! You win.");
        printEmptyLine();
        printEmptyLine();
    }

    public void printPlayerBusted() {
        pauseForEffect(RESULT_PAUSE_TIME);
        printLine("You busted...Dealer wins.");
        printEmptyLine();
        printEmptyLine();
    }

    public void printPlayerWins(int handValue) {
        pauseForEffect(RESULT_PAUSE_TIME);
        console.printf("You win with %d!\n", handValue);
        printEmptyLine();
        printEmptyLine();
    }

    private void printLine(String message) {
        console.printf(message + "\n");
    }

    private void printEmptyLine() {
        console.printf("\n");
    }

    private void pauseForEffect(int milliseconds) {
        // Pause for dramatic effect. Make sure to handle interrupt.
        try {
            Thread.sleep(milliseconds);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
