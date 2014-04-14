import java.io.Console;

public final class Blackjack {
    private static int OPTION_PROMPT_PAUSE_TIME= 1000;
    private static int BET_PROMPT_PAUSE_TIME = 500;

    private Console console;
    private Commentary commentary;
    private Deck deck;
    private float chipCount;

    private enum GameOption { DEAL, QUIT }
    private enum HandOption { STAY, HIT, DOUBLE_DOWN }
    private enum IsSplitHand { TRUE, FALSE }

    private Blackjack(Console console) {
        assert console != null;
        this.console = console;

        commentary = new Commentary(console);
        deck = new Deck();
        chipCount = 100;
    }

    public static void main(String[] args) {
        Console console = System.console();
        // Running game from outside a console is not supported!
        if (console == null)
            return;

        Blackjack game = new Blackjack(console);
        game.play();
    }

    // Play until the user quits or runs out of chips.
    private void play() {
        boolean gameIsDone = false;

        commentary.printWelcome();

        while (!gameIsDone && chipCount >= 1.0) {
            GameOption gameOption = getValidGameOption();

            if (gameOption == GameOption.QUIT) {
                gameIsDone = true;
                commentary.printGoodbye();
            }
            else {
                assert gameOption == GameOption.DEAL;

                int betAmount = getValidBetAmount();
                assert betAmount >= 1 && betAmount <= chipCount;

                Hand dealerHand = new Hand();
                Hand playerHand = new Hand();
                dealStartingHands(dealerHand, playerHand);

                boolean allowInsurance = allowInsurance(dealerHand, betAmount);
                if (allowInsurance) {
                    commentary.printDealing();
                    printStartingHands(dealerHand, playerHand);

                    boolean takeInsurance = getValidInsuranceDecision();
                    if (takeInsurance) {
                        playInsuranceBet(dealerHand, betAmount);
                    }
                }

                if (allowSplitting(dealerHand, playerHand, betAmount)) {
                    if (!allowInsurance) {
                        // Make sure we don't print the starting hands twice.
                        commentary.printDealing();
                        printStartingHands(dealerHand, playerHand);
                    }

                    boolean splitHand = getValidSplitDecision();

                    if (splitHand) {
                        playSplitHands(dealerHand, playerHand, betAmount);
                    }
                    else {
                        playSingleHand(dealerHand, playerHand, betAmount);
                    }
                }
                else {
                    if (!allowInsurance) {
                        commentary.printDealing();
                        printStartingHands(dealerHand, playerHand);
                    }

                    playSingleHand(dealerHand, playerHand, betAmount);
                }

                // The minimum bet is 1 chip.
                assert chipCount >= 0.0;
                if (chipCount < 1.0) {
                    commentary.printOutOfChips();
                }
            }
        }
    }

    private void dealStartingHands(Hand dealerHand, Hand playerHand) {
        deck.shuffle();

        playerHand.addCard(deck.dealNextCard());
        dealerHand.addCard(deck.dealNextCard());
        playerHand.addCard(deck.dealNextCard());
        dealerHand.addCard(deck.dealNextCard());
    }

    private void playInsuranceBet(Hand dealerHand, int betAmount) {
        float insuranceBet = betAmount / 2.0f;
        assert insuranceBet <= chipCount - betAmount;

        // Insurance pays 2:1.
        if (dealerHand.isBlackjack()) {
            increaseChipCount(2.0f * insuranceBet);
        }
        else {
            decreaseChipCount(insuranceBet);
            commentary.printLostInsuranceBet();
        }
    }

    private boolean allowInsurance(Hand dealerHand, int betAmount) {
        // Make sure player has enough money to make the insurance bet.
        float insuranceBet = betAmount / 2.0f;
        return dealerHand.getFirstCard().isAce() && insuranceBet <= chipCount - betAmount;
    }

    private void playSingleHand(Hand dealerHand, Hand playerHand, int betAmount) {
        HandInstance instance = new HandInstance(dealerHand, playerHand, betAmount);
        instance.play(IsSplitHand.FALSE);
    }

    // We allow splitting once and split hands are not eligible for blackjack.
    private void playSplitHands(Hand dealerHand, Hand playerHand, int betAmount) {
        // Clone necessary becase HandInstance class modifies the dealer hand.
        Hand copyDealerHand = dealerHand.clone();
        Hand firstHand = new Hand();
        Hand secondHand = new Hand();

        // Split the hands.
        firstHand.addCard(playerHand.getFirstCard());
        secondHand.addCard(playerHand.getSecondCard());
        firstHand.addCard(deck.dealNextCard());
        secondHand.addCard(deck.dealNextCard());

        HandInstance firstInstance = new HandInstance(dealerHand, firstHand, betAmount);
        HandInstance secondInstance = new HandInstance(copyDealerHand, secondHand, betAmount);

        commentary.printSplitting();

        commentary.printDealingFirstHand();
        printStartingHands(dealerHand, firstHand);
        firstInstance.play(IsSplitHand.TRUE);

        commentary.printDealingSecondHand();
        printStartingHands(copyDealerHand, secondHand);
        secondInstance.play(IsSplitHand.TRUE);
    }

    private boolean allowSplitting(Hand dealerHand, Hand playerHand, int betAmount) {
        return playerHand.isPair() && !dealerHand.isBlackjack() && 2 * betAmount <= chipCount; 
    }

    private void printStartingHands(Hand dealerHand, Hand playerHand) {
        commentary.printDealerStartingHand(dealerHand);
        commentary.printPlayerHand(playerHand);
    }

    private void increaseChipCount(float amount) {
        chipCount += amount;
    }

    private void decreaseChipCount(float amount) {
        chipCount -= amount;
    }

    private GameOption getValidGameOption() {
        pauseForEffect(OPTION_PROMPT_PAUSE_TIME);

        // Get a valid game option from user. Repeatedly prompt until option is valid.
        String str;
        while (true) {
            str = console.readLine("[d-deal  q-quit]: ");
            if (str.equals("d") || str.equals("q")) {
                break;
            }
        }

        switch (str) {
            case "d":
                return GameOption.DEAL;
            case "q":
                return GameOption.QUIT;
            default:
                assert false;
                return GameOption.QUIT;
        }
    }

    private int getValidBetAmount() {
        pauseForEffect(BET_PROMPT_PAUSE_TIME);

        // Repeatedly prompt until a valid bet amount is entered.
        // We need the try block because parseInt throws.
        int maxBet = (int)Math.floor(chipCount);
        while (true) {
            try {
                String betStr = console.readLine("[Enter bet amount (1-%d)]: ", maxBet);
                int betAmount = Integer.parseInt(betStr);

                if (betAmount >= 1 && betAmount <= maxBet)
                    return betAmount;
            }
            catch (NumberFormatException e) {
                continue;
            }
        }
    }

    private HandOption getValidStartingHandOption() {
        pauseForEffect(OPTION_PROMPT_PAUSE_TIME);

        // Get a valid initial hand option from user. Repeatedly prompt until option is valid.
        // Needed because initially the player has more than simply hit/stay.
        String str;
        while (true) {
            str = console.readLine("[s-stay  h-hit  d-double]: ");
            if (str.equals("s") || str.equals("h") || str.equals("d")) {
                break;
            }
        }

        switch (str) {
            case "s":
                return HandOption.STAY;
            case "h":
                return HandOption.HIT;
            case "d":
                return HandOption.DOUBLE_DOWN;
            default:
                assert false;
                return HandOption.STAY;
        }
    }

    private HandOption getValidHandOption() {
        pauseForEffect(OPTION_PROMPT_PAUSE_TIME);

        // Get a valid hand option from user. Repeatedly prompt until option is valid.
        String str;
        while (true) {
            str = console.readLine("[s-stay  h-hit]: ");
            if (str.equals("s") || str.equals("h"))
                break;
        }

        switch (str) {
            case "s":
                return HandOption.STAY;
            case "h":
                return HandOption.HIT;
            default:
                assert false;
                return HandOption.STAY;
        }
    }

    private boolean getValidInsuranceDecision() {
        pauseForEffect(OPTION_PROMPT_PAUSE_TIME);

        String str;
        while (true) {
            str = console.readLine("[Take insurance? (y/n)]: ");
            if (str.equals("y")) {
                return true;
            }
            else if (str.equals("n")) {
                return false;
            }
        }
    }

    private boolean getValidSplitDecision() {
        pauseForEffect(OPTION_PROMPT_PAUSE_TIME);

        String str;
        while (true) {
            str = console.readLine("[Split the pair? (y/n)]: ");
            if (str.equals("y")) {
                return true;
            }
            else if (str.equals("n")) {
                return false;
            }
        }
    }

    private void pauseForEffect(int milliseconds) {
        // Sleep for a few seconds for effect.
        try {
            Thread.sleep(milliseconds);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Inner class that implements the logic of playing out a single hand instance.
    private final class HandInstance {
        private Hand dealerHand;
        private Hand playerHand;
        int betAmount;
        boolean isSplitHand;

        public HandInstance(Hand dealerStartingHand, Hand playerStartingHand,
            int startingBetAmount) {
            dealerHand = dealerStartingHand;
            playerHand = playerStartingHand;
            betAmount = startingBetAmount;
            isSplitHand = false;
        }

        // Play a single hand. Assumes the hand is not a pair. A pair hand should
        // already be split before calling this function.
        public void play(IsSplitHand isSplitHand) {
            assert !playerHand.isPair();

            this.isSplitHand = (isSplitHand == IsSplitHand.TRUE);

            if (dealerHand.isBlackjack() || (playerHand.isBlackjack() && !this.isSplitHand)) {
                processBlackjack();
                return;
            }

            // Not allowed to take additional cards if this is a hand that was split from aces.
            if (!this.isSplitHand || (this.isSplitHand && !playerHand.getFirstCard().isAce())) {
                playPlayerHand();
            }

            if (playerHand.isBusted()) {
                processPlayerBust();
            }
            else {
                playDealerHand(dealerHand);
                finishHand();
            }
        }

        private void processBlackjack() {
            boolean allowBlackjack = !isSplitHand;

            commentary.printDealerHand(dealerHand);
            commentary.printPlayerHand(playerHand);

            if (dealerHand.isBlackjack() && playerHand.isBlackjack()) {
                if (allowBlackjack) {
                    commentary.printBlackjackPush();
                }
                else {
                    commentary.printPush(dealerHand.getSoftValue());
                }
            }
            else if (dealerHand.isBlackjack()) {
                decreaseChipCount(betAmount);
                commentary.printDealerBlackjack();
            }
            else {
                assert playerHand.isBlackjack();
                assert allowBlackjack;
                // Blackjack pays 3:2.
                increaseChipCount(1.5f * betAmount);
                commentary.printPlayerBlackjack();
            }
        }

        private void processPlayerBust() {
            decreaseChipCount(betAmount);

            commentary.printDealerStartingHand(dealerHand);
            commentary.printPlayerHand(playerHand);
            commentary.printPlayerBusted();
        }

        private void finishHand() {
            // Player has not busted and dealer has played out his hand.
            commentary.printDealerHand(dealerHand);
            commentary.printPlayerHand(playerHand);

            Hand winningHand = evaluateWinningHand();

            if (winningHand == null) {
                commentary.printPush(dealerHand.getSoftValue());
            }
            else if (winningHand == dealerHand) {
                decreaseChipCount(betAmount);
                commentary.printDealerWins(dealerHand.getSoftValue());
            }
            else {
                increaseChipCount(betAmount);
                if (dealerHand.isBusted()) {
                    commentary.printDealerBusted();
                }
                else {
                    commentary.printPlayerWins(playerHand.getSoftValue());
                }
            }
        }

        private void playPlayerHand() {
            // Deal out player hand until a stay or bust. This function is called
            // when there is no blackjack for the dealer or player.
            assert !dealerHand.isBlackjack();
            assert !playerHand.isBlackjack();

            while (true) {
                // This class does not display the starting hands.
                if (!playerHand.isStartingHand()) {
                    commentary.printDealerStartingHand(dealerHand);
                    commentary.printPlayerHand(playerHand);
                }

                HandOption handOption;

                if (playerHand.isStartingHand()) {
                    handOption = getValidStartingHandOption();
                }
                else {
                    handOption = getValidHandOption();
                }

                if (handOption == HandOption.STAY) {
                    commentary.printStaying();
                    return;
                }
                else if (handOption == HandOption.DOUBLE_DOWN) {
                    float maxBetAmount = Math.min(2 * betAmount, chipCount);
                    betAmount = (int)Math.floor(maxBetAmount);

                    playerHand.addCard(deck.dealNextCard());
                    commentary.printDoublingDown();
                    return;
                }
                else if (handOption == HandOption.HIT) {
                    playerHand.addCard(deck.dealNextCard());
                    commentary.printHitting();

                    if (playerHand.isBusted()) {
                        return;
                    }
                }
                else {
                    assert false : "Allowed an invalid hand option.";
                }
            }
        }

        private void playDealerHand(Hand dealerHand) {
            // Deal out cards until dealer reaches at least 17 or busts.
            while (true) {
                int softValue = dealerHand.getSoftValue();
                if ((softValue >= 17 && softValue <= 21) || dealerHand.isBusted()) {
                    break;
                }

                dealerHand.addCard(deck.dealNextCard());
            }
        }

        private Hand evaluateWinningHand() {
            assert !playerHand.isBusted();

            // It is enough to compare soft hand value since softValue >= hardValue
            // and softValue = hardValue if there are no aces in the hand.
            // Return null if the hand is a push.
            Hand winningHand = null;
            int dealerValue = dealerHand.getSoftValue();
            int playerValue = playerHand.getSoftValue();

            if (dealerHand.isBusted() || playerValue > dealerValue) {
                winningHand = playerHand;
            }
            else if (dealerValue > playerValue) {
                winningHand = dealerHand;
            }

            return winningHand;
        }
    }
}
