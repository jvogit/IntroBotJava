package com.gmail.justinxvopro.introbot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmail.justinxvopro.introbot.models.Config;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class BotCore extends ListenerAdapter {
	@Getter
	private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	@Getter
	private static JDA jda;
	private static final File CONFIG_FILE = new File("config.json");
	@Getter
	private static Config CONFIG;
	private static Logger LOGGER = LoggerFactory.getLogger(BotCore.class);

	public static void main(String args[]) {

		try {
			CONFIG = generateConfigFile();
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(1);
		}

		String token = findTokenFromArguments(args).orElseGet(CONFIG::getToken);
		try {
			jda = new JDABuilder().setToken(token).setAudioSendFactory(new NativeAudioSendFactory())
					.addEventListeners(new VoiceListener(), new SimpleCommandListener()).build();
			LOGGER.info(jda.getInviteUrl(Permission.VOICE_CONNECT));
		} catch (LoginException e) {
			e.printStackTrace();
		}
	}
	
	public static void reloadConfig() {
		try {
			CONFIG = generateConfigFile();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static Optional<String> findTokenFromArguments(String args[]) {
		if (args.length >= 2) {
			for (int i = 0; i < args.length - 1; i++) {
				if (args[i].equalsIgnoreCase("-token"))
					return Optional.of(args[i + 1]);
			}
		}

		return Optional.empty();
	}

	private static Config generateConfigFile() throws IOException {
		if (!CONFIG_FILE.exists()) {
			Files.copy(BotCore.class.getResourceAsStream("/config.json"), CONFIG_FILE.toPath());
		}

		return OBJECT_MAPPER.readValue(CONFIG_FILE, Config.class);
	}

	@Override
	public void onReady(ReadyEvent event) {
		CONFIG.getMembers().forEach((x, y) -> {
			LOGGER.info("{} : {}", x, y);
		});
	}

}
