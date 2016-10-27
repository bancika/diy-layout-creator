package org.diylc.plugins.cloud.model;

public class CommentEntity {

  private int id;
  private int userId;
  private String username;
  private String comment;
  private String postedAt;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getDate() {
    return postedAt;
  }

  public void setDate(String date) {
    this.postedAt = date;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((comment == null) ? 0 : comment.hashCode());
    result = prime * result + ((postedAt == null) ? 0 : postedAt.hashCode());
    result = prime * result + id;
    result = prime * result + userId;
    result = prime * result + ((username == null) ? 0 : username.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CommentEntity other = (CommentEntity) obj;
    if (comment == null) {
      if (other.comment != null)
        return false;
    } else if (!comment.equals(other.comment))
      return false;
    if (postedAt == null) {
      if (other.postedAt != null)
        return false;
    } else if (!postedAt.equals(other.postedAt))
      return false;
    if (id != other.id)
      return false;
    if (userId != other.userId)
      return false;
    if (username == null) {
      if (other.username != null)
        return false;
    } else if (!username.equals(other.username))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "CommentEntity [id=" + id + ", username=" + username + ", date=" + postedAt + "]";
  }
}
