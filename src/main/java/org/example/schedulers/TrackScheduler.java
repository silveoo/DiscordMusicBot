package org.example.schedulers;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.core.object.entity.channel.MessageChannel;
import org.example.command.Command;

import java.util.HashMap;
import java.util.Map;

public final class TrackScheduler implements AudioLoadResultHandler {

    private final AudioPlayer player;
    private final MessageChannel channel;

    public TrackScheduler(final AudioPlayer player, final MessageChannel channel) {
        this.channel = channel;
        this.player = player;
    }

    @Override
    public void trackLoaded(final AudioTrack track) {
        // LavaPlayer found an audio source for us to play
        player.playTrack(track);
        channel.createMessage("Играю композицию: **" + track.getInfo().title+"**").subscribe();
    }

    @Override
    public void playlistLoaded(final AudioPlaylist playlist) {
        // LavaPlayer found multiple AudioTracks from some playlist
    }

    @Override
    public void noMatches() {
        // LavaPlayer did not find any audio to extract
        System.out.println("NO MATCHES FOR THIS URL");
        channel.createMessage("По этой ссылке ничего не найдено, либо данный источник не поддерживается");

    }

    @Override
    public void loadFailed(final FriendlyException exception) {
        // LavaPlayer could not parse an audio source for some reason
        System.out.println("LOAD ERROR");
        channel.createMessage("Произошла ошибка, попробуйте еще раз");
    }
}