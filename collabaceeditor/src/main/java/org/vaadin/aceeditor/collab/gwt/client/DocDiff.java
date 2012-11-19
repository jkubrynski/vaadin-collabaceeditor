package org.vaadin.aceeditor.collab.gwt.client;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.vaadin.aceeditor.collab.gwt.shared.Doc;
import org.vaadin.aceeditor.collab.gwt.shared.MarkerDiff;
import org.vaadin.aceeditor.collab.gwt.shared.MarkerWithContext;
import org.vaadin.aceeditor.gwt.shared.Marker;
import org.vaadin.diffsync.gwt.client.GwtTextDiff;
import org.vaadin.diffsync.gwt.shared.Diff;

public class DocDiff implements Diff<Doc> {

	private final GwtTextDiff td;
	private final Map<String, MarkerWithContext> added;
	private final Map<String, MarkerDiff> moved;
	private final Collection<String> removed;

	public static final DocDiff IDENTITY = new DocDiff(GwtTextDiff.IDENTITY);
	private static final Map<String, MarkerWithContext> NO_ADDED = Collections
			.emptyMap();
	private static final Map<String, MarkerDiff> NO_MOVED = Collections
			.emptyMap();

	// private static final Collection<String> NO_REMOVED =
	// Collections.emptyList();

	public static DocDiff create(GwtTextDiff td) {
		return new DocDiff(td);
	}

	public static DocDiff create(GwtTextDiff td,
			Map<String, MarkerWithContext> addedMarkers,
			Map<String, MarkerDiff> movedMarkers,
			Collection<String> deletedMarkers) {
		return new DocDiff(td, addedMarkers, movedMarkers, deletedMarkers);
	}

	public static DocDiff removeMarker(String markerId) {
		List<String> removed = Collections.singletonList(markerId);
		return new DocDiff(GwtTextDiff.IDENTITY, NO_ADDED, NO_MOVED, removed);
	}

	public static DocDiff diff(Doc doc1, Doc doc2) {
		GwtTextDiff td = GwtTextDiff.diff(doc1.getText(), doc2.getText());
		String text2 = doc2.getText();

		Map<String, Marker> m1 = doc1.getMarkers();
		Map<String, Marker> m2 = doc2.getMarkers();

		Map<String, MarkerWithContext> added = new HashMap<String, MarkerWithContext>();
		Map<String, MarkerDiff> diffs = new HashMap<String, MarkerDiff>();
		for (Entry<String, Marker> e : m2.entrySet()) {
			Marker c1 = m1.get(e.getKey());
			if (c1 != null) {
				MarkerDiff d = MarkerDiff.diff(c1, e.getValue());
				if (!d.isIdentity()) {
					diffs.put(e.getKey(), d);
				}
			} else {
				added.put(e.getKey(),
						new MarkerWithContext(e.getValue(), text2));
			}
		}

		Collection<String> removedIds = new LinkedList<String>(m1.keySet());
		removedIds.removeAll(m2.keySet());

		return new DocDiff(td, added, diffs, removedIds);
	}

	private DocDiff(GwtTextDiff td) {
		this.td = td;
		this.added = Collections.emptyMap();
		this.moved = Collections.emptyMap();
		this.removed = Collections.emptyList();
	}

	private DocDiff(GwtTextDiff td, Map<String, MarkerWithContext> addedMarkers,
			Map<String, MarkerDiff> movedMarkers,
			Collection<String> deletedMarkers) {
		this.td = td;
		this.added = addedMarkers;
		this.moved = movedMarkers;
		this.removed = deletedMarkers;
	}

	public GwtTextDiff getTextDiff() {
		return td;
	}

//	@Override
	public Doc applyTo(Doc doc) {

		final String text1 = doc.getText();
		final String text2 = td.applyTo(text1);

		Map<String, Marker> m1 = doc.getMarkers();
		Map<String, Marker> m2 = new HashMap<String, Marker>();
		for (Entry<String, MarkerWithContext> e : added.entrySet()) {
			Marker existing = m1.get(e.getKey());
			if (existing == null) {
				Marker adjusted = adjustMarkerBasedOnContext(e.getValue(),
						text2);
				if (adjusted != null) {
					m2.put(e.getKey(), adjusted);
				}
			}
		}

		for (Entry<String, Marker> e : m1.entrySet()) {
			if (removed.contains(e.getKey())) {
				continue;
			}
			MarkerDiff md = moved.get(e.getKey());
			if (md != null) {
				m2.put(e.getKey(), md.applyTo(e.getValue()));
			} else {
				m2.put(e.getKey(), e.getValue());
			}
		}

		return new Doc(text2, m2);
	}

//	@Override
	public boolean isIdentity() {
		return td.isIdentity() && added.isEmpty() && moved.isEmpty()
				&& removed.isEmpty();
	}

	public boolean markersChanged() {
		return !(added.isEmpty() && moved.isEmpty() && removed.isEmpty());
	}

	public Map<String, MarkerWithContext> getAddedMarkersAsUnmodifiable() {
		return Collections.unmodifiableMap(added);
	}

	public Collection<String> getRemovedMarkerIdsAsUnmodifiable() {
		return Collections.unmodifiableCollection(removed);
	}

	public Map<String, MarkerDiff> getMarkerDiffsAsUnmodifiable() {
		return Collections.unmodifiableMap(moved);
	}

	public static Marker adjustMarkerBasedOnContext(MarkerWithContext ma,
			String text) {
		int start = GwtTextDiff.getDMP().match_main(text, ma.getStartContext(),
				ma.getMarker().getStart());
		if (start == -1) {
			return null;
		}
		int end = GwtTextDiff.getDMP().match_main(text, ma.getEndContext(),
				ma.getMarker().getEnd());
		if (end == -1) {
			return null;
		}
		return ma.getMarker().withNewPos(start, end);
	}
}
