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
package org.diylc.plugins.cloud.service;

import java.util.List;

import org.diylc.common.IPlugInPort;
import org.diylc.plugins.cloud.model.ProjectEntity;

/**
 * Used to create paging of the search results. Use
 * {@link SearchSession#startSession(String, String, String)} to start the search session and then
 * {@link SearchSession#hasMoreData()} returns true if there's more data available and
 * {@link SearchSession#requestMoreData()} returns the next page.
 * 
 * @author Branislav Stojkovic
 */
public class SearchSession {

  private final IPlugInPort plugInPort;
  private String searchFor;
  private String category;
  private String sort;
  private int itemsPerPage = 10;

  private int currentPage;
  private List<ProjectEntity> currentResults;

  public SearchSession(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
  }

  public List<ProjectEntity> startSession(String searchFor, String category, String sort) throws CloudException {
    this.searchFor = searchFor;
    this.category = category;
    this.sort = sort;
    this.currentPage = 1;

    return currentResults = plugInPort.getCloudService().search(searchFor, category, sort, currentPage, itemsPerPage);
  }

  public boolean hasMoreData() {
    return this.currentResults != null && this.currentResults.size() == itemsPerPage;
  }

  public List<ProjectEntity> requestMoreData() throws CloudException {
    currentPage++;
    return currentResults = plugInPort.getCloudService().search(searchFor, category, sort, currentPage, itemsPerPage);
  }
}
