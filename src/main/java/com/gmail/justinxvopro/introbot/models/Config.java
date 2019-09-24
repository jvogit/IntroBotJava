package com.gmail.justinxvopro.introbot.models;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Config {
	private String token;
	private Map<String, String> members;
	private List<String> admins;
}
