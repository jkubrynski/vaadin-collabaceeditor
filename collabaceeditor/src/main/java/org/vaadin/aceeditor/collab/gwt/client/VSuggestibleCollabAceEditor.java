package org.vaadin.aceeditor.collab.gwt.client;

import java.util.Map;

import org.vaadin.aceeditor.collab.gwt.shared.Doc;
import org.vaadin.aceeditor.gwt.client.EditorFacade.Key;
import org.vaadin.aceeditor.gwt.client.EditorFacade.KeyPressHandler;
import org.vaadin.aceeditor.gwt.client.SuggestionHandler;
import org.vaadin.aceeditor.gwt.client.SuggestionHandler.SuggestionRequestedListener;
import org.vaadin.aceeditor.gwt.shared.Marker;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;

public class VSuggestibleCollabAceEditor extends VCollabDocAceEditor
		implements KeyPressHandler, SuggestionRequestedListener {

	private boolean suggestionEnabled = false;

	SuggestionHandler suha;

	public VSuggestibleCollabAceEditor() {
		super();

	}

	@Override
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		super.updateFromUIDL(uidl, client);
		if (client.updateComponent(this, uidl, true)) {
			return;
		}

		if (!suggestionEnabled && uidl.hasAttribute("suggestion-enabled")) {
			markerEditor.addHandler(this);
			suggestionEnabled = true;
			suha = new SuggestionHandler(markerEditor);
			suha.addListener(this);
		}

		if (suggestionEnabled) {
			suha.updateFromUIDL(uidl);
		}
	}

	@Override
	protected void applyDiff(DocDiff diff) {
		super.applyDiff(diff);

		if (suggestionEnabled && !diff.getTextDiff().isIdentity()) {
			suha.updatePopupPosition();
		}

	}

	@Override
	public void textChanged() {
		super.textChanged();
		if (suggestionEnabled) {
			suha.textChanged();
		}
	}

	@Override
	public void cursorChanged() {
		super.cursorChanged();
		if (suggestionEnabled) {
			suha.cursorChanged();
		}
	}

//	@Override
	public boolean keyPressed(Key key) {
		return suggestionEnabled ? suha.keyPressed(key) : true;
	}

//	@Override
	public void suggestionRequested(int cursor) {
		valueChangedSendEvenIfIdentityASAP();
	}

	@Override
	protected void beforeSend() {
		super.beforeSend();
		if (!suggestionEnabled) {
			return;
		}
		int cursor = suha.getSuggestionCursor();
		if (cursor >= 0) {
			getClient().updateVariable(getPaintableId(),
					"suggestion-requested", cursor, false);
		}
	}

	@Override
	protected Doc getValue() {
		Map<String, Marker> markers = markerEditor.getMarkers();
		markers.remove(SuggestionHandler.SUGGESTION_MARKER_ID);
		return new Doc(markerEditor.getText(), markers);
	}
}
