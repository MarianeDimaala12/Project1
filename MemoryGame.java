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

        mainMenuPanel.add(resumeBtn);
        mainMenuPanel.add(newGameBtn);
        mainMenuPanel.add(leaderboardBtn);
        mainMenuPanel.add(settingsBtn);
        mainMenuPanel.add(resetLeaderboardBtn);
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
        descriptions.put("Missionary Families of Christ", "Faith-based evangelization and service organization.");

        descriptions.put("DWCC Brass Band", "Official ceremonial band of DWCC for music and parades.");
        descriptions.put("Association of Student Grantees", "Organization for scholarship and grant beneficiaries.");
        descriptions.put("ATEMS", "Alliance for Transformative Education through Mathematics and Science — STEM academic organization.");
        descriptions.put("AJE", "Association of Junior Executives — Business administration student group.");
        descriptions.put("AMMS", "Association of Marketing Management Students — Business and marketing events group.");
        descriptions.put("CELT", "Childhood Education and Language Teaching — Organization for future preschool and language educators.");
        descriptions.put("PE Mentors", "Physical Education Mentors — Student group for PE leadership and sports.");
        descriptions.put("AHRMS", "Association of Hotel and Restaurant Management Students — Culinary and hospitality organization.");
        descriptions.put("LIA", "LIA — Intellectual and academic literary circle.");
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
        JButton exitBtn = new JButton("Exit");
        styleControlButton(startBtn);
        styleControlButton(exitBtn);
        btnRow.add(startBtn);
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
            CardButton b = new CardButton(content);
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

        // show leaderboard automatically if setting enabled
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

        public CardButton(String content) {
            super(" ");
            this.content = content;
            setFont(getFont().deriveFont(Font.BOLD, 14f));
            setFocusPainted(false);
            setForeground(PRIMARY_TEXT);
            setBorder(new LineBorder(new Color(0xBDBDBD)));
            setBackground(BOARD_BG);
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
            setText("<html><center>" + content + "</center></html>");
        }

        public void hideFace() {
            faceUp = false;
            setText(" ");
        }
    }

    private void saveScoreToLeaderboard() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String entry = playerName + " - " + score + " - " + timestamp + System.lineSeparator();
            Files.write(Paths.get(LEADERBOARD_FILE), entry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
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
        List<String> lines = loadLeaderboardRaw();
        List<LeaderboardEntry> entries = new ArrayList<>();

        for (String line : lines) {
            // expected format: "Name - score - timestamp"
            String[] parts = line.split("\\s*-\\s*");
            if (parts.length >= 3) {
                String name = parts[0].trim();
                String scoreStr = parts[1].trim();
                String timestamp = parts[2].trim();
                // If timestamp contains extra hyphens (unlikely), rebuild it
                if (parts.length > 3) {
                    StringJoiner sj = new StringJoiner(" - ");
                    for (int i = 2; i < parts.length; i++) sj.add(parts[i].trim());
                    timestamp = sj.toString();
                }
                int s = 0;
                try {
                    s = Integer.parseInt(scoreStr);
                } catch (NumberFormatException nfe) {
                    // skip malformed score entries gracefully
                    continue;
                }
                entries.add(new LeaderboardEntry(name, s, timestamp));
            }
        }

        if (entries.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No leaderboard data yet.");
            return;
        }

        // Sort by score desc by default
        entries.sort((a, b) -> Integer.compare(b.score, a.score));

        // Build table model with automatic rank numbers
        String[] columns = {"Rank", "Player", "Score", "Timestamp"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 2) return Integer.class;
                return String.class;
            }
        };

        int rank = 1;
        for (LeaderboardEntry e : entries) {
            model.addRow(new Object[]{rank++, e.name, e.score, e.timestamp});
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(24);

        // Enable sorting with proper comparator for integers (score / rank)
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        sorter.setComparator(0, Comparator.comparingInt(o -> (Integer)o));
        sorter.setComparator(2, Comparator.comparingInt(o -> (Integer)o));
        table.setRowSorter(sorter);

        // Visual tweaks
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(2).setMaxWidth(100);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(600, 350));

        // Dialog with buttons
        JDialog dlg = new JDialog(this, "Leaderboard — Top Players", true);
        dlg.setLayout(new BorderLayout(8,8));
        dlg.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("Close");
        JButton exportBtn = new JButton("Export CSV");
        JButton clearBtn = new JButton("Clear Leaderboard");
        bottom.add(exportBtn);
        bottom.add(clearBtn);
        bottom.add(closeBtn);
        dlg.add(bottom, BorderLayout.SOUTH);

        closeBtn.addActionListener(ev -> dlg.dispose());
        exportBtn.addActionListener(ev -> {
            try {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Export leaderboard to CSV");
                if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File sel = fc.getSelectedFile();
                    if (!sel.getName().toLowerCase().endsWith(".csv")) sel = new File(sel.getAbsolutePath() + ".csv");
                    try (FileWriter fw = new FileWriter(sel)) {
                        fw.write(String.join(",", columns) + System.lineSeparator());
                        for (int r = 0; r < model.getRowCount(); r++) {
                            Object rankObj = model.getValueAt(r, 0);
                            Object playerObj = model.getValueAt(r, 1);
                            Object scoreObj = model.getValueAt(r, 2);
                            Object tsObj = model.getValueAt(r, 3);
                            fw.write(String.format("%s,%s,%s,%s%n",
                                    rankObj.toString(), playerObj.toString(), scoreObj.toString(), tsObj.toString()));
                        }
                    }
                    JOptionPane.showMessageDialog(this, "Exported successfully.");
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage());
            }
        });
        clearBtn.addActionListener(ev -> {
            int conf = JOptionPane.showConfirmDialog(dlg, "Clear entire leaderboard?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                try {
                    Files.write(Paths.get(LEADERBOARD_FILE), "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
                    JOptionPane.showMessageDialog(dlg, "Leaderboard cleared.");
                    dlg.dispose();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(dlg, "Could not clear leaderboard: " + ex.getMessage());
                }
            }
        });

        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
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
