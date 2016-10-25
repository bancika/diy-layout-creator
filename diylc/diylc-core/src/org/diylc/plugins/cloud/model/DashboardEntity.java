package org.diylc.plugins.cloud.model;

import java.util.List;

public class DashboardEntity {

  private List<ProjectEntity> latestProjects;
  private List<ProjectEntity> mostDownloadedProjects;
  private List<UserEntity> topContributors;
  private List<CountryDetails> contributorsByCountry;
  private List<CountryDetails> visitorsByCountry;
}
