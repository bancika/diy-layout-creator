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
package org.diylc.plugins.chatbot.presenter;

import com.diyfever.httpproxy.PhpFlatProxy;
import com.diyfever.httpproxy.ProxyFactory;
import org.apache.log4j.Logger;
import org.checkerframework.checker.units.qual.N;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.PropertyWrapper;
import org.diylc.plugins.chatbot.model.IChatbotAPI;
import org.diylc.plugins.cloud.model.CommentEntity;
import org.diylc.plugins.cloud.model.IServiceAPI;
import org.diylc.plugins.cloud.model.ProjectEntity;
import org.diylc.plugins.cloud.model.UserEntity;
import org.diylc.plugins.cloud.presenter.CloudException;
import org.diylc.plugins.cloud.presenter.CloudPresenter;
import org.diylc.plugins.cloud.presenter.NotLoggedInException;
import org.diylc.presenter.ComparatorFactory;
import org.diylc.presenter.ComponentProcessor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collections;
import java.util.List;

/**
 * Contains all the back-end logic for using the cloud and manipulating projects on the cloud.
 * 
 * @author Branislav Stojkovic
 */
public class ChatbotPresenter {

  public static final ChatbotPresenter Instance = new ChatbotPresenter();

  private static String ERROR = "Error";

  private final static Logger LOG = Logger.getLogger(ChatbotPresenter.class);
  private static final Object SUCCESS = "Success";

  private IChatbotAPI service;
  private String serviceUrl;

  private ChatbotPresenter() {}

  private IChatbotAPI getService() {
    if (service == null) {
      serviceUrl =
          ConfigurationManager.getInstance().readString(IServiceAPI.URL_KEY, "http://www.diy-fever.com/diylc/api/v1/ai");
      ProxyFactory factory = new ProxyFactory(new PhpFlatProxy());
      service = factory.createProxy(IChatbotAPI.class, serviceUrl);
    }
    return service;
  }

  public String promptChatbot(String project, String netlist, String prompt) throws NotLoggedInException {
    if (!CloudPresenter.Instance.isLoggedIn())
      throw new NotLoggedInException();
    return getService().promptChatbot(CloudPresenter.Instance.getCurrentUsername(), CloudPresenter.Instance.getCurrentToken(),
        CloudPresenter.Instance.getMachineId(), project, netlist, prompt);
  }
}
