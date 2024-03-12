import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class LyricsDisplayApp extends JFrame {
    private JTextField songNameField;
    private JTextField artistField;
    private JDialog lyricsDialog;
    private JTextArea lyricsTextArea; // JTextArea to display lyrics
    private List<String> lyrics; // Variable to hold plain lyrics
    private List<Double> timestamps; // Variable to hold timestamps
    private Timer timer; // Timer for automatic scrolling

    public LyricsDisplayApp() {
        setTitle("Lyrics Display");
        setSize(400, 200); // Reduced size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 2));
        inputPanel.add(new JLabel("Enter the song name:"));
        songNameField = new JTextField();
        songNameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                artistField.requestFocusInWindow();
            }
        });
        inputPanel.add(songNameField);
        inputPanel.add(new JLabel("Enter the artist:"));
        artistField = new JTextField();
        inputPanel.add(artistField);
        JButton displayButton = new JButton("Display Lyrics");
        displayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayLyrics();
            }
        });
        inputPanel.add(displayButton);

        add(inputPanel, BorderLayout.NORTH);

        // Add ActionListener to artistField
        artistField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayLyrics();
            }
        });
    }

    private void displayLyrics() {
        String songName = songNameField.getText();
        String artist = artistField.getText();

        try {
            fetchLyrics(artist, songName);
            // Display the lyrics in a dialog
            showLyricsDialog();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: Unable to retrieve lyrics.");
        }
    }

    private void fetchLyrics(String artist, String songTitle) throws IOException, ParseException {
        // Construct the URL for the lrclib.net API
        String apiUrl = "https://lrclib.net/api/search";
        String query = artist + " " + songTitle;
        String urlString = apiUrl + "?q=" + query.replace(" ", "+");

        // Send GET request to the API and fetch response
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Read the response
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Parse JSON response
        JSONParser parser = new JSONParser();
        JSONArray jsonArray = (JSONArray) parser.parse(response.toString());

        // Extract lyrics data from JSON response
        lyrics = new ArrayList<>();
        timestamps = new ArrayList<>();
        for (Object obj : jsonArray) {
            JSONObject record = (JSONObject) obj;
            String plainLyrics = (String) record.get("plainLyrics");
            String syncedLyrics = (String) record.get("syncedLyrics");

            // If synced lyrics are available, use them; otherwise, fallback to plain lyrics
            String lyricsToAdd = (syncedLyrics != null && !syncedLyrics.isEmpty()) ? syncedLyrics : plainLyrics;

            // Remove timestamps from lyrics if not null or empty
            if (lyricsToAdd != null && !lyricsToAdd.isEmpty()) {
                // Split lyrics by lines
                String[] lines = lyricsToAdd.split("\\r?\\n");
                for (String linee : lines) {
                    // Ensure lyricsToAdd is not null before attempting to manipulate it
                    if (linee != null) {
                        // Remove timestamps from each line
                        linee = linee.replaceAll("\\[\\d{2}:\\d{2}.\\d{2}\\]", "").trim();
                        // Add non-empty lines to the lyrics list
                        if (!linee.isEmpty()) {
                            lyrics.add(linee);
                        }
                    }
                }
            }
        }
    }

    private void showLyricsDialog() {
        // Create a new dialog to display the lyrics
        lyricsDialog = new JDialog(this);
        lyricsDialog.setUndecorated(false); // Restore title bar
        lyricsDialog.setSize(500, 400); // Reduced size and increased readability
        lyricsDialog.setLocationRelativeTo(this); // Center window
        lyricsDialog.setResizable(true); // Allow resizing

        // Create a panel to display lyrics
        lyricsTextArea = new JTextArea();
        lyricsTextArea.setEditable(false);
        lyricsTextArea.setOpaque(false);
        lyricsTextArea.setFont(new Font("SansSerif", Font.BOLD, 16)); // Change font to bold and size 16
        lyricsTextArea.setForeground(Color.BLACK); // Change font color to black

        // Construct the lyrics string
        StringBuilder lyricsBuilder = new StringBuilder();
        for (String lyric : lyrics) {
            lyricsBuilder.append(lyric).append("\n"); // Append each lyric and add newline
        }
        lyricsTextArea.setText(lyricsBuilder.toString()); // Set lyrics in JTextArea
        lyricsTextArea.setCaretPosition(0); // Set caret position to the beginning

        // Add document listener to adjust caret position
        lyricsTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                adjustCaretPosition();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                adjustCaretPosition();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                adjustCaretPosition();
            }

            private void adjustCaretPosition() {
                int lineNumber = 0;
                try {
                    int caretPosition = lyricsTextArea.getCaretPosition();
                    lineNumber = lyricsTextArea.getLineOfOffset(caretPosition);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                Rectangle rect;
                try {
                    rect = lyricsTextArea.modelToView(lyricsTextArea.getLineStartOffset(lineNumber));
                    if (rect != null) {
                        lyricsTextArea.scrollRectToVisible(rect);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Add the panel to the dialog
        lyricsDialog.add(new JScrollPane(lyricsTextArea));

        // Make the dialog movable and resizable
        lyricsDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        lyricsDialog.setModal(false);

        // Start timer for automatic scrolling
        timer = new Timer(1000, new ActionListener() {
            int currentIndex = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentIndex < timestamps.size()) {
                    double currentTime = currentIndex == 0 ? 0 : timestamps.get(currentIndex - 1);
                    double nextTime = timestamps.get(currentIndex);
                    double deltaTime = nextTime - currentTime;

                    // Scroll the lyrics text area by one line
                    Rectangle visibleRect = lyricsTextArea.getVisibleRect();
                    visibleRect.y += lyricsTextArea.getFontMetrics(lyricsTextArea.getFont()).getHeight();
                    lyricsTextArea.scrollRectToVisible(visibleRect);

                    currentIndex++;
                } else {
                    // Stop the timer when lyrics end
                    timer.stop();
                }
            }
        });

        timer.setInitialDelay(0);
        timer.start();

        // Make the dialog visible after setting caret position
        lyricsDialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LyricsDisplayApp app = new LyricsDisplayApp();
                app.setVisible(true);
            }
        });
    }
}