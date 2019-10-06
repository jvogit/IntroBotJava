package com.gmail.justinxvopro.introbot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;

public class GuildAudioManager {
	@Getter
	private MixingSendHandler sendHandler;
	private Guild guild;
	private AudioPlayerManager manager;
	
	public GuildAudioManager(AudioPlayerManager manager, Guild g) {
		this.manager = manager;
		sendHandler = new MixingSendHandler(manager, () -> {
			g.getAudioManager().closeAudioConnection();
		});
		this.guild = g;
		this.guild.getAudioManager().setSendingHandler(sendHandler);
	}
	
	public void reset() {
		sendHandler.clearAudioPlayers();
		sendHandler.createAudioPlayers(manager);
	}
	
	public void queueTrack(AudioTrack track) {
		sendHandler.queue(track);
	}
}
