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
package org.diylc.announcements;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.diylc.appframework.miscutils.ConfigurationManager;

import com.diyfever.httpproxy.PhpFlatProxy;
import com.diyfever.httpproxy.ProxyFactory;

import org.diylc.plugins.cloud.model.IServiceAPI;

public class AnnouncementProvider {

  private String serviceUrl = "http://www.diy-fever.com/diylc/api/v1/announcements.html";
  private String LAST_READ_KEY = "announcement.lastReadDate";
  private String USER_ID_KEY = "userId";

  private IAnnouncementService service;

  private Date lastDate;
  private List<Announcement> announcements;

  private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

  public AnnouncementProvider() {
    String lastDateStr = ConfigurationManager.getInstance().readString(LAST_READ_KEY, null);
    try {
      this.lastDate = lastDateStr == null ? null : dateFormat.parse(lastDateStr);
    } catch (ParseException e) {
    }
    serviceUrl =
        ConfigurationManager.getInstance().readString(IServiceAPI.URL_KEY, "http://www.diy-fever.com/diylc/api/v1");
    ProxyFactory factory = new ProxyFactory(new PhpFlatProxy());
    service = factory.createProxy(IAnnouncementService.class, serviceUrl);
  }
  
  private String getUserId() {
    String userId = ConfigurationManager.getInstance().readString(USER_ID_KEY, null);
    if (userId == null) {
      userId = UUID.randomUUID().toString();
      ConfigurationManager.getInstance().writeValue(USER_ID_KEY, userId);
    }
    return userId;
  }

  public String getCurrentAnnouncements(boolean forceLast) throws ParseException {
    announcements = service.getAnnouncements(getUserId());
    if (announcements == null)
    	return null;
    boolean hasUnread = false;
    StringBuilder sb = new StringBuilder("<html>");
    for (int i = 0; i < announcements.size(); i++) {
      Date date = dateFormat.parse(announcements.get(i).getDate());
      if (lastDate == null || lastDate.before(date) || lastDate.equals(date) || (forceLast && i == announcements.size() - 1)) {
        sb.append("<font size='4'><b>").append(announcements.get(i).getTitle()).append("</b> on ")
            .append(announcements.get(i).getDate()).append("</font>").append("<p>")
            .append(announcements.get(i).getText()).append("</p>");
        hasUnread = true;
      }
    }
    if (!hasUnread)
      return "";    
    sb.append("</html>");
    return sb.toString();
  }

  public void dismissed() {
    Date date = new Date();
    ConfigurationManager.getInstance().writeValue(LAST_READ_KEY, dateFormat.format(date));
  }
}
