/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

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
package org.diylc.plugins.cloud.model;

import java.io.Serializable;

import org.diylc.core.annotations.DynamicList;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.MultiLineText;

/**
 * Represents a cloud project object, identified by {@link ProjectEntity#id}.
 * 
 * @author Branislav Stojkovic
 */
public class ProjectEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  private int id;
  private String name;
  private String description;
  private String owner;
  private String category;
  private String categoryForDisplay;
  private String updated;
  private String thumbnailUrl;
  private String downloadUrl;
  private String keywords;
  private int viewCount;
  private int downloadCount;
  private int commentCount;
  private String[] categories;

  public ProjectEntity() {
    super();
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @EditableProperty(defaultable = false, sortOrder = 1)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @MultiLineText
  @EditableProperty(defaultable = false, sortOrder = 3)
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  @DynamicList(availableValueFunction="getCategories")
  @EditableProperty(defaultable = false, sortOrder = 2)
  public String getCategoryForDisplay() {
    return categoryForDisplay;
  }

  public void setCategoryForDisplay(String categoryForDisplay) {
    this.categoryForDisplay = categoryForDisplay;
  }

  public String[] getCategories() {
    return categories;
  }

  public void setCategories(String[] categories) {
    this.categories = categories;
  }

  public String getUpdated() {
    return updated;
  }

  public void setUpdated(String updated) {
    this.updated = updated;
  }

  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  public String getDownloadUrl() {
    return downloadUrl;
  }

  public void setDownloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
  }

  public int getViewCount() {
    return viewCount;
  }

  public void setViewCount(int viewCount) {
    this.viewCount = viewCount;
  }

  public int getDownloadCount() {
    return downloadCount;
  }

  public void setDownloadCount(int downloadCount) {
    this.downloadCount = downloadCount;
  }

  public int getCommentCount() {
    return commentCount;
  }

  public void setCommentCount(int commentCount) {
    this.commentCount = commentCount;
  }

  @EditableProperty(defaultable = false, sortOrder = 4)
  public String getKeywords() {
    return keywords;
  }

  public void setKeywords(String keywords) {
    this.keywords = keywords;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((category == null) ? 0 : category.hashCode());
    result = prime * result + commentCount;
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + downloadCount;
    result = prime * result + ((downloadUrl == null) ? 0 : downloadUrl.hashCode());
    result = prime * result + id;
    result = prime * result + ((keywords == null) ? 0 : keywords.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((owner == null) ? 0 : owner.hashCode());
    result = prime * result + ((thumbnailUrl == null) ? 0 : thumbnailUrl.hashCode());
    result = prime * result + ((updated == null) ? 0 : updated.hashCode());
    result = prime * result + viewCount;
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
    ProjectEntity other = (ProjectEntity) obj;
    if (category == null) {
      if (other.category != null)
        return false;
    } else if (!category.equals(other.category))
      return false;
    if (commentCount != other.commentCount)
      return false;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (downloadCount != other.downloadCount)
      return false;
    if (downloadUrl == null) {
      if (other.downloadUrl != null)
        return false;
    } else if (!downloadUrl.equals(other.downloadUrl))
      return false;
    if (id != other.id)
      return false;
    if (keywords == null) {
      if (other.keywords != null)
        return false;
    } else if (!keywords.equals(other.keywords))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (owner == null) {
      if (other.owner != null)
        return false;
    } else if (!owner.equals(other.owner))
      return false;
    if (thumbnailUrl == null) {
      if (other.thumbnailUrl != null)
        return false;
    } else if (!thumbnailUrl.equals(other.thumbnailUrl))
      return false;
    if (updated == null) {
      if (other.updated != null)
        return false;
    } else if (!updated.equals(other.updated))
      return false;
    if (viewCount != other.viewCount)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return name + " by " + owner + " updated on " + updated;
  }
}
