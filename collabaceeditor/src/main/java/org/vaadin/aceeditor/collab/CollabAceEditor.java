package org.vaadin.aceeditor.collab;

import java.util.Map;

import org.vaadin.aceeditor.gwt.ace.AceMode;
import org.vaadin.aceeditor.gwt.ace.AceTheme;
import org.vaadin.diffsync.AbstractDiffSyncComponent;
import org.vaadin.diffsync.Shared;
import org.vaadin.diffsync.TextDiff;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;

/**
 * 
 * Enables multiple people to edit the same text in Ace editor.
 * 
 * The communication between the client and the server is done using text diffs.
 * 
 * @see <a href="http://ace.ajax.org">Ace Editor</a>
 * @see <a href="http://code.google.com/p/google-diff-match-patch/">The text
 *      diff algorithm used.</a>
 * 
 */
@SuppressWarnings("serial")
@com.vaadin.ui.ClientWidget(org.vaadin.aceeditor.collab.gwt.client.VCollabAceEditor.class)
public class CollabAceEditor extends
		AbstractDiffSyncComponent<String, TextDiff> {

	private AceMode mode = null;
	private AceTheme theme = null;
	private String fontSize = "12px";
	private Boolean hScrollVisible = false;
	private String modeFileURL;
	private String themeFileURL;

	public CollabAceEditor(Shared<String, TextDiff> shared) {
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

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);
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
	protected void paintDiff(TextDiff diff, PaintTarget target)
			throws PaintException {
		target.addVariable(this, "diff", diff.getDiffString());
	}

	@Override
	protected TextDiff diffFromVariables(Map<String, Object> variables) {
		if (variables.containsKey("diff")) {
			return TextDiff.fromString((String)variables.get("diff"));
		}
		return null;
	}

}
