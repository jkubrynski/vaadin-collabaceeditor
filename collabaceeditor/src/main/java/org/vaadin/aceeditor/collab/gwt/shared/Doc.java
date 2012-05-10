package org.vaadin.aceeditor.collab.gwt.shared;

import java.util.Collections;
import java.util.Map;

import org.vaadin.aceeditor.gwt.shared.Marker;

/**
 * Doc = text + {@link Marker Markers} in the text.
 * 
 */
public class Doc {

	private final String text;
	private final Map<String, Marker> markers;

	private static final Doc EMPTY_DOC = new Doc("");

	public static Doc emptyDoc() {
		return EMPTY_DOC;
	}

	public Doc() {
		this.text = "";
		this.markers = Collections.emptyMap();
	}

	public Doc(String text) {
		this.text = text;
		this.markers = Collections.emptyMap();
	}

	public Doc(String text, Map<String, Marker> markers) {
		this.text = text;
		this.markers = markers;
	}

	public String getText() {
		return text;
	}

	public Map<String, Marker> getMarkers() {
		return Collections.unmodifiableMap(markers);
	}

	@Override
	public String toString() {
		return text + "\n" + markers.toString();
	}
}
