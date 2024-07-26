import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.toedter.calendar.JDateChooser;

public class PlannedManager {
    private List<Gorev> gorevListesi; // Gorev objesi listesi
    private JPanel gorevPanel;
    private JPanel gorevlerPanel;
    private JTextField yeniGorevTextField;
    private JDateChooser dateChooser;
    private DatabaseManager db = new DatabaseManager();

    public PlannedManager() {
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
                String yeniGorevMetni = yeniGorevTextField.getText();
                Date secilenTarih = dateChooser.getDate(); // JDateChooser'dan tarihi al

                if (yeniGorevMetni != null && !yeniGorevMetni.isEmpty() && secilenTarih != null) {
                    Gorev yeniGorev = new Gorev(yeniGorevMetni, secilenTarih);
                    gorevEkle(yeniGorev); // Görev eklemeyi çağırma
                    kaydetGorev(yeniGorev); // Görevi veritabanına kaydet
                    guncelleGorevListesi(); // Görev listesini güncelleme
                    yeniGorevTextField.setText(""); // TextField'i temizle
                }
            }
        });
        yeniGorevPanel.add(yeniGorevButton, BorderLayout.EAST);

        gorevPanel.add(yeniGorevPanel, BorderLayout.SOUTH);

        // Tarih seçici bileşeni oluştur
        JPanel tarihSeciciPanel = new JPanel();
        tarihSeciciPanel.setLayout(new FlowLayout());
        tarihSeciciPanel.setBackground(Color.DARK_GRAY);

        JLabel dateLabel=new JLabel("Tarih Seç: ");
        dateLabel.setForeground(Color.white);
        tarihSeciciPanel.add(dateLabel);


        dateChooser = new JDateChooser();
        dateChooser.setPreferredSize(new Dimension(100, dateChooser.getPreferredSize().height));
        dateChooser.setDateFormatString("yyyy-MM-dd"); // Tarih formatını ayarla (isteğe bağlı)

        tarihSeciciPanel.add(dateChooser);

        gorevPanel.add(tarihSeciciPanel, BorderLayout.NORTH); // Tarih seçici panelini ekliyoruz

        veritabaniGorevleriYukle(); // Veritabanından görevleri yükle
    }

    public JPanel getGorevPanel() {
        return gorevPanel;
    }

    public void gorevEkle(Gorev gorev) {
        gorevListesi.add(gorev);
    }

    public List<Gorev> getGorevListesi() {
        return new ArrayList<>(gorevListesi);
    }

    private void guncelleGorevListesi() {
        Color color = new Color(91, 90, 90);
        gorevlerPanel.removeAll(); // Mevcut görevleri temizle

        for (Gorev gorev : gorevListesi) {
            JPanel gorevItemPanel = new JPanel();
            gorevItemPanel.setLayout(new BorderLayout());
            gorevItemPanel.setBackground(color);
            gorevItemPanel.setMaximumSize(new Dimension(800, 40)); // Sabit boyut ayarı
            gorevItemPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JTextArea gorevLabel = new JTextArea(gorev.getMetin() + " - " + formatDate(gorev.getTarih()));
            gorevLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            gorevLabel.setLineWrap(true); // Satır sonuna geldiğinde alt satıra geç
            gorevLabel.setWrapStyleWord(true); // Kelime bütünlüğünü koruyarak alt satıra geç
            gorevLabel.setBackground(color); // Arka plan rengini ayarla
            gorevLabel.setForeground(Color.white);
            gorevLabel.setEditable(false); // Düzenlenemez yapma
            gorevLabel.setMaximumSize(new Dimension(600, Integer.MAX_VALUE)); // Sabit boyut ayarı
            gorevItemPanel.add(gorevLabel, BorderLayout.CENTER);

            // Sağ tıklama menüsü oluşturma
            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem silMenuItem = new JMenuItem("Sil");
            silMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Silme işlemi burada gerçekleştirilecek
                    gorevListesi.remove(gorev);
                    silGorev(gorev); // Görevi veritabanından sil
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

            gorevlerPanel.add(gorevItemPanel);

            // Görevler arasına çizgi ekleme
            JSeparator separator = new JSeparator();
            separator.setMaximumSize(new Dimension(800, 1)); // Çizgi boyutunu ayarla
            gorevlerPanel.add(separator);
        }

        gorevlerPanel.revalidate();
        gorevlerPanel.repaint();
    }

    private void kaydetGorev(Gorev gorev) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String tarihStr = sdf.format(gorev.getTarih());
        String query = "INSERT INTO tasks (task_description, task_date) VALUES ('" +
                gorev.getMetin() + "', '" + tarihStr + "')";
        db.executeUpdate(query);
    }

    private void silGorev(Gorev gorev) {
        String query = "DELETE FROM tasks WHERE task_description = '" + gorev.getMetin() + "'";
        db.executeUpdate(query);
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    private void veritabaniGorevleriYukle() {
        gorevListesi.clear(); // Mevcut görev listesini temizle
        String query = "SELECT task_description, task_date FROM tasks WHERE task_date IS NOT NULL";
        try {
            ResultSet rs = db.executeSelectQuery(query);
            while (rs.next()) {
                String gorevMetni = rs.getString("task_description");
                Date tarih = rs.getDate("task_date");
                if (tarih != null) {
                    Gorev gorev = new Gorev(gorevMetni, tarih);
                    gorevEkle(gorev);
                }
            }
            guncelleGorevListesi();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Görev sınıfı
    public class Gorev {
        private String metin;
        private Date tarih;

        public Gorev(String metin, Date tarih) {
            this.metin = metin;
            this.tarih = tarih;
        }

        public String getMetin() {
            return metin;
        }

        public void setMetin(String metin) {
            this.metin = metin;
        }

        public Date getTarih() {
            return tarih;
        }

        public void setTarih(Date tarih) {
            this.tarih = tarih;
        }
    }

    public void update() {
        // Güncelleme işlemleri burada yapılır
        System.out.println("Planned Manager updated");
    }
}

