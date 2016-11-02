package org.diylc.swing.plugins.cloud.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;
import org.diylc.common.ITask;
import org.diylc.core.IView;
import org.diylc.plugins.cloud.model.CommentEntity;
import org.diylc.plugins.cloud.model.ProjectEntity;
import org.diylc.plugins.cloud.presenter.CloudPresenter;
import org.diylc.swing.ISimpleView;
import org.diylc.swing.gui.components.HTMLTextArea;

/**
 * {@link JDialog} that shows a list of comments posted to a cloud project and provides controls for
 * posting more comments.
 * 
 * @author Branislav Stojkovic
 */
public class CommentDialog extends JDialog {

  private static final String JUST_NOW = "just now";

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(CommentDialog.class);

  private ProjectEntity project;
  private List<CommentEntity> comments;

  private JPanel mainPanel;
  private JPanel listPanel;
  private JTextArea replyArea;
  private JButton sendButton;

  private int lastPosition = 0;

  private ISimpleView cloudUI;

  public CommentDialog(ISimpleView cloudUI, ProjectEntity project, List<CommentEntity> comments) {
    super(cloudUI.getOwnerFrame(), "Comments on " + project.getName());
    this.project = project;
    this.comments = comments;
    this.cloudUI = cloudUI;
    setMinimumSize(new Dimension(400, 600));
    setContentPane(getMainPanel());
    pack();
    setLocationRelativeTo(cloudUI.getOwnerFrame());
    setModal(true);
    if (!CloudPresenter.Instance.isLoggedIn()) {
      getReplyArea().setEnabled(false);
      getReplyArea().setText("Must be logged in to reply.");
      getSendButton().setEnabled(false);
    }
  }

  private JPanel getListPanel() {
    if (listPanel == null) {
      listPanel = new JPanel(new GridBagLayout());
      listPanel.setBackground(Color.white);
      for (CommentEntity c : comments) {
        addComment(c);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 10000;
        gbc.weighty = 1;
        listPanel.add(new JLabel(), gbc);
      }
    }
    return listPanel;
  }

  private JPanel getMainPanel() {
    if (mainPanel == null) {
      mainPanel = new JPanel(new GridBagLayout());
      mainPanel.setBackground(Color.white);

      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weighty = 3;
      gbc.weightx = 1;
      gbc.fill = GridBagConstraints.BOTH;
      JScrollPane scroll = new JScrollPane(getListPanel());
      scroll.getVerticalScrollBar().setUnitIncrement(16);
      mainPanel.add(scroll, gbc);

      // Add components to the bottom
      gbc.insets = new Insets(2, 2, 2, 2);
      gbc.gridy++;
      gbc.weighty = 0;
      gbc.fill = GridBagConstraints.NONE;
      gbc.anchor = GridBagConstraints.LINE_START;
      mainPanel.add(new JLabel("Write a reply:"), gbc);

      gbc.gridy++;
      gbc.fill = GridBagConstraints.BOTH;
      gbc.weighty = 0.5;
      mainPanel.add(new JScrollPane(getReplyArea()), gbc);

      gbc.gridy++;
      gbc.weighty = 0;
      gbc.fill = GridBagConstraints.NONE;
      gbc.anchor = GridBagConstraints.LINE_END;
      mainPanel.add(getSendButton(), gbc);
    }
    return mainPanel;
  }

  private JTextArea getReplyArea() {
    if (replyArea == null) {
      replyArea = new HTMLTextArea();
      replyArea.setFont(getSendButton().getFont());
    }
    return replyArea;
  }

  private JButton getSendButton() {
    if (sendButton == null) {
      sendButton = new JButton("Send Reply");
      sendButton.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          final String comment = getReplyArea().getText();
          if (comment.trim().length() == 0)
            cloudUI.showMessage("Cannot post an empty comment.", "Cloud Error", IView.ERROR_MESSAGE);

          cloudUI.executeBackgroundTask(new ITask<Void>() {

            @Override
            public Void doInBackground() throws Exception {
              CloudPresenter.Instance.postComment(project.getId(), comment);
              return null;
            }

            @Override
            public void failed(Exception e) {
              cloudUI.showMessage("Error posting comment.", "Cloud Error", IView.ERROR_MESSAGE);
            }

            @Override
            public void complete(Void result) {
              getReplyArea().setText("");
              CommentEntity newComment = new CommentEntity();
              newComment.setPostedAt(JUST_NOW);
              newComment.setUsername(CloudPresenter.Instance.getCurrentUsername());
              newComment.setComment(comment);
              addComment(newComment).requestFocusInWindow();
              getListPanel().revalidate();
            }
          });
        }
      });
    }
    return sendButton;
  }

  private JTextArea addComment(CommentEntity c) {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.NONE;
    gbc.gridx = 0;
    gbc.gridy = lastPosition++;
    gbc.insets = new Insets(2, 2, 2, 2);
    gbc.anchor = GridBagConstraints.LINE_START;
    gbc.weightx = 1;
    getListPanel().add(
        new JLabel("<html><b>" + c.getUsername() + "</b> wrote " + (c.getPostedAt().equals(JUST_NOW) ? "" : "on ")
            + c.getPostedAt() + " </html>"), gbc);
    gbc.gridy = lastPosition++;

    JTextArea commentArea = new HTMLTextArea(c.getComment());
    commentArea.setEditable(false);
    commentArea.setFont(getSendButton().getFont());
    commentArea.setLineWrap(true);
    commentArea.setWrapStyleWord(true);
    // commentArea.setRequestFocusEnabled(false);
    commentArea.setForeground(Color.gray);
    // DefaultCaret caret = (DefaultCaret) commentArea.getCaret();
    // caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
    getListPanel().add(commentArea, gbc);
    gbc.gridy = lastPosition++;

    gbc.fill = GridBagConstraints.HORIZONTAL;
    getListPanel().add(new JSeparator(), gbc);
    return commentArea;
  }
}
