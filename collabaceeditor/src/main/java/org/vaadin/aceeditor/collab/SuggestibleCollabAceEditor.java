package org.vaadin.aceeditor.collab;

import java.util.List;
import java.util.Map;

import org.vaadin.aceeditor.Suggester;
import org.vaadin.aceeditor.collab.gwt.shared.Doc;
import org.vaadin.aceeditor.gwt.shared.Suggestion;
import org.vaadin.diffsync.Shared;

import com.vaadin.event.ShortcutAction;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;

@SuppressWarnings("serial")
@com.vaadin.ui.ClientWidget(org.vaadin.aceeditor.collab.gwt.client.VSuggestibleCollabAceEditor.class)
public class SuggestibleCollabAceEditor extends CollabDocAceEditor {

	private Suggester suggester;
	private ShortcutAction shortcut;
	private List<Suggestion> suggestions;

	public SuggestibleCollabAceEditor(Shared<Doc, DocDiff> shared) {
		super(shared);
	}

	/**
	 * Sets a {@link Suggester}.
	 * 
	 * <p>
	 * The suggester is queried for suggestions when the user requests them. The
	 * suggestions are then shown to the user.
	 * </p>
	 * 
	 * @param suggester
	 * @param shortcut
	 */
	public void setSuggester(Suggester suggester, ShortcutAction shortcut) {
		this.suggester = suggester;
		this.shortcut = shortcut;
		requestRepaint();
	}

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);

		if (suggester != null) {
			target.addAttribute("suggestion-enabled", true);

		}
		if (shortcut != null) {
			Integer[] mods = new Integer[3];
			for (int i = 0; i < mods.length; ++i) {
				mods[i] = 0;
			}
			for (int m : shortcut.getModifiers()) {
				if (m == 18)
					mods[0] = 1; // alt
				if (m == 16)
					mods[1] = 1; // shift
				if (m == 17)
					mods[2] = 1; // ctrl
			}

			target.addAttribute("suggestion-key", shortcut.getKeyCode());
			target.addAttribute("suggestion-keymods", mods);
		}

		paintSuggestions(target);
	}

	private void paintSuggestions(PaintTarget target) throws PaintException {
		if (suggestions == null) {
			return;
		}
		String[] suggs = new String[suggestions.size()];
		String[] displ = new String[suggestions.size()];
		String[] descr = new String[suggestions.size()];
		String[] selStarts = new String[suggestions.size()];
		String[] selEnds = new String[suggestions.size()];
		int i = 0;
		for (Suggestion s : suggestions) {
			suggs[i] = s.getValueText();
			displ[i] = s.getDisplayText();
			descr[i] = s.getDescriptionText();
			selStarts[i] = "" + s.getSelectionStart();
			selEnds[i] = "" + s.getSelectionEnd();
			++i;
		}
		target.addAttribute("suggestion-values", suggs);
		target.addAttribute("suggestion-displays", displ);
		target.addAttribute("suggestion-descriptions", descr);
		target.addAttribute("suggestion-sel-starts", selStarts);
		target.addAttribute("suggestion-sel-ends", selEnds);
		suggestions = null;
	}

	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);

		if (suggester != null && variables.containsKey("suggestion-requested")) {
			int cursor = (Integer) variables.get("suggestion-requested");
			suggestions = suggester.getSuggestions(getShadow().getText(),
					cursor);
			requestRepaint();
		}
	}

}
