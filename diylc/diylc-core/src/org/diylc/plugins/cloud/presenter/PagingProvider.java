package org.diylc.plugins.cloud.presenter;

import java.util.List;

import org.diylc.plugins.cloud.model.ProjectEntity;

/**
 * Used to create paging of the search results. Use
 * {@link PagingProvider#startSession(String, String, String)} to start the search session and then
 * {@link PagingProvider#hasMoreData()} returns true if there's more data available and
 * {@link PagingProvider#requestMoreData()} returns the next page.
 * 
 * @author Branislav Stojkovic
 */
public class PagingProvider {

  private CloudPresenter cloudPresenter;

  private String searchFor;
  private String category;
  private String sort;
  private int itemsPerPage = 10;

  private int currentPage;
  private List<ProjectEntity> currentResults;

  public PagingProvider(CloudPresenter cloudPresenter) {
    this.cloudPresenter = cloudPresenter;
  }

  public List<ProjectEntity> startSession(String searchFor, String category, String sort) throws CloudException {
    this.searchFor = searchFor;
    this.category = category;
    this.sort = sort;
    this.currentPage = 1;

    return currentResults = cloudPresenter.search(searchFor, category, sort, currentPage, itemsPerPage);
  }

  public boolean hasMoreData() {
    return this.currentResults != null && this.currentResults.size() == itemsPerPage;
  }

  public List<ProjectEntity> requestMoreData() throws CloudException {
    currentPage++;
    return currentResults = cloudPresenter.search(searchFor, category, sort, currentPage, itemsPerPage);
  }
}
