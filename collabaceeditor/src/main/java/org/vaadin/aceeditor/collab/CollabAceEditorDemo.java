package org.vaadin.aceeditor.collab;

import org.vaadin.aceeditor.collab.gwt.shared.Doc;
import org.vaadin.aceeditor.gwt.ace.AceMode;
import org.vaadin.diffsync.Shared;
import org.vaadin.diffsync.gwt.shared.SendPolicy;

import com.vaadin.Application;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class CollabAceEditorDemo extends Application {

	// The shared text to be edited.
	// A static variable so that everybody gets the same instance.
	//private static SharedText sharedText = new SharedText("var foo = {};\n");
	private static Shared<Doc, DocDiff> sharedText = new Shared<Doc, DocDiff>(new Doc("var foo = {};\n"));
	
	@Override
	public void init() {
		setMainWindow(new CollabDemoWindow());
	}
	
	// Subclassing Window and overriding getWindow to make multiple tabs work.

	private class CollabDemoWindow extends Window {
		private SuggestibleCollabAceEditor editor;
		CollabDemoWindow() {
			super();
			getContent().setSizeFull();
			VerticalLayout la = new VerticalLayout();
			la.setSizeFull();
			addComponent(la);
			
			editor = new SuggestibleCollabAceEditor(sharedText);
			editor.setSizeFull();
			editor.setMode(AceMode.javascript);
			editor.setSendPolicy(SendPolicy.DELAY_AFTER_IDLE);
			editor.setSendDelay(1000);
			
//			editor.addListener(new SelectionChangeListener() {
//				public void selectionChanged(int start, int end) {
//					System.out.println("Selection: "+start+"-"+end);
//				}
//			});
			
			la.addComponent(new Label("Everybody can edit this same JavaScript code:"));
			la.addComponent(editor);
			la.setExpandRatio(editor, 1);
			
		}
	}
	
	@Override
	public Window getWindow(String name) {
		Window w = super.getWindow(name);
		if (w == null) {
			w = new CollabDemoWindow();
			w.setName(name);
			addWindow(w);
			w.open(new ExternalResource(w.getURL()));
		}
		return w;
	}
}
