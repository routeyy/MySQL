package projects.service;

import java.util.List;
import java.util.NoSuchElementException;
import projects.dao.ProjectDao;
import projects.entity.Project;

// serves as the "middle layer" that passes data back-and-forth between ProjectsApp and ProjectDao
public class ProjectService {
  private ProjectDao projectDao = new ProjectDao();

  public Project addProject(Project project) {
    // calls insertProject() on projectDao
    return projectDao.insertProject(project);
  }

  public List<Project> fetchAllProjects() {
    // calls fetchAllProjects() on projectDao
    return projectDao.fetchAllProjects();
  }

  public Project fetchProjectById(Integer projectId) {
    // calls fetchProjectById() on projectDao, and throws an exception if the project doesn't exist
    return projectDao.fetchProjectById(projectId).orElseThrow(() -> new NoSuchElementException(
        "Project with project ID=" + projectId + " does not exist."));
  }

}
