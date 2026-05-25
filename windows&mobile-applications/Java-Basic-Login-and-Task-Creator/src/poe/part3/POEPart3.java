/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package poe.part3;

import javax.swing.JOptionPane;

/**
 *
 * @author lab_services_student
 */
public class POEPart3 {

    public static void main(String[] args) {
        String logUsername = JOptionPane.showInputDialog("Enter the username ");
        String logPassword = JOptionPane.showInputDialog("Enter the password ");
        String logFirstName = JOptionPane.showInputDialog("Enter the firstname ");
        String logLastName = JOptionPane.showInputDialog("Enter the lastname ");

        Login login = new Login(logUsername, logPassword, logFirstName, logLastName);

        login.checkUserName();
        login.checkPasswordComplexity();
        String registrationMessage = login.registerUser();

        if (registrationMessage.toLowerCase().contains("successfully")) {
            if (login.loginUser()) {
                JOptionPane.showMessageDialog(null, "Welcome to EasyKanban");

                String[] featureOptions = {"1) Add tasks", "2) Show report ", "3) Quit"};
                int choice = 0;

                // Task object with dynamic task list
                Task task = new Task();
                int addAnotherTask = JOptionPane.YES_OPTION;

                do {
                    String choiceStr = (String) JOptionPane.showInputDialog(null, "Choose an option", "Menu", JOptionPane.QUESTION_MESSAGE, null, featureOptions, featureOptions[0]);
                    try {
                        choice = Integer.parseInt(choiceStr.substring(0, 1));
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, "Invalid choice. Please enter a number (1-3).");
                        continue;
                    }

                    switch (choice) {
                        case 1:
                            // Add tasks until user chooses to stop
                            do {
                                String taskName = JOptionPane.showInputDialog(null, "Enter the task name");
                                int taskNumber = task.getTaskCount() + 1;
                                String taskDescription = JOptionPane.showInputDialog(null, "Enter the task description");
                                if (taskDescription.length() > 50) {
                                    JOptionPane.showMessageDialog(null, "Please enter a task description of less than 50 characters");
                                    continue;
                                }
                                String developerDetails = JOptionPane.showInputDialog(null, "Enter the developer details (First Last)");
                                int taskDuration = Integer.parseInt(JOptionPane.showInputDialog(null, "Enter the task duration (in hours)"));
                                String[] taskStatusOptions = {"To Do", "Done", "Doing"};
                                String taskStatus = (String) JOptionPane.showInputDialog(null, "Choose task status", "Task Status", JOptionPane.QUESTION_MESSAGE, null, taskStatusOptions, taskStatusOptions[0]);

                                task.addTask(developerDetails, taskName, taskDescription, taskDuration, taskStatus);
                                JOptionPane.showMessageDialog(null, task.printTaskDetails(taskStatus, developerDetails, taskNumber, taskName, taskDescription, taskDuration));

                                
                                addAnotherTask = JOptionPane.showConfirmDialog(null, "Add another task?", "Add Task", JOptionPane.YES_NO_OPTION);
                            } while (addAnotherTask == JOptionPane.YES_OPTION);
                            int[] taskDurations = task.getTaskDurations(); // Get the array using the getter
                            int totalHours = task.returnTotalHours(taskDurations);
                            JOptionPane.showMessageDialog(null, "Total combined hours of all entered tasks: " + totalHours + " hours");
                            break;          
                        case 2:
                            String[] reportOptions = {"1) Display tasks with status Done", "2) Display task with longest duration", "3) Search for a task by name", "4) Search for tasks by developer", "5) Delete a task", "6) Display all tasks", "7) Back to main menu"};
                            int reportChoice = 0;
                            do {
                                String reportChoiceStr = (String) JOptionPane.showInputDialog(null, "Choose a report option", "Reports", JOptionPane.QUESTION_MESSAGE, null, reportOptions, reportOptions[0]);
                                try {
                                    reportChoice = Integer.parseInt(reportChoiceStr.substring(0, 1));
                                } catch (NumberFormatException e) {
                                    JOptionPane.showMessageDialog(null, "Invalid choice. Please enter a number (1-7).");
                                    continue;
                                }

                                switch (reportChoice) {
                                    case 1:
                                        task.displayTasksWithStatusDone();
                                        break;
                                    case 2:
                                        task.displayTaskWithLongestDuration();
                                        break;
                                    case 3:
                                        String searchTaskName = JOptionPane.showInputDialog(null, "Enter the task name to search for");
                                        task.searchTaskByName(searchTaskName);
                                        break;
                                    case 4:
                                        String searchDeveloperName = JOptionPane.showInputDialog(null, "Enter the developer name to search for tasks");
                                        task.searchTasksByDeveloper(searchDeveloperName);
                                        break;
                                    case 5:
                                        String deleteTaskName = JOptionPane.showInputDialog(null, "Enter the task name to delete");
                                        task.deleteTask(deleteTaskName);
                                        break;
                                    case 6:
                                        task.displayAllTasks();
                                        break;
                                    case 7:
                                        break;
                                    default:
                                        JOptionPane.showMessageDialog(null, "Invalid choice. Please enter a valid option.");
                                }
                            } while (reportChoice != 7);
                            break;

                        case 3:
                            JOptionPane.showMessageDialog(null, "Quitting application");
                            break;

                        default:
                            JOptionPane.showMessageDialog(null, "Invalid choice. Please enter a valid option.");
                    }
                } while (choice != 3);
            } else {
                JOptionPane.showMessageDialog(null, "Username or password incorrect");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Registration failed: " + registrationMessage);
        }
    }
}