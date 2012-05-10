package org.vaadin.aceeditor.collab.gwt.client;

import org.vaadin.aceeditor.gwt.client.AceEditorFacade;
import org.vaadin.aceeditor.gwt.client.EditorFacade;
import org.vaadin.aceeditor.gwt.client.EditorFacade.TextChangeListener;
import org.vaadin.diffsync.gwt.client.TextDiff;
import org.vaadin.diffsync.gwt.client.VAbstractDiffSyncComponent;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;

public class VCollabAceEditor extends
		VAbstractDiffSyncComponent<String, TextDiff> {

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
	protected void applyDiff(TextDiff diff) {
		String oldText = editor.getText();
		String newText = diff.applyTo(oldText);
		int oldCursor = editor.getCursor();
		int newCursor = TextDiff.positionInNewText(oldText, oldCursor, newText);
		editor.setText(newText, false);
		editor.setCursor(newCursor, false);
	}

	@Override
	protected String getValue() {
		return editor.getText();
	}

	@Override
	protected String initialValue() {
		return "";
	}

	@Override
	protected TextDiff diff(String v1, String v2) {
		return TextDiff.diff(v1, v2);
	}

	@Override
	protected void diffToClient(TextDiff diff, ApplicationConnection client,
			String paintableId, boolean immediate) {
		client.updateVariable(paintableId, "diff", diff.getDiffString(), immediate);

	}

	@Override
	protected TextDiff diffFromUIDL(UIDL uidl) {
		if (uidl.hasVariable("diff")) {
			return TextDiff.fromString(uidl.getStringVariable("diff"));
		}
		return null;
	}
	
	

}
