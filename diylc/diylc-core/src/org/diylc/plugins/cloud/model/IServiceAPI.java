package org.diylc.plugins.cloud.model;

import java.io.File;
import java.util.List;

import com.diyfever.httpproxy.ParamName;

/***
 * Interface of DIYLC PHP server located at www.diy-fever.com/diylc/api/v1
 * 
 * @author bancika
 * 
 */
public interface IServiceAPI {

  public static final String URL_KEY = "serviceUrl";

  /**
   * Creates a user with the specified details.
   * 
   * @param username
   * @param password
   * @param email
   * @param website
   * @param bio
   * @return "Success" if the operation is successful or error message if it failed.
   */
  String createUser(@ParamName("username") String username, @ParamName("password") String password,
      @ParamName("email") String email, @ParamName("website") String website, @ParamName("bio") String bio);

  /***
   * Updates user details using token to authenticate.
   * 
   * @param username
   * @param token
   * @param machineId
   * @param email
   * @param website
   * @param bio
   * @return "Success" if the operation is successful or error message if it failed.
   */
  String updateUserDetails(@ParamName("username") String username, @ParamName("token") String token,
      @ParamName("machineId") String machineId, @ParamName("email") String email, @ParamName("website") String website,
      @ParamName("bio") String bio);

  /***
   * Tries to login with the provided credentials and machineId.
   * 
   * @param username
   * @param password
   * @param machineId
   * @return login token if the login was successful or string literal "Error" if it failed.
   */
  String login(@ParamName("username") String username, @ParamName("password") String password,
      @ParamName("machineId") String machineId);

  /***
   * Retrieves current user's details.
   * 
   * @param username
   * @param password
   * @param machineId
   * @return string with an error message if it fails, or an instance of {@link UserEntity} if it
   *         succeeds.
   */
  Object getUserDetails(@ParamName("username") String username, @ParamName("token") String token,
      @ParamName("machineId") String machineId);

  /***
   * Tries to authenticate with the token that was previously created by calling login() function.
   * 
   * @param username
   * @param password
   * @param machineId
   * @return "Success" if the login is successful or error message if it failed.
   */
  String loginWithToken(@ParamName("username") String username, @ParamName("token") String token,
      @ParamName("machineId") String machineId);

  /***
   * Updates password of the current user using token to authenticate.
   * 
   * @param username
   * @param token
   * @param machineId
   * @param password
   * @return "Success" if the login is successful or error message if it failed.
   */
  String updatePassword(@ParamName("username") String username, @ParamName("oldPassword") String oldPassword,
      @ParamName("newPassword") String newPassword);

  /***
   * Searches for projects that meet the specified search criteria. Supports pagination.
   * 
   * @param criteria
   * @param category
   * @param page
   * @param itemsPerPage
   * @param sort
   * @return string with error message if the error occurred or a list of {@link ProjectEntity}
   *         objects.
   */
  Object search(@ParamName("criteria") String criteria, @ParamName("category") String category,
      @ParamName("page") Integer page, @ParamName("itemsPerPage") Integer itemsPerPage, @ParamName("sort") String sort);

  /**
   * Uploads a project using token to authenticate.
   * 
   * @param username
   * @param token
   * @param machineId
   * @param projectName
   * @param category
   * @param description
   * @param diylcVersion
   * @param keywords
   * @param thumbnail
   * @param project
   * @return "Success" if the upload was successful or error message if it failed.
   */
  String upload(@ParamName("username") String username, @ParamName("token") String token,
      @ParamName("machineId") String machineId, @ParamName("projectName") String projectName,
      @ParamName("category") String category, @ParamName("description") String description,
      @ParamName("diylcVersion") String diylcVersion, @ParamName("keywords") String keywords,
      @ParamName("thumbnail") File thumbnail, @ParamName("project") File project);

  /**
   * @return a {@link List} of available categories.
   */
  List<String> getCategories();

  /**
   * @return a {@link List} of available sortings.
   */
  List<String> getSortings();

  /**
   * Returns all available comments for the given projectId.
   * 
   * @param projectId
   * @return string with error message if the error occurred or a list of {@link CommentEntity}
   *         objects.
   */
  Object getComments(@ParamName("projectId") int projectId);
}
