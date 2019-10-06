package com.gmail.justinxvopro.introbot.audio;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

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
	
	public static void reset(Guild guild) {
		Optional.of(audioManagers.get(guild)).ifPresent(GuildAudioManager::reset);
	}

	public static void queueTrack(Guild guild, VoiceChannel channel, File file) {
		queueTrack(guild, channel, file.toPath().toString());
	}
	
	public static void queueTrack(Guild guild, VoiceChannel voice, String identifier, Consumer<Optional<AudioTrack>> onLoad) {
		if (!audioManagers.containsKey(guild)) {
			audioManagers.put(guild, new GuildAudioManager(audioManager, guild));
			LOGGER.info("Created!");
		}
		
		guild.getAudioManager().openAudioConnection(voice);

		audioManager.loadItem(identifier, new AudioLoadResultHandler() {

			@Override
			public void trackLoaded(AudioTrack track) {
				LOGGER.info("Loaded Audio Track {}", track.getInfo().title);
				audioManagers.get(guild).queueTrack(track);
				onLoad.accept(Optional.of(track));
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				LOGGER.info("Playlist Success");
				AudioTrack track = playlist.getTracks().get(0);
				audioManagers.get(guild).queueTrack(track);
				onLoad.accept(Optional.of(track));
			}

			@Override
			public void noMatches() {
				LOGGER.info("No matches!");
				onLoad.accept(Optional.empty());
			}

			@Override
			public void loadFailed(FriendlyException exception) {
				LOGGER.error("Fail load {}", exception.getMessage());
				onLoad.accept(Optional.empty());
			}

		});
	}
	
	public static void queueTrack(Guild guild, VoiceChannel voice, String identifier) {
		queueTrack(guild, voice, identifier, (track)->{});
	}
}
