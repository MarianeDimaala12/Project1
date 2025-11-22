
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.ArrayList;
import javax.sound.sampled.*;    // ADDED for audio
import java.net.URL;            // ADDED for online resources
import java.io.IOException;     // ADDED for audio handling
import javax.swing.Timer;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.Properties;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.SwingConstants; // optional — already covered by javax.swing.* but explicit helps clarity


public class MemoryGame extends JFrame {

    private static final String[][] LEVEL_CONTENT = {
            {"SIT", "JPCS", "SOE", "SOA"},
            {"TDG", "TAJ", "GTS", "Halcons", "SBHTM", "SAFA"},
            {"SE", "SAS", "SCJ", "SC", "SEB", "OBRA", "DDC", "Musika Divinista"},
            {"Miss DWCC Organization", "SAO", "SAYM", "Phoenix Debate Council", "DivinisTanghalan",
                    "DWCC Saver-G", "Peer Facilitators' Club", "Mangyan Student Organization",
                    "DWCC Rotaract Club of Calapan", "Missionary Families of Christ"},
            {"DWCC Brass Band", "Association of Student Grantees", "ATEMS", "AJE", "AMMS", "CELT",
                    "PE Mentors", "AHRMS", "LIA", "SYFINEX", "JPIA", "SVD Co-Missionary"}
    };

    private static final int[] PAIRS_BY_LEVEL = {4, 6, 8, 10, 12};
    private static final int[] LIVES_BY_LEVEL =  {5, 10, 15, 20, 25};
    private static final int[] TIME_LIMIT_BY_LEVEL_SECONDS = {0, 0, 60, 90, 120};

    private static final Color WINDOW_BG = Color.WHITE;
    private static final Color HEADER_FOOTER = new Color(0x1F5C34);
    private static final Color BOARD_BG = new Color(0xE8F3EC);
    private static final Color MATCHED_COLOR = new Color(0xC9F2D0);
    private static final Color HEADER_TEXT = Color.WHITE;
    private static final Color PRIMARY_TEXT = Color.BLACK;

    private String playerName = null;
    private int level = 1;
    private int pairs;
    private int lives;
    private int timeLimitSeconds;
    private int score = 0;
    private int matchesFound = 0;
    private int consecutiveMatches = 0;
    private int attempts = 0;

    private JPanel boardPanel;
    // MAIN & PAUSE MENU PANELS
    private JPanel mainMenuPanel;
    private JPanel pauseMenuPanel;

    private JLabel infoLabel;
    private JLabel livesLabel;
    private JLabel scoreLabel;
    private JLabel timerLabel;
    private JButton restartButton;
    private javax.swing.Timer flipBackTimer;
    private javax.swing.Timer countdownTimer;
    private int timeRemaining;

    private List<CardButton> cards = new ArrayList<>();
    private CardButton firstSelected = null;
    private CardButton secondSelected = null;

    private final Map<String, String> descriptions = new HashMap<>();

    // Leaderboard storage
    private static final String LEADERBOARD_FILE = "leaderboard.txt";
    // Settings file
    private static final String SETTINGS_FILE = "settings.properties";
    private boolean showLeaderboardAfterGame = false;

    public MemoryGame() {
        super("DWCC Memory Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8,8));
        getContentPane().setBackground(WINDOW_BG);

        createDescriptions();
        createTopPanel();
        createBoardPanel();
        createMainMenuPanel();
        createPauseMenuPanel();

        loadSettings(); // load showLeaderboardAfterGame

        // show new fade-in welcome screen (Option B: replaces previous popup)
        SwingUtilities.invokeLater(() -> {
            boolean ok = showWelcomeScreenAndGetName();
            if (!ok) System.exit(0);
            level = 1;
            score = 0;
            startLevel();
        });

        setVisible(true);
    }

    private void createMainMenuPanel() {
        mainMenuPanel = new JPanel();
        mainMenuPanel.setLayout(new GridLayout(6, 1, 10, 10));
        mainMenuPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
        mainMenuPanel.setBackground(Color.WHITE);

        JButton resumeBtn = new JButton("Resume Game");
        JButton newGameBtn = new JButton("Start New Game");
        JButton leaderboardBtn = new JButton("Leaderboard");
        JButton settingsBtn = new JButton("Settings");
        JButton exitBtn = new JButton("Exit Game");
        

        styleControlButton(resumeBtn);
        styleControlButton(newGameBtn);
        styleControlButton(leaderboardBtn);
        styleControlButton(settingsBtn);
        styleControlButton(exitBtn);
      

        resumeBtn.addActionListener(e -> returnToGameScreen());
        newGameBtn.addActionListener(e -> restartLevel());
        leaderboardBtn.addActionListener(e -> showLeaderboardDialog());
        settingsBtn.addActionListener(e -> showSettingsDialog());
        exitBtn.addActionListener(e -> System.exit(0));
        

        mainMenuPanel.add(resumeBtn);
        mainMenuPanel.add(newGameBtn);
        mainMenuPanel.add(leaderboardBtn);
        mainMenuPanel.add(settingsBtn);
        mainMenuPanel.add(exitBtn);
    }

    private void createPauseMenuPanel() {
        pauseMenuPanel = new JPanel();
        pauseMenuPanel.setLayout(new GridLayout(6, 1, 10, 10));
        pauseMenuPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
        pauseMenuPanel.setBackground(Color.WHITE);

        JButton resumeBtn = new JButton("Resume Game");
        JButton newGameBtn = new JButton("Start New Game");
        JButton leaderboardBtn = new JButton("Leaderboard");
        JButton settingsBtn = new JButton("Settings");
        JButton exitBtn = new JButton("Exit Game");
        JButton resetLeaderboardBtn = new JButton("Reset Leaderboard");

        styleControlButton(resumeBtn);
        styleControlButton(newGameBtn);
        styleControlButton(leaderboardBtn);
        styleControlButton(settingsBtn);
        styleControlButton(exitBtn);
        styleControlButton(resetLeaderboardBtn);

        resumeBtn.addActionListener(e -> returnToGameScreen());
        newGameBtn.addActionListener(e -> restartLevel());
        leaderboardBtn.addActionListener(e -> showLeaderboardDialog());
        settingsBtn.addActionListener(e -> showSettingsDialog());
        exitBtn.addActionListener(e -> System.exit(0));
        resetLeaderboardBtn.addActionListener(e -> resetLeaderboard());

        pauseMenuPanel.add(resumeBtn);
        pauseMenuPanel.add(newGameBtn);
        pauseMenuPanel.add(leaderboardBtn);
        pauseMenuPanel.add(settingsBtn);
        pauseMenuPanel.add(resetLeaderboardBtn);
        pauseMenuPanel.add(exitBtn);
    }
    
    private Map<String, String> cardImages = new HashMap<>();

private void createCardImages() {
        cardImages.put("SIT", "https://drive.google.com/file/d/12C34UeTLTawd-sHXfqae6pMfmr9z_vBy/view?usp=drive_link");
        cardImages.put("JPCS", "https://drive.google.com/file/d/18_SFpsHkzdHO3JatRDLXP720WPJVQcP9/view?usp=drive_link");
        cardImages.put("SOE", "https://drive.google.com/file/d/1GB27bkTqAwk_-P_dBbOpToyzy8CjpLZ5/view?usp=drive_link");
        cardImages.put("SOA", "https://drive.google.com/file/d/1YO81BFl7rRRpybRByusQd9eMe-0HDMQj/view?usp=drive_link");
        
        cardImages.put("TDG", "https://drive.google.com/file/d/1LAvuSvOuuH3ZP6BBdvleXctl4AkX4861/view?usp=sharing");
        cardImages.put("TAJ", "https://drive.google.com/file/d/1jJdlmt9StJ82WBpTO8Qb6DOUi_te8wxV/view?usp=sharing");
        cardImages.put("GTS", "https://drive.google.com/file/d/1FSYJ5iuubUCLqCbAa1kWi0xX5g-9dg7O/view?usp=sharing");
        cardImages.put("Halcons", "https://drive.google.com/file/d/171jgtLzTwPNCKqBIiYlyTY0za8Sz1jDR/view?usp=sharing");
        cardImages.put("SBHTM", "https://drive.google.com/file/d/1jny3rfbZK3W-QHp4kTqjT-uPKWhwGntW/view?usp=sharing");
        cardImages.put("SAFA", "https://drive.google.com/file/d/1wGZLe_QREplPHsMcWTa-1MmYstrFILz1/view?usp=sharing");
        
        cardImages.put("SE", "https://drive.google.com/file/d/1gTFSCoijoqqI-4x0N8SfrbGfWd9rgZGQ/view?usp=sharing");
        cardImages.put("SAS", "https://drive.google.com/file/d/1P7HcMHRTUzYSMNjjtKDLr1zBPPOAqPkb/view?usp=sharing");
        cardImages.put("SCJ", "https://drive.google.com/file/d/19FwFD0WlGmPM7NJd3ZvTUCN7_BxMte0u/view?usp=sharing");
        cardImages.put("SC", "https://drive.google.com/file/d/1yHz587hPktqTqC_N1XahPEpsxHrDt8jk/view?usp=sharing");
        cardImages.put("SEB", "https://drive.google.com/file/d/1EjHCX1lmurc2GC4_0R8lolXqjfmI2Oqy/view?usp=sharing");
        cardImages.put("OBRA", "https://drive.google.com/file/d/1VyH4PIExUwahR7K5DdoX23T8DTSvvo8F/view?usp=sharing");
        cardImages.put("DDC", "https://drive.google.com/file/d/1M5u7DdOzRgEIY20c-_hTeWrvPFZDGBgr/view?usp=sharing");
        cardImages.put("Musika Divinista", "https://drive.google.com/file/d/1UpL1mbF00YXY1VQld-91e11x0I6Kvrsa/view?usp=sharing");
        
        cardImages.put("Ms. DWCC Organization", "https://drive.google.com/file/d/1-mSpQKz_9SohhWfqNFKRHz191Tns-msq/view?usp=sharing");
        cardImages.put("SAO", "https://drive.google.com/file/d/1VenUfytZ63XRvbG_O16TmFFxDtfLxjf7/view?usp=sharing");
        cardImages.put("SAYM", "https://drive.google.com/file/d/1kQKP8UDecJkLMZeipqH_OHwFcaykSIK6/view?usp=sharing");
        cardImages.put("Phoenix Debate Club", "https://drive.google.com/file/d/1bEv23kno8Nwjw65tryvMFlyxGeFMBhRS/view?usp=sharing");
        cardImages.put("DivinisTanghalan", "https://drive.google.com/file/d/1ofwnt0VErxZB5by6eQSsYgq45mm9DCxl/view?usp=sharing");
        cardImages.put("DWCC Saver-G", "https://drive.google.com/file/d/12AU3kQ__vuUQ_LGx3GWAQ03dLYBnozKy/view?usp=sharing");
        cardImages.put("Peer Facilitators' Club", "https://drive.google.com/file/d/1WviXP53jSVkKeXozRLtfz-UicbTUIhfR/view?usp=sharing");
        cardImages.put("Mangyan Student Organization", "https://drive.google.com/file/d/1MH6MZ_N7Q1QcyuE0VGggCrKmiBxZ6UdM/view?usp=sharing");
        cardImages.put("DWCC Rotaract Club of Calapan", "https://drive.google.com/file/d/1ZNZCryfxmCnQyneceb89_p56bSBGR_La/view?usp=sharing");
        cardImages.put("UAPSA", "https://drive.google.com/file/d/1kGv6uWMiOf3DGF20pQdva4IW6z4nXwe-/view?usp=sharing");
        
        cardImages.put("PICE", "https://drive.google.com/file/d/1T5Tq41TAq8X0IWLQlyCiFh7zqL4JmuSG/view?usp=sharing"); 
        cardImages.put("Association of Student Grantees", "https://drive.google.com/file/d/1RdYCDdWY1l3G-3qoIQq_U7yUiqOvVlhP/view?usp=sharing");
        cardImages.put("ATEMS", "https://drive.google.com/file/d/1J9VAHyz8FZSGVe5Hz4CNAzzmksG1oT1k/view?usp=sharing");
        cardImages.put("AJE", "https://drive.google.com/file/d/12C34UeTLTawd-sHXfqae6pMfmr9z_vBy/view?usp=sharing");
        cardImages.put("AMMS", "https://drive.google.com/file/d/1d_ValNb2CtLool9eAKdNjSZtS6SNPVqM/view?usp=sharing");
        cardImages.put("CELTS", "https://drive.google.com/file/d/1_taJW5xaMgGQTGJ7q22NB5HgkuN5S5Ce/view?usp=sharing");
        cardImages.put("AJPS", "https://drive.google.com/file/d/1UvpRTmTUqrnbHMJ-VLzV6One2j0E_M6M/view?usp=sharing");
        cardImages.put("JIECEP", "https://drive.google.com/file/d/1XllnhA_VM3AnF_bT600Fu0KAK0AYVXTu/view?usp=sharing");
        cardImages.put("LIA", "https://drive.google.com/file/d/1758bB-gzrBrPiwjE76fCYRVUw47hpakt/view?usp=sharing");
        cardImages.put("SYFINEX", "https://drive.google.com/file/d/1kX6pCng0zT7FGBUgwzFTP-Likif0L02g/view?usp=sharing");
        cardImages.put("JPIA", "https://drive.google.com/file/d/1-hL1cKDOklAqa8el7AaEbBxw6QyCVBCv/view?usp=sharing");
        cardImages.put("SVD Co-Missionary", "https://drive.google.com/file/d/1n09R2pDyNvQCDZVS6ydxakOgdSPG3_U4/view?usp=sharing");

        // Add the rest similarly...
    }


    private void createDescriptions() {
        descriptions.put("SIT", "School of Information Technology — The academic unit that trains future IT professionals.");
        descriptions.put("JPCS", "Junior Philippine Computer Society — An organization for IT student leadership and skills.");
        descriptions.put("SOE", "School of Education — Prepares future teachers and educators.");
        descriptions.put("SOA", "School of Accountancy — Academic unit for future Certified Public Accountants.");

        descriptions.put("TDG", "The DWCC Gazette — The official campus publication of DWCC.");
        descriptions.put("TAJ", "The Accountants Journal — Research publication of the Accountancy department.");
        descriptions.put("GTS", "Guild of Tourism Students — Organization representing tourism students.");
        descriptions.put("Halcons", "The DWCC Halcons — Official athletic team of DWCC.");
        descriptions.put("SBHTM", "School of Business Hospitality and Tourism Management — Academic unit in business and tourism.");
        descriptions.put("SAFA", "School of Architecture and Fine Arts — Trains students in creative and architectural design.");

        descriptions.put("SE", "School of Engineering — College for aspiring engineers.");
        descriptions.put("SAS", "School of Arts and Sciences — Handles general education and liberal arts programs.");
        descriptions.put("SCJ", "School of Criminal Justice — Trains students in criminology and law enforcement.");
        descriptions.put("SC", "Student Council — Highest governing body of the student community.");
        descriptions.put("SEB", "Student Electoral Board — Oversees campus-wide elections.");
        descriptions.put("OBRA", "Obra Divinista — Official arts and creative production guild.");
        descriptions.put("DDC", "DWCC Dance Company — Performing arts and dance troupe.");
        descriptions.put("Musika Divinista", "Musika Divinista — Official music choir and ensemble of DWCC.");

        descriptions.put("Miss DWCC Organization", "The official organization behind the Miss DWCC pageant.");
        descriptions.put("SAO", "Student Affairs Office — Handles all student services and concerns.");
        descriptions.put("SAYM", "Saint Arnold Youth Ministry — Religious formation group for students.");
        descriptions.put("Phoenix Debate Council", "Phoenix Debate Council — Competitive public speaking and debate group.");
        descriptions.put("DivinisTanghalan", "DivinisTanghalan — Theater performance and stage acting guild.");
        descriptions.put("DWCC Saver-G", "Campus group focused on environmental preservation and sustainability.");
        descriptions.put("Peer Facilitators' Club", "Peer support and counseling advocacy group.");
        descriptions.put("Mangyan Student Organization", "Organization of DWCC students belonging to the Mangyan tribes.");
        descriptions.put("DWCC Rotaract Club of Calapan", "Community service club affiliated with Rotary International.");
        descriptions.put("UAPSA", "United Architects of the Philippines Students Auxiliary - Student group for future architects, promoting design skills and professional growth.");

        descriptions.put("PICE", "Philippine Institute of Civil Engineers - Student organization for aspiring civil engineers, promoting skills and professional development.");
        descriptions.put("Association of Student Grantees", "Organization for scholarship and grant beneficiaries.");
        descriptions.put("ATEMS", "Alliance for Transformative Education through Mathematics and Science — STEM academic organization.");
        descriptions.put("AJE", "Association of Junior Executives — Business administration student group.");
        descriptions.put("AMMS", "Association of Marketing Management Students — Business and marketing events group.");
        descriptions.put("CELTS", "Childhood Education and Language Teaching Students— Organization for future preschool and language educators.");
        descriptions.put("AJPS", "Alliance of Junior Political Scientists — Student group for future political scientists, fostering knowledge and leadership skills.");
        descriptions.put("JIECEP", "Junior Institute of Electronics Engineers of the Philippines — Student organization for aspiring electronics engineers, promoting skills and professional growth.");
        descriptions.put("LIA", "Legion of Imaginative Artists — Intellectual and academic literary circle.");
        descriptions.put("SYFINEX", "SYFINEX — Financial literacy and investment organization.");
        descriptions.put("JPIA", "Junior Philippine Institute of Accountants — National accounting student organization.");
        descriptions.put("SVD Co-Missionary", "SVD Co-Missionary — Religious volunteer and mission assistance group.");
    }

    private boolean showWelcomeScreenAndGetName() {
        final JDialog dialog = new JDialog((Frame) null, "Welcome to DWCC Memory Game", true);
        dialog.setUndecorated(true);
        dialog.getContentPane().setBackground(WINDOW_BG);

        JPanel root = new JPanel(new BorderLayout(12,12));
        root.setBorder(new EmptyBorder(18,18,18,18));
        root.setBackground(WINDOW_BG);

        String logoUrl = "https://upload.wikimedia.org/wikipedia/commons/8/86/Divine_Word_College_of_Calapan_seal.png";
        JLabel logoLabel = new JLabel("", SwingConstants.CENTER);
        try {
            ImageIcon icon = new ImageIcon(new URL(logoUrl));
            Image img = icon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(img));
        } catch (Exception ex) {
            logoLabel.setText("DWCC");
            logoLabel.setFont(logoLabel.getFont().deriveFont(Font.BOLD, 28f));
            System.out.println("Logo load failed: " + ex.getMessage());
        }
        root.add(logoLabel, BorderLayout.NORTH);

        JLabel title = new JLabel("<html><center>⭐ DWCC MEMORY QUEST ⭐<br><i>Test your memory. Discover DWCC. Conquer all levels.</i></center></html>", SwingConstants.CENTER);
        title.setForeground(HEADER_FOOTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBackground(WINDOW_BG);
        root.add(title, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8,8));
        bottom.setBackground(WINDOW_BG);

        JTextArea instructions = new JTextArea(
                "Match pairs representing DWCC schools, orgs, and clubs.\n" +
                        "There are 5 levels. Lives and timers vary per level.\n" +
                        "Enter your name to begin your quest!"
        );
        instructions.setEditable(false);
        instructions.setOpaque(false);
        instructions.setFocusable(false);
        instructions.setFont(new Font("SansSerif", Font.PLAIN, 13));
        instructions.setForeground(PRIMARY_TEXT);
        bottom.add(instructions, BorderLayout.NORTH);

        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        inputRow.setBackground(WINDOW_BG);
        JLabel nameLbl = new JLabel("Please enter your username:");
        JTextField nameField = new JTextField(20);
        inputRow.add(nameLbl);
        inputRow.add(nameField);
        bottom.add(inputRow, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        btnRow.setBackground(WINDOW_BG);
        JButton startBtn = new JButton("Start Adventure");
        nameField.addActionListener(e -> startBtn.doClick());

        JButton leaderboardBtn = new JButton("View Leaderboard");
        JButton exitBtn = new JButton("Exit");

        styleControlButton(startBtn);
        styleControlButton(leaderboardBtn);
        styleControlButton(exitBtn);

        leaderboardBtn.addActionListener(e -> showLeaderboardDialog());

        btnRow.add(startBtn);
        btnRow.add(leaderboardBtn);
        btnRow.add(exitBtn);

        bottom.add(btnRow, BorderLayout.SOUTH);


        root.add(bottom, BorderLayout.SOUTH);

        dialog.getContentPane().add(root);
        dialog.pack();
        dialog.setLocationRelativeTo(null);

        String soundUrl = "https://www2.cs.uic.edu/~i101/SoundFiles/BabyElephantWalk60.wav";
        final Clip[] playingClip = new Clip[1];
        try {
            URL su = new URL(soundUrl);
            playingClip[0] = playClipFromURL(su);
        } catch (Exception ex) {
            System.out.println("Could not play intro sound: " + ex.getMessage());
        }

        try {
            dialog.setOpacity(0f);
        } catch (UnsupportedOperationException uex) { }

        Timer fadeTimer = new Timer(30, null);
        fadeTimer.addActionListener(new ActionListener() {
            float op = 0f;
            @Override
            public void actionPerformed(ActionEvent e) {
                op += 0.03f;
                if (op > 1f) op = 1f;
                try {
                    dialog.setOpacity(op);
                } catch (Exception ex) { }
                if (op >= 1f) {
                    fadeTimer.stop();
                }
            }
        });
        fadeTimer.start();

        final boolean[] started = {false};
        startBtn.addActionListener(ev -> {
            String n = nameField.getText();
            if (n != null) n = n.trim();
            if (n == null || n.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter your name to start.", "Name required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            playerName = n;
            started[0] = true;
            if (playingClip[0] != null && playingClip[0].isRunning()) {
                playingClip[0].stop();
                playingClip[0].close();
            }
            dialog.dispose();
        });

        exitBtn.addActionListener(ev -> {
            int conf = JOptionPane.showConfirmDialog(dialog, "Exit game?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                if (playingClip[0] != null && playingClip[0].isRunning()) {
                    playingClip[0].stop();
                    playingClip[0].close();
                }
                dialog.dispose();
            }
        });

        dialog.setVisible(true);

        return started[0];
    }

    private Clip playClipFromURL(URL url) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
            return clip;
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException ex) {
            System.out.println("Audio play error: " + ex.getMessage());
            return null;
        }
    }

    private void createTopPanel() {
        JPanel top = new JPanel(new BorderLayout(6,6));
        top.setBorder(new EmptyBorder(8,8,0,8));
        top.setBackground(HEADER_FOOTER);

        infoLabel = new JLabel("DWCC Memory Game");
        infoLabel.setFont(infoLabel.getFont().deriveFont(Font.BOLD, 18f));
        infoLabel.setForeground(HEADER_TEXT);

        JPanel status = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        status.setOpaque(true);
        status.setBackground(HEADER_FOOTER);

        livesLabel = new JLabel();
        livesLabel.setForeground(HEADER_TEXT);
        scoreLabel = new JLabel();
        scoreLabel.setForeground(HEADER_TEXT);
        timerLabel = new JLabel();
        timerLabel.setForeground(HEADER_TEXT);

        status.add(livesLabel);
        status.add(scoreLabel);
        status.add(timerLabel);

        top.add(infoLabel, BorderLayout.WEST);
        top.add(status, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottom.setBackground(HEADER_FOOTER);

        restartButton = new JButton("Restart Level");
        styleControlButton(restartButton);
        restartButton.addActionListener(e -> {
            int conf = JOptionPane.showConfirmDialog(this, "Restart current level?", "Restart", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) restartLevel();
        });
        bottom.add(restartButton);

        add(bottom, BorderLayout.SOUTH);

        JButton menuButton = new JButton("Main Menu");
        styleControlButton(menuButton);
        menuButton.addActionListener(e -> showMainMenu());

        JButton pauseButton = new JButton("Pause");
        styleControlButton(pauseButton);
        pauseButton.addActionListener(e -> showPauseMenu());

        bottom.add(menuButton);
        bottom.add(pauseButton);

    }

    private void showMainMenu() {
        setContentPane(mainMenuPanel);
        revalidate();
        repaint();
    }

    private void showPauseMenu() {
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
        setContentPane(pauseMenuPanel);
        revalidate();
        repaint();
    }

    private void returnToGameScreen() {
        if (countdownTimer != null && timeLimitSeconds > 0) {
            countdownTimer.start();
        }
        // safer way to restore the original content pane: we keep board panel in CENTER; just set a root content
        Container content = getContentPane();
        setContentPane(new JPanel(new BorderLayout()));
        getContentPane().setBackground(WINDOW_BG);
        getContentPane().add(boardPanel, BorderLayout.CENTER);
        getContentPane().add(infoLabel.getParent().getParent(), BorderLayout.NORTH); // may be brittle; but we set top in constructor
        revalidate();
        repaint();
    }

    private void styleControlButton(JButton btn) {
        btn.setBackground(Color.WHITE);
        btn.setForeground(HEADER_FOOTER);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(HEADER_FOOTER, 2));
    }

    private void createBoardPanel() {
        boardPanel = new JPanel();
        boardPanel.setBorder(new EmptyBorder(10,10,10,10));
        boardPanel.setBackground(BOARD_BG);
        add(boardPanel, BorderLayout.CENTER);
    }

    private void startLevel() {
        pairs = PAIRS_BY_LEVEL[level - 1];
        lives = LIVES_BY_LEVEL[level - 1];
        timeLimitSeconds = TIME_LIMIT_BY_LEVEL_SECONDS[level - 1];
        matchesFound = 0;
        attempts = 0;
        consecutiveMatches = 0;
        timeRemaining = timeLimitSeconds;
        updateInfoLabels();

        setupCardsGrid(pairs);
        startCountdownIfNeeded();
    }

    private void updateInfoLabels() {
        livesLabel.setText("Lives: " + lives);
        scoreLabel.setText("Score: " + score);
        timerLabel.setText(timeLimitSeconds > 0 ? "Time: " + formatTime(timeRemaining) : "Time: --");
        infoLabel.setText("Player: " + (playerName == null ? "?" : playerName) +
                "  |  Level " + level + " — Pairs: " + pairs + "  Attempts: " + attempts +
                "  Matches: " + matchesFound);
    }

    private void setupCardsGrid(int pairs) {
        cards.clear();
        boardPanel.removeAll();

        String[] levelCards = LEVEL_CONTENT[level - 1];
        List<String> pairContents = new ArrayList<>();
        for (String s : levelCards) {
            pairContents.add(s);
            pairContents.add(s);
        }
        Collections.shuffle(pairContents);

        int total = pairContents.size();
        int cols = (int)Math.ceil(Math.sqrt(total));
        int rows = (int)Math.ceil((double)total / cols);

        boardPanel.setLayout(new GridLayout(rows, cols, 8, 8));

        for (String content : pairContents) {
        CardButton b = new CardButton(content); // will now display image if mapped
        b.addActionListener(e -> onCardClicked(b));
        cards.add(b);
        boardPanel.add(b);
}


        boardPanel.revalidate();
        boardPanel.repaint();
        packIfNeeded();
    }

    private void packIfNeeded() {
        setSize(Math.max(800, getWidth()), Math.max(600, getHeight()));
        revalidate();
    }

    private void onCardClicked(CardButton b) {
        if (flipBackTimer != null && flipBackTimer.isRunning()) return;
        if (b.isMatched() || b.isFaceUp()) return;

        b.showFace();

        if (firstSelected == null) {
            firstSelected = b;
            return;
        } else if (firstSelected == b) {
            return;
        } else {
            secondSelected = b;
            attempts++;
            infoLabel.setText("Player: " + (playerName == null ? "?" : playerName) +
                    "  |  Level " + level + " — Attempts: " + attempts);

            if (firstSelected.getContent().equals(secondSelected.getContent())) {
                firstSelected.setMatched(true);
                secondSelected.setMatched(true);
                matchesFound++;
                consecutiveMatches++;

                int basePoints = 100;
                int timeBonus = (timeLimitSeconds > 0) ? Math.max(0, timeRemaining) : 0;
                int streakBonus = consecutiveMatches > 1 ? (consecutiveMatches - 1) * 50 : 0;
                score += basePoints + timeBonus + streakBonus;
                Toolkit.getDefaultToolkit().beep();

                boolean timerWasRunning = false;
                if (countdownTimer != null && countdownTimer.isRunning()) {
                    countdownTimer.stop();
                    timerWasRunning = true;
                }

                String key = firstSelected.getContent();
                String desc = descriptions.getOrDefault(key, "Part of the DWCC community.");
                showInfoDialog("Match found!", "<html><b>" + key + "</b><br/><i>" + desc + "</i></html>");

                if (timerWasRunning && timeRemaining > 0) {
                    countdownTimer.start();
                }

                firstSelected = null;
                secondSelected = null;
                updateInfoLabels();

                if (matchesFound == pairs) {
                    onLevelComplete();
                }

            } else {

                consecutiveMatches = 0;
                lives--;
                Toolkit.getDefaultToolkit().beep();
                try { Thread.sleep(80); } catch (Exception ex) { Thread.currentThread().interrupt(); }
                Toolkit.getDefaultToolkit().beep();

                flipBackTimer = new javax.swing.Timer(800, e -> {
                    if (firstSelected != null && !firstSelected.isMatched()) firstSelected.hideFace();
                    if (secondSelected != null && !secondSelected.isMatched()) secondSelected.hideFace();
                    firstSelected = null;
                    secondSelected = null;
                    flipBackTimer.stop();
                });
                flipBackTimer.setRepeats(false);
                flipBackTimer.start();

                updateInfoLabels();
                if (lives <= 0) {
                    onGameEndLose();
                }
            }
        }
    }

    private void onLevelComplete() {
        if (countdownTimer != null) countdownTimer.stop();
        int option = JOptionPane.showConfirmDialog(this,
                "Level " + level + " complete!\nScore: " + score + "\nProceed to next level?",
                "Level Complete",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            goToNextLevel();
        } else {
            String[] opts = {"Restart Level", "Exit Game"};
            int choice = JOptionPane.showOptionDialog(this, "What would you like to do?",
                    "Level Complete", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);

            if (choice == 0) restartLevel();
            else {
                int conf = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to exit?", "Exit", JOptionPane.YES_NO_OPTION);
                if (conf == JOptionPane.YES_OPTION) System.exit(0);
            }
        }
    }

    private void onGameEndLose() {
        if (countdownTimer != null) countdownTimer.stop();

        String player = (playerName == null || playerName.trim().isEmpty()) ? "Player" : playerName;
        String msg = String.format("%s, you LOST at Level %d!\nFinal Score: %d", player, level, score);
        saveScoreToLeaderboard();

        // show leaderboard automatically if setting enabled
        if (showLeaderboardAfterGame) {
            showLeaderboardDialog();
        }

        Object[] options = {"Play Again (Level 1)", "Exit"};
        int choice = JOptionPane.showOptionDialog(this, msg, "Game Over",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            playerName = null;
            boolean ok = showWelcomeScreenAndGetName();
            if (ok) {
                level = 1;
                score = 0;
                startLevel();
            } else {
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }

    private void onGameEndWin() {
        if (countdownTimer != null) countdownTimer.stop();

        String player = (playerName == null || playerName.trim().isEmpty()) ? "Player" : playerName;
        String msg = String.format("%s, you WIN! You finished Level %d!\nFinal Score: %d", player, level, score);

        saveScoreToLeaderboard();

        // Show leaderboard automatically if option is enabled
        if (showLeaderboardAfterGame) {
            showLeaderboardDialog();
        }


        Object[] options = {"Play Again (Level 1)", "Exit"};
        int choice = JOptionPane.showOptionDialog(this, msg, "You Win!",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            playerName = null;
            boolean ok = showWelcomeScreenAndGetName();
            if (ok) {
                level = 1;
                score = 0;
                startLevel();
            } else {
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }

    private void goToNextLevel() {
        if (level < PAIRS_BY_LEVEL.length) {
            level++;
            startLevel();
        } else {
            onGameEndWin();
        }
    }

    private void restartLevel() {
        if (countdownTimer != null) countdownTimer.stop();
        matchesFound = 0;
        attempts = 0;
        consecutiveMatches = 0;
        timeRemaining = timeLimitSeconds;
        startLevel();
    }

    private void startCountdownIfNeeded() {
        if (countdownTimer != null) countdownTimer.stop();
        if (timeLimitSeconds > 0) {
            timeRemaining = timeLimitSeconds;
            timerLabel.setText("Time: " + formatTime(timeRemaining));
            countdownTimer = new javax.swing.Timer(1000, e -> {
                timeRemaining--;
                timerLabel.setText("Time: " + formatTime(timeRemaining));
                if (timeRemaining <= 0) {
                    countdownTimer.stop();
                    lives = 0;
                    updateInfoLabels();
                    onGameEndLose();
                }
            });
            countdownTimer.start();
        } else {
            timerLabel.setText("Time: --");
        }
    }

    private String formatTime(int secs) {
        int mm = secs / 60;
        int ss = secs % 60;
        return String.format("%02d:%02d", mm, ss);
    }

    private void showInfoDialog(String title, String message) {
        final JOptionPane optionPane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION);
        final JDialog dialog = optionPane.createDialog(this, title);

        JButton okButton = null;
        for (Component c : optionPane.getComponents()) {
            if (c instanceof JButton) {
                okButton = (JButton) c;
                break;
            }
        }
        if (okButton != null) {
            dialog.getRootPane().setDefaultButton(okButton);
        }

        dialog.setVisible(true);
    }

    private class CardButton extends JButton {
    private String content;
    private boolean faceUp = false;
    private boolean matched = false;
    private ImageIcon faceIcon;

    public CardButton(String content) {
        super(" ");
        this.content = content;
        setFont(getFont().deriveFont(Font.BOLD, 14f));
        setFocusPainted(false);
        setForeground(PRIMARY_TEXT);
        setBorder(new LineBorder(new Color(0xBDBDBD)));
        setBackground(BOARD_BG);

        // Load image from Google Drive
        String url = cardImages.get(content);
    if (url != null) {
        try {
            ImageIcon icon = new ImageIcon(new URL(url));
            Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            faceIcon = new ImageIcon(img);
        } catch (Exception ex) {
            System.out.println("Failed to load image for " + content + ": " + ex.getMessage());
        }
}

    }

    public String getContent() { return content; }
    public boolean isFaceUp() { return faceUp; }
    public boolean isMatched() { return matched; }

    public void setMatched(boolean m) {
        matched = m;
        setEnabled(!m);
    }

    public void showFace() {
    faceUp = true;
    if (faceIcon != null) setIcon(faceIcon);
    else setText("<html><center>" + content + "</center></html>");
}


    public void hideFace() {
        faceUp = false;
        setIcon(null);
        setText(" ");
    }
}


    private void saveScoreToLeaderboard() {
    try {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        int timeUsed = timeLimitSeconds > 0
                ? (timeLimitSeconds - timeRemaining)
                : 0;

        String entry = playerName + "|" +
                       score + "|" +
                       level + "|" +
                       lives + "|" +
                       matchesFound + "|" +
                       timeUsed + "|" +
                       date + System.lineSeparator();

        Files.write(
                Paths.get(LEADERBOARD_FILE),
                entry.getBytes(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );

    } catch (IOException ex) {
        System.out.println("Error saving leaderboard: " + ex.getMessage());
    }
}


    private void resetLeaderboard() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to CLEAR the leaderboard?",
                "Confirm Reset",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Files.write(Paths.get(LEADERBOARD_FILE), "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
                JOptionPane.showMessageDialog(this, "Leaderboard has been reset.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error resetting leaderboard.");
            }
        }
    }

    private List<String> loadLeaderboardRaw() {
        try {
            if (!Files.exists(Paths.get(LEADERBOARD_FILE))) return new ArrayList<>();
            return Files.readAllLines(Paths.get(LEADERBOARD_FILE));
        } catch (IOException ex) {
            return new ArrayList<>();
        }
    }
    

    /**
     * New, improved leaderboard dialog with JTable, sortable columns, and automatic ranking.
     */
    private void showLeaderboardDialog() {
    java.util.List<String[]> rows = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader(LEADERBOARD_FILE))) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\\|");
            if (parts.length == 7) {
                rows.add(parts);
            }
        }
    } catch (Exception ex) {
        System.out.println("Leaderboard read error: " + ex.getMessage());
    }

    rows.sort((a, b) -> Integer.compare(Integer.parseInt(b[1]), Integer.parseInt(a[1])));

    String[] columnNames = {
            "Rank", "Name", "Score", "Level Reached", "Lives Left",
            "Matches Made", "Time Consumed", "Date"
    };

    Object[][] data = new Object[rows.size()][8];
    int rank = 1;

    for (int i = 0; i < rows.size(); i++) {
        String[] r = rows.get(i);
        data[i][0] = rank++;
        data[i][1] = r[0];
        data[i][2] = r[1];
        data[i][3] = r[2];
        data[i][4] = r[3];
        data[i][5] = r[4];
        data[i][6] = r[5] + " sec";
        data[i][7] = r[6];
    }

    JTable table = new JTable(data, columnNames) {
        @Override
        public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);
            if (!isRowSelected(row)) {
                switch (row) {
                    case 0: c.setBackground(new Color(255, 215, 0)); break; // Gold
                    case 1: c.setBackground(new Color(192, 192, 192)); break; // Silver
                    case 2: c.setBackground(new Color(205, 127, 50)); break; // Bronze
                    default: c.setBackground(Color.WHITE); break;
                }
            }
            return c;
        }
    };
    
    table.setEnabled(false);
    table.setRowHeight(25);
    DefaultTableCellRenderer center = new DefaultTableCellRenderer();
    center.setHorizontalAlignment(SwingConstants.CENTER);
    for (int i = 0; i < table.getColumnCount(); i++) {
        table.getColumnModel().getColumn(i).setCellRenderer(center);
    }

    JScrollPane scroll = new JScrollPane(table);
    scroll.setPreferredSize(new Dimension(800, 300));

    JOptionPane.showMessageDialog(this, scroll, "Leaderboard", JOptionPane.PLAIN_MESSAGE);
}



    private static class LeaderboardEntry {
        String name;
        int score;
        String timestamp;
        LeaderboardEntry(String n, int s, String t) { name = n; score = s; timestamp = t; }
    }

    private void loadSettings() {
        Properties p = new Properties();
        try {
            File f = new File(SETTINGS_FILE);
            if (f.exists()) {
                try (FileReader fr = new FileReader(f)) {
                    p.load(fr);
                }
            }
            String val = p.getProperty("showLeaderboardAfterGame", "false");
            showLeaderboardAfterGame = Boolean.parseBoolean(val);
        } catch (Exception ex) {
            System.out.println("Failed to load settings: " + ex.getMessage());
            showLeaderboardAfterGame = false;
        }
    }

    private void saveSettings() {
        Properties p = new Properties();
        p.setProperty("showLeaderboardAfterGame", Boolean.toString(showLeaderboardAfterGame));
        try (FileWriter fw = new FileWriter(SETTINGS_FILE)) {
            p.store(fw, "MemoryGame settings");
        } catch (IOException ex) {
            System.out.println("Failed to save settings: " + ex.getMessage());
        }
    }

    private void showSettingsDialog() {
        JCheckBox autoShow = new JCheckBox("Show leaderboard automatically after each finished game", showLeaderboardAfterGame);
        JPanel panel = new JPanel(new BorderLayout(6,6));
        panel.setBorder(new EmptyBorder(8,8,8,8));
        panel.add(new JLabel("<html><b>Settings</b></html>"), BorderLayout.NORTH);
        panel.add(autoShow, BorderLayout.CENTER);

        int res = JOptionPane.showConfirmDialog(this, panel, "Settings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            showLeaderboardAfterGame = autoShow.isSelected();
            saveSettings();
            JOptionPane.showMessageDialog(this, "Settings saved.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MemoryGame());
    }
}
