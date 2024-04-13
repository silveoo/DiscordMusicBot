package org.example;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.AudioChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.voice.AudioProvider;
import org.example.command.Command;
import org.example.provider.LavaPlayerAudioProvider;
import org.example.schedulers.TrackScheduler;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicBot {
    public static void main(String[] args) {
        final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

        playerManager.getConfiguration()
                .setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);

        AudioSourceManagers.registerRemoteSources(playerManager);

        final AudioPlayer player = playerManager.createPlayer();

        AudioProvider provider = new LavaPlayerAudioProvider(player);

        commands.put("join", event -> {
            final Member member = event.getMember().orElse(null);
            if (member != null) {
                final VoiceState voiceState = member.getVoiceState().block();
                if (voiceState != null) {
                    final AudioChannel channel = voiceState.getChannel().block(); //VoiceChannel
                    if (channel != null) {
                        channel.join(spec -> spec.setProvider(provider)).block();
                        // Отправляем сообщение в чат
                        event.getMessage().getChannel().block().createMessage("Захожу в канал **" + channel.getName()+"**").block();
                    }
                }
            }
        });


        commands.put("play", event -> {
            final String content = event.getMessage().getContent();
            final List<String> command = Arrays.asList(content.split(" "));
            final MessageChannel channel = event.getMessage().getChannel().block();
            playerManager.loadItem(command.get(1), new TrackScheduler(player, channel));
            System.out.println("URL: " + command.get(1));

        });

        final GatewayDiscordClient client = DiscordClientBuilder.create(args[0]).build()
                .login()
                .block();

        client.getEventDispatcher().on(MessageCreateEvent.class)
                        .subscribe(event -> {
                            final String content = event.getMessage().getContent();
                            for(final Map.Entry<String, Command> entry: commands.entrySet()) {
                                if(content.startsWith('!' + entry.getKey())){
                                    entry.getValue().execute(event);
                                    break;
                                }
                            }
                        });

        client.onDisconnect().block();
    }

    private static final Map<String, Command> commands = new HashMap<>();

    static {
        commands.put("sunboy", event -> event.getMessage()
                .getChannel().block()
                .createMessage("SAMBOY").block());
    }
}