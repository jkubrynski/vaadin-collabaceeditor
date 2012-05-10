package org.vaadin.aceeditor.collab.gwt.shared;

import org.vaadin.aceeditor.gwt.shared.Marker;
import org.vaadin.diffsync.gwt.shared.Diff;

public class MarkerDiff implements Diff<Marker> {

	private final int dStart;
	private final int dEnd;

	private static final MarkerDiff IDENTITY = new MarkerDiff(0, 0);

	public static MarkerDiff create(int dStart, int dEnd) {
		if (dStart == 0 && dEnd == 0) {
			return IDENTITY;
		}
		return new MarkerDiff(dStart, dEnd);
	}

	public static MarkerDiff diff(Marker m1, Marker m2) {
		if (m1.getStart() == m2.getStart() && m1.getEnd() == m2.getEnd()) {
			return IDENTITY;
		}
		return new MarkerDiff(m1, m2);
	}

	private MarkerDiff(int dStart, int dEnd) {
		this.dStart = dStart;
		this.dEnd = dEnd;
	}

	private MarkerDiff(Marker m1, Marker m2) {
		dStart = m2.getStart() - m1.getStart();
		dEnd = m2.getEnd() - m1.getEnd();
	}

//	@Override
	public Marker applyTo(Marker value) {
		return value.withNewPos(value.getStart() + dStart, value.getEnd()
				+ dEnd);
	}

//	@Override
	public boolean isIdentity() {
		return dStart == 0 && dEnd == 0;
	}

	@Override
	public String toString() {
		return Integer.toString(dStart) + "," + Integer.toString(dEnd);
	}

	public static MarkerDiff fromString(String s) {
		String[] items = s.split(",", 2);
		return new MarkerDiff(Integer.valueOf(items[0]),
				Integer.valueOf(items[1]));
	}

	public int getStartDiff() {
		return dStart;
	}

	public int getEndDiff() {
		return dEnd;
	}

}
