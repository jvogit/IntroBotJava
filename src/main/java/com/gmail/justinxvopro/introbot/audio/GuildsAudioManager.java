package com.gmail.justinxvopro.introbot.audio;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gmail.justinxvopro.introbot.Loggable;
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class GuildsAudioManager implements Loggable {
	private static Map<Guild, GuildAudioManager> audioManagers = new HashMap<>();
	private static AudioPlayerManager audioManager = new DefaultAudioPlayerManager();
	private static Logger LOGGER = LoggerFactory.getLogger(GuildsAudioManager.class);
	static {
		AudioSourceManagers.registerLocalSource(audioManager);
		AudioSourceManagers.registerRemoteSources(audioManager);
		audioManager.getConfiguration().setOutputFormat(StandardAudioDataFormats.DISCORD_PCM_S16_BE);
	}

	public static void populate(Collection<Guild> guilds) {
		guilds.forEach(guild -> {
			audioManagers.put(guild, new GuildAudioManager(audioManager, guild));
		});
	}

	public static void queueTrack(Guild guild, VoiceChannel channel, File file) {
		if (!audioManagers.containsKey(guild)) {
			audioManagers.put(guild, new GuildAudioManager(audioManager, guild));
			LOGGER.info("Created!");
		}

//		if (guild.getSelfMember().getVoiceState().inVoiceChannel()) {
//			if (!audioManagers.get(guild).getSendHandler().isPlaying()) {
//				guild.getAudioManager().openAudioConnection(channel);
//			}
//		} else {
//			guild.getAudioManager().openAudioConnection(channel);
//		}
		
		guild.getAudioManager().openAudioConnection(channel);

		audioManager.loadItem(file.toPath().toString(), new AudioLoadResultHandler() {

			@Override
			public void trackLoaded(AudioTrack track) {
				LOGGER.info("Loaded Audio Track {}", track.getInfo().title);
				audioManagers.get(guild).queueTrack(track);
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
			}

			@Override
			public void noMatches() {
				LOGGER.info("No matches!");
			}

			@Override
			public void loadFailed(FriendlyException exception) {
				LOGGER.error("Fail load {}", exception.getMessage());
			}

		});
	}
}
