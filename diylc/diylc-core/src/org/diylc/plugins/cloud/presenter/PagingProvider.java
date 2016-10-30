package org.diylc.plugins.cloud.presenter;

import java.util.List;

import org.diylc.plugins.cloud.model.ProjectEntity;

public class PagingProvider {

  private CloudPresenter cloudPresenter;

  private String searchFor;
  private String category;
  private String sort;
  private int itemsPerPage = 10;

  private int currentPage;
  private List<ProjectEntity> currentResults;

  public PagingProvider(CloudPresenter cloudPresenter) {
    this(cloudPresenter, 10);
  }

  public PagingProvider(CloudPresenter cloudPresenter, int itemsPerPage) {
    this.cloudPresenter = cloudPresenter;
    this.itemsPerPage = itemsPerPage;
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
