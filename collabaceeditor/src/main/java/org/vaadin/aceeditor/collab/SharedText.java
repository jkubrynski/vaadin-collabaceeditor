package org.vaadin.aceeditor.collab;

import org.vaadin.diffsync.Shared;
import org.vaadin.diffsync.text.TextDiff;

/**
 * A shared text that can be edited collaboratively.
 */
public class SharedText extends Shared<String, TextDiff> {
	
	public SharedText() {
		this("");
	}
	
	public SharedText(String value) {
		super(value);
	}
	
}
