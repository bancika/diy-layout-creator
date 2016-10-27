package org.diylc.plugins.cloud.model;

import java.util.ArrayList;
import java.util.List;

public class CommentTree {

  private CommentEntity rootComment;
  private List<CommentEntity> replies;

  public CommentTree(CommentEntity rootComment) {
    this.rootComment = rootComment;
    this.replies = new ArrayList<CommentEntity>();
  }

  public CommentEntity getRootComment() {
    return rootComment;
  }

  public List<CommentEntity> getReplies() {
    return replies;
  }
}
