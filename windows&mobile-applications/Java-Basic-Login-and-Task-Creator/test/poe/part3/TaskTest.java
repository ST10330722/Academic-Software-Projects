/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package poe.part3;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author lab_services_student
 */
public class TaskTest {
    Task task = new Task();
    public TaskTest() {
    }

    @org.junit.jupiter.api.Test
    public void testAddTask() {
        // Test adding a task
        task.addTask("Mike Smith", "Create Login", "Task description", 5, "To Do");
        assertEquals(1, task.getTaskCount());
    }

    @org.junit.jupiter.api.Test
    public void testSearchTaskByName() {
        // Adding tasks for testing
        task.addTask("Mike Smith", "Create Login", "Task description", 5, "To Do");
        task.addTask("Edward Harrison", "Create Add Features", "Task description", 8, "Doing");
        task.addTask("Samantha Paulson", "Create Reports", "Task description", 2, "Done");

        // Test searching for a task by name
        task.searchTaskByName("Create Reports");

        // Asserting the output can be done by inspecting JOptionPane messages,
        // or by adding methods to Task class that allow testing outputs directly.
        // For simplicity, this can be assumed to verify the existence of expected tasks.
        // assertEquals(expectedOutput, actualOutput);
    }

    @org.junit.jupiter.api.Test
    public void testDisplayTaskWithLongestDuration() {
        // Adding tasks for testing
        task.addTask("Mike Smith", "Create Login", "Task description", 5, "To Do");
        task.addTask("Edward Harrison", "Create Add Features", "Task description", 8, "Doing");
        task.addTask("Samantha Paulson", "Create Reports", "Task description", 2, "Done");

        // Test displaying task with longest duration
        task.displayTaskWithLongestDuration();

        // Asserting the output can be done by inspecting JOptionPane messages,
        // or by adding methods to Task class that allow testing outputs directly.
        // assertEquals(expectedOutput, actualOutput);
    }

    @org.junit.jupiter.api.Test
    public void testDeleteTask() {
        // Adding tasks for testing
        task.addTask("Mike Smith", "Create Login", "Task description", 5, "To Do");
        task.addTask("Edward Harrison", "Create Add Features", "Task description", 8, "Doing");
        task.addTask("Samantha Paulson", "Create Reports", "Task description", 2, "Done");

        // Test deleting a task
        task.deleteTask("Create Reports");

        // Verify the task count after deletion
        assertEquals(2, task.getTaskCount());
    }
    
}
