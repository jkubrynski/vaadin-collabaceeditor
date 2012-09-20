package org.vaadin.aceeditor.collab;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.vaadin.aceeditor.ErrorChecker;
import org.vaadin.aceeditor.collab.gwt.shared.Doc;
import org.vaadin.aceeditor.collab.gwt.shared.MarkerDiff;
import org.vaadin.aceeditor.collab.gwt.shared.MarkerWithContext;
import org.vaadin.aceeditor.gwt.shared.Marker;
import org.vaadin.diffsync.DiffCalculator;
import org.vaadin.diffsync.TextDiff;

public class ErrorCheckTask implements DiffCalculator<Doc, DocDiff> {

	private long latestErrorId = 0;

	private String nextErrorId() {
		return collabId + "-" + (++latestErrorId);
	}

	private final long collabId;
	private final ErrorChecker checker;

	public ErrorCheckTask(long collabId, ErrorChecker checker) {
		this.collabId = collabId;
		this.checker = checker;
	}

//	@Override
	public boolean needsToRunAfter(DocDiff diff, long byCollaboratorId) {
		return diff==null || !diff.getTextDiff().isIdentity();
	}

//	@Override
	public DocDiff calcDiff(Doc value) throws InterruptedException {

		LinkedList<String> earlierErrors = new LinkedList<String>();
		for (Entry<String, Marker> e : value.getMarkers().entrySet()) {
			if (e.getValue().getType() == Marker.Type.ERROR) {
				earlierErrors.add(e.getKey());
			}
		}
		String text = value.getText();

		Collection<Marker> errors = checker.getErrors(text);

		HashMap<String, MarkerWithContext> added = new HashMap<String, MarkerWithContext>();
		for (Marker em : errors) {
			MarkerWithContext mwc = new MarkerWithContext(em, text);
			added.put(nextErrorId(), mwc);
		}

		// ...
		DocDiff dd = DocDiff.create(TextDiff.IDENTITY, added,
				new HashMap<String, MarkerDiff>(), earlierErrors);
		return dd;
	}
}
