package com.github.AndrewAlbizati;

import com.github.AndrewAlbizati.events.GiveawayCommandHandler;
import com.github.AndrewAlbizati.events.JoinButtonPress;
import com.github.AndrewAlbizati.events.LeaveButtonPress;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.user.UserStatus;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Bot {
    private final String token;
    private DiscordApi api;
    private final HashMap<Long, Giveaway> giveawayHashMap;
    public Bot(String token) {
        this.token = token;
        giveawayHashMap = new HashMap<>();
    }

    public void start() {
        // Start Discord bot
        api = new DiscordApiBuilder().setToken(token).login().join();
        System.out.println("Logged in as " + api.getYourself().getDiscriminatedName());

        // Set bot status
        api.updateStatus(UserStatus.ONLINE);

        // Create slash command (may take a few minutes to update on Discord)
        SlashCommand.with("giveaway", "Creates a giveaway on a Discord server",
                List.of(
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "PRIZE", "Name of the item being given away", true),
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "TIME", "Formatted as \"1y 2n 3d 4h 5m 6s\" (y=year, n=month, d=day, h=hour, m=min, s=sec) (default to 1 day)"),
                        SlashCommandOption.create(SlashCommandOptionType.LONG, "WINNERS", "Amount of winners for the giveaway (default 1)")
                )).createGlobal(api).join();

        loadGiveaways();

        api.addSlashCommandCreateListener(new GiveawayCommandHandler(this));
        api.addMessageComponentCreateListener(new JoinButtonPress(this));
        api.addMessageComponentCreateListener(new LeaveButtonPress(this));

        Mainloop mainloop = new Mainloop(this);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(mainloop, 0, 15, TimeUnit.SECONDS);
    }

    public void loadGiveaways() {
        try {
            File mainDir = new File("giveaways");
            JSONParser parser = new JSONParser();
            for (File file : mainDir.listFiles()) {
                try {
                    JSONObject object = (JSONObject) parser.parse(new FileReader(file));
                    TextChannel textChannel = api.getTextChannelById((long) object.get("textChannelId")).get();

                    String prize = (String) object.get("prize");
                    long createTimestamp = (long) object.get("createTimestamp");
                    long endTimestamp = (long) object.get("endTimestamp");
                    long winnersCount = (long) object.get("winnersCount");

                    long messageId = Long.parseLong(file.getName().substring(0, file.getName().length() - 5));
                    Message message = api.getMessageById(messageId, textChannel).get();

                    long userId1 = (long) object.get("creatorId");
                    User creator = api.getUserById(userId1).get();

                    Giveaway giveaway = new Giveaway(prize, createTimestamp, endTimestamp, winnersCount, message, creator);
                    for (Object obj : (JSONArray) object.get("users")) {
                        long userId = (long) obj;
                        giveaway.addUser(userId);
                    }

                    giveawayHashMap.put(messageId, giveaway);
                } catch (NullPointerException | IOException | ParseException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    System.out.println("Failed to load " + file.getName());
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void addGiveaway(long messageId, Giveaway giveaway) {
        giveawayHashMap.put(messageId, giveaway);
    }

    public Set<Long> getGiveaways() {
        return giveawayHashMap.keySet();
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
