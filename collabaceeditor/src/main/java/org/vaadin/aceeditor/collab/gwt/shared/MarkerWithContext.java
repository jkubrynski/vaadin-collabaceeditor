package org.vaadin.aceeditor.collab.gwt.shared;

import org.vaadin.aceeditor.gwt.shared.Marker;

public class MarkerWithContext {

	private static final int CONTEXT_LEN = 5;

	private final Marker marker;
	private final String startContext;
	private final String endContext;

	public MarkerWithContext(Marker marker, String startContext,
			String endContext) {
		this.marker = marker;
		this.startContext = startContext;
		this.endContext = endContext;
	}

	public MarkerWithContext(Marker marker, String inText) {
		this.marker = marker;
		this.startContext = createStartContext(marker, inText);
		this.endContext = createEndContext(marker, inText);
	}

	public Marker getMarker() {
		return marker;
	}

	public String getStartContext() {
		return startContext;
	}

	public String getEndContext() {
		return endContext;
	}

	public static String createStartContext(Marker m, String text) {
		if (m.getStart() < 0 || m.getStart() >= text.length() || m.getEnd() < 0
				|| m.getEnd() >= text.length() || m.getStart() > m.getEnd()) {
			return "";
		}
		if (m.getStart() == m.getEnd()) {
			return createEndContext(m, text);
		}
		return text.substring(m.getStart(),
				Math.min(m.getEnd(), m.getStart() + CONTEXT_LEN));
	}

	public static String createEndContext(Marker m, String text) {
		if (m.getEnd() < 0 || m.getEnd() >= text.length()) {
			return "";
		}
		return text.substring(m.getEnd(),
				Math.min(m.getEnd() + CONTEXT_LEN, text.length()));
	}
}