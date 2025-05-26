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
package org.diylc.plugins.chatbot.model;

import java.io.Serializable;
import java.util.Objects;

public class SubscriptionEntity implements Serializable {
  private String tier;
  private String endDate;
  private int remainingCredits;

  public SubscriptionEntity(String tier, String endDate, int remainingCredits) {
    this.tier = tier;
    this.endDate = endDate;
    this.remainingCredits = remainingCredits;
  }

  public String getTier() {
    return tier;
  }

  public void setTier(String tier) {
    this.tier = tier;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  public int getRemainingCredits() {
    return remainingCredits;
  }

  public void setRemainingCredits(int remainingCredits) {
    this.remainingCredits = remainingCredits;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass())
      return false;
    SubscriptionEntity that = (SubscriptionEntity) o;
    return remainingCredits == that.remainingCredits && Objects.equals(tier,
        that.tier) && Objects.equals(endDate, that.endDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tier, endDate, remainingCredits);
  }
}
