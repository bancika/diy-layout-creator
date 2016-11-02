package org.diylc.plugins.cloud.presenter;

import java.util.List;

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
  
  private String searchFor;
  private String category;
  private String sort;
  private int itemsPerPage = 10;

  private int currentPage;
  private List<ProjectEntity> currentResults;

  public SearchSession() {
  }

  public List<ProjectEntity> startSession(String searchFor, String category, String sort) throws CloudException {
    this.searchFor = searchFor;
    this.category = category;
    this.sort = sort;
    this.currentPage = 1;

    return currentResults = CloudPresenter.Instance.search(searchFor, category, sort, currentPage, itemsPerPage);
  }

  public boolean hasMoreData() {
    return this.currentResults != null && this.currentResults.size() == itemsPerPage;
  }

  public List<ProjectEntity> requestMoreData() throws CloudException {
    currentPage++;
    return currentResults = CloudPresenter.Instance.search(searchFor, category, sort, currentPage, itemsPerPage);
  }
}
