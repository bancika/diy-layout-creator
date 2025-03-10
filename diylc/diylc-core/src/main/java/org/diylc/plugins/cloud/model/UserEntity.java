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

/**
 * Represents cloud user details.
 * 
 * @author Branislav Stojkovic
 */
public class UserEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  private String username;
  private String email;
  private String website;
  private String bio;

  public UserEntity(String username, String email, String website, String bio) {
    super();
    this.username = username;
    this.email = email;
    this.website = website;
    this.bio = bio;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getWebsite() {
    return website;
  }

  public void setWebsite(String website) {
    this.website = website;
  }

  public String getBio() {
    return bio;
  }

  public void setBio(String bio) {
    this.bio = bio;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((bio == null) ? 0 : bio.hashCode());
    result = prime * result + ((email == null) ? 0 : email.hashCode());
    result = prime * result + ((username == null) ? 0 : username.hashCode());
    result = prime * result + ((website == null) ? 0 : website.hashCode());
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
    UserEntity other = (UserEntity) obj;
    if (bio == null) {
      if (other.bio != null)
        return false;
    } else if (!bio.equals(other.bio))
      return false;
    if (email == null) {
      if (other.email != null)
        return false;
    } else if (!email.equals(other.email))
      return false;
    if (username == null) {
      if (other.username != null)
        return false;
    } else if (!username.equals(other.username))
      return false;
    if (website == null) {
      if (other.website != null)
        return false;
    } else if (!website.equals(other.website))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "UserEntity [userName=" + username + "]";
  }

}
