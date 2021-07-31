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
import java.util.HashSet;
import java.util.Set;

public class BetBot extends ListenerAdapter {

    private final static Set<User> users = new HashSet<>();

    public static void main(String[] args) throws LoginException {
        if (args.length < 1) {
            System.out.println("You have to provide a token as first argument!");
            System.exit(1);
        }

        JDA jda = JDABuilder.createLight(args[0])
                .setChunkingFilter(ChunkingFilter.ALL) // enable member chunking for all guilds
                .setMemberCachePolicy(MemberCachePolicy.ALL) // ignored if chunking enabled
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(new BetBot())
                .setActivity(Activity.competing("~~help"))
                .build();

        jda.upsertCommand("ping", "Calculate ping of the bot").queue();
    }

    private static void showUsers(MessageChannel channel) {
        int i = 1;
        channel.sendMessage("User List").queue();
        for (User u : users) {
            channel.sendMessage(i++ + ". " + u.getAsMention()).queue();
        }
    }

    private static void showChannelUsers(MessageChannel channel) {
        int i = 1;
        channel.sendMessage("User List").queue();
        for (User u : channel.getJDA().getUsers()) {
            channel.sendMessage(i++ + ". " + u.getAsMention()).queue();
        }
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
                channel.sendMessage("Help!").queue();
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
                    User target = event.getJDA().getUserByTag(commandArgs[1]);
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
                for (User u : users) {
                    channel.sendMessage(i++ + ". " + u.getAsMention() + ": " + Math.round(Math.random() * 100)).queue();
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
}
