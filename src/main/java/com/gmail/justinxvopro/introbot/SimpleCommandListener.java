package com.gmail.justinxvopro.introbot;

import java.util.HashSet;
import java.util.Set;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SimpleCommandListener extends ListenerAdapter {
	public static Set<Long> DISABLED = new HashSet<Long>();
	public static boolean GLOBAL_DISABLE = false;

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getMessage().getContentRaw().charAt(0) != '.')
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
			}
		}
	}

}
