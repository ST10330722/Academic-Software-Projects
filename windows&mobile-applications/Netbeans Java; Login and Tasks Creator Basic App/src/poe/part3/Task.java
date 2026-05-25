/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package poe.part3;

import javax.swing.JOptionPane;

/**
 *
 * @author lab_services_student
 */
public class Task {
    private String[] developers;
    private String[] taskNames;
    private String[] taskIDs;
    private int[] taskDurations;
    private String[] taskStatuses;

    public Task() {
        developers = new String[0];
        taskNames = new String[0];
        taskIDs = new String[0];
        taskDurations = new int[0];
        taskStatuses = new String[0];
    }
    
    public int[] getTaskDurations() {
        return taskDurations;
    }
    public int getTaskCount() {
        return developers.length; // Task count based on developer array length
    }

    public boolean checkTaskDescription(String taskDescription) {
        return taskDescription.length() <= 50;
    }

    public String createTaskID(String taskName, int taskNumber, String developerLastName) {
        return (taskName.substring(0, 2) + ":" + (taskNumber - 1) + ":"  + developerLastName.substring(developerLastName.length() - 3)).toUpperCase();
    }

    public void addTask(String developerDetails, String taskName, String taskDescription, int taskDuration, String taskStatus) {
        // Resize arrays dynamically to accommodate new tasks
        developers = resizeStringArray(developers);
        taskNames = resizeStringArray(taskNames);
        taskIDs = resizeStringArray(taskIDs);
        taskDurations = resizeIntArray(taskDurations);
        taskStatuses = resizeStringArray(taskStatuses);

        developers[developers.length - 1] = developerDetails;
        taskNames[taskNames.length - 1] = taskName;
        taskIDs[taskIDs.length - 1] = createTaskID(taskName, getTaskCount() + 1, developerDetails.split(" ")[1]);
        taskDurations[taskDurations.length - 1] = taskDuration;
        taskStatuses[taskStatuses.length - 1] = taskStatus;
    }

    private String[] resizeStringArray(String[] array) {
        String[] newArray = new String[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    private int[] resizeIntArray(int[] array) {
        int[] newArray = new int[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    public void displayTasksWithStatusDone() {
        StringBuilder report = new StringBuilder();
        for (int i = 0; i < getTaskCount(); i++) {
            if ("Done".equalsIgnoreCase(taskStatuses[i])) {
                report.append("Developer: ").append(developers[i]).append("\n");
                report.append("Task Name: ").append(taskNames[i]).append("\n");
                report.append("Task Duration: ").append(taskDurations[i]).append(" hours\n\n");
            }
        }
        JOptionPane.showMessageDialog(null, report.toString());
    }

     public void displayTaskWithLongestDuration() {
        int maxDuration = -1;
        int index = -1;
        for (int i = 0; i < getTaskCount(); i++) {
            if (taskDurations[i] > maxDuration) {
                maxDuration = taskDurations[i];
                index = i;
            }
        }
        if (index != -1) {
            JOptionPane.showMessageDialog(null, "Developer: " + developers[index] + "\nTask Duration: " + taskDurations[index] + " hours");
        }
    }

    public void searchTaskByName(String taskName) {
        StringBuilder report = new StringBuilder();
        for (int i = 0; i < getTaskCount(); i++) {
            if (taskNames[i].equalsIgnoreCase(taskName)) {
                report.append("Task Name: ").append(taskNames[i]).append("\n");
                report.append("Developer: ").append(developers[i]).append("\n");
                report.append("Task Status: ").append(taskStatuses[i]).append("\n\n");
            }
        }
        JOptionPane.showMessageDialog(null, report.toString());
    }

    public void searchTasksByDeveloper(String developerName) {
        StringBuilder report = new StringBuilder();
        for (int i = 0; i < getTaskCount(); i++) {
            if (developers[i].equalsIgnoreCase(developerName)) {
                report.append("Task Name: ").append(taskNames[i]).append("\n");
                report.append("Task Status: ").append(taskStatuses[i]).append("\n\n");
            }
        }
        JOptionPane.showMessageDialog(null, report.toString());
    }

    public void deleteTask(String taskName) {
        for (int i = 0; i < getTaskCount(); i++) {
            if (taskNames[i].equalsIgnoreCase(taskName)) {
                // Shift elements to the left to remove the deleted task
                for (int j = i; j < getTaskCount() - 1; j++) {
                    developers[j] = developers[j + 1];
                    taskNames[j] = taskNames[j + 1];
                    taskIDs[j] = taskIDs[j + 1];
                    taskDurations[j] = taskDurations[j + 1];
                    taskStatuses[j] = taskStatuses[j + 1];
                }
                developers = resizeStringArray(developers, -1); // Reduce array size by 1
                taskNames = resizeStringArray(taskNames, -1);
                taskIDs = resizeStringArray(taskIDs, -1);
                taskDurations = resizeIntArray(taskDurations, -1);
                taskStatuses = resizeStringArray(taskStatuses, -1);
                JOptionPane.showMessageDialog(null, "Task '" + taskName + "' has been deleted.");
                return;
            }
        }
        JOptionPane.showMessageDialog(null, "Task '" + taskName + "' not found.");
    }

    private String[] resizeStringArray(String[] array, int adjustment) {
        int newLength = array.length + adjustment;
        if (newLength <= 0) {
            return new String[0]; // Handle cases where the array becomes empty
        }
        String[] newArray = new String[newLength];
        System.arraycopy(array, 0, newArray, 0, Math.min(array.length, newLength));
        return newArray;
    }

    private int[] resizeIntArray(int[] array, int adjustment) {
        int newLength = array.length + adjustment;
        if (newLength <= 0) {
            return new int[0]; // Handle cases where the array becomes empty
        }
        int[] newArray = new int[newLength];
        System.arraycopy(array, 0, newArray, 0, Math.min(array.length, newLength));
        return newArray;
    }

    public void displayAllTasks() {
        StringBuilder report = new StringBuilder();
        for (int i = 0; i < getTaskCount(); i++) {
            report.append(printTaskDetails(taskStatuses[i], developers[i], i + 1, taskNames[i], "", taskDurations[i])).append("\n\n");
        }
        JOptionPane.showMessageDialog(null, report.toString());
        
    }
    
    public String printTaskDetails(String taskStatus, String developerDetails, int taskNumber, String taskName, String taskDescription, int taskDuration) {
        StringBuilder details = new StringBuilder();
        details.append("Task ID: ").append(taskIDs[taskNumber - 1]).append("\n");
        details.append("Task Number: ").append(taskNumber).append("\n");
        details.append("Task Name: ").append(taskName).append("\n");
        details.append("Developer: ").append(developerDetails).append("\n");
        details.append("Task Status: ").append(taskStatus).append("\n");
        details.append("Task Duration: ").append(taskDuration).append(" hours\n");
        if (taskDescription.length() > 0) {
            details.append("Task Description: ").append(taskDescription).append("\n");
        }
        return details.toString();
    }
    public int returnTotalHours(int[] taskDurations) {
        int totalHours = 0;
        for (int duration : taskDurations) {
            totalHours += duration;
        }
        return totalHours;
    }
}

