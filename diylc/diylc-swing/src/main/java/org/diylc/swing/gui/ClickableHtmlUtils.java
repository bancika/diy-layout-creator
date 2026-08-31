/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.swing.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.apache.log4j.Logger;
import org.diylc.announcements.AnnouncementUtils;
import org.diylc.appframework.miscutils.Utils;

/**
 * Utility for creating interactive Swing HTML components with clickable links.
 */
public class ClickableHtmlUtils {

  private static final Logger LOG = Logger.getLogger(ClickableHtmlUtils.class);

  /**
   * Creates a JEditorPane configured to display HTML with clickable links and whole-text click navigation.
   *
   * @param html the HTML content to display
   * @param defaultUrlToOpen optional default URL to open when clicking non-hyperlink area of text (if null, extracted from html)
   * @param onLinkOpened optional callback executed when a link is opened
   * @return configured JEditorPane
   */
  public static JEditorPane createClickableHtmlPane(String html, final String defaultUrlToOpen,
      final Runnable onLinkOpened) {
    JEditorPane editorPane = new JEditorPane();
    editorPane.setContentType("text/html");
    editorPane.setText(html != null ? html : "");
    editorPane.setEditable(false);
    editorPane.setOpaque(false);
    editorPane.setBackground(new Color(0, 0, 0, 0));
    editorPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

    final String targetUrl =
        defaultUrlToOpen != null ? defaultUrlToOpen : AnnouncementUtils.extractFirstUrl(html);
    final long[] lastClickTime = new long[1];

    editorPane.addHyperlinkListener(new HyperlinkListener() {

      @Override
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
          lastClickTime[0] = System.currentTimeMillis();
          String url = e.getURL() != null ? e.getURL().toString() : e.getDescription();
          if (url != null) {
            openUrl(url);
            if (onLinkOpened != null) {
              onLinkOpened.run();
            }
          }
        }
      }
    });

    if (targetUrl != null) {
      editorPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      editorPane.addMouseListener(new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
          if (System.currentTimeMillis() - lastClickTime[0] > 500) {
            lastClickTime[0] = System.currentTimeMillis();
            openUrl(targetUrl);
            if (onLinkOpened != null) {
              onLinkOpened.run();
            }
          }
        }
      });
    }

    return editorPane;
  }

  /**
   * Creates a JComponent suitable for display inside JOptionPane message dialogs with clickable links.
   *
   * @param message HTML or text message
   * @return JComponent with clickable links
   */
  public static JComponent createClickableMessageComponent(String message) {
    if (message == null || message.isEmpty()) {
      return new JEditorPane();
    }
    String html = message;
    if (!html.trim().toLowerCase().startsWith("<html>")) {
      html = "<html>" + html + "</html>";
    }
    JEditorPane pane = createClickableHtmlPane(html, null, null);
    Font font = UIManager.getFont("Label.font");
    if (font != null) {
      pane.setFont(font);
    }
    Color fg = UIManager.getColor("Label.foreground");
    if (fg != null) {
      pane.setForeground(fg);
    }
    return pane;
  }

  /**
   * Opens a URL in the system browser.
   *
   * @param url URL to open
   */
  public static void openUrl(String url) {
    if (url == null || url.trim().isEmpty()) {
      return;
    }
    String normalized = AnnouncementUtils.normalizeUrl(url);
    try {
      Utils.openURL(normalized);
    } catch (Exception ex) {
      LOG.error("Could not open URL: " + normalized, ex);
    }
  }
}
