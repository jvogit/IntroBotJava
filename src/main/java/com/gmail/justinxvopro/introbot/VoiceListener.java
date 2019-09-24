package com.gmail.justinxvopro.introbot;

import java.io.File;

import com.gmail.justinxvopro.introbot.audio.GuildsAudioManager;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.EventListener;

public class VoiceListener implements EventListener, Loggable {

	@Override
	public void onEvent(GenericEvent event) {
		if(!(event instanceof GuildVoiceUpdateEvent)) return;
		
		GuildVoiceUpdateEvent ve = (GuildVoiceUpdateEvent) event;
		
		if(ve.getChannelJoined() == null) return;
		if(ve.getEntity().equals(ve.getEntity().getGuild().getSelfMember())) return;
		if(SimpleCommandListener.GLOBAL_DISABLE || SimpleCommandListener.DISABLED.contains(ve.getEntity().getGuild().getIdLong())) return;
		if(!BotCore.getCONFIG().getMembers().containsKey(ve.getEntity().getId())) return;
		
		getLogger().info("Detected {} : {}", ve.getEntity().getId(), ve.getEntity().getEffectiveName());
		File path = new File(BotCore.getCONFIG().getMembers().get(ve.getEntity().getId()));
		GuildsAudioManager.queueTrack(ve.getEntity().getGuild(), ve.getChannelJoined(), path);
	}

}
