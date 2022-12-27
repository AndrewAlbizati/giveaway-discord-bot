package com.github.AndrewAlbizati;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;

public class Giveaway {
    private final ArrayList<Long> users;
    private final String prize;
    private final long createTimestamp;
    private final long endTimestamp;
    private final long winnersCount;
    private final boolean announceWinner;
    private final Message message;
    private final TextChannel textChannel;
    private final User creator;

    public Giveaway(String prize, long createTimestamp, long endTimestamp, long winnersCount, boolean announceWinner, Message message, User creator) {
        users = new ArrayList<>();
        this.prize = prize;
        this.createTimestamp = createTimestamp;
        this.endTimestamp = endTimestamp;
        this.winnersCount = winnersCount;
        this.announceWinner = announceWinner;
        this.message = message;
        this.textChannel = message.getChannel();
        this.creator = creator;
    }

    public String getPrize() {
        return prize;
    }

    public long getCreateTimestamp() {
        return createTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public long getWinnersCount() {
        return winnersCount;
    }

    public boolean getAnnounceWinner() {
        return announceWinner;
    }

    public Message getMessage() {
        return message;
    }

    public TextChannel getTextChannel() {
        return textChannel;
    }

    public User getCreator() {
        return creator;
    }

    public void addUser(long user) {
        users.add(user);
    }

    public boolean contains(long user) {
        return users.contains(user);
    }

    public void removeUser(long user) {
        users.remove(user);
    }

    public void updateMessage() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(prize);
        eb.setTimestamp(Instant.ofEpochSecond(endTimestamp));

        // Create description formatted as:
        /*
        Free iPhone Giveaway

        Ends: in a day (December 27, 2022 10:04 PM)
        Hosted by: @andrewalbizati
        Entries: 22
        Winners: 1

        Today at 10:04 PM
         */
        eb.setDescription("Ends: <t:" + endTimestamp + ":R> (<t:" + endTimestamp + ":f>)\nHosted by: " + creator.getMentionTag() + "\nEntries: **" + users.size() + "**\nWinners: **" + winnersCount + "**");

        message.createUpdater()
                .removeAllEmbeds()
                .addEmbed(eb)
                .applyChanges();
    }

    public boolean saveToFile() {
        JSONObject giveawayObject = new JSONObject();
        giveawayObject.put("prize", prize);
        giveawayObject.put("createTimestamp", createTimestamp);
        giveawayObject.put("endTimestamp", endTimestamp);
        giveawayObject.put("winnersCount", winnersCount);
        giveawayObject.put("announceWinner", announceWinner);
        giveawayObject.put("textChannelId", textChannel.getId());
        giveawayObject.put("creatorId", creator.getId());
        giveawayObject.put("users", users);

        try {
            Files.write(Paths.get("giveaways/" + message.getId() + ".json"), giveawayObject.toJSONString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void deleteFile() {
        try {
            Files.delete(Paths.get("giveaways/" + message.getId() + ".json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
