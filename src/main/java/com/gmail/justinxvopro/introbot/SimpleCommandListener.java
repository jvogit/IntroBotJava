package com.gmail.justinxvopro.introbot;

import java.util.HashSet;
import java.util.Set;

import com.gmail.justinxvopro.introbot.audio.GuildsAudioManager;
import com.gmail.justinxvopro.introbot.models.CommandUtil;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SimpleCommandListener extends ListenerAdapter {
	public static Set<Long> DISABLED = new HashSet<Long>();
	public static boolean GLOBAL_DISABLE = false;

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getMessage().getContentRaw().isEmpty() || event.getMessage().getContentRaw().charAt(0) != '.')
			return;
		if (event.getMember().hasPermission(Permission.VOICE_MUTE_OTHERS)) {
			switch (event.getMessage().getContentRaw().split("\\s+")[0].substring(1)) {
			case "enable":
				DISABLED.remove(event.getGuild().getIdLong());
				event.getTextChannel().sendMessage("Enable IntroBot!").queue();
				break;
			case "disable":
				DISABLED.add(event.getGuild().getIdLong());
				event.getTextChannel().sendMessage("Disabled IntroBot!").queue();
				break;
			case "dis":
				GuildsAudioManager.reset(event.getGuild());
				event.getGuild().getAudioManager().closeAudioConnection();
				break;
			case "help":
				event.getTextChannel().sendMessage(".enable .disable").queue();
				break;
			}
		}
		if (BotCore.getCONFIG().getAdmins().contains(event.getAuthor().getId())) {
			switch (event.getMessage().getContentRaw().split("\\s+")[0].substring(1)) {
			case "globalenable":
				GLOBAL_DISABLE = false;
				event.getTextChannel().sendMessage("Enabled IntroBot!").queue();
				break;
			case "globaldisable":
				GLOBAL_DISABLE = true;
				event.getTextChannel().sendMessage("Disabled IntroBot!").queue();
				break;
			case "dis":
				event.getGuild().getAudioManager().closeAudioConnection();
				break;
			case "help":
				event.getTextChannel().sendMessage(".globalenable .globaldisable").queue();
				break;
			case "reload":
				event.getTextChannel().sendMessage("Reloading. . .").queue(msg -> {
					BotCore.reloadConfig();
					msg.editMessage("Reloaded!").queue();
				});
				break;
			}
		}
		switch (event.getMessage().getContentRaw().split("\\s+")[0].substring(1)) {
		case "skip":
		case "reset":
			GuildsAudioManager.reset(event.getGuild());
			event.getGuild().getAudioManager().closeAudioConnection();
			break;
		case "play":
			if (event.getMember().getVoiceState().inVoiceChannel()) {
				String query = CommandUtil
						.joinArguments(event.getMessage().getContentRaw().substring(".play".length()).split("\\s+"));
				event.getTextChannel().sendMessage("Searching. . . " + query).queue(msg -> {
					GuildsAudioManager.queueTrack(event.getGuild(), event.getMember().getVoiceState().getChannel(),
							"ytsearch:" + query, (track) -> {
								track.ifPresent(t -> {
									msg.editMessage("Playing. . . " + t.getInfo().title).queue();
								});
								if (!track.isPresent()) {
									msg.editMessage("Something went wrong!").queue();
								}
							});
				});
			} else {
				event.getTextChannel().sendMessage("Not in voice channel!").queue();
			}
			break;
		}
	}

}
