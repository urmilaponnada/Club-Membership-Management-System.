import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class ClubMembershipApp extends JFrame {
    private static final int MAX_MEMBERS = 30;
    private static final String DATA_FILE = "club_membership_data.ser";

    static class Club implements Serializable {
        int id;
        String name, description;
        Club(int id, String name, String description) {
            this.id = id; this.name = name; this.description = description;
        }
    }

    static class Student implements Serializable {
        int id;
        String name, email, department;
        Student(int id, String name, String email, String department) {
            this.id = id; this.name = name; this.email = email; this.department = department;
        }
    }

    static class Membership implements Serializable {
        int studentId, clubId;
        String date;
        Membership(int studentId, int clubId) {
            this.studentId = studentId;
            this.clubId = clubId;
            this.date = new java.text.SimpleDateFormat("dd-MM-yyyy").format(new Date());
        }
    }

    static class Store implements Serializable {
        List<Club> clubs = new ArrayList<>();
        List<Student> students = new ArrayList<>();
        List<Membership> memberships = new ArrayList<>();
        int nextClubId = 1, nextStudentId = 1;
    }

    private Store store = new Store();
    private JTable clubTable, studentTable, membershipTable, searchTable;
    private DefaultTableModel clubModel, studentModel, membershipModel, searchModel;
    private JComboBox<String> studentCombo, clubCombo;
    private JTextField searchField;
    private JLabel summaryLabel;

    public ClubMembershipApp() {
        setTitle("Club Membership Management System");
        setSize(1050, 680);
        setMinimumSize(new Dimension(850, 550));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        loadData();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", createDashboard());
        tabs.addTab("Clubs", createClubPanel());
        tabs.addTab("Students", createStudentPanel());
        tabs.addTab("Membership", createMembershipPanel());
        tabs.addTab("Search", createSearchPanel());
        add(tabs);

        refreshAll();
    }

    private JPanel createDashboard() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        JLabel heading = new JLabel("College Club Membership Dashboard", SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 25));
        panel.add(heading, BorderLayout.NORTH);

        summaryLabel = new JLabel("", SwingConstants.CENTER);
        summaryLabel.setFont(new Font("Arial", Font.PLAIN, 19));
        panel.add(summaryLabel, BorderLayout.CENTER);

        JTextArea info = new JTextArea(
            "PROJECT FEATURES\n\n" +
            "• Create, view, edit and delete clubs\n" +
            "• Register and manage students\n" +
            "• Allow students to join clubs\n" +
            "• Maximum 30 members for new registrations\n" +
            "• Prevent duplicate memberships\n" +
            "• Flag existing clubs with more than 30 members\n" +
            "• Search clubs and view membership counts\n\n" +
            "All information is saved locally in " + DATA_FILE
        );
        info.setEditable(false);
        info.setFont(new Font("Arial", Font.PLAIN, 15));
        info.setBackground(new Color(245, 248, 252));
        info.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panel.add(info, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createClubPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        clubModel = model("ID", "Club Name", "Description", "Members", "Status");
        clubTable = new JTable(clubModel);
        panel.add(new JScrollPane(clubTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        JButton add = new JButton("Add Club");
        JButton edit = new JButton("Edit Selected");
        JButton delete = new JButton("Delete Selected");
        JButton refresh = new JButton("Refresh");
        add.addActionListener(e -> addClub());
        edit.addActionListener(e -> editClub());
        delete.addActionListener(e -> deleteClub());
        refresh.addActionListener(e -> refreshAll());
        buttons.add(add); buttons.add(edit); buttons.add(delete); buttons.add(refresh);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        studentModel = model("ID", "Student Name", "Email", "Department");
        studentTable = new JTable(studentModel);
        panel.add(new JScrollPane(studentTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        JButton add = new JButton("Register Student");
        JButton edit = new JButton("Edit Selected");
        JButton delete = new JButton("Delete Selected");
        add.addActionListener(e -> addStudent());
        edit.addActionListener(e -> editStudent());
        delete.addActionListener(e -> deleteStudent());
        buttons.add(add); buttons.add(edit); buttons.add(delete);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createMembershipPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        membershipModel = model("Student ID", "Student Name", "Club ID", "Club Name", "Joined Date");
        membershipTable = new JTable(membershipModel);
        panel.add(new JScrollPane(membershipTable), BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
        studentCombo = new JComboBox<>();
        clubCombo = new JComboBox<>();
        JButton join = new JButton("Join Club");
        join.addActionListener(e -> joinClub());
        top.add(new JLabel("Select Student:"));
        top.add(studentCombo);
        top.add(new JLabel("Select Club:"));
        top.add(clubCombo);
        top.add(join);
        panel.add(top, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
        searchField = new JTextField(25);
        JButton search = new JButton("Search");
        JButton showAll = new JButton("Show All Clubs");
        search.addActionListener(e -> searchClubs());
        showAll.addActionListener(e -> { searchField.setText(""); searchClubs(); });
        top.add(new JLabel("Club name:"));
        top.add(searchField);
        top.add(search);
        top.add(showAll);
        panel.add(top, BorderLayout.NORTH);

        searchModel = model("Club ID", "Club Name", "Description", "Members", "Status");
        searchTable = new JTable(searchModel);
        panel.add(new JScrollPane(searchTable), BorderLayout.CENTER);
        return panel;
    }

    private DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private int memberCount(int clubId) {
        int count = 0;
        for (Membership m : store.memberships)
            if (m.clubId == clubId) count++;
        return count;
    }

    private Club findClub(int id) {
        for (Club c : store.clubs) if (c.id == id) return c;
        return null;
    }

    private Student findStudent(int id) {
        for (Student s : store.students) if (s.id == id) return s;
        return null;
    }

    private void refreshAll() {
        refreshClubs();
        refreshStudents();
        refreshMemberships();
        refreshCombos();
        searchClubs();
        if (summaryLabel != null) {
            summaryLabel.setText("<html><div style='text-align:center'>" +
                "<h1>Clubs: " + store.clubs.size() + "</h1>" +
                "<h1>Students: " + store.students.size() + "</h1>" +
                "<h1>Memberships: " + store.memberships.size() + "</h1></div></html>");
        }
    }

    private void refreshClubs() {
        if (clubModel == null) return;
        clubModel.setRowCount(0);
        for (Club c : store.clubs) {
            int count = memberCount(c.id);
            String status = count > MAX_MEMBERS ? "OVER CAPACITY (EXISTING)" :
                            count == MAX_MEMBERS ? "FULL" : "OPEN";
            clubModel.addRow(new Object[]{c.id, c.name, c.description, count, status});
        }
    }

    private void refreshStudents() {
        if (studentModel == null) return;
        studentModel.setRowCount(0);
        for (Student s : store.students)
            studentModel.addRow(new Object[]{s.id, s.name, s.email, s.department});
    }

    private void refreshMemberships() {
        if (membershipModel == null) return;
        membershipModel.setRowCount(0);
        for (Membership m : store.memberships) {
            Student s = findStudent(m.studentId);
            Club c = findClub(m.clubId);
            if (s != null && c != null)
                membershipModel.addRow(new Object[]{s.id, s.name, c.id, c.name, m.date});
        }
    }

    private void refreshCombos() {
        if (studentCombo == null) return;
        studentCombo.removeAllItems();
        clubCombo.removeAllItems();
        for (Student s : store.students)
            studentCombo.addItem(s.id + " - " + s.name);
        for (Club c : store.clubs)
            clubCombo.addItem(c.id + " - " + c.name + " (" + memberCount(c.id) + "/30)");
    }

    private void searchClubs() {
        if (searchModel == null) return;
        searchModel.setRowCount(0);
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase();
        for (Club c : store.clubs) {
            if (c.name.toLowerCase().contains(query) || c.description.toLowerCase().contains(query)) {
                int count = memberCount(c.id);
                String status = count > MAX_MEMBERS ? "OVER CAPACITY (EXISTING)" :
                                count == MAX_MEMBERS ? "FULL" : "OPEN";
                searchModel.addRow(new Object[]{c.id, c.name, c.description, count, status});
            }
        }
    }

    private void addClub() {
        JTextField name = new JTextField();
        JTextField desc = new JTextField();
        Object[] fields = {"Club name:", name, "Description:", desc};
        if (JOptionPane.showConfirmDialog(this, fields, "Add Club",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            if (name.getText().trim().isEmpty()) {
                message("Club name is required."); return;
            }
            store.clubs.add(new Club(store.nextClubId++, name.getText().trim(), desc.getText().trim()));
            saveData(); refreshAll();
        }
    }

    private void editClub() {
        int row = clubTable.getSelectedRow();
        if (row < 0) { message("Please select a club."); return; }
        int id = (int) clubModel.getValueAt(row, 0);
        Club c = findClub(id);
        JTextField name = new JTextField(c.name);
        JTextField desc = new JTextField(c.description);
        Object[] fields = {"Club name:", name, "Description:", desc};
        if (JOptionPane.showConfirmDialog(this, fields, "Edit Club",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            if (name.getText().trim().isEmpty()) { message("Club name is required."); return; }
            c.name = name.getText().trim();
            c.description = desc.getText().trim();
            saveData(); refreshAll();
        }
    }

    private void deleteClub() {
        int row = clubTable.getSelectedRow();
        if (row < 0) { message("Please select a club."); return; }
        int id = (int) clubModel.getValueAt(row, 0);
        if (memberCount(id) > 0) {
            message("This club has members. Remove/migrate memberships before deleting it.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete selected club?",
                "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            store.clubs.removeIf(c -> c.id == id);
            saveData(); refreshAll();
        }
    }

    private void addStudent() {
        JTextField name = new JTextField();
        JTextField email = new JTextField();
        JTextField dept = new JTextField();
        Object[] fields = {"Student name:", name, "Email:", email, "Department:", dept};
        if (JOptionPane.showConfirmDialog(this, fields, "Register Student",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            if (name.getText().trim().isEmpty() || email.getText().trim().isEmpty()) {
                message("Student name and email are required."); return;
            }
            for (Student s : store.students) {
                if (s.email.equalsIgnoreCase(email.getText().trim())) {
                    message("This email is already registered."); return;
                }
            }
            store.students.add(new Student(store.nextStudentId++, name.getText().trim(),
                email.getText().trim(), dept.getText().trim()));
            saveData(); refreshAll();
        }
    }

    private void editStudent() {
        int row = studentTable.getSelectedRow();
        if (row < 0) { message("Please select a student."); return; }
        int id = (int) studentModel.getValueAt(row, 0);
        Student s = findStudent(id);
        JTextField name = new JTextField(s.name);
        JTextField email = new JTextField(s.email);
        JTextField dept = new JTextField(s.department);
        Object[] fields = {"Student name:", name, "Email:", email, "Department:", dept};
        if (JOptionPane.showConfirmDialog(this, fields, "Edit Student",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            if (name.getText().trim().isEmpty() || email.getText().trim().isEmpty()) {
                message("Student name and email are required."); return;
            }
            for (Student other : store.students) {
                if (other.id != id && other.email.equalsIgnoreCase(email.getText().trim())) {
                    message("Another student already uses this email."); return;
                }
            }
            s.name = name.getText().trim();
            s.email = email.getText().trim();
            s.department = dept.getText().trim();
            saveData(); refreshAll();
        }
    }

    private void deleteStudent() {
        int row = studentTable.getSelectedRow();
        if (row < 0) { message("Please select a student."); return; }
        int id = (int) studentModel.getValueAt(row, 0);
        boolean hasMembership = store.memberships.stream().anyMatch(m -> m.studentId == id);
        if (hasMembership) {
            message("This student has club memberships. Student cannot be deleted.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Delete selected student?",
                "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            store.students.removeIf(s -> s.id == id);
            saveData(); refreshAll();
        }
    }

    private void joinClub() {
        if (studentCombo.getSelectedItem() == null || clubCombo.getSelectedItem() == null) {
            message("First register at least one student and one club."); return;
        }
        int studentId = Integer.parseInt(studentCombo.getSelectedItem().toString().split(" - ")[0]);
        int clubId = Integer.parseInt(clubCombo.getSelectedItem().toString().split(" - ")[0]);

        for (Membership m : store.memberships) {
            if (m.studentId == studentId && m.clubId == clubId) {
                message("This student is already a member of this club."); return;
            }
        }

        int count = memberCount(clubId);
        if (count >= MAX_MEMBERS) {
            message(count > MAX_MEMBERS
                ? "Registration blocked. This club is already OVER CAPACITY."
                : "Registration blocked. This club has reached its 30-member limit.");
            return;
        }

        store.memberships.add(new Membership(studentId, clubId));
        saveData(); refreshAll();
        message("Student successfully joined the club!");
    }

    private void message(String text) {
        JOptionPane.showMessageDialog(this, text);
    }

    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            store = (Store) in.readObject();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Could not read saved data. A new data store will be used.\n" + e.getMessage());
            store = new Store();
        }
    }

    private void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(store);
        } catch (IOException e) {
            message("Error saving data: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClubMembershipApp().setVisible(true));
    }
}
