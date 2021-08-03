import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;
import java.util.*;

public class BetBot extends ListenerAdapter {

    private final static Set<User> users = new HashSet<>();
    private final static EmbedBuilder eb = new EmbedBuilder();

    private static final BetInfo betInfo = new BetInfo();

    public static void main(String[] args) throws LoginException {
        if (args.length < 1) {
            System.out.println("You have to provide a token as first argument!");
            System.exit(1);
        }

        JDA jda = JDABuilder.createLight(args[0])
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(new BetBot())
                .setActivity(Activity.competing("~~help"))
                .build();

        jda.upsertCommand("ping", "Calculate ping of the bot").queue();
    }

    private static void showUsers(MessageChannel channel) {
        int i = 1;
        channel.sendMessage("Betting User List").queue();
        StringBuilder sb = new StringBuilder();
        for (User u : users) {
            if (!u.isBot()) {
                sb.append(i++).append(". ").append(u.getName()).append("\n");
            }
        }
        eb.setTitle("User List").setDescription(sb.toString());
        channel.sendMessage(eb.build()).queue();
    }

    private static void showChannelUsers(MessageChannel channel) {
        int i = 1;
        channel.sendMessage("User List").queue();
        StringBuilder sb = new StringBuilder();
        for (User u : channel.getJDA().getUsers()) {
            if (!u.isBot()) {
                sb.append(i++).append(". ").append(u.getName()).append("\n");
            }
        }
        eb.setTitle("User List").setDescription(sb.toString());
        channel.sendMessage(eb.build()).queue();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        MessageChannel channel = event.getChannel();
        User user = event.getAuthor();

        if (user.isBot()) {
            return;
        }

        if (msg.getContentRaw().startsWith("~~")) {
            String[] commandArgs = msg.getContentRaw().substring(2).split(" ");

            if (commandArgs[0].equalsIgnoreCase("help")) {
                eb.setTitle("Bet Bot")
                        .setDescription("~~\n" +
                                "ping - ping check\n" +
                                "user - user list\n");
                eb.setFooter("help!");
                channel.sendMessage(eb.build()).queue();
            }

            if (commandArgs[0].equalsIgnoreCase("ping")) {
                long time = System.currentTimeMillis();
                channel.sendMessage("Pong!")
                        .queue(response -> {
                            response.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue();
                        });
            }

            if (commandArgs[0].equalsIgnoreCase("user")) {
                showChannelUsers(channel);
            }

            if (commandArgs[0].equalsIgnoreCase("bet")) {
                showUsers(channel);
            }

            if (commandArgs[0].equalsIgnoreCase("hello")) {
                channel.sendMessage("Hello! " + user.getAsMention()).queue();
            }

            if (commandArgs[0].equalsIgnoreCase("who")) {
                showUsers(channel);
            }

            if (commandArgs[0].equalsIgnoreCase("set") && commandArgs.length == 1) {
                users.add(user);
                channel.sendMessage("Set " + user.getAsMention()).queue();
                showUsers(channel);
            }

            if (commandArgs[0].equalsIgnoreCase("set") && commandArgs.length > 1) {
                try {
                    User target = event.getJDA().getUserById(commandArgs[1].substring(3, 21));
                    users.add(target);
                    channel.sendMessage("Set " + target.getAsMention()).queue();
                } catch (Exception e) {
                    e.printStackTrace();
                    channel.sendMessage("No User").queue();
                }
                showUsers(channel);
            }

            if (commandArgs[0].equalsIgnoreCase("reset") && commandArgs.length == 1) {
                users.remove(user);
                channel.sendMessage("reset " + user.getAsMention()).queue();
                showUsers(channel);
            }

            if (commandArgs[0].equalsIgnoreCase("reset") && commandArgs.length > 1) {
                if (commandArgs[1].equalsIgnoreCase("all")) {
                    users.clear();
                } else {
                    channel.sendMessage("reset " + user.getAsMention()).queue();
                    showUsers(channel);
                }
            }

            if (commandArgs[0].equalsIgnoreCase("dice")) {
                int i = 1;
                StringBuilder sb = new StringBuilder();
                for (User u : users) {
                    sb.append(i++).append(". ")
                            .append(u.getAsMention())
                            .append(": ")
                            .append(Math.round(Math.random() * 100))
                            .append("\n");
                }
                eb.setTitle("DICE")
                        .setAuthor(user.getAsTag())
                        .setDescription(sb.toString());
                channel.sendMessage(eb.build()).queue();
            }

            if (commandArgs[0].equalsIgnoreCase("bet")) {

                if (commandArgs[1].equalsIgnoreCase("set")) {

                }

                if (commandArgs[1].equalsIgnoreCase("leaderboard")) {

                }

            }
        }
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (!event.getName().equals("ping")) return;
        long time = System.currentTimeMillis();
        event.reply("Pong!").setEphemeral(true)
                .flatMap(v ->
                        event.getHook().editOriginalFormat("Pong: %d ms", System.currentTimeMillis() - time)
                ).queue();
    }

    static class BetInfo {
        private String title;
        private Integer totalPoint;
        private Map<User, Integer> agree;
        private Map<User, Integer> disagree;

        public BetInfo() {
            this.title = "";
            this.totalPoint = 0;
            this.agree = new HashMap<>();
            this.disagree = new HashMap<>();
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Integer getTotalPoint() {
            return totalPoint;
        }

        public void setTotalPoint(Integer totalPoint) {
            this.totalPoint = totalPoint;
        }

        public Map<User, Integer> getAgree() {
            return agree;
        }

        public void setAgree(Map<User, Integer> agree) {
            this.agree = agree;
        }

        public Map<User, Integer> getDisagree() {
            return disagree;
        }

        public void setDisagree(Map<User, Integer> disagree) {
            this.disagree = disagree;
        }

        public void clear() {
            this.title = "";
            this.totalPoint = 0;
            this.agree.clear();
            this.disagree.clear();
        }
    }
}
