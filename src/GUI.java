import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class GUI {
    private JPanel mainContent; // Ana içerik paneli
    private TaskManager tm; // Görev yönetimi sınıfı referansı
    private ImportantManager im; // Önemli yönetimi sınıfı referansı
    private PlannedManager pm; // Planlanan yönetimi sınıfı referansı
    private Map<String, JPanel> listPanels; // Liste adı ve panel eşlemesi için kullanılacak map
    private JPanel listContainer; // Liste butonlarının bulunduğu panel
    private DatabaseManager db;

    public GUI() {
        // Ana pencere oluşturma
        JFrame jFrame = new JFrame("TO DO LIST");
        jFrame.setSize(800, 600);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setLayout(new BorderLayout()); // Layout manager ayarı

        // Yan menü paneli oluşturma
        JPanel sideMenu = new JPanel();
        sideMenu.setLayout(new BoxLayout(sideMenu, BoxLayout.Y_AXIS));
        sideMenu.setPreferredSize(new Dimension(200, jFrame.getHeight()));
        sideMenu.setBackground(Color.gray);

        // Menü butonlarını oluşturma
        JButton button1 = createCustomButton("Günüm", "ikonlar/task.png");
        JButton button2 = createCustomButton("Önemli", "ikonlar/star.png");
        JButton button3 = createCustomButton("Planlanan", "ikonlar/calender.png");

        // Butonları estetik hale getirme
        Font buttonFont = new Font("Arial", Font.BOLD, 14);
        button1.setFont(buttonFont);
        button2.setFont(buttonFont);
        button3.setFont(buttonFont);

        // Butonları sola hizalama
        button1.setAlignmentX(Component.LEFT_ALIGNMENT);
        button2.setAlignmentX(Component.LEFT_ALIGNMENT);
        button3.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Butonları yan menüye ekleme
        sideMenu.add(Box.createVerticalStrut(20)); // İlk boşluk
        sideMenu.add(button1);
        sideMenu.add(Box.createVerticalStrut(20)); // Butonlar arasına boşluk
        sideMenu.add(button2);
        sideMenu.add(Box.createVerticalStrut(20)); // Butonlar arasına boşluk
        sideMenu.add(button3);
        sideMenu.add(Box.createVerticalStrut(20)); // Butonlar arasına boşluk

        // Yatay çizgi ekleme
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sideMenu.add(separator);

        // Liste butonları için panel oluşturma
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(Color.gray);

        // JScrollPane oluşturma
        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setPreferredSize(new Dimension(200, 300)); // Yan menüye uygun boyut
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Kenar çizgisi kaldırma
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Liste Ekle butonu oluşturma ve sola alt köşeye yerleştirme
        /*JButton button4 = createCustomButton("Liste Ekle", "ikonlar/add.png");
        button4.setAlignmentX(Component.LEFT_ALIGNMENT);
        sideMenu.add(scrollPane); // JScrollPane'i yan menüye ekleme
        sideMenu.add(Box.createVerticalStrut(20)); // Çizgi ile yeni buton arasında boşluk
        sideMenu.add(button4);
        sideMenu.add(Box.createVerticalStrut(20)); // Son boşluk*/

        // Butonlar için ActionListener ekleme
        button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tm.update(); // TaskManager'daki update metodunu çağırarak güncel görevleri yükle
                showPanel("GorevYonetimi");
            }
        });


        button2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                im.update(); // ImportantManager'daki update metodunu çağırarak önemli görevleri güncelle
                showPanel("Önemli");
            }
        });


        button3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                im.update();
                showPanel("Planlanan");
            }
        });

        // Map'i oluşturma
        listPanels = new HashMap<>();

        // Liste Ekle butonu için ActionListener ekleme
        /*button4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newListName = JOptionPane.showInputDialog(jFrame, "Yeni liste adı girin:", "Yeni Liste Oluştur", JOptionPane.PLAIN_MESSAGE);
                if (newListName != null && !newListName.isEmpty()) {
                    // Yeni liste adıyla TaskManager paneli oluştur
                    TaskListManager taskListManager=new TaskListManager(db,newListName);
                    listPanels.put(newListName, taskListManager.getTaskPanel());
                    mainContent.add(taskListManager.getTaskPanel(), newListName);

                    // Buton oluşturma
                    JButton newListButton = createCustomButton(newListName, "ikonlar/list.png");
                    newListButton.setAlignmentX(Component.LEFT_ALIGNMENT);
                    listContainer.add(newListButton); // Yatay çizgiden sonra eklemek için

                    // Sağ tıklama menüsü oluşturma
                    JPopupMenu popupMenu = new JPopupMenu();
                    JMenuItem deleteItem = new JMenuItem("Sil");
                    popupMenu.add(deleteItem);

                    // Sağ tıklama olaylarını butona ekleme
                    newListButton.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                            if (SwingUtilities.isRightMouseButton(e)) {
                                popupMenu.show(e.getComponent(), e.getX(), e.getY());
                            }
                        }
                    });

                    // Sil menü öğesine tıklama olayını ekleme
                    deleteItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // Butonu ve içeriğini kaldırma
                            listContainer.remove(newListButton);
                            mainContent.remove(taskListManager.getTaskPanel());
                            listPanels.remove(newListName);

                            // Yeniden çizim yapma
                            listContainer.revalidate();
                            listContainer.repaint();
                            mainContent.revalidate();
                            mainContent.repaint();
                        }
                    });

                    // Yeni liste butonu için ActionListener ekleme
                    newListButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            showPanel(newListName);
                        }
                    });

                    // Yeniden çizim yapma
                    listContainer.revalidate();
                    listContainer.repaint();
                    showPanel(newListName);
                }
            }
        });*/

        // Ana içerik paneli oluşturma
        mainContent = new JPanel();
        mainContent.setLayout(new CardLayout());

        // Varsayılan TaskManager panelini ana içerik paneline ekleme
        tm = new TaskManager();
        mainContent.add(tm.getGorevPanel(), "GorevYonetimi");

        // Önemli panelini ana içerik paneline ekleme
        im = new ImportantManager();
        mainContent.add(im.getGorevPanel(), "Önemli");

        // Planlanan panelini ana içerik paneline ekleme
        pm = new PlannedManager();
        mainContent.add(pm.getGorevPanel(), "Planlanan");

        // Ana içeriği ana pencereye ekleme
        jFrame.add(sideMenu, BorderLayout.WEST);
        jFrame.add(mainContent, BorderLayout.CENTER);

        // Pencereyi görünür yapma
        jFrame.setVisible(true);
    }

    private JButton createCustomButton(String text, String iconPath) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE); // Yazı rengi
        button.setFocusPainted(false); // Fokus çerçevesi kaldırma
        button.setBorderPainted(false); // Kenar çizgisi kaldırma
        button.setOpaque(true); // Opaque modunu etkinleştirme
        button.setBackground(Color.gray); // Arka plan rengi
        button.setMargin(new Insets(10, 20, 10, 20)); // Buton iç kenar boşluğu

        // İkonu boyutlandırma ve butona ekleme
        ImageIcon icon = new ImageIcon(iconPath);
        Image img = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH); // 32x32 boyutunda ölçeklendirme
        button.setIcon(new ImageIcon(img));

        // Buton için fare olayları
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.LIGHT_GRAY); // Arka plan rengini değiştirme
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.gray); // Arka plan rengini eski haline getirme
            }
        });

        return button;
    }

    private void showPanel(String panelName) {
        CardLayout cl = (CardLayout) (mainContent.getLayout());
        cl.show(mainContent, panelName);
    }
}
