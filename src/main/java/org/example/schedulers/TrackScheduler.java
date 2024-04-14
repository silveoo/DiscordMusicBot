package org.example.schedulers;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.core.object.entity.channel.MessageChannel;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class TrackScheduler implements AudioLoadResultHandler, AudioEventListener {

    private final AudioPlayer player;
    private MessageChannel channel;
    private final BlockingQueue<AudioTrack> queue;

    public TrackScheduler(final AudioPlayer player, final MessageChannel channel) {
        this.channel = channel;
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void setChannel(MessageChannel channel){
        this.channel = channel;
    }

    public void queue(final AudioTrack track) {
        // Проверяем, играет ли уже что-то в AudioPlayer
        if (!player.startTrack(track, true)) {
            // Если что-то играет, добавляем трек в очередь
            boolean offerResult = queue.offer(track);
            System.out.println("Трек добавлен в очередь: " + track.getInfo().title + " Результат: " + offerResult); // Логирование результата добавления
            channel.createMessage("Добавлен в очередь трек **" + track.getInfo().title + "**").subscribe();
        } else {
            System.out.println("Воспроизведение трека: " + track.getInfo().title); // Логирование воспроизведения трека
            channel.createMessage("Играю трек **" + track.getInfo().title + "**").subscribe();
        }
    }


    public void nextTrack() {
        if (!queue.isEmpty()) {
            AudioTrack nextTrack = queue.poll();
            System.out.println("Следующий трек для воспроизведения: " + nextTrack.getInfo().title); // Логирование следующего трека
            channel.createMessage("Играю следующий трек: **" + player.getPlayingTrack().getInfo().title + "**").subscribe();
            player.startTrack(nextTrack, false);
        } else {
            player.stopTrack();
            System.out.println("Очередь пуста"); // Логирование пустой очереди
            channel.createMessage("Треки в очереди закончились");
        }
    }

    public String getQueueInfo(){
        StringBuilder builder = new StringBuilder();
        AudioTrack currentTrack = player.getPlayingTrack();
        int counter = 0;
        if(currentTrack != null)
            builder.append("Сейчас играет: **").append(currentTrack.getInfo().title).append("**").append("\n");
        else builder.append("Сейчас ничего не играет");

        if(!queue.isEmpty()){
            builder.append("Следующие треки в очереди: ").append("\n");
            for(AudioTrack track : queue) {
                counter++;
                builder.append(counter).append(". ").append(track.getInfo().title).append("\n");
            }
        }
        return builder.toString();
    }


    @Override
    public void trackLoaded(final AudioTrack track) {
        // LavaPlayer found an audio source for us to play
        queue(track);
        System.out.println("Загружен трек " + track.getInfo().title);
    }

    @Override
    public void playlistLoaded(final AudioPlaylist playlist) {
        // LavaPlayer found multiple AudioTracks from some playlist
        System.out.println("Загружен плейлист " + playlist.getName());
        for (final AudioTrack track : playlist.getTracks()) {
            queue(track); // Добавляем все треки из плейлиста в очередь
        }
        channel.createMessage("Добавлен плейлист: **" + playlist.getName() + "**").subscribe();
    }


    @Override
    public void noMatches() {
        // LavaPlayer did not find any audio to extract
        System.out.println("NO MATCHES FOR THIS URL");
        channel.createMessage("По этой ссылке **ничего не найдено**, либо данный источник не поддерживается");

    }

    @Override
    public void loadFailed(final FriendlyException exception) {
        // LavaPlayer could not parse an audio source for some reason
        System.out.println("LOAD ERROR");
        channel.createMessage("Произошла **ошибка**, попробуйте еще раз");
    }


    @Override
    public void onEvent(AudioEvent event) {
        System.out.println("Событие: " + event.getClass().getSimpleName()); // Логирование типа события
        if (event instanceof TrackEndEvent) {
            TrackEndEvent trackEndEvent = (TrackEndEvent) event;
            System.out.println("Трек закончился: " + trackEndEvent.track.getInfo().title); // Логирование окончания трека
            if (trackEndEvent.endReason.mayStartNext) {
                nextTrack(); // Вызываем метод для воспроизведения следующего трека в очереди
            }
        }
    }

}