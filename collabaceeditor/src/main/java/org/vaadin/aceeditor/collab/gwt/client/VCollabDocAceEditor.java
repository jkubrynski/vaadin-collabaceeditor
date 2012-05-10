package org.vaadin.aceeditor.collab.gwt.client;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.vaadin.aceeditor.collab.gwt.shared.Doc;
import org.vaadin.aceeditor.collab.gwt.shared.MarkerDiff;
import org.vaadin.aceeditor.collab.gwt.shared.MarkerWithContext;
import org.vaadin.aceeditor.gwt.client.AceMarkerEditorFacade;
import org.vaadin.aceeditor.gwt.client.EditorFacade.CursorChangeListener;
import org.vaadin.aceeditor.gwt.client.EditorFacade.SelectionChangeListener;
import org.vaadin.aceeditor.gwt.client.EditorFacade.TextChangeListener;
import org.vaadin.aceeditor.gwt.client.MarkerEditorFacade;
import org.vaadin.aceeditor.gwt.shared.Marker;
import org.vaadin.aceeditor.gwt.shared.Util;
import org.vaadin.diffsync.gwt.client.TextDiff;
import org.vaadin.diffsync.gwt.client.VAbstractDiffSyncComponent;

import com.google.gwt.core.client.JsArrayMixed;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;

public class VCollabDocAceEditor extends
		VAbstractDiffSyncComponent<Doc, DocDiff> implements TextChangeListener,
		CursorChangeListener, SelectionChangeListener {

	protected MarkerEditorFacade markerEditor = new AceMarkerEditorFacade();

	private boolean editing = false;
	private String currentEdit;
	private Date currentEditDate;

	private boolean listenersAdded = false;

	private boolean listeningSelections;

	private boolean selectionChanged;

	private String userId;

	private String userStyle;

	public VCollabDocAceEditor() {
		markerEditor.initializeEditor();
		initWidget(markerEditor.getWidget());
	}

	private void newEditMarker() {
		removeEditMarker();
		createEditMarker();
		editing = true;
	}

	private void removeEditMarker() {
		;
		if (currentEdit != null) {
			markerEditor.removeMarker(currentEdit);
		}
	}

	private void createEditMarker() {
		String mid = newItemId();
		currentEdit = mid;
		int cursor = markerEditor.getCursor();
		markerEditor.putMarker(mid,
				Marker.newEditMarker(cursor, cursor + 1, userId, userStyle));
	}

	@Override
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		if (client.updateComponent(this, uidl, true)) {
			return;
		}
		if (!listenersAdded) {
			addListeners();
		}

		if (uidl.hasAttribute("ace-scl")) {
			setListeningSelections(uidl.getBooleanAttribute("ace-scl"));
		}

		if (uidl.hasAttribute("userid")) {
			this.userId = uidl.getStringAttribute("userid");
			markerEditor.setUserId(userId);
			this.userStyle = uidl.getStringAttribute("userstyle");
		} else {
			this.userId = null;
			markerEditor.setUserId(null);
			this.userStyle = null;
		}

		markerEditor.settingsFromUIDL(uidl);

		if (uidl.hasAttribute("amts-type")) {
			Marker.Type type = Marker.Type.valueOf(uidl
					.getStringAttribute("amts-type"));
			String dataStr = uidl.getStringAttribute("amts-data");
			int[] sel = markerEditor.getSelection();
			if (sel[0] != sel[1]) {
				Marker m = new Marker(type, Math.min(sel[0], sel[1]), Math.max(
						sel[0], sel[1]), dataStr);
				markerEditor.putMarker(newItemId(), m);
				if (listeningSelections) {
					selectionChanged = true;
				}
				valueChanged();
			}

		}

		// Is this the proper place to call super.updateFromUIDL?
		super.updateFromUIDL(uidl, client);

		if (uidl.hasAttribute("scrolltopos")) {
			int scrollTo = uidl.getIntAttribute("scrolltopos");
			markerEditor.scrollTo(scrollTo, false);
		}
		if (uidl.hasAttribute("scrolltomarker")) {
			String mid = uidl.getStringAttribute("scrolltomarker");
			Marker m = markerEditor.getMarker(mid);
			if (m != null) {
				markerEditor.scrollTo(m.getStart(), false);
			}
		}

		markerEditor.setReadOnly(uidl.hasAttribute("readonly"));
	}

	private void setListeningSelections(boolean listening) {
		if (listeningSelections == listening) {
			return;
		}
		if (listening) {
			markerEditor.addListener((SelectionChangeListener) this);
		}
		listeningSelections = listening;
	}

	private void addListeners() {
		markerEditor.addListener((TextChangeListener) this);
		markerEditor.addListener((CursorChangeListener) this);
		listenersAdded = true;
	}

//	@Override
	public void textChanged() {
//		VConsole.log("textChanged");
		if (userId != null) {
			updateEditMarker();
		}
		if (listeningSelections) {
			selectionChanged = true;
		}
		valueChanged();
	}

	private void updateEditMarker() {
		if (!editing) {
			newEditMarker();
		} else if (currentEditDate.getTime() + 3000 < (new Date()).getTime()) {
//			VConsole.log("timeout removing --- ");
			newEditMarker();
		}
		currentEditDate = new Date();
	}

//	@Override
	public void cursorChanged() {
		if (currentEdit != null) {
			Marker em = markerEditor.getMarker(currentEdit);
			if (em == null || !em.touches(markerEditor.getCursor())) {
				editing = false;
			}
		}
		if (listeningSelections) {
			selectionChanged = true;
			valueChanged();
		}
	}

//	@Override
	public void selectionChanged() {
		if (listeningSelections) {
			selectionChanged = true;
			valueChanged();
		}
	}

	@Override
	protected void applyDiff(DocDiff diff) {
		String oldText = markerEditor.getText();
		String newText = diff.getTextDiff().applyTo(oldText);
		boolean textChanged = !diff.getTextDiff().isIdentity();

		JsArrayMixed realDiffs = null;
		if (textChanged) {
			realDiffs = TextDiff.getDMP().diff_main(oldText, newText);
		}

		Map<String, Marker> oldMarkers = markerEditor.getMarkers();

		for (String mid : diff.getRemovedMarkerIdsAsUnmodifiable()) {
			oldMarkers.remove(mid);
		}

		Map<String, MarkerDiff> diffed = diff.getMarkerDiffsAsUnmodifiable();
		for (Entry<String, Marker> e : oldMarkers.entrySet()) {
			MarkerDiff md = diffed.get(e.getKey());
			if (md == null) {
				oldMarkers.put(e.getKey(),
						markerMovedByDiffs(e.getValue(), realDiffs));
			} else {
				oldMarkers.put(e.getKey(), md.applyTo(e.getValue()));
			}
		}
		for (Entry<String, MarkerWithContext> e : diff
				.getAddedMarkersAsUnmodifiable().entrySet()) {
			Marker m = DocDiff
					.adjustMarkerBasedOnContext(e.getValue(), newText);
			oldMarkers.put(e.getKey(), m);
		}

		markerEditor.clearMarkers();
		if (textChanged) {
			int newCursor = TextDiff.getDMP().diff_xIndex(realDiffs,
					markerEditor.getCursor());
			int[] oldSel = markerEditor.getSelection();
			int newSelStart = TextDiff.getDMP().diff_xIndex(realDiffs,
					oldSel[0]);
			int newSelEnd = TextDiff.getDMP().diff_xIndex(realDiffs, oldSel[1]);
			markerEditor.setTextCursorSelection(newText, newCursor,
					newSelStart, newSelEnd, false);
		}
		for (Entry<String, Marker> e : oldMarkers.entrySet()) {
			markerEditor.putMarker(e.getKey(), e.getValue());
		}

		if (listeningSelections) {
			selectionChanged = true;
		}
	}

	private static Marker markerMovedByDiffs(Marker m, JsArrayMixed diffs) {
		if (diffs == null) {
			return m;
		}
		int start = TextDiff.getDMP().diff_xIndex(diffs, m.getStart());
		int end = TextDiff.getDMP().diff_xIndex(diffs, m.getEnd());
		return m.withNewPos(start, end);
	}

	@Override
	protected void beforeSend() {
//		VConsole.log("beforeSend " + selectionChanged);
		if (selectionChanged) {
			int[] sel = markerEditor.getSelection();
			getClient().updateVariable(getPaintableId(), "ace-selstart",
					sel[0], false);
			getClient().updateVariable(getPaintableId(), "ace-selend", sel[1],
					false);
			selectionChanged = false;
		}
	}

	@Override
	protected Doc getValue() {
		return new Doc(markerEditor.getText(), markerEditor.getMarkers());
	}

	@Override
	protected Doc initialValue() {
		return Doc.emptyDoc();
	}

	@Override
	protected DocDiff diff(Doc v1, Doc v2) {
		return DocDiff.diff(v1, v2);
	}

	@Override
	protected void diffToClient(DocDiff diff, ApplicationConnection client,
			String paintableId, boolean immediate) {
		addedToClient(diff, client, paintableId);
		movedToClient(diff, client, paintableId);
		removedToClient(diff, client, paintableId);

		client.updateVariable(paintableId, "dt",
				diff.getTextDiff().getDiffString(), immediate);
	}

	private void addedToClient(DocDiff diff, ApplicationConnection client,
			String paintableId) {
		Map<String, MarkerWithContext> ams = diff
				.getAddedMarkersAsUnmodifiable();
		if (ams.isEmpty()) {
			return;
		}
		String[] addedIds = new String[ams.size()];
		String[] addedTypes = new String[ams.size()];
		String[] addedDatas = new String[ams.size()];
		String[] addedStarts = new String[ams.size()];
		String[] addedEnds = new String[ams.size()];
		String[] addedStartContexts = new String[ams.size()];
		String[] addedEndContexts = new String[ams.size()];
		int i = 0;
		for (Entry<String, MarkerWithContext> e : ams.entrySet()) {
			Marker m = e.getValue().getMarker();
			addedIds[i] = e.getKey();
			addedTypes[i] = "" + m.getType();
			if (m.getData() != null) {
				addedDatas[i] = m.getData().getDataString();
			} else {
				addedDatas[i] = ""; // ?
			}
			addedStarts[i] = "" + m.getStart();
			addedEnds[i] = "" + m.getEnd();
			addedStartContexts[i] = e.getValue().getStartContext();
			addedEndContexts[i] = e.getValue().getEndContext();
			i++;
		}
		client.updateVariable(paintableId, "mai", addedIds, false);
		client.updateVariable(paintableId, "mat", addedTypes, false);
		client.updateVariable(paintableId, "mas", addedStarts, false);
		client.updateVariable(paintableId, "mae", addedEnds, false);
		client.updateVariable(paintableId, "masc", addedStartContexts, false);
		client.updateVariable(paintableId, "maec", addedEndContexts, false);
		client.updateVariable(paintableId, "mad", addedDatas, false);

	}

	private void movedToClient(DocDiff diff, ApplicationConnection client,
			String paintableId) {
		Map<String, MarkerDiff> mds = diff.getMarkerDiffsAsUnmodifiable();
		if (mds.isEmpty()) {
			return;
		}
		String[] movedIds = new String[mds.size()];
		String[] movedStarts = new String[mds.size()];
		String[] movedEnds = new String[mds.size()];
		int i = 0;
		for (Entry<String, MarkerDiff> e : mds.entrySet()) {
			movedIds[i] = e.getKey();
			movedStarts[i] = "" + e.getValue().getStartDiff();
			movedEnds[i] = "" + e.getValue().getEndDiff();
			i++;
		}
		client.updateVariable(paintableId, "mmi", movedIds, false);
		client.updateVariable(paintableId, "mms", movedStarts, false);
		client.updateVariable(paintableId, "mme", movedEnds, false);

	}

	private void removedToClient(DocDiff diff, ApplicationConnection client,
			String paintableId) {
		Collection<String> rems = diff.getRemovedMarkerIdsAsUnmodifiable();
		if (rems.isEmpty()) {
			return;
		}
		String[] removedIds = new String[rems.size()];
		int i = 0;
		for (String rem : rems) {
			removedIds[i++] = rem;
		}
		client.updateVariable(paintableId, "mri", removedIds, false);
	}

	@Override
	protected DocDiff diffFromUIDL(UIDL uidl) {
		if (uidl.hasVariable("dt")) {
			Map<String, MarkerWithContext> added = addedFromUIDL(uidl);
			Map<String, MarkerDiff> moved = movedFromUIDL(uidl);
			Collection<String> removedIds = removedFromUIDL(uidl);
			TextDiff td = TextDiff.fromString(uidl.getStringVariable("dt"));
			return DocDiff.create(td, added, moved, removedIds);
		}
		return null;
	}
	
	// "mXY"
	// X: a=added, m=moved, r=removed
	// Y: i=id, t=type, d=data, s=start, sc=startcontext, e=end, ec=endcontext

	private Map<String, MarkerWithContext> addedFromUIDL(UIDL uidl) {
		if (!uidl.hasVariable("mai")) {
			return Collections.emptyMap();
		}
		String[] addedIds = uidl.getStringArrayVariable("mai");
		String[] ts = uidl.getStringArrayVariable("mat");
		String[] ds = uidl.getStringArrayVariable("mad");
		String[] ss = uidl.getStringArrayVariable("mas");
		String[] scs = uidl.getStringArrayVariable("masc");
		String[] es = uidl.getStringArrayVariable("mae");
		String[] ecs = uidl.getStringArrayVariable("maec");
		HashMap<String, MarkerWithContext> ret = new HashMap<String, MarkerWithContext>();
		for (int i = 0; i < addedIds.length; ++i) {
			Marker m = new Marker(Marker.Type.valueOf(ts[i]),
					Integer.valueOf(ss[i]), Integer.valueOf(es[i]), ds[i]);
			MarkerWithContext ma = new MarkerWithContext(m, scs[i], ecs[i]);
			ret.put(addedIds[i], ma);
		}
		return ret;
	}

	private Map<String, MarkerDiff> movedFromUIDL(UIDL uidl) {
		if (!uidl.hasVariable("mmi")) {
			return Collections.emptyMap();
		}
		String[] movedIds = uidl.getStringArrayVariable("mmi");
		String[] movedStarts = uidl.getStringArrayVariable("mms");
		String[] movedEnds = uidl.getStringArrayVariable("mme");
		HashMap<String, MarkerDiff> ret = new HashMap<String, MarkerDiff>();
		for (int i = 0; i < movedIds.length; ++i) {
			ret.put(movedIds[i],
					MarkerDiff.create(Integer.valueOf(movedStarts[i]),
							Integer.valueOf(movedEnds[i])));
		}
		return ret;
	}

	private Collection<String> removedFromUIDL(UIDL uidl) {
		if (!uidl.hasVariable("mri")) {
			return Collections.emptyList();
		}
		String[] removed = uidl.getStringArrayVariable("mri");
		LinkedList<String> ret = new LinkedList<String>();
		for (String remId : removed) {
			ret.add(remId);
		}
		return ret;
	}

}
