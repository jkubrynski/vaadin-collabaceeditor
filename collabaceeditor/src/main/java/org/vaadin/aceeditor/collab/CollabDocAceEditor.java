package org.vaadin.aceeditor.collab;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.vaadin.aceeditor.SelectionChangeListener;
import org.vaadin.aceeditor.collab.gwt.shared.Doc;
import org.vaadin.aceeditor.collab.gwt.shared.MarkerDiff;
import org.vaadin.aceeditor.collab.gwt.shared.MarkerWithContext;
import org.vaadin.aceeditor.gwt.ace.AceMode;
import org.vaadin.aceeditor.gwt.ace.AceTheme;
import org.vaadin.aceeditor.gwt.shared.Marker;
import org.vaadin.aceeditor.gwt.shared.Util;
import org.vaadin.diffsync.AbstractDiffSyncComponent;
import org.vaadin.diffsync.Shared;
import org.vaadin.diffsync.TextDiff;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;

/**
 * Enables multiple people to edit the same
 * {@link org.vaadin.aceeditor.collab.gwt.shared.Doc Doc} in Ace editor.
 * 
 * @see <a href="http://ace.ajax.org">Ace Editor</a>
 * @see <a href="http://code.google.com/p/google-diff-match-patch/">The text
 *      diff algorithm used.</a>
 */
@SuppressWarnings("serial")
@com.vaadin.ui.ClientWidget(org.vaadin.aceeditor.collab.gwt.client.VCollabDocAceEditor.class)
public class CollabDocAceEditor extends
		AbstractDiffSyncComponent<Doc, DocDiff> {

	private User user;
	private AceMode mode = null;
	private AceTheme theme = null;
	private String fontSize = null;
	private Boolean hScrollVisible = false;
	private String modeFileURL;
	private String themeFileURL;
	private LinkedList<SelectionChangeListener> scListeners = new LinkedList<SelectionChangeListener>();
	private Marker addMarkerToSelection;
	private int scrollToPosition;
	private String scrollToMarkerId;

	public CollabDocAceEditor(Shared<Doc, DocDiff> shared) {
		super(shared);
		setWidth("400px");
		setHeight("300px");
	}

	/**
	 * 
	 */
	public AceMode getMode() {
		return mode;
	}

	/**
	 * Sets the Ace mode.
	 * 
	 * <p>
	 * NOTE: The corresponding mode JavaScript file must be loaded. If it's not
	 * already, use {@link #setMode(AceMode, String)} with the file URL.
	 * </p>
	 * 
	 * @param mode
	 */
	public void setMode(AceMode mode) {
		setMode(mode, null);
	}

	/**
	 * Sets the Ace mode after loading it from the given URL (if necessary).
	 * 
	 * @param mode
	 * @param modeFileURL
	 */
	public void setMode(AceMode mode, String modeFileURL) {
		this.mode = mode;
		this.modeFileURL = modeFileURL;
		requestRepaint();
	}

	/**
	 * 
	 */
	public AceTheme getTheme() {
		return theme;
	}

	/**
	 * Sets the Ace theme.
	 * 
	 * <p>
	 * NOTE: The corresponding theme JavaScript file must be loaded. If it's not
	 * already, use {@link #setTheme(AceTheme, String)} with the file URL.
	 * </p>
	 * 
	 * @param theme
	 */
	public void setTheme(AceTheme theme) {
		setTheme(theme, null);
	}

	/**
	 * Sets the Ace theme after loading it from the given URL (if necessary).
	 * 
	 * @param theme
	 * @param themeFileURL
	 */
	public void setTheme(AceTheme theme, String themeFileURL) {
		this.theme = theme;
		this.themeFileURL = themeFileURL;
		requestRepaint();
	}

	/**
	 * 
	 */
	public String getFontSize() {
		return fontSize;
	}

	/**
	 * 
	 * @param fontSize
	 */
	public void setFontSize(String fontSize) {
		this.fontSize = fontSize;
		requestRepaint();
	}

	/**
	 * 
	 */
	public Boolean gethScrollVisible() {
		return hScrollVisible;
	}

	/**
	 * 
	 * @param hScrollVisible
	 */
	public void sethScrollVisible(Boolean hScrollVisible) {
		this.hScrollVisible = hScrollVisible;
		requestRepaint();
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
		requestRepaint();
	}

	public void addListener(SelectionChangeListener scl) {
		scListeners.add(scl);
		if (scListeners.size() == 1) {
			requestRepaint();
		}
	}

	public void removeListener(SelectionChangeListener scl) {
		scListeners.remove(scl);
		if (scListeners.size() == 0) {
			requestRepaint();
		}
	}

	public void addMarkerToSelection(Marker m) {
		addMarkerToSelection = m;
		requestRepaint();
	}

	public void scrollToPosition(int position) {
		scrollToPosition = position;
		requestRepaint();
	}

	public void scrollToMarkerId(String markerId) {
		scrollToMarkerId = markerId;
		requestRepaint();
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
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);
		if (user != null) {
			target.addAttribute("userid", user.getUserId());
			target.addAttribute("userstyle", user.getStyle());
		}
		if (mode != null) {
			target.addAttribute("ace-mode", mode.toString());
		}
		if (modeFileURL != null) {
			target.addAttribute("ace-mode-url", modeFileURL);
		}
		if (theme != null) {
			target.addAttribute("ace-theme", theme.toString());
		}
		if (themeFileURL != null) {
			target.addAttribute("ace-theme-url", themeFileURL);
		}
		if (fontSize != null) {
			target.addAttribute("ace-font-size", fontSize);
		}
		target.addAttribute("ace-hscroll-visible", hScrollVisible);
		target.addAttribute("ace-scl", !scListeners.isEmpty());

		if (addMarkerToSelection != null) {
			target.addAttribute("amts-type",
					"" + addMarkerToSelection.getType());
			target.addAttribute("amts-data",
					addMarkerToSelection.getDataString());
			addMarkerToSelection = null;
		}

		if (scrollToPosition >= 0) {
			target.addAttribute("scrolltopos", scrollToPosition);
			scrollToPosition = -1;
		}
		if (scrollToMarkerId != null) {
			target.addAttribute("scrolltomarker", scrollToMarkerId);
			scrollToMarkerId = null;
		}

	}

	@Override
	protected void paintDiff(DocDiff diff, PaintTarget target)
			throws PaintException {
		target.addVariable(this, "dt", diff.getTextDiff().getDiffString());

		paintAdded(diff, target);
		paintMoved(diff, target);
		paintRemoved(diff, target);
	}

	private void paintAdded(DocDiff diff, PaintTarget target)
			throws PaintException {
		Map<String, MarkerWithContext> ams = diff
				.getAddedMarkersAsUnmodifiable();
		if (ams.isEmpty()) {
			return;
		}
		String[] addedIds = new String[ams.size()];
		String[] addedTypes = new String[ams.size()];
		String[] addedStarts = new String[ams.size()];
		String[] addedEnds = new String[ams.size()];
		String[] addedStartContexts = new String[ams.size()];
		String[] addedEndContexts = new String[ams.size()];
		String[] addedDatas = new String[ams.size()];
		int i = 0;
		for (Entry<String, MarkerWithContext> e : ams.entrySet()) {
			Marker m = e.getValue().getMarker();
			addedIds[i] = e.getKey();
			addedTypes[i] = m.getType().toString();
			addedStarts[i] = "" + m.getStart();
			addedEnds[i] = "" + m.getEnd();
			addedStartContexts[i] = "" + e.getValue().getStartContext();
			addedEndContexts[i] = "" + e.getValue().getEndContext();
			addedDatas[i] = m.getDataString();
			i++;
		}
		target.addVariable(this, "mai", addedIds);
		target.addVariable(this, "mat", addedTypes);
		target.addVariable(this, "mas", addedStarts);
		target.addVariable(this, "mae", addedEnds);
		target.addVariable(this, "masc", addedStartContexts);
		target.addVariable(this, "maec", addedEndContexts);
		target.addVariable(this, "mad", addedDatas);
	}

	private void paintMoved(DocDiff diff, PaintTarget target)
			throws PaintException {
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
		target.addVariable(this, "mmi", movedIds);
		target.addVariable(this, "mms", movedStarts);
		target.addVariable(this, "mme", movedEnds);
	}

	private void paintRemoved(DocDiff diff, PaintTarget target)
			throws PaintException {
		Collection<String> rems = diff.getRemovedMarkerIdsAsUnmodifiable();
		if (rems.isEmpty()) {
			return;
		}
		String[] removedIds = new String[rems.size()];
		int i = 0;
		for (String rem : rems) {
			removedIds[i++] = rem;
		}
		target.addVariable(this, "mri", removedIds);
	}

	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);
		if (variables.containsKey("ace-selstart")) {
			int start = (Integer) variables.get("ace-selstart");
			int end = (Integer) variables.get("ace-selend");
			for (SelectionChangeListener li : scListeners) {
				li.selectionChanged(start, end);
			}
		}
	}

	@Override
	protected DocDiff diffFromVariables(Map<String, Object> variables) {
		TextDiff td;
		if (variables.containsKey("dt")) {
			td = TextDiff.fromString((String)variables.get("dt"));
		} else {
			td = TextDiff.IDENTITY;
		}

		Map<String, MarkerWithContext> added = addedFromVariables(variables);
		Map<String, MarkerDiff> moved = movedFromVariables(variables);
		Collection<String> removed = removedFromVariables(variables);

		return DocDiff.create(td, added, moved, removed);
	}

	private Map<String, MarkerWithContext> addedFromVariables(
			Map<String, Object> variables) {
		if (!variables.containsKey("mai")) {
			return Collections.emptyMap();
		}
		String[] addedIds = (String[]) variables.get("mai");
		String[] ts = (String[]) variables.get("mat");
		String[] ds = (String[]) variables.get("mad");
		String[] ss = (String[]) variables.get("mas");
		String[] scs = (String[]) variables.get("masc");
		String[] es = (String[]) variables.get("mae");
		String[] ecs = (String[]) variables.get("maec");
		HashMap<String, MarkerWithContext> ret = new HashMap<String, MarkerWithContext>();
		for (int i = 0; i < addedIds.length; ++i) {
			Marker m = new Marker(Marker.Type.valueOf(ts[i]),
					Integer.valueOf(ss[i]), Integer.valueOf(es[i]), ds[i]);
			MarkerWithContext ma = new MarkerWithContext(m, scs[i], ecs[i]);
			ret.put(addedIds[i], ma);
		}
		return ret;
	}

	private Map<String, MarkerDiff> movedFromVariables(
			Map<String, Object> variables) {
		if (!variables.containsKey("mmi")) {
			return Collections.emptyMap();
		}
		String[] movedIds = (String[]) variables.get("mmi");
		String[] movedStarts = (String[]) variables.get("mms");
		String[] movedEnds = (String[]) variables.get("mme");
		HashMap<String, MarkerDiff> ret = new HashMap<String, MarkerDiff>();
		for (int i = 0; i < movedIds.length; ++i) {
			ret.put(movedIds[i],
					MarkerDiff.create(Integer.valueOf(movedStarts[i]),
							Integer.valueOf(movedEnds[i])));
		}
		return ret;
	}

	private Collection<String> removedFromVariables(
			Map<String, Object> variables) {
		if (!variables.containsKey("mri")) {
			return Collections.emptyList();
		}
		String[] removed = (String[]) variables.get("mri");
		LinkedList<String> ret = new LinkedList<String>();
		for (String remId : removed) {
			ret.add(remId);
		}
		return ret;
	}

	public Shared<Doc, DocDiff> getSharedDoc() {
		return getShared();
	}

}
