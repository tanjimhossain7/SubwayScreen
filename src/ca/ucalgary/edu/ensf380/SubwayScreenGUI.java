package ca.ucalgary.edu.ensf380;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("serial")
public class SubwayScreenGUI extends JFrame {
    private final TrainStationManager stationManager;
    private final JLabel[][] trainLabels;
    private final JLabel[][] announcementLabels;
    @SuppressWarnings("unused")
	private final String[] lines = {"R", "B", "G"};
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public SubwayScreenGUI() {
        stationManager = new TrainStationManager();
        trainLabels = new JLabel[12][5];  // 12 trains with 5 stations each
        announcementLabels = new JLabel[12][1];  // 12 announcements, one per train
        setupGUI();
        startUpdatingDisplay();
    }

    private void setupGUI() {
        setTitle("Train Screen");
        setSize(800, 600); // Reduced size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(12, 1));  // 12 rows for 12 trains

        for (int i = 0; i < 12; i++) {
            JPanel panel = new JPanel(new GridLayout(2, 1));
            JPanel stationPanel = new JPanel(new GridLayout(1, 5));
            for (int j = 0; j < 5; j++) {
                trainLabels[i][j] = new JLabel("", JLabel.CENTER);
                trainLabels[i][j].setFont(new Font("Arial", Font.PLAIN, 12));  // Smaller font size
                stationPanel.add(trainLabels[i][j]);
            }
            announcementLabels[i][0] = new JLabel("", JLabel.CENTER);
            announcementLabels[i][0].setFont(new Font("Arial", Font.PLAIN, 12));  // Smaller font size
            panel.add(stationPanel);
            panel.add(announcementLabels[i][0]);
            add(panel);
        }

        setVisible(true);
    }

    private void startUpdatingDisplay() {
        Runnable updateTask = () -> {
            List<TrainStationManager.TrainData> allTrains = stationManager.getTrainPositions("R");
            allTrains.addAll(stationManager.getTrainPositions("B"));
            allTrains.addAll(stationManager.getTrainPositions("G"));
            updateStationLabels(allTrains);
        };
        scheduler.scheduleAtFixedRate(updateTask, 0, 5, TimeUnit.SECONDS);
    }

    private void updateStationLabels(List<TrainStationManager.TrainData> allTrains) {
        for (int i = 0; i < allTrains.size() && i < 12; i++) {
            TrainStationManager.TrainData data = allTrains.get(i);
            List<String> surroundingStations = data.getSurroundingStations();
            trainLabels[i][0].setText("Prev: " + (surroundingStations.size() > 0 ? surroundingStations.get(0) : ""));
            trainLabels[i][1].setText("Train " + data.getTrainNumber() + ": " + data.getStationName());
            trainLabels[i][1].setBackground(getLineColor(data.getTrainNumber().charAt(0)));
            trainLabels[i][1].setOpaque(true);
            for (int j = 2; j <= 4; j++) {
                trainLabels[i][j].setText("Next " + (j - 1) + ": " + (surroundingStations.size() >= j ? surroundingStations.get(j - 1) : ""));
            }
            announcementLabels[i][0].setText("Next Station: " + data.getDestination());
        }
    }

    private Color getLineColor(char line) {
        switch (line) {
            case 'R':
                return Color.RED;
            case 'B':
                return Color.BLUE;
            case 'G':
                return Color.GREEN;
            default:
                return Color.BLACK;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SubwayScreenGUI::new);
    }
}
