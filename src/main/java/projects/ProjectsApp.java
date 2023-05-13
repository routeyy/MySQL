package projects;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import projects.entity.Project;
import projects.exception.DbException;
import projects.service.ProjectService;

public class ProjectsApp {
  // @formatter:off
  private List<String> operations = List.of(
      "1) Add a project.",
      "2) List projects.",
      "3) Select a project."
  );
  // @formatter:on
  private Scanner scanner = new Scanner(System.in);
  private ProjectService projectService = new ProjectService();
  private Project curProject;

  public static void main(String[] args) {
    // runs processUserSelections()
    new ProjectsApp().processUserSelections();
  }

  private void processUserSelections() {
    /*
     * this method uses numerous other methods to accomplish tasks chosen by the user, such as
     * creating a project
     * 
     * uses a switch statement that analyzes the user's input, and does a task accordingly e.g. if
     * user's input is blank, exitMenu() is called throws an exception if the user input isn't an
     * integer, otherwise hit default
     */
    boolean done = false;

    while (!done) {
      try {
        int selection = getUserSelection();

        switch (selection) {
          case -1:
            done = exitMenu();
            break;

          case 1:
            createProject();
            break;

          case 2:
            listProjects();
            break;
            
          case 3:
            selectProject();
            break;

          default:
            System.out.println("\n" + selection + " is not a valid selection. Try again.");
        }

      } catch (Exception e) {
        System.out.println("\nError: " + e + " Try again.");
      }
    }
  }

  private void selectProject() {
    /*
     * list the available projects
     * 
     * retrieve the user's input, store that input in projectId, deselect any currently
     * selected projects, then set curRecipe to the user's selection by fetching the project
     * that has the same projectId from projectDao through projectService
     */
    listProjects();
    
    Integer projectId = getIntInput("\nEnter a project ID to select a project");
    
    curProject = null;
    
    curProject = projectService.fetchProjectById(projectId);
  }

  private void listProjects() {
    //fetch the projects from projectDao through projectService and list them
    List<Project> projects = projectService.fetchAllProjects();

    System.out.println("\nProjects:");

    projects.forEach(project -> System.out.println
        ("   " + project.getProjectId() + ": " + project.getProjectName()));

  }

  private void createProject() {
    // call getInput methods and assign them variables
    String projectName = getStringInput("Enter the project name");
    BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours");
    BigDecimal actualHours = getDecimalInput("Enter the actual hours");
    Integer difficulty = getIntInput("Enter the project difficulty (1-5)");
    String notes = getStringInput("Enter the project notes");

    // create a new project that will "adopt" the user's inputs
    Project project = new Project();

    project.setProjectName(projectName);
    project.setEstimatedHours(estimatedHours);
    project.setActualHours(actualHours);
    project.setDifficulty(difficulty);
    project.setNotes(notes);

    Project dbProject = projectService.addProject(project);

    System.out.println("You have successfuly created project: " + dbProject);
  }

  private BigDecimal getDecimalInput(String prompt) {
    // retrieves a string input through getStringInput(), and converts it to a BigDecimal
    String input = getStringInput(prompt);

    if (Objects.isNull(input)) {
      return null;
    }

    try {
      return new BigDecimal(input).setScale(2);
    } catch (NumberFormatException e) {
      throw new DbException(input + " is not a valid decimal number.");
    }
  }

  private boolean exitMenu() {
    // simply breaks the while loop by setting done to true
    System.out.println("\nExiting the menu.");
    return true;
  }

  private int getUserSelection() {
    /*
     * show the possible tasks that can be done, retrieve an Integer input through getIntInput(), if
     * null, return -1, else return the user's input
     */
    printOperations();

    Integer input = getIntInput("\nEnter a menu selection");

    return Objects.isNull(input) ? -1 : input;
  }

  private Integer getIntInput(String prompt) {
    // retrieves a string input through getStringInput(), and converts it to a Integer
    String input = getStringInput(prompt);

    if (Objects.isNull(input)) {
      return null;
    }

    try {
      return Integer.valueOf(input);
    } catch (NumberFormatException e) {
      throw new DbException(input + " is not a valid number.");
    }
  }

  private String getStringInput(String prompt) {
    // lowest level input method: get string from user, other methods call this method to convert
    System.out.print(prompt + ": ");
    String input = scanner.nextLine();

    return input.isBlank() ? null : input.trim();
  }

  private void printOperations() {
    // prints out each operation in operations (above)
    System.out.println("\nThese are the available selections. Press the Enter key to quit:");

    operations.forEach(line -> System.out.println("   " + line));
    
    if(Objects.isNull(curProject)) {
      System.out.println("\nYou are not working with a project.");
    } else {
      System.out.println("\nYou are working with project: " + curProject);
    }
  }

}
