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

    private final static int DEFAULT_POINT = 1000;

    private final static Map<User, Integer> users = new HashMap<>();
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

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        MessageChannel channel = event.getChannel();
        User user = event.getAuthor();
        eb.clear();

        if (user.isBot()) {
            return;
        }

        if (msg.getContentRaw().startsWith("~~")) {
            String[] commandArgs = msg.getContentRaw().substring(2).split(" ");

            // 도움말
            if (commandArgs[0].equalsIgnoreCase("help")) {
                eb.setTitle("BetBot Commands")
                        .setDescription("\n\\~\\~ping - ping check\n" +
                                "\\~\\~user - user list\n" +
                                "\\~\\~who - BetBot user list\n");
                channel.sendMessage(eb.build()).queue();
            }

            // 핑 테스트
            if (commandArgs[0].equalsIgnoreCase("ping")) {
                long time = System.currentTimeMillis();
                channel.sendMessage("Pong!")
                        .queue(response
                                -> response.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time)
                                .queue());
            }

            // 인사
            if (commandArgs[0].equalsIgnoreCase("hello")) {
                channel.sendMessage("Hello! " + user.getAsMention()).queue();
            }

            // BetBot에 참여한 유저 확인
            if (commandArgs[0].equalsIgnoreCase("user")) {
                showUsers(channel);
            }

            // 채널 내 유저 확인
            if (commandArgs[0].equalsIgnoreCase("betuser")) {
                showChannelUsers(channel);
            }

            // 유저 설정
            if (commandArgs[0].equalsIgnoreCase("set")) {
                // 본인 설정시
                if (commandArgs.length == 1) {
                    users.put(user, DEFAULT_POINT);
                    channel.sendMessage("Set " + user.getAsMention()).queue();
                    showUsers(channel);
                // 다른 유저 설정시
                } else {
                    try {
                        User target = event.getJDA().getUserById(commandArgs[1].substring(3, 21));
                        users.put(target, DEFAULT_POINT);
                        channel.sendMessage("Set " + target.getAsMention()).queue();
                    } catch (Exception e) {
                        e.printStackTrace();
                        channel.sendMessage("No User").queue();
                    }
                    showUsers(channel);
                }
            }

            // 유저 설정 해제
            if (commandArgs[0].equalsIgnoreCase("reset")) {
                // 본인 설정 해제시
                if (commandArgs.length == 1) {
                    users.remove(user);
                    channel.sendMessage("reset " + user.getAsMention()).queue();
                    showUsers(channel);
                // 다른 유저 설정 해제시
                } else {
                    if (commandArgs[1].equalsIgnoreCase("all")) {
                        users.clear();
                    } else {
                        try {
                            User target = event.getJDA().getUserById(commandArgs[1].substring(3, 21));
                            users.put(target, DEFAULT_POINT);
                            channel.sendMessage("Set " + target.getAsMention()).queue();
                        } catch (Exception e) {
                            e.printStackTrace();
                            channel.sendMessage("No User").queue();
                        }
                        showUsers(channel);
                    }
                }
            }

            // 베팅 시작 (~~start [주제])
            if (commandArgs[0].equalsIgnoreCase("start")) {
                betInfo.clear();
                betInfo.setTitle(commandArgs[1]);
                betInfo.setUser(user);
                betInfo.setBetStatus(BetStatus.BETTING);
            }

            // 베팅 유저 참여
            // ~~bet [y/n] [point] : 본인 베팅 참여시
            // ~~bet [user] [y/n] [point] : 다른 유저 베팅 참여시킬 때
            if (commandArgs[0].equalsIgnoreCase("bet")) {
                // ~~bet [y/n] [point] : 본인 베팅 참여시
                if (commandArgs.length == 3) {
                    if (users.get(user) == null) {
                        channel.sendMessage("First, set user. (~~set / ~~set @user)").queue();
                        return;
                    }

                    if (betInfo.checkBetting(user)) {
                        channel.sendMessage("You have already placed a bet.").queue();
                        return;
                    }

                    try {
                        int targetPoint = Integer.parseInt(commandArgs[2]);
                        int userPoint = users.get(user);
                        if (userPoint < targetPoint) {
                            channel.sendMessage("lack of points..").queue();
                            return;
                        }

                        String targetVote = commandArgs[1];

                        if (targetVote.equalsIgnoreCase("y")) {
                            betInfo.getAgree().put(user, targetPoint);
                            eb.setTitle(user.getAsTag())
                                    .setDescription(targetPoint + " / \uD83D\uDC4D");
                        } else if (targetVote.equalsIgnoreCase("n")) {
                            betInfo.getDisagree().put(user, targetPoint);
                            eb.setTitle(user.getAsTag())
                                    .setDescription(targetPoint + " / \uD83D\uDC4E");
                        } else {
                            eb.setTitle("~~bet set (y/n) (point)");
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    channel.sendMessage(eb.build()).queue();
                // ~~bet [user] [y/n] [point] : 다른 유저 베팅 참여시킬 때
                } else if (commandArgs.length == 4) {
                    User target = event.getJDA().getUserById(commandArgs[1].substring(3, 21));
                    if (target == null) {
                        channel.sendMessage("No User..").queue();
                        return;
                    }
                    if (users.get(target) == null) {
                        channel.sendMessage("First, set user. (~~set / ~~set @user)").queue();
                        return;
                    }
                    try {
                        int targetPoint = Integer.parseInt(commandArgs[3]);
                        int userPoint = users.get(target);
                        if (userPoint < targetPoint) {
                            channel.sendMessage("lack of points..").queue();
                            return;
                        }

                        String targetVote = commandArgs[2];

                        if (targetVote.equalsIgnoreCase("y")) {
                            betInfo.setTotalPoint(betInfo.getTotalPoint() + targetPoint);
                            betInfo.getAgree().put(target, targetPoint);
                            eb.setTitle(target.getAsTag())
                                    .setDescription(targetPoint + " / \uD83D\uDC4D");
                        } else if (targetVote.equalsIgnoreCase("n")) {
                            betInfo.setTotalPoint(betInfo.getTotalPoint() + targetPoint);
                            betInfo.getDisagree().put(target, targetPoint);
                            eb.setTitle(target.getAsTag())
                                    .setDescription(targetPoint + " / \uD83D\uDC4E");
                        } else {
                            eb.setTitle("~~bet set (user) (y/n) (point)");
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    channel.sendMessage(eb.build()).queue();
                }
            }

            // 베팅 종료 (참여 불가)
            if (commandArgs[0].equalsIgnoreCase("voteend")) {
                betInfo.setBetStatus(BetStatus.VOTE_END);
                channel.sendMessage("Vote End! Wait for the result.").queue();
            }

            // 결과 발표 (~~end [y/n])
            if (commandArgs[0].equalsIgnoreCase("betend")) {
                if (commandArgs[1].equalsIgnoreCase("y")) {

                } else if (commandArgs[1].equalsIgnoreCase("n")) {

                // error
                } else {
                    return;
                }
                betInfo.clear();
            }

            // 유저들 포인트 보기
            if (commandArgs[0].equalsIgnoreCase("leaderboard")) {
                if (betInfo.getBetStatus() == BetStatus.INITIALIZE
                        && betInfo.getBetStatus() == BetStatus.END) {
                    return;
                }

                StringBuilder sb = new StringBuilder();
                sb.append("\uD83D\uDC4D\n");
                for (User u : betInfo.getAgree().keySet()) {
                    sb.append(u.getAsTag())
                            .append(": ")
                            .append(betInfo.getAgree().get(u))
                            .append("\n");
                }
                sb.append("\n\uD83D\uDC4E\n");
                for (User u : betInfo.getDisagree().keySet()) {
                    sb.append(u.getAsTag())
                            .append(": ")
                            .append(betInfo.getDisagree().get(u))
                            .append("\n");
                }
                eb.setTitle("- " + betInfo.getTitle())
                        .setAuthor(betInfo.getUser().getAsTag())
                        .setDescription(sb.toString());
                channel.sendMessage(eb.build()).queue();
            }

            // 주사위 굴리기
            if (commandArgs[0].equalsIgnoreCase("dice")) {
                int i = 1;
                StringBuilder sb = new StringBuilder();
                for (User u : users.keySet()) {
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

    private static void showUsers(MessageChannel channel) {
        if (users.keySet().isEmpty()) {
            channel.sendMessage("No Users").queue();
            return;
        }
        int i = 1;
        StringBuilder sb = new StringBuilder();
        for (User u : users.keySet()) {
            if (!u.isBot()) {
                sb.append(i++).append(". ").append(u.getName())
                        .append(" : ")
                        .append(users.get(u))
                        .append("\n");
            }
         }
        eb.setTitle("BetBot User List").setDescription(sb.toString());
        channel.sendMessage(eb.build()).queue();
    }

    private static void showChannelUsers(MessageChannel channel) {
        int i = 1;
        StringBuilder sb = new StringBuilder();
        for (User u : channel.getJDA().getUsers()) {
            if (!u.isBot()) {
                sb.append(i++).append(". ").append(u.getName()).append("\n");
            }
        }
        eb.setTitle("Channel User List").setDescription(sb.toString());
        channel.sendMessage(eb.build()).queue();
    }
}
