package com.github.AndrewAlbizati.events;

import com.github.AndrewAlbizati.Bot;
import com.github.AndrewAlbizati.Giveaway;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

import java.awt.*;
import java.util.concurrent.ExecutionException;

/**
 * Handles when a user executes the /giveaway command
 */
public class GiveawayCommandHandler implements SlashCommandCreateListener {
    private final Bot bot;
    public GiveawayCommandHandler(Bot bot) {
        this.bot = bot;
    }

    /**
     * Main function that handles the /giveaway slash command.
     * @param event The SlashCommandCreateEvent made when the slash command is executed.
     */
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();

        // Ignore other slash commands
        if (!interaction.getCommandName().equalsIgnoreCase("giveaway")) {
            return;
        }


        // Return error message if a title (required) is not provided
        if (interaction.getArgumentStringRepresentationValueByName("PRIZE").isEmpty()) {
            EmbedBuilder errorBuilder = createErrorEmbed(new IllegalArgumentException("Please provide a title for this giveaway."));
            interaction.createImmediateResponder()
                    .addEmbed(errorBuilder)
                    .setFlags(MessageFlag.EPHEMERAL)
                    .respond();
            return;
        }
        String title = interaction.getArgumentStringRepresentationValueByName("PRIZE").get();


        long timestamp = interaction.getArgumentLongValueByName("TIME").orElse(System.currentTimeMillis()/1000 + 86400);
        // Return error message if a timestamp from the past is provided
        if (timestamp < System.currentTimeMillis()/1000) {
            EmbedBuilder errorBuilder = createErrorEmbed(new IllegalArgumentException("Please provide a timestamp in the future."));
            interaction.createImmediateResponder()
                    .addEmbed(errorBuilder)
                    .setFlags(MessageFlag.EPHEMERAL)
                    .respond();
            return;
        }


        long winnersCount = interaction.getArgumentLongValueByName("WINNERS").orElse(1L);
        // Return error message if less than 1 winner is provided
        if (winnersCount < 1) {
            EmbedBuilder errorBuilder = createErrorEmbed(new IllegalArgumentException("Please provide at least 1 winner."));
            interaction.createImmediateResponder()
                    .addEmbed(errorBuilder)
                    .setFlags(MessageFlag.EPHEMERAL)
                    .respond();
            return;
        }


        boolean announceWinner = interaction.getArgumentBooleanValueByName("ANNOUNCE").orElse(true);

        try {
            Message message = interaction.createImmediateResponder()
                    .addComponents(ActionRow.of(Button.primary("signup", "🎉")))
                    .respond().get().update().get();

            Giveaway giveaway = new Giveaway(title, System.currentTimeMillis()/1000, timestamp, winnersCount, announceWinner, message, interaction.getUser());
            giveaway.updateMessage();
            giveaway.saveToFile();
            bot.addGiveaway(message.getId(), giveaway);
        } catch (InterruptedException | ExecutionException e) {
            EmbedBuilder errorBuilder = createErrorEmbed(e);
            interaction.createImmediateResponder()
                    .addEmbed(errorBuilder)
                    .setFlags(MessageFlag.EPHEMERAL)
                    .respond();
        }
    }

    private EmbedBuilder createErrorEmbed(Exception e) {
        EmbedBuilder errorBuilder = new EmbedBuilder();
        errorBuilder.setTitle("ERROR");
        errorBuilder.setColor(Color.RED);
        errorBuilder.setDescription("There was an error adding the giveaway (" + e.getMessage() + ").");

        return errorBuilder;
    }
}