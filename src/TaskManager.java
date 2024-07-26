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

public class TaskManager {
    private List<String> gorevListesi;
    private JPanel gorevPanel;
    private JPanel gorevlerPanel;
    private JTextField yeniGorevTextField;
    private DatabaseManager db = new DatabaseManager();
    private boolean gunumFilter = false; // Flag to track filter state

    public TaskManager() {
        gorevListesi = new ArrayList<>();
        gorevPanel = new JPanel();
        gorevPanel.setLayout(new BorderLayout());
        gorevPanel.setBackground(Color.DARK_GRAY);

        gorevlerPanel = new JPanel();
        gorevlerPanel.setLayout(new BoxLayout(gorevlerPanel, BoxLayout.Y_AXIS));
        gorevlerPanel.setBackground(Color.DARK_GRAY);

        JScrollPane scrollPane = new JScrollPane(gorevlerPanel);
        gorevPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel yeniGorevPanel = new JPanel();
        yeniGorevPanel.setLayout(new BorderLayout());
        yeniGorevPanel.setBackground(Color.DARK_GRAY);

        yeniGorevTextField = new JTextField();
        yeniGorevTextField.setFont(new Font("Arial", Font.PLAIN, 14));
        yeniGorevPanel.add(yeniGorevTextField, BorderLayout.CENTER);

        JButton yeniGorevButton = new JButton("Yeni Görev Ekle");
        yeniGorevButton.setFont(new Font("Arial", Font.BOLD, 14));
        yeniGorevButton.setBackground(Color.DARK_GRAY);
        yeniGorevButton.setForeground(Color.WHITE);
        yeniGorevButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String yeniGorev = yeniGorevTextField.getText();
                if (yeniGorev != null && !yeniGorev.isEmpty()) {
                    String query = "INSERT INTO tasks (task_description) VALUES ('" + yeniGorev + "')";
                    db.executeUpdate(query);
                    gorevEkle(yeniGorev); // Görev eklemeyi çağırma
                    guncelleGorevListesi(); // Görev listesini güncelleme
                    yeniGorevTextField.setText(""); // TextField'i temizle
                }
            }
        });
        yeniGorevPanel.add(yeniGorevButton, BorderLayout.EAST);

        // "Günüm" button implementation
        JButton gunumButton = new JButton("Günüm");
        gunumButton.setFont(new Font("Arial", Font.BOLD, 14));
        gunumButton.setBackground(Color.DARK_GRAY);
        gunumButton.setForeground(Color.WHITE);
        gunumButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gunumFilter = !gunumFilter; // Toggle filter state
                guncelleGorevListesi(); // Update task list based on filter
            }
        });
        yeniGorevPanel.add(gunumButton, BorderLayout.WEST);

        gorevPanel.add(yeniGorevPanel, BorderLayout.SOUTH);
        veritabaniGorevleriYukle();
    }

    public JPanel getGorevPanel() {
        return gorevPanel;
    }

    public void gorevEkle(String gorev) {
        gorevListesi.add(gorev);
    }

    public List<String> getGorevListesi() {
        return new ArrayList<>(gorevListesi);
    }

    private void guncelleGorevListesi() {
        Color color = new Color(91, 90, 90);
        gorevlerPanel.removeAll(); // Mevcut görevleri temizle

        for (String gorev : gorevListesi) {
            // Apply filter based on "Günüm" button state
            if (gunumFilter && !isStarred(gorev)) {
                continue; // Skip non-important tasks when filter is active
            }

            JPanel gorevItemPanel = new JPanel();
            gorevItemPanel.setLayout(new BoxLayout(gorevItemPanel, BoxLayout.X_AXIS));
            gorevItemPanel.setBackground(color);
            gorevItemPanel.setMaximumSize(new Dimension(800, 40)); // Sabit boyut ayarı
            gorevItemPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Görevi veritabanından durumuna göre işaretleyen checkbox oluşturma
            JCheckBox checkBox = new JCheckBox();
            checkBox.setBackground(color); // Arka plan rengini ayarla
            checkBox.setSelected(isCompleted(gorev)); // Veritabanında yıldız işareti varsa seçili yap
            gorevItemPanel.add(checkBox);

            // Yıldız şeklinde checkbox oluşturma
            JCheckBox starCheckBox = new JCheckBox();
            starCheckBox.setBackground(color); // Arka plan rengini ayarla
            starCheckBox.setSelected(isStarred(gorev)); // Veritabanında tamamlanan görevse seçili yap
            ImageIcon icon = new ImageIcon("ikonlar/star.png");
            ImageIcon icon2 = new ImageIcon("ikonlar/star_selected.png");
            Image img = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH); // 16x16 boyutunda ölçeklendirme
            Image img2 = icon2.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH); // 16x16 boyutunda ölçeklendirme
            starCheckBox.setIcon(new ImageIcon(img)); // Normal yıldız simgesi
            starCheckBox.setSelectedIcon(new ImageIcon(img2)); // Seçili yıldız simgesi
            gorevItemPanel.add(starCheckBox);

            JTextArea gorevLabel = new JTextArea(gorev);
            gorevLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            gorevLabel.setLineWrap(true); // Satır sonuna geldiğinde alt satıra geç
            gorevLabel.setWrapStyleWord(true); // Kelime bütünlüğünü koruyarak alt satıra geç
            gorevLabel.setBackground(color); // Arka plan rengini ayarla
            gorevLabel.setForeground(Color.white);
            gorevLabel.setEditable(false); // Düzenlenemez yapma
            gorevLabel.setMaximumSize(new Dimension(600, Integer.MAX_VALUE)); // Sabit boyut ayarı
            gorevItemPanel.add(new JScrollPane(gorevLabel)); // JScrollPane ile sar

            // Sağ tıklama menüsü oluşturma
            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem silMenuItem = new JMenuItem("Sil");
            silMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Silme işlemi burada gerçekleştirilecek
                    gorevListesi.remove(gorev);
                    String query="DELETE FROM tasks WHERE task_description = '" + gorev + "'";
                    db.executeUpdate(query);
                    guncelleGorevListesi(); // Görev listesini güncelle
                }
            });
            popupMenu.add(silMenuItem);

            // Metin alanına sağ tıklama menüsü eklemek
            gorevLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });

            // Checkboxlar için ActionListener ekleme
            checkBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean selected = checkBox.isSelected();
                    setCompleted(gorev, selected); // Yıldız işaretli olarak güncelle
                }
            });

            starCheckBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean selected = starCheckBox.isSelected();
                    setStarred(gorev, selected); // Tamamlanma durumunu güncelle
                }
            });

            gorevlerPanel.add(gorevItemPanel);

            // Görevler arasına çizgi ekleme
            JSeparator separator = new JSeparator();
            separator.setMaximumSize(new Dimension(800, 1)); // Çizgi boyutunu ayarla
            gorevlerPanel.add(separator);
        }

        gorevlerPanel.revalidate();
        gorevlerPanel.repaint();
    }

    private void veritabaniGorevleriYukle() {
        gorevListesi.clear();
        String query = "SELECT task_description, durum_no, is_completed FROM tasks";
        try {
            ResultSet rs = db.executeSelectQuery(query);
            while (rs.next()) {
                String gorev = rs.getString("task_description");
                int durumNo = rs.getInt("durum_no");
                int isComplete = rs.getInt("is_completed");
                gorevEkle(gorev);

                // Görevin durumuna göre checkboxları güncelle
                if (durumNo == 2) {
                    setStarred(gorev, true); // Yıldız işareti varsa
                }
                if (isComplete == 2) {
                    setCompleted(gorev, true); // Tamamlanmış görevse
                }
            }
            guncelleGorevListesi();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setStarred(String gorev, boolean starred) {
        int durumNo = starred ? 2 : 1; // Yıldız işareti varsa durum_no 2, yoksa 1
        String query = "UPDATE tasks SET durum_no = " + durumNo + " WHERE task_description = '" + gorev + "'";
        db.executeUpdate(query); // Veritabanında güncelleme yap
    }

    private void setCompleted(String gorev, boolean completed) {
        String isCompleted = completed ? "2" : "1"; // Tamamlandı işareti varsa 2, yoksa 1
        String query = "UPDATE tasks SET is_completed = " + isCompleted + " WHERE task_description = '" + gorev + "'";
        db.executeUpdate(query); // Veritabanında güncelleme yap
    }

    private boolean isStarred(String gorev) {
        String query = "SELECT durum_no FROM tasks WHERE task_description = '" + gorev + "'";
        try {
            ResultSet rs = db.executeSelectQuery(query);
            if (rs.next()) {
                int durumNo = rs.getInt("durum_no");
                return durumNo == 2;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Varsayılan olarak false döndür
    }

    private boolean isCompleted(String gorev) {
        String query = "SELECT is_completed FROM tasks WHERE task_description = '" + gorev + "'";
        try {
            ResultSet rs = db.executeSelectQuery(query);
            if (rs.next()) {
                int isComplete = rs.getInt("is_completed");
                return isComplete == 2;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Varsayılan olarak false döndür
    }

    public void update() {
        veritabaniGorevleriYukle();
    }
}