package org.vaadin.aceeditor.collab.gwt.client;

import org.vaadin.aceeditor.gwt.client.AceEditorFacade;
import org.vaadin.aceeditor.gwt.client.EditorFacade;
import org.vaadin.aceeditor.gwt.client.EditorFacade.TextChangeListener;
import org.vaadin.diffsync.gwt.client.GwtTextDiff;
import org.vaadin.diffsync.gwt.client.VAbstractDiffSyncComponent;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;

public class VCollabAceEditor extends
		VAbstractDiffSyncComponent<String, GwtTextDiff> {

	EditorFacade editor = new AceEditorFacade();

	public VCollabAceEditor() {
		editor.initializeEditor();
		editor.addListener(new TextChangeListener() {
//			@Override
			public void textChanged() {
				valueChanged();
			}
		});
		initWidget(editor.getWidget());
	}

	@Override
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		super.updateFromUIDL(uidl, client);
		if (client.updateComponent(this, uidl, true)) {
			return;
		}

		editor.settingsFromUIDL(uidl);
	}

	@Override
	protected void applyDiff(GwtTextDiff diff) {
		String oldText = editor.getText();
		String newText = diff.applyTo(oldText);
		int oldCursor = editor.getCursor();
		int newCursor = GwtTextDiff.positionInNewText(oldText, oldCursor, newText);
		editor.setText(newText, false);
		editor.setCursor(newCursor, false);
	}

	@Override
	protected String getValue() {
		return editor.getText();
	}

	@Override
	protected GwtTextDiff diff(String v1, String v2) {
		return GwtTextDiff.diff(v1, v2);
	}

	@Override
	protected void diffToClient(GwtTextDiff diff, ApplicationConnection client,
			String paintableId, boolean immediate) {
		client.updateVariable(paintableId, "diff", diff.getDiffString(), immediate);

	}

	@Override
	protected GwtTextDiff diffFromUIDL(UIDL uidl) {
		if (uidl.hasVariable("diff")) {
			return GwtTextDiff.fromString(uidl.getStringVariable("diff"));
		}
		return null;
	}
	
	

}
