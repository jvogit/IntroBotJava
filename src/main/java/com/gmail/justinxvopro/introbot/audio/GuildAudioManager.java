package com.gmail.justinxvopro.introbot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;

public class GuildAudioManager {
	@Getter
	private MixingSendHandler sendHandler;
	private Guild guild;
	
	public GuildAudioManager(AudioPlayerManager manager, Guild g) {
		sendHandler = new MixingSendHandler(manager, () -> {
			g.getAudioManager().closeAudioConnection();
		});
		this.guild = g;
		this.guild.getAudioManager().setSendingHandler(sendHandler);
	}
	
	public void queueTrack(AudioTrack track) {
		sendHandler.queue(track);
	}
}
