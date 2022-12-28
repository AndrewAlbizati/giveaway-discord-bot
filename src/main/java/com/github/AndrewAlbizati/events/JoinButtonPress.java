package com.github.AndrewAlbizati.events;

import com.github.AndrewAlbizati.Bot;
import com.github.AndrewAlbizati.Giveaway;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.event.interaction.MessageComponentCreateEvent;
import org.javacord.api.interaction.MessageComponentInteraction;
import org.javacord.api.listener.interaction.MessageComponentCreateListener;

/**
 * Handles whenever a button is pressed to join giveaway.
 */
public class JoinButtonPress implements MessageComponentCreateListener {
    private final Bot bot;
    public JoinButtonPress(Bot bot) {
        this.bot = bot;
    }

    /**
     * Main function that handles join button presses.
     * @param messageComponentCreateEvent The event.
     */
    @Override
    public void onComponentCreate(MessageComponentCreateEvent messageComponentCreateEvent) {
        MessageComponentInteraction messageComponentInteraction = messageComponentCreateEvent.getMessageComponentInteraction();
        String customId = messageComponentInteraction.getCustomId();
        long messageId = messageComponentInteraction.getMessage().getId();
        long userId = messageComponentInteraction.getUser().getId();

        // Ignore different button presses
        if (!customId.equalsIgnoreCase("signup")) {
            return;
        }

        // Ignore button presses on messages not created by this bot
        if (!bot.containsGiveaway(messageId)) {
            return;
        }

        Giveaway giveaway = bot.getGiveaway(messageId);

        // Ignore if the user has already been entered
        if (giveaway.getUsers().contains(userId)) {
            return;
        }
        giveaway.addUser(userId);

        messageComponentInteraction.acknowledge();
        if (giveaway.saveToFile()) {
            // Save to file was successful
            giveaway.updateMessage();
            messageComponentInteraction.createFollowupMessageBuilder()
                    .setContent("You've been entered in the **" + giveaway.getPrize() + "** giveaway! Press the 💥 button to leave.")
                    .addComponents(ActionRow.of(Button.primary("leave-" + giveaway.getMessage().getId(), "💥")))
                    .setFlags(MessageFlag.EPHEMERAL)
                    .send();
        } else {
            // Save to file was unsuccessful
            messageComponentInteraction.createFollowupMessageBuilder()
                    .setContent("Error entering the **" + giveaway.getPrize() + "** giveaway! Please try again.")
                    .setFlags(MessageFlag.EPHEMERAL)
                    .send();
        }
    }
}
