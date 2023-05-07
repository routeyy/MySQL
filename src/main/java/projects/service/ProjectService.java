package projects.service;

import projects.dao.ProjectDao;
import projects.entity.Project;

public class ProjectService {
  private ProjectDao projectDao = new ProjectDao();

  public Project addProject(Project project) {
    //serves as the "middle layer" that passes data back-and-forth between ProjectsApp and ProjectDao
    return projectDao.insertProject(project);
  }

}
