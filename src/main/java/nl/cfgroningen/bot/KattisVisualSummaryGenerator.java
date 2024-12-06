package nl.cfgroningen.bot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import nl.cfgroningen.scores.UniversityScoreInformation;
import nl.cfgroningen.scores.UniversityScoreInformation.UniversityUserInformation;

public class KattisVisualSummaryGenerator {
    // private KattisBot bot;

    private Color fromHex(String hex) {
        return new Color(
                Integer.valueOf(hex.substring(1, 3), 16),
                Integer.valueOf(hex.substring(3, 5), 16),
                Integer.valueOf(hex.substring(5, 7), 16));
    }

    private static Point GRAPH_START = new Point(0, 0);
    private static Point GRAPH_END = new Point((int) ((1920 / 2) * 0.6), 1080 / 2 - 30);

    private static final int PADDING_RIGHT = 160;
    private static final List<Color> WARM_COLORS = List.of(
            new Color(255, 87, 34), new Color(255, 140, 0), new Color(255, 193, 7), new Color(255, 235, 59),
            new Color(255, 152, 0), new Color(255, 87, 34), new Color(255, 193, 7), new Color(255, 235, 59),
            new Color(255, 152, 0), new Color(255, 87, 34), new Color(255, 140, 0), new Color(255, 193, 7),
            new Color(255, 235, 59), new Color(255, 152, 0), new Color(255, 87, 34), new Color(255, 140, 0),
            new Color(255, 193, 7), new Color(255, 235, 59), new Color(255, 152, 0), new Color(255, 87, 34),
            new Color(255, 140, 0), new Color(255, 193, 7), new Color(255, 235, 59), new Color(255, 152, 0),
            new Color(255, 87, 34), new Color(255, 140, 0), new Color(255, 193, 7), new Color(255, 235, 59),
            new Color(255, 152, 0), new Color(255, 87, 34), new Color(255, 140, 0), new Color(255, 193, 7),
            new Color(255, 235, 59), new Color(255, 152, 0), new Color(255, 87, 34), new Color(255, 140, 0),
            new Color(255, 193, 7), new Color(255, 235, 59), new Color(255, 152, 0), new Color(255, 87, 34),
            new Color(255, 140, 0), new Color(255, 193, 7), new Color(255, 235, 59), new Color(255, 152, 0),
            new Color(255, 87, 34), new Color(255, 140, 0), new Color(255, 193, 7), new Color(255, 235, 59),
            new Color(255, 152, 0));

    private void generateGraph(List<UniversityScoreInformation> scoreInfo, Graphics2D g) {
        int padding = 50;
        int graphWidth = GRAPH_END.x - GRAPH_START.x;
        int graphHeight = GRAPH_END.y - GRAPH_START.y;

        g.setColor(fromHex("#2c2f33"));

        Set<String> uniqueUsers = new HashSet<>();
        for (UniversityScoreInformation info : scoreInfo) {
            for (UniversityUserInformation user : info.getStudents()) {
                uniqueUsers.add(user.getName());
            }
        }

        long minTime = scoreInfo.stream().mapToLong(UniversityScoreInformation::getUnixMillis).min().orElse(0);
        long maxTime = scoreInfo.stream().mapToLong(UniversityScoreInformation::getUnixMillis).max().orElse(0);

        double minScore = scoreInfo.stream()
                .flatMapToDouble(info -> info.getStudents().stream().mapToDouble(UniversityUserInformation::getScore))
                .min().orElse(0) - 20;
        double maxScore = scoreInfo.stream()
                .flatMapToDouble(info -> info.getStudents().stream().mapToDouble(UniversityUserInformation::getScore))
                .max().orElse(0) + 10;

        double timeRange = maxTime - minTime;
        double scoreRange = maxScore - minScore;

        g.setStroke(new BasicStroke(3));

        if (timeRange == 0 || scoreRange == 0) {
            return;
        }

        // Draw scales
        Color greyColor = fromHex("#404040");
        g.setColor(greyColor);
        for (int i = 2; i <= 11; i++) {
            int x = GRAPH_START.x + padding + (int) (i * graphWidth / 10.0);
            g.drawLine(x, GRAPH_END.y + padding, x, GRAPH_START.y + padding); // Extend line to top
        }

        for (int i = 2; i <= 10; i++) {
            int y = GRAPH_END.y + padding - (int) (i * graphHeight / 10.0);
            g.drawLine(GRAPH_START.x + padding, y, GRAPH_END.x + padding + padding + 5, y); // Extend line to right
        }

        // Use bigger font
        g.setFont(new Font("Roboto", Font.BOLD, 20));

        // Draw labels
        g.setColor(Color.WHITE);
        for (int i = 2; i <= 10; i++) {
            int x = GRAPH_START.x + padding - 5;
            int y = GRAPH_END.y + padding - (int) (i * graphHeight / 10.0);
            g.drawString(String.valueOf((int) (minScore + (i * scoreRange / 10.0))), x - 35, y + 5); // Moved label
            // lower
        }

        int colorIndex = 0;
        for (String user : uniqueUsers) {
            Color userColor = WARM_COLORS.get(colorIndex % WARM_COLORS.size());
            colorIndex++;
            for (int i = 0; i < scoreInfo.size() - 1; i++) {
                UniversityScoreInformation currentInfo = scoreInfo.get(i);
                UniversityScoreInformation nextInfo = scoreInfo.get(i + 1);

                UniversityUserInformation currentUserInfo = currentInfo.getStudents().stream()
                        .filter(u -> u.getName().equals(user))
                        .findFirst()
                        .orElse(null);

                UniversityUserInformation nextUserInfo = nextInfo.getStudents().stream()
                        .filter(u -> u.getName().equals(user))
                        .findFirst()
                        .orElse(null);

                if (currentUserInfo != null && nextUserInfo != null) {
                    double currentX = GRAPH_START.x + padding
                            + (graphWidth * (currentInfo.getUnixMillis() - minTime) / timeRange);
                    double nextX = GRAPH_START.x + padding
                            + (graphWidth * (nextInfo.getUnixMillis() - minTime) / timeRange);

                    double currentY = GRAPH_END.y + padding
                            - (graphHeight * (currentUserInfo.getScore() - minScore) / scoreRange);
                    double nextY = GRAPH_END.y + padding
                            - (graphHeight * (nextUserInfo.getScore() - minScore) / scoreRange);

                    g.setColor(userColor);
                    g.drawLine((int) currentX, (int) currentY, (int) nextX, (int) nextY);

                    // Draw a little circle at the joint
                    g.fillOval((int) currentX - 5, (int) currentY - 5, 10, 10);
                }
            }

            // Draw user label at the end of their line
            if (scoreInfo.size() > 0) {
                UniversityScoreInformation lastInfo = scoreInfo.get(scoreInfo.size() - 1);
                UniversityUserInformation lastUserInfo = lastInfo.getStudents().stream()
                        .filter(u -> u.getName().equals(user))
                        .findFirst()
                        .orElse(null);

                if (lastUserInfo != null) {
                    double lastX = GRAPH_START.x + padding
                            + (graphWidth * (lastInfo.getUnixMillis() - minTime) / timeRange);
                    double lastY = GRAPH_END.y + padding
                            - (graphHeight * (lastUserInfo.getScore() - minScore) / scoreRange);

                    g.setColor(Color.WHITE);
                    g.drawString(user, (int) lastX + 5, (int) lastY - 5);
                }
            }
        }
    }

    private void writeSummaryInformation(List<UniversityScoreInformation> scoreInfo, Graphics2D g) {
        // Write name of the university
        g.setColor(Color.WHITE);
        g.setFont(new Font("Roboto", Font.BOLD, 30));
        g.drawString(scoreInfo.get(0).getName(), GRAPH_END.x + PADDING_RIGHT, GRAPH_START.y + 30);

        // Write the number of users
        g.setFont(new Font("Roboto", Font.PLAIN, 20));
        g.drawString("Users: " + scoreInfo.get(0).getUsers(), GRAPH_END.x + PADDING_RIGHT, GRAPH_START.y + 60);

        // Get the start rank
        int startRank = scoreInfo.get(0).getRank();
        int endRank = scoreInfo.get(scoreInfo.size() - 1).getRank();

        // Write the rank
        g.setFont(new Font("Roboto", Font.PLAIN, 20));
        String rankText = "New Rank " + endRank;
        g.drawString(rankText, GRAPH_END.x + PADDING_RIGHT, GRAPH_START.y + 90);
        Color darkGreen = new Color(0, 128, 0); // Dark green color
        Color lighterGreen = new Color(144, 238, 144); // Lighter green color for the arrow
        Color darkRed = new Color(128, 0, 0); // Dark red color
        Color lighterRed = new Color(238, 144, 144); // Lighter red color for the arrow
        FontMetrics metrics = g.getFontMetrics();
        int textWidth = metrics.stringWidth(rankText);
        int textHeight = metrics.getHeight();
        int arrowX = GRAPH_END.x + PADDING_RIGHT + textWidth + 15;
        int arrowY = GRAPH_START.y + 90 - textHeight / 2 - 5; // Adjust Y position to align with text
        if (startRank > endRank) {
            drawLittleRoundedBoxWithArrowInIt(g, arrowX, arrowY, 20, darkGreen, lighterGreen, true);
        } else {
            drawLittleRoundedBoxWithArrowInIt(g, arrowX, arrowY, 20, darkRed, lighterRed, false);
        }

        g.setColor(Color.WHITE);

        // Write the score
        double startScore = scoreInfo.get(0).getScore();
        double endScore = scoreInfo.get(scoreInfo.size() - 1).getScore();

        g.setFont(new Font("Roboto", Font.PLAIN, 20));
        String startScoreText = "Start Score: " + startScore;
        String endScoreText = "End Score: " + endScore;
        g.drawString(startScoreText, GRAPH_END.x + PADDING_RIGHT, GRAPH_START.y + 120);
        g.drawString(endScoreText, GRAPH_END.x + PADDING_RIGHT, GRAPH_START.y + 150);

        // Draw the score arrow
        int arrowYScore = GRAPH_START.y + 150 - textHeight / 2 - 5; // Adjust Y position to align with text
        int arrowXScore = GRAPH_END.x + PADDING_RIGHT + metrics.stringWidth(endScoreText) + 15;

        if (startScore < endScore) {
            drawLittleRoundedBoxWithArrowInIt(g, arrowXScore, arrowYScore, 20, darkGreen, lighterGreen, true);
        } else {
            drawLittleRoundedBoxWithArrowInIt(g, arrowXScore, arrowYScore, 20, darkRed, lighterRed, false);
        }

        // Add top performers
        g.setColor(Color.WHITE);
        g.setFont(new Font("Roboto", Font.BOLD, 20));
        g.drawString("Top Performers", GRAPH_END.x + PADDING_RIGHT, GRAPH_START.y + 210);

        g.setFont(new Font("Roboto", Font.PLAIN, 20));

        UniversityScoreInformation delta = summarySession(scoreInfo);

        int y = GRAPH_START.y + 240;
        List<UniversityUserInformation> topPerformers = delta.getStudents().stream()
                .sorted((u1, u2) -> Double.compare(u2.getScore(), u1.getScore()))
                .limit(5)
                .toList();
        for (UniversityUserInformation user : topPerformers) {
            g.drawString(user.getName() + " - " + user.getScore(), GRAPH_END.x + PADDING_RIGHT, y);
            y += 30;
        }
    }

    private void drawLittleRoundedBoxWithArrowInIt(Graphics g, int x, int y, int size, Color boxColor, Color arrowColor,
                                                   boolean arrowUp) {
        int arrowSize = 10;
        int arrowX = x + size / 2 - arrowSize / 2;
        int arrowY = y + size / 2 - arrowSize / 2;
        g.setColor(boxColor);
        g.fillRoundRect(x, y, size, size, 10, 10);
        g.setColor(arrowColor); // Set color to the specified arrow color
        if (arrowUp) {
            g.fillPolygon(new int[]{arrowX, arrowX + arrowSize / 2, arrowX + arrowSize},
                    new int[]{arrowY + arrowSize, arrowY, arrowY + arrowSize}, 3);
        } else {
            g.fillPolygon(new int[]{arrowX, arrowX + arrowSize / 2, arrowX + arrowSize},
                    new int[]{arrowY, arrowY + arrowSize, arrowY}, 3);
        }
    }

    public byte[] generateVisualSummary() {
        List<UniversityScoreInformation> scoreInfo = new ArrayList<>();

        double[] currentScores = {100, 110, 120, 130, 140};
        String[] userNames = {"Mattia", "Alice", "Bob", "Charlie", "Diana"};
        for (int i = 0; i < 30; i++) {
            List<UniversityUserInformation> users = new ArrayList<>();
            for (int j = 0; j < userNames.length; j++) {
                users.add(new UniversityUserInformation(j + 1, userNames[j],
                        "https://open.kattis.com/users/" + userNames[j].toLowerCase(), currentScores[j]));
                currentScores[j] += Math.random() * 10;
            }
            scoreInfo.add(new UniversityScoreInformation(123 - i, 100 + i, 1, "rug bois", "https://google.com", users,
                    System.currentTimeMillis() + i * 20000));
        }

        BufferedImage image = new BufferedImage(1920 / 2, 1080 / 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) image.getGraphics();

        // Enable antialiasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(fromHex("#38343c"));
        g.fillRect(0, 0, image.getWidth(), image.getHeight());

        generateGraph(scoreInfo, g);
        writeSummaryInformation(scoreInfo, g);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpeg", baos);
        } catch (Exception e) {
            // Caca
        }

        return baos.toByteArray();
    }

    // public static void main(String[] args) {
    // KattisVisualSummaryGenerator generator = new KattisVisualSummaryGenerator();

    // JFrame frame = new JFrame();
    // frame.getContentPane().setLayout(new FlowLayout());
    // frame.getContentPane().add(new JLabel(new
    // ImageIcon(generator.generateVisualSummary())));
    // frame.pack();
    // frame.setLocation(200, 200);
    // frame.setVisible(true);
    // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // while (true) {
    // frame.getContentPane().removeAll();
    // frame.getContentPane().add(new JLabel(new
    // ImageIcon(generator.generateVisualSummary())));
    // frame.revalidate();
    // frame.repaint();

    // try {
    // Thread.sleep(1000);
    // } catch (InterruptedException e) {
    // e.printStackTrace();
    // }
    // }
    // }

    private UniversityScoreInformation deltaInfo(UniversityScoreInformation info, UniversityScoreInformation oldinfo) {
        // Compare students
        List<UniversityScoreInformation.UniversityUserInformation> deltaStudents = new ArrayList<>();
        for (UniversityScoreInformation.UniversityUserInformation student : info.getStudents()) {
            oldinfo.getStudents().stream()
                    .filter(oldStudent -> oldStudent.getName().equals(student.getName()))
                    .findFirst()
                    .ifPresentOrElse(oldStudent -> {
                        if (!student.equals(oldStudent)) {
                            deltaStudents.add(student);
                        }
                    }, () -> deltaStudents.add(student)); // New student
        }
        info.setStudents(deltaStudents);

        return info;
    }

    public UniversityScoreInformation summarySession(List<UniversityScoreInformation> infos) {
        UniversityScoreInformation info = infos.get(0);
        UniversityScoreInformation lastInfo = infos.get(infos.size() - 1);

        return this.deltaInfo(info, lastInfo);
    }

}
