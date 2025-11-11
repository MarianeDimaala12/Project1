 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 import java.util.List;
 import java.util.ArrayList;
 
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
 
         SwingUtilities.invokeLater(() -> {
             boolean ok = showWelcomeScreenAndGetName();
             if (!ok) System.exit(0);
             level = 1;
             score = 0;
             startLevel();
         });
 
         setVisible(true);
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
         while (true) {
             JPanel panel = new JPanel(new BorderLayout(8,8));
             panel.setBackground(WINDOW_BG);
             panel.setBorder(new EmptyBorder(12,12,12,12));
 
             JLabel title = new JLabel("🎉 Welcome to the DWCC Memory Game! 🎉", SwingConstants.CENTER);
             title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
             title.setForeground(HEADER_FOOTER);
             panel.add(title, BorderLayout.NORTH);
 
             JTextArea instructions = new JTextArea(
                     "Match all pairs to complete each level.\n" +
                             "There are 5 levels. Lives and timers vary per level.\n\n" +
                             "Enter your player name to start:");
             instructions.setEditable(false);
             instructions.setFocusable(false);
             instructions.setOpaque(false);
             instructions.setFont(instructions.getFont().deriveFont(13f));
             instructions.setForeground(PRIMARY_TEXT);
             panel.add(instructions, BorderLayout.CENTER);
 
             JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
             inputRow.setBackground(WINDOW_BG);
             JTextField nameField = new JTextField(20);
             inputRow.add(new JLabel("Player name:"));
             inputRow.add(nameField);
             panel.add(inputRow, BorderLayout.SOUTH);
 
             Object[] options = {"Start Game", "Exit"};
             int choice = JOptionPane.showOptionDialog(this, panel, "Welcome",
                     JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                     options, options[0]);
 
             if (choice == 1 || choice == JOptionPane.CLOSED_OPTION) {
                 int conf = JOptionPane.showConfirmDialog(this, "Exit game?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
                 if (conf == JOptionPane.YES_OPTION) return false;
                 else continue;
             }
 
             String inputName = nameField.getText();
             if (inputName != null) inputName = inputName.trim();
             if (inputName == null || inputName.isEmpty()) {
                 JOptionPane.showMessageDialog(this, "Please enter your name to start.", "Name required", JOptionPane.WARNING_MESSAGE);
                 continue;
             }
 
             playerName = inputName;
             return true;
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
                 JOptionPane.showMessageDialog(this,
                         "<html><b>" + key + "</b><br/><i>" + desc + "</i></html>",
                         "Match found!", JOptionPane.INFORMATION_MESSAGE);
 
                 
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
                     if (firstSelected != null) firstSelected.hideFace();
                     if (secondSelected != null) secondSelected.hideFace();
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
         }
 
         public String getContent() { return content; }
         public boolean isFaceUp() { return faceUp; }
         public boolean isMatched() { return matched; }
 
         public void setMatched(boolean m) {
             matched = m;
             setEnabled(!m);
             if (m) {
                 setBackground(MATCHED_COLOR);
                 setBorder(new LineBorder(HEADER_FOOTER, 2));
             } else {
                 setBackground(null);
                 setBorder(new LineBorder(new Color(0xBDBDBD)));
             }
         }
 
         public void showFace() {
             faceUp = true;
             setText("<html><center>" + content + "</center></html>");
             setBackground(Color.WHITE);
         }
 
         public void hideFace() {
             faceUp = false;
             setText(" ");
             setBackground(null);
         }
     }
 
     public static void main(String[] args) {
         SwingUtilities.invokeLater(() -> new MemoryGame());
     }
 }

 

