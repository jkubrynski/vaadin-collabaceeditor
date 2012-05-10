package org.vaadin.aceeditor.collab;

import java.util.HashMap;

public class User {

	protected static HashMap<String, User> users = new HashMap<String, User>();

	private final String userId;
	private final String name;

	private static Integer latestUserId = 0;

	protected static String newUserId() {
		synchronized (latestUserId) {
			return "" + (++latestUserId);
		}
	}

	public static User getUser(String userId) {
		synchronized (users) {
			return users.get(userId);
		}
	}

	public static User newUser(String name) {
		User user = new User(newUserId(), name);
		synchronized (users) {
			users.put(user.getUserId(), user);
		}
		return user;
	}

	protected User(String userId, String name) {
		this.userId = userId;
		this.name = name;
	}

	public String getUserId() {
		return userId;
	}

	public String getName() {
		return name;
	}

	public String getStyle() {
		return getStyle(userId);
	}

	public static String getStyle(String userId) {
		return "of-user-" + getStyleNumber(userId);
	}

	private static int getStyleNumber(String userId) {
		return userId.hashCode() % 4;
	}

	// private static Pattern nonWordchar = Pattern.compile("[^\\w]");
	// private static boolean isValidUserId(String userId) {
	// return !nonWordchar.matcher(userId).find();
	// }

	@Override
	public boolean equals(Object other) {
		if (other instanceof User) {
			return ((User) other).getUserId().equals(userId);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return userId.hashCode();
	}


}
