package com.github.AndrewAlbizati;

import com.github.AndrewAlbizati.events.GiveawayCommandHandler;
import com.github.AndrewAlbizati.events.JoinButtonPress;
import com.github.AndrewAlbizati.events.LeaveButtonPress;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionType;

import java.util.HashMap;
import java.util.List;

public class Bot {
    private final String token;
    private final HashMap<Long, Giveaway> giveawayHashMap;
    public Bot(String token) {
        this.token = token;
        giveawayHashMap = new HashMap<>();
    }

    public void start() {
        // Start Discord bot
        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();
        System.out.println("Logged in as " + api.getYourself().getDiscriminatedName());

        // Set bot status
        api.updateStatus(UserStatus.ONLINE);

        // Create slash command (may take a few minutes to update on Discord)
        SlashCommand.with("giveaway", "Creates a giveaway on a Discord server",
                List.of(
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "PRIZE", "Name of the item being given away", true),
                        SlashCommandOption.create(SlashCommandOptionType.LONG, "TIME", "Time that the giveaway winner(s) will be decided (default to 1 day)"),
                        SlashCommandOption.create(SlashCommandOptionType.LONG, "WINNERS", "Amount of winners for the giveaway (default 1)"),
                        SlashCommandOption.create(SlashCommandOptionType.BOOLEAN, "ANNOUNCE", "Whether or not the bot should announce the winner(s) (default true)")
                )).createGlobal(api).join();

        api.addSlashCommandCreateListener(new GiveawayCommandHandler(this));
        api.addMessageComponentCreateListener(new JoinButtonPress(this));
        api.addMessageComponentCreateListener(new LeaveButtonPress(this));
    }

    public void addGiveaway(long messageId, Giveaway giveaway) {
        giveawayHashMap.put(messageId, giveaway);
    }

    public void removeGiveaway(long messageId) {
        giveawayHashMap.remove(messageId);
    }

    public Giveaway getGiveaway(long messageId) {
        return giveawayHashMap.get(messageId);
    }

    public boolean containsGiveaway(long messageId) {
        return giveawayHashMap.containsKey(messageId);
    }
}
