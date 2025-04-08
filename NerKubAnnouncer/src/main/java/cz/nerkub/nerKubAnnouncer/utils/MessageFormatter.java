package cz.nerkub.nerKubAnnouncer.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageFormatter {

	public static String format(String message) {
		message = ChatColor.translateAlternateColorCodes('&', message)
				.replace("<br>", "\n")
				.replace("\\n", "\n");

		if (message.contains("<center>")) {
			message = message.replace("<center>", "");
			message = centerText(message);
		}
		return message;
	}

	public static List<BaseComponent[]> parseToMultipleComponents(String input) {
		List<BaseComponent[]> lines = new ArrayList<>();

		input = ChatColor.translateAlternateColorCodes('&', input)
				.replace("<br>", "\n")
				.replace("\\n", "\n");

		String[] splitLines = input.split("\n");

		for (String rawLine : splitLines) {
			if (rawLine == null || rawLine.trim().isEmpty()) {
				lines.add(new ComponentBuilder("").create());
				continue;
			}

			String line = rawLine;
			boolean center = line.contains("<center>");
			if (center) line = line.replace("<center>", "");

			ComponentBuilder builder = new ComponentBuilder();
			int cursor = 0;

			Pattern pattern = Pattern.compile(
					"(<click\\[([^]]+)]><hover\\[([^]]+)]>(.*?)</hover></click>)|" +
							"(<hover\\[([^]]+)]>(.*?)</hover>)|" +
							"(<click\\[([^]]+)]>(.*?)</click>)|" +
							"<#([A-Fa-f0-9]{6})>"
			);

			Matcher matcher = pattern.matcher(line);
			ChatColor currentHex = null;

			while (matcher.find()) {
				String before = line.substring(cursor, matcher.start());
				if (!before.isEmpty()) {
					TextComponent text = new TextComponent(before);
					if (currentHex != null) text.setColor(currentHex);
					builder.append(text);
				}

				if (matcher.group(1) != null) {
					String url = matcher.group(2);
					String hover = matcher.group(3);
					String text = matcher.group(4);
					TextComponent comp = new TextComponent(text);
					comp.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
					comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hover)));
					if (currentHex != null) comp.setColor(currentHex);
					builder.append(comp);
				} else if (matcher.group(5) != null) {
					String hover = matcher.group(6);
					String text = matcher.group(7);
					TextComponent comp = new TextComponent(text);
					comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hover)));
					if (currentHex != null) comp.setColor(currentHex);
					builder.append(comp);
				} else if (matcher.group(8) != null) {
					String url = matcher.group(9);
					String text = matcher.group(10);
					TextComponent comp = new TextComponent(text);
					comp.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
					if (currentHex != null) comp.setColor(currentHex);
					builder.append(comp);
				} else if (matcher.group(11) != null) {
					String hex = matcher.group(11);
					try {
						currentHex = ChatColor.of("#" + hex);
					} catch (IllegalArgumentException e) {
						System.err.println("[NerKubAnnouncer] Invalid hex color: <#" + hex + ">");
					}
				}

				cursor = matcher.end();
			}

			if (cursor < line.length()) {
				TextComponent rest = new TextComponent(line.substring(cursor));
				if (currentHex != null) rest.setColor(currentHex);
				builder.append(rest);
			}

			BaseComponent[] comps = builder.create();
			if (center && comps.length > 0 && comps[0] instanceof TextComponent text) {
				String padding = getCenterPadding(comps);
				text.setText(padding + text.getText());
			}

			lines.add(comps);
		}

		return lines;
	}

	public static String centerText(String input) {
		final int CENTER_PX = 154;
		input = ChatColor.translateAlternateColorCodes('&', input);
		String stripped = ChatColor.stripColor(input);

		int messagePxSize = 0;
		boolean previousCode = false;
		boolean isBold = false;

		for (char c : stripped.toCharArray()) {
			if (c == '§') {
				previousCode = true;
				continue;
			} else if (previousCode) {
				previousCode = false;
				isBold = (c == 'l' || c == 'L');
				continue;
			}
			messagePxSize += isBold ? 6 : 4;
			messagePxSize++;
		}

		int halved = messagePxSize / 2;
		int toCompensate = CENTER_PX - halved;
		int spaceLength = 4;
		int compensated = 0;

		StringBuilder sb = new StringBuilder();
		while (compensated < toCompensate) {
			sb.append(" ");
			compensated += spaceLength;
		}
		return sb.toString() + input;
	}

	private static String getCenterPadding(BaseComponent[] comps) {
		final int CENTER_PX = 154;
		int messagePxSize = 0;

		for (BaseComponent comp : comps) {
			String text = ChatColor.stripColor(comp.toPlainText());
			for (char c : text.toCharArray()) {
				messagePxSize += 4 + 1;
			}
		}

		int halved = messagePxSize / 2;
		int toCompensate = CENTER_PX - halved;
		int spaceLength = 4;
		int compensated = 0;
		StringBuilder sb = new StringBuilder();
		while (compensated < toCompensate) {
			sb.append(" ");
			compensated += spaceLength;
		}
		return sb.toString();
	}
}
