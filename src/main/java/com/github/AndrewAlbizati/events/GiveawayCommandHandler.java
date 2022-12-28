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


        String unformattedTimestamp = interaction.getArgumentStringRepresentationValueByName("TIME").orElse("1d");
        // Return error message if a timestamp from the past is provided or is invalid
        if (parseTimestampInput(unformattedTimestamp) == -1L) {
            EmbedBuilder errorBuilder = createErrorEmbed(new IllegalArgumentException("Please provide a valid timestamp in the future."));
            interaction.createImmediateResponder()
                    .addEmbed(errorBuilder)
                    .setFlags(MessageFlag.EPHEMERAL)
                    .respond();
            return;
        }

        long timestamp = parseTimestampInput(unformattedTimestamp);


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


        try {
            Message message = interaction.createImmediateResponder()
                    .addComponents(ActionRow.of(Button.primary("signup", "🎉")))
                    .respond().get().update().get();

            Giveaway giveaway = new Giveaway(title, System.currentTimeMillis()/1000, timestamp, winnersCount, message, interaction.getUser());
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

    private long parseTimestampInput(String input) {
        try {
            long time = System.currentTimeMillis()/1000;
            for (String val : input.split(" ")) {
                int amount = Integer.parseInt(val.substring(0, 1));
                String type = val.substring(1, 2);

                switch (type.toLowerCase()) {
                    case "y":
                        time += 31536000L * amount;
                        break;
                    case "n":
                        time += 2628288L * amount;
                        break;
                    case "d":
                        time += 86400L * amount;
                        break;
                    case "h":
                        time += 3600L * amount;
                        break;
                    case "m":
                        time += 60L * amount;
                    case "s":
                        time += amount;
                        break;

                    default:
                        throw new IllegalArgumentException();
                }
            }

            return time;
        } catch (Exception e) {
            e.printStackTrace();
            return -1L;
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