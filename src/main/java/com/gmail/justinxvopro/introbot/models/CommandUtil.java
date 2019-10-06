package com.gmail.justinxvopro.introbot.models;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandUtil {
	public static String joinArguments(String[] s) {
		return Stream.of(s).collect(Collectors.joining(" "));
	}
}
