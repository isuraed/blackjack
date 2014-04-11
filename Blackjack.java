import java.io.Console;

public final class Blackjack {
    private Console console;
    private Commentary commentary;
    private Deck deck;
    private int chipCount;

    private Blackjack(Console console) {
        assert console != null;
        this.console = console;

        commentary = new Commentary(console);
        deck = new Deck();
        chipCount = 100;
    }

    private enum GameOption { DEAL, QUIT }
    
    private enum HandOption { STAY, HIT, DOUBLE_DOWN }

    public static void main(String[] args) {
        Console console = System.console();
        // Running game from outside a console is not supported!
        if (console == null)
            return;

        Blackjack game = new Blackjack(console);
        game.play();
    }

    private void play() {
        boolean gameIsDone = false;

        commentary.printWelcome();

        while (!gameIsDone && chipCount > 0) {
            GameOption gameOption = getValidGameOption();

            if (gameOption == GameOption.QUIT) {
                commentary.printGoodbye();
                gameIsDone = true;
            }
            else {
                assert gameOption == GameOption.DEAL;

                boolean handIsDone = false;

                int betAmount = getValidBetAmount(chipCount);
                assert betAmount >= 1 && betAmount <= chipCount;

                Hand dealerHand = new Hand();
                Hand playerHand = new Hand();

                deck.shuffle();

                playerHand.addCard(deck.dealNextCard());
                dealerHand.addCard(deck.dealNextCard());
                playerHand.addCard(deck.dealNextCard());
                dealerHand.addCard(deck.dealNextCard());

                playHand(dealerHand, playerHand, betAmount);
            }
        }
    }

    private void playHand(Hand dealerHand, Hand playerHand, int betAmount) {
        boolean handIsDone = false;

        commentary.printDealing();

        // Handle blackjack case first.
        if (dealerHand.isBlackjack() || playerHand.isBlackjack()) {
            handIsDone = true;

            commentary.printDealerHand(dealerHand);
            commentary.printPlayerHand(playerHand);

            if (dealerHand.isBlackjack() && playerHand.isBlackjack()) {
                commentary.printBlackjackPush();
            }
            else if (dealerHand.isBlackjack()) {
                decreaseChipCount(betAmount);
                commentary.printDealerBlackjack();
            }
            else {
                increaseChipCount(betAmount);
                commentary.printPlayerBlackjack();
            }
        }

        while (!handIsDone) {
            commentary.printDealerStartingHand(dealerHand);
            commentary.printPlayerHand(playerHand);

            HandOption handOption = getValidHandOption();

            if (handOption == HandOption.HIT) {
                playerHand.addCard(deck.dealNextCard());

                commentary.printHitting();

                if (playerHand.isBusted()) {
                    handIsDone = true;

                    decreaseChipCount(betAmount);

                    commentary.printDealerStartingHand(dealerHand);
                    commentary.printPlayerHand(playerHand);
                    commentary.printPlayerBusted();
                }
            }
            else {
                assert handOption == HandOption.STAY;
                handIsDone = true;

                playDealerHand(dealerHand);
                
                commentary.printStaying();
                commentary.printDealerHand(dealerHand);
                commentary.printPlayerHand(playerHand);

                // Evaluate winner
                assert !playerHand.isBusted();
                Hand winningHand = null;
                int playerSoftValue = playerHand.getSoftValue();
                int playerHardValue = playerHand.getHardValue();

                if (dealerHand.isBusted()) {
                    winningHand = playerHand;
                }
                else if (playerSoftValue <= 21) {
                    // Since softValue >= hardValue.
                    if (dealerHand.getSoftValue() > playerSoftValue)
                        winningHand = dealerHand;
                    else if (playerSoftValue > dealerHand.getSoftValue())
                        winningHand = playerHand;
                }
                else {
                    // Player's soft hand is no good so compare player's hard value.
                    if (dealerHand.getSoftValue() > playerHardValue)
                        winningHand = dealerHand;
                    else if (playerHardValue > dealerHand.getSoftValue())
                        winningHand = playerHand;
                }

                if (winningHand == null) {
                    // A push.
                    commentary.printPush(dealerHand.getSoftValue());
                }
                else if (winningHand == dealerHand) {
                    decreaseChipCount(betAmount);
                    commentary.printDealerWins(dealerHand.getSoftValue());
                }
                else {
                    increaseChipCount(betAmount);

                    int winningValue = playerSoftValue;
                    if (playerSoftValue > 21)
                        winningValue = playerHardValue;

                    if (dealerHand.isBusted())
                        commentary.printDealerBusted();
                    else
                        commentary.printPlayerWins(winningValue);
                }
            }
        }
    }

    private void playDealerHand(Hand dealerHand) {
        // Deal out cards until dealer reaches at least 17 or busts.
        while (true) {
            int effectiveValue;
            if (dealerHand.getSoftValue() <= 21)
                effectiveValue = dealerHand.getSoftValue();
            else
                effectiveValue = dealerHand.getHardValue();

            if ((effectiveValue >= 17 && effectiveValue <= 21) || dealerHand.isBusted())
                break;

            dealerHand.addCard(deck.dealNextCard());
        }
    }
    
    private void increaseChipCount(int bet) {
        chipCount += bet;
    }

    private void decreaseChipCount(int bet) {
        chipCount -= bet;
    }

    private GameOption getValidGameOption() {
        pauseForEffect(2000);

        // Get a valid game option from user. Repeatedly prompt until option is valid.
        String optionStr;
        while (true) {
            optionStr = console.readLine("[d-deal, q-quit]: ");
            if (optionStr.equals("d") || optionStr.equals("q"))
                break;
        }

        switch (optionStr) {
            case "d":
                return GameOption.DEAL;
            case "q":
                return GameOption.QUIT;
            default:
                assert false;
                return GameOption.QUIT;
        }
    }

    private int getValidBetAmount(int chipCount) {
        pauseForEffect(1000);

        // Repeatedly prompt until a valid bet amount is entered.
        // We need the try block because parseInt throws.
        while (true) {
            try {
                String betStr = console.readLine("[Enter bet amount (1-%d)]: ", chipCount);
                int betAmount = Integer.parseInt(betStr);

                if (betAmount >= 1 && betAmount <= chipCount)
                    return betAmount;
            }
            catch (NumberFormatException e) {
                continue;
            }
        }
    }

    private HandOption getValidInitialHandOption() {
        pauseForEffect(2000);

        // Get a valid initial hand option from user. Repeatedly prompt until option is valid.
        // Needed because initially the player has more than simply hit/stay.
        String str;
        while (true) {
            str = console.readLine("[s-stay  h-hit  d-double down]: ");
            if (str.equals("s") || str.equals("h") || str.equals("d"))
                break;
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
        pauseForEffect(2000);

        // Get a valid hand option from user. Repeatedly prompt until option is valid.
        String optionStr;
        while (true) {
            optionStr = console.readLine("[s-stay, h-hit]: ");
            if (optionStr.equals("s") || optionStr.equals("h"))
                break;
        }

        switch (optionStr) {
            case "s":
                return HandOption.STAY;
            case "h":
                return HandOption.HIT;
            default:
                assert false;
                return HandOption.STAY;
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
}
