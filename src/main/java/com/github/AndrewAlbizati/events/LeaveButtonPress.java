package com.github.AndrewAlbizati.events;

import com.github.AndrewAlbizati.Bot;
import com.github.AndrewAlbizati.Giveaway;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.event.interaction.MessageComponentCreateEvent;
import org.javacord.api.interaction.MessageComponentInteraction;
import org.javacord.api.listener.interaction.MessageComponentCreateListener;

/**
 * Handles whenever a button is pressed to leave giveaway.
 */
public class LeaveButtonPress implements MessageComponentCreateListener {
    private final Bot bot;
    public LeaveButtonPress(Bot bot) {
        this.bot = bot;
    }

    /**
     * Main function that handles leave button presses.
     * @param messageComponentCreateEvent The event.
     */
    @Override
    public void onComponentCreate(MessageComponentCreateEvent messageComponentCreateEvent) {
        MessageComponentInteraction messageComponentInteraction = messageComponentCreateEvent.getMessageComponentInteraction();
        String customId = messageComponentInteraction.getCustomId();
        long userId = messageComponentInteraction.getUser().getId();

        // Ignore different button presses
        if (!customId.toLowerCase().contains("leave")) {
            return;
        }

        long messageId = Long.parseLong(customId.split("-")[1]);

        // Ignore button presses on messages not created by this bot
        if (!bot.containsGiveaway(messageId)) {
            return;
        }

        Giveaway giveaway = bot.getGiveaway(messageId);

        // Ignore if the user hasn't entered the giveaway
        if (!giveaway.getUsers().contains(userId)) {
            return;
        }
        giveaway.removeUser(userId);

        messageComponentInteraction.acknowledge();
        if (giveaway.saveToFile()) {
            // Save to file was successful
            giveaway.updateMessage();
            messageComponentInteraction.createFollowupMessageBuilder()
                    .setContent("Successfully left the **" + giveaway.getPrize() + "** giveaway!")
                    .setFlags(MessageFlag.EPHEMERAL)
                    .send();
        } else {
            // Save to file was unsuccessful
            messageComponentInteraction.createFollowupMessageBuilder()
                    .setContent("Error leaving the **" + giveaway.getPrize() + "** giveaway! Try again.")
                    .setFlags(MessageFlag.EPHEMERAL)
                    .send();
        }
    }
}
