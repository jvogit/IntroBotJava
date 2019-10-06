package com.gmail.justinxvopro.introbot.audio;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.gmail.justinxvopro.introbot.Loggable;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import net.dv8tion.jda.api.audio.AudioSendHandler;

public class MixingSendHandler implements AudioSendHandler,Loggable {
  private final StereoPcmAudioMixer mixer = new StereoPcmAudioMixer();
  private final List<AudioPlayer> sounds = new ArrayList<>();
  private byte[] lastData;
  private Queue<AudioTrack> tq = new ConcurrentLinkedQueue<>();
  private Runnable onTrackEnd;
  
  public MixingSendHandler(AudioPlayerManager manager, Runnable onTrackEnd) {
	  this.createAudioPlayers(manager);
	  this.onTrackEnd = onTrackEnd;
  }
  
  public void createAudioPlayers(AudioPlayerManager manager) {
	  for(int i = 0; i < 4; i++) {
		  AudioPlayer player = manager.createPlayer();
		  player.addListener(new TrackHandler());
		  sounds.add(player);
	  }
  }
  
  public void clearAudioPlayers() {
	  tq.clear();
	  sounds.forEach(action -> {
		  action.destroy();
	  });
	  sounds.clear();
  }
  
  public void queue(AudioTrack track) {
	  Optional<AudioPlayer> player = sounds.stream().filter(ap -> ap.getPlayingTrack() == null).findAny();
	  if(player.isPresent()) {
		  player.get().playTrack(track);
	  }else {
		  tq.add(track);
	  }
  }
  
  @Override
  public boolean canProvide() {
    checkFrameData();
    return lastData != null;
  }

  @Override
  public ByteBuffer provide20MsAudio() {
    checkFrameData();

    byte[] data = lastData;
    lastData = null;
//    getLogger().info("Provide 20ms audio");
//    if(data == null) {
//    	getLogger().info("Null! Data");
//    }else {
//    	getLogger().info("DATA BYTE size " + data.length);
//    }
    
    return ByteBuffer.wrap(data);
  }

  @Override
  public boolean isOpus() {
    return false;
  }
  
  public boolean isPlaying() {
	  return sounds.stream().anyMatch(ap -> ap.getPlayingTrack() != null);
  }

  private void checkFrameData() {
    if (lastData == null) {
      lastData = getData();
    }
  }

  private byte[] getData() {
    mixer.reset();

    for (AudioPlayer sound : sounds) {
      AudioFrame frame = sound.provide();

      if (frame != null) {
        mixer.add(frame);
      } else {
      }
    }

    return mixer.get();
  }
  
  private class TrackHandler extends AudioEventAdapter implements Loggable {
	  @Override
	  public void onTrackStart(AudioPlayer player, AudioTrack track) {
		  getLogger().info(player.toString() + " playing " + track.getIdentifier());
	  }
	  @Override
	  public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason reason) {
		  getLogger().info(player.toString() + " end " + track.getIdentifier());
		  AudioTrack tracke = tq.poll();
		  if(tracke!=null) {
			  player.playTrack(tracke);
		  }else {
			  if(!isPlaying()) {
				  onTrackEnd.run();
			  }
		  }
	  }
  }
}
