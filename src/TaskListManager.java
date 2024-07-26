import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TaskListManager {
    private List<String> taskList;
    private JPanel taskPanel;
    private JPanel taskContainerPanel;
    private JTextField newTaskField;
    private DatabaseManager db;
    private String listName;
    private boolean importantFilter = false;

    public TaskListManager(DatabaseManager db, String listName) {
        this.taskList = new ArrayList<>();
        this.db = db;
        this.listName = listName;

        taskPanel = new JPanel();
        taskPanel.setLayout(new BorderLayout());
        taskPanel.setBackground(Color.DARK_GRAY);

        taskContainerPanel = new JPanel();
        taskContainerPanel.setLayout(new BoxLayout(taskContainerPanel, BoxLayout.Y_AXIS));
        taskContainerPanel.setBackground(Color.DARK_GRAY);

        JScrollPane scrollPane = new JScrollPane(taskContainerPanel);
        taskPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel newTaskPanel = new JPanel();
        newTaskPanel.setLayout(new BorderLayout());
        newTaskPanel.setBackground(Color.DARK_GRAY);

        newTaskField = new JTextField();
        newTaskField.setFont(new Font("Arial", Font.PLAIN, 14));
        newTaskPanel.add(newTaskField, BorderLayout.CENTER);

        JButton addTaskButton = new JButton("Yeni Görev Ekle");
        addTaskButton.setFont(new Font("Arial", Font.BOLD, 14));
        addTaskButton.setBackground(Color.DARK_GRAY);
        addTaskButton.setForeground(Color.WHITE);
        addTaskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newTask = newTaskField.getText().trim();
                if (!newTask.isEmpty()) {
                    addTask(newTask);
                    newTaskField.setText(""); // Clear the text field
                }
            }
        });
        newTaskPanel.add(addTaskButton, BorderLayout.EAST);

        JButton importantButton = new JButton("Önemli");
        importantButton.setFont(new Font("Arial", Font.BOLD, 14));
        importantButton.setBackground(Color.DARK_GRAY);
        importantButton.setForeground(Color.WHITE);
        importantButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                importantFilter = !importantFilter;
                updateTaskList(); // Update the task list based on filter
            }
        });
        newTaskPanel.add(importantButton, BorderLayout.WEST);

        taskPanel.add(newTaskPanel, BorderLayout.SOUTH);

        // Create table for the new list
        db.createListTable(listName);
        loadTasksFromDatabase();
    }

    public JPanel getTaskPanel() {
        return taskPanel;
    }

    private void addTask(String taskDescription) {
        taskList.add(taskDescription);
        addTaskToDatabase(taskDescription);
        updateTaskList();
    }

    private void addTaskToDatabase(String taskDescription) {
        String sql = "INSERT INTO " + listName + " (task_description, is_completed) VALUES (?, FALSE)";
        db.executePreparedStatement(sql, taskDescription);
    }

    private void updateTaskList() {
        taskContainerPanel.removeAll();
        List<String> tasksFromDatabase = loadTasksFromDatabase();
        for (String task : tasksFromDatabase) {
            if (importantFilter && !isStarred(task)) {
                continue; // Skip non-important tasks when filter is active
            }
            JPanel taskItemPanel = createTaskItemPanel(task);
            taskContainerPanel.add(taskItemPanel);
        }
        taskContainerPanel.revalidate();
        taskContainerPanel.repaint();
    }

    private List<String> loadTasksFromDatabase() {
        List<String> tasks = new ArrayList<>();
        String sql = "SELECT task_description FROM " + listName;
        try {
            ResultSet rs = db.executeSelectQuery(sql);
            while (rs.next()) {
                tasks.add(rs.getString("task_description"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    private JPanel createTaskItemPanel(String task) {
        JPanel taskItemPanel = new JPanel();
        taskItemPanel.setLayout(new BoxLayout(taskItemPanel, BoxLayout.X_AXIS));
        taskItemPanel.setBackground(new Color(91, 90, 90));
        taskItemPanel.setMaximumSize(new Dimension(800, 40));
        taskItemPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JCheckBox checkBox = new JCheckBox();
        checkBox.setBackground(new Color(91, 90, 90));
        checkBox.setSelected(isCompleted(task));
        taskItemPanel.add(checkBox);

        JCheckBox starCheckBox = new JCheckBox();
        starCheckBox.setBackground(new Color(91, 90, 90));
        starCheckBox.setSelected(isStarred(task));
        ImageIcon starIcon = new ImageIcon("ikonlar/star.png");
        ImageIcon starSelectedIcon = new ImageIcon("ikonlar/star_selected.png");
        starCheckBox.setIcon(new ImageIcon(starIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
        starCheckBox.setSelectedIcon(new ImageIcon(starSelectedIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
        taskItemPanel.add(starCheckBox);

        JTextArea taskLabel = new JTextArea(task);
        taskLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        taskLabel.setLineWrap(true);
        taskLabel.setWrapStyleWord(true);
        taskLabel.setBackground(new Color(91, 90, 90));
        taskLabel.setForeground(Color.white);
        taskLabel.setEditable(false);
        taskLabel.setMaximumSize(new Dimension(600, Integer.MAX_VALUE));
        taskItemPanel.add(new JScrollPane(taskLabel));

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteMenuItem = new JMenuItem("Sil");
        deleteMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeTask(task);
            }
        });
        popupMenu.add(deleteMenuItem);

        taskLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        checkBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean selected = checkBox.isSelected();
                setCompleted(task, selected);
            }
        });

        starCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean selected = starCheckBox.isSelected();
                setStarred(task, selected);
            }
        });

        return taskItemPanel;
    }

    private void removeTask(String task) {
        taskList.remove(task);
        String sql = "DELETE FROM " + listName + " WHERE task_description = ?";
        db.executePreparedStatement(sql, task);
        updateTaskList();
    }

    private void setStarred(String task, boolean starred) {
        int status = starred ? 2 : 1;
        String sql = "UPDATE " + listName + " SET durum_no = ? WHERE task_description = ?";
        db.executePreparedStatement(sql, status, task);
    }

    private void setCompleted(String task, boolean completed) {
        int status = completed ? 2 : 1;
        String sql = "UPDATE " + listName + " SET is_completed = ? WHERE task_description = ?";
        db.executePreparedStatement(sql, status, task);
    }

    private boolean isStarred(String task) {
        String sql = "SELECT durum_no FROM " + listName + " WHERE task_description = ?";
        try {
            ResultSet rs = db.executeSelectQuery(sql);
            if (rs.next()) {
                int status = rs.getInt("durum_no");
                return status == 2;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isCompleted(String task) {
        String sql = "SELECT is_completed FROM " + listName + " WHERE task_description = ?";
        try {
            ResultSet rs = db.executeSelectQuery(sql);
            if (rs.next()) {
                int status = rs.getInt("is_completed");
                return status == 2;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
