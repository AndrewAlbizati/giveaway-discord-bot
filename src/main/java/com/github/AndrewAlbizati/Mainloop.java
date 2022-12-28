package com.github.AndrewAlbizati;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Mainloop implements Runnable {
    private final Bot bot;
    public Mainloop(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void run() {
        for (long messageId : bot.getGiveaways()) {
            Giveaway giveaway = bot.getGiveaway(messageId);
            if (giveaway.getEndTimestamp() > System.currentTimeMillis()/1000) {
                continue;
            }

            long[] winners = new long[(int) giveaway.getWinnersCount()];
            Random rand = new Random();
            ArrayList<Long> entries = new ArrayList<>(giveaway.getUsers());

            // Iterate through winners count, or total amount of entries (whichever is smaller)
            for (int i = 0; i < Math.min(giveaway.getWinnersCount(), giveaway.getUsers().size()); i++) {
                long winner = entries.get(rand.nextInt(entries.size()));
                winners[i] = winner;
                entries.remove(winner);
            }

            giveaway.updateMessageWithWinners(winners);
            giveaway.deleteFile();
            bot.removeGiveaway(messageId);

            if (Math.min(giveaway.getWinnersCount(), giveaway.getUsers().size()) == 0) {
                return;
            }

            StringBuilder winnersListStr = new StringBuilder();
            for (int i = 0; i < winners.length; i++) {
                winnersListStr.append("<@!" + winners[i] + ">");
                if (i != winners.length - 1) {
                    winnersListStr.append(", ");
                }
            }
            giveaway.getMessage().reply("Congratulations " + winnersListStr + "! You won the **" + giveaway.getPrize() + "**!");
        }
    }
}
