package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;

public class ProjectDao extends DaoBase {
  private static final String CATEGORY_TABLE = "category";
  private static final String MATERIAL_TABLE = "material";
  private static final String PROJECT_TABLE = "project";
  private static final String PROJECT_CATEGORY_TABLE = "project_category";
  private static final String STEP_TABLE = "step";

  public Project insertProject(Project project) {
    /* @formatter:off
     * 
     * inserts the project inputed by the user into the database
     * 
     * stores the sql statement in the sql string
     * 
     * creates a connection
     *    starts a transaction;
     *    throws an outer exception IF a connection can't be made
     * 
     * creates a preparedstatement
     *    validates project parameters, executes the sql statement (in stmt), assigns the last
     *    inserted id to the project, and commits the transaction (writes changes to database);
     *    throws an inner exception IF the sql statement is incorrect AND rolls back the
     *    transaction
     * 
     * @formatter:on
     */

    // @formatter:off
    String sql = ""
        + "INSERT INTO " + PROJECT_TABLE + " "
        + "(project_name, estimated_hours, actual_hours, difficulty, notes) "
        + "VALUES "
        + "(?, ?, ?, ?, ?)";
    // @formatter:on

    try (Connection conn = DbConnection.getConnection()) {
      startTransaction(conn);

      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        setParameter(stmt, 1, project.getProjectName(), String.class);
        setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
        setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
        setParameter(stmt, 4, project.getDifficulty(), Integer.class);
        setParameter(stmt, 5, project.getNotes(), String.class);

        stmt.executeUpdate();

        Integer projectId = getLastInsertId(conn, PROJECT_TABLE);
        commitTransaction(conn);

        project.setProjectId(projectId);
        return project;

      } catch (Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }

    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public List<Project> fetchAllProjects() {
    /* @formatter:off
     * 
     * fetches all projects from the database
     * 
     * stores the sql statement in the sql string
     * 
     * creates a connection
     *    starts a transaction;
     *    throws an outer exception IF a connection can't be made
     * 
     * creates a preparedstatement
     *    executes the sql statement (in stmt);
     *    throws an inner exception IF the sql statement is incorrect AND rolls back the
     *    transaction
     *    
     * creates a resultset
     *    creates a new list and a while loop that analyzes the results of the sql query;
     *    if the resultset pointer encounters a row, i.e. a project (name, notes, hours, etc.),
     *    add that project to the list
     * 
     * @formatter:on
     */
    
    String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_name";

    try (Connection conn = DbConnection.getConnection()) {
      startTransaction(conn);

      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        try (ResultSet rs = stmt.executeQuery()) {
          List<Project> projects = new LinkedList<Project>();

          while (rs.next()) {
            projects.add(extract(rs, Project.class));

            /*
             * "projects.add(extract(rs, Project.class))" can be manually done:
             * 
             * Project project = new Project();
             * 
             * project.setActualHours(rs.getBigDecimal("actual_hours"));
             * project.setDifficulty(rs.getObject("difficulty", Integer.class));
             * project.setEstimatedHours(rs.getBigDecimal("estimated_hours"));
             * project.setNotes(rs.getString("notes"));
             * project.setProjectId(rs.getObject("project_id", Integer.class));
             * project.setProjectName(rs.getString("project_name"));
             * 
             * projects.add(project);
             *
             * 
             */
          }

          return projects;
        }

      } catch (Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }

    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public Optional<Project> fetchProjectById(Integer projectId) {
    /* @formatter:off
     * 
     * fetches the project selected by the user from the database
     * 
     * stores the sql statement in the sql string
     * 
     * creates a connection
     *    starts a transaction;
     *    creates a try/catch block that creates a new project object set to null and commits
     *    the transaction;
     *    once the preparedstatement (read below) and resultset (read below) have been created,
     *    if the project object is not null (as in the project exists and has a name, notes,
     *    hours, etc.), set that project's materials, steps, and categories to those fetched
     *    by fetchMaterialsForProject(), fetchStepsForProject(), and fetchCategoriesForProject();
     *    throws an outer exception IF a connection can't be made
     *    
     * 
     * creates a preparedstatement
     *    validates the projectId parameter, and executes the sql statement (in stmt);
     *    throws an inner exception IF the sql statement is incorrect AND rolls back the
     *    transaction
     *    
     * creates a resultset
     *    the if statement analyzes the results of the sql query;
     *    if the resultset pointer encounters a row, i.e. a project (name, notes, hours, etc.),
     *    store that project to the project object
     * 
     * @formatter:on
     */
    
    String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?";

    try (Connection conn = DbConnection.getConnection()) {
      startTransaction(conn);

      try {
        Project project = null;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
          setParameter(stmt, 1, projectId, Integer.class);

          try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
              project = extract(rs, Project.class);
            }
          }
        }

        if (Objects.nonNull(project)) {
          project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
          project.getSteps().addAll(fetchStepsForProject(conn, projectId));
          project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
        }

        commitTransaction(conn);

        return Optional.ofNullable(project);
      } catch (Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }

    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId)
      throws SQLException {
    /* @formatter:off
     * 
     * fetches the categories of the project selected by the user from the database
     * 
     * stores the sql statement in the sql string
     * 
     * creates a preparedstatement
     *    validates the projectId parameter, and executes the sql statement (in stmt);
     *    throws an inner exception IF the sql statement is incorrect AND rolls back the
     *    transaction
     *    
     * creates a resultset
     *    creates a new list and a while loop that analyzes the results of the sql query;
     *    if the resultset pointer encounters a row, i.e. a category, add that category to
     *    the list
     *    
     * @formatter:on
     */
    
    // @formatter:off
    String sql = ""
        + "SELECT c.* FROM " + CATEGORY_TABLE + " c "
        + "JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
        + "WHERE project_id = ?";
    // @formatter:on

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      setParameter(stmt, 1, projectId, Integer.class);

      try (ResultSet rs = stmt.executeQuery()) {
        List<Category> categories = new LinkedList<Category>();

        while (rs.next()) {
          categories.add(extract(rs, Category.class));
        }

        return categories;
      }
    }
  }

  private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {
    /* @formatter:off
     * 
     * fetches the steps of the project selected by the user from the database
     * 
     * stores the sql statement in the sql string
     * 
     * creates a preparedstatement
     *    validates the projectId parameter, and executes the sql statement (in stmt);
     *    throws an inner exception IF the sql statement is incorrect AND rolls back the
     *    transaction
     *    
     * creates a resultset
     *    creates a new list and a while loop that analyzes the results of the sql query;
     *    if the resultset pointer encounters a row, i.e. a step, add that step to
     *    the list
     *    
     * @formatter:on
     */
    
    String sql = "SELECT * FROM " + STEP_TABLE + " WHERE project_id = ?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      setParameter(stmt, 1, projectId, Integer.class);

      try (ResultSet rs = stmt.executeQuery()) {
        List<Step> steps = new LinkedList<Step>();

        while (rs.next()) {
          steps.add(extract(rs, Step.class));
        }

        return steps;
      }
    }
  }

  private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId)
      throws SQLException {
    /* @formatter:off
     * 
     * fetches the materials of the project selected by the user from the database
     * 
     * stores the sql statement in the sql string
     * 
     * creates a preparedstatement
     *    validates the projectId parameter, and executes the sql statement (in stmt);
     *    throws an inner exception IF the sql statement is incorrect AND rolls back the
     *    transaction
     *    
     * creates a resultset
     *    creates a new list and a while loop that analyzes the results of the sql query;
     *    if the resultset pointer encounters a row, i.e. a material, add that material to
     *    the list
     *    
     * @formatter:on
     */
    
    String sql = "SELECT * FROM " + MATERIAL_TABLE + " WHERE project_id = ?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      setParameter(stmt, 1, projectId, Integer.class);

      try (ResultSet rs = stmt.executeQuery()) {
        List<Material> materials = new LinkedList<Material>();

        while (rs.next()) {
          materials.add(extract(rs, Material.class));
        }

        return materials;
      }
    }
  }

}
