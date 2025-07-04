package test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SimpleWeatherApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SimpleWeatherGUI());
    }
}

class SimpleWeatherGUI {
    private JFrame frame;
    private JTextField cityField;
    private JLabel resultLabel;
    private BackgroundPanel backgroundPanel;

    public SimpleWeatherGUI() {
        // Setting up the frame
        frame = new JFrame("Weather App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 350);

        // Custom panel with initial background image
        backgroundPanel = new BackgroundPanel("D:\\java project\\java\\task\\src\\test\\weather_background.jpg");
        backgroundPanel.setLayout(new BorderLayout());

        // Input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setOpaque(false); // Make it transparent to show the background
        inputPanel.setLayout(new FlowLayout());

        JLabel cityLabel = new JLabel("City:");
        cityLabel.setForeground(Color.WHITE); // To make text visible on the background
        cityField = new JTextField(10);
        JButton fetchButton = new JButton("Get Weather");

        inputPanel.add(cityLabel);
        inputPanel.add(cityField);
        inputPanel.add(fetchButton);

        // Result label
        resultLabel = new JLabel("Enter a city to get weather info.", SwingConstants.CENTER);
        resultLabel.setForeground(Color.WHITE); // To make text visible on the background

        // Adding components to the background panel
        backgroundPanel.add(inputPanel, BorderLayout.NORTH);
        backgroundPanel.add(resultLabel, BorderLayout.CENTER);

        // Adding background panel to the frame
        frame.add(backgroundPanel);

        // Center the frame on the screen
        frame.setLocationRelativeTo(null);

        // Button action listener
        fetchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String city = cityField.getText().trim();
                if (!city.isEmpty()) {
                    fetchWeather(city);
                } else {
                    JOptionPane.showMessageDialog(frame, "Please enter a city name!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Make the frame visible
        frame.setVisible(true);
    }

    private void fetchWeather(String city) {
        String apiKey = "c373608ebe5aab99a7400841401b652a"; // Replace with your OpenWeatherMap API key
        String apiUrl = String.format("https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric", city, apiKey);

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                parseAndDisplayWeather(response.toString());
            } else {
                resultLabel.setText("Error: Unable to fetch weather data.");
            }
        } catch (Exception e) {
            resultLabel.setText("Error: " + e.getMessage());
        }
    }

    private void parseAndDisplayWeather(String jsonResponse) {
        try {
            String cityName = extractValue(jsonResponse, "\"name\":\"", "\"");
            String temp = extractValue(jsonResponse, "\"temp\":", ",");
            String description = extractValue(jsonResponse, "\"description\":\"", "\"");

            if (cityName != null && temp != null && description != null) {
                double temperature = Double.parseDouble(temp);
                String result = String.format("City: %s\nTemperature: %.1f °C\nCondition: %s", cityName, temperature, description);
                resultLabel.setText("<html>" + result.replace("\n", "<br>") + "</html>");

                // Change background based on temperature
                String newBackgroundPath = getBackgroundPath(temperature);
                backgroundPanel.setBackgroundImage(newBackgroundPath);
                backgroundPanel.repaint();

                // Adjust text color based on background brightness
                resultLabel.setForeground(isDarkBackground(temperature) ? Color.WHITE : Color.BLACK);
            } else {
                resultLabel.setText("Error: Invalid data received.");
            }
        } catch (Exception e) {
            resultLabel.setText("Error parsing weather data.");
        }
    }

    private String extractValue(String json, String keyStart, String keyEnd) {
        try {
            int start = json.indexOf(keyStart) + keyStart.length();
            int end = json.indexOf(keyEnd, start);
            return json.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }

    private String getBackgroundPath(double temperature) {
        if (temperature < -10) {
            return "D:\\java project\\java\\task\\src\\test\\snowy_weather.jpg";
        } else if (temperature < 0) {
            return "D:\\java project\\java\\task\\src\\test\\cold_weather.jpg";
        } else if (temperature <= 20) {
            return "D:\\java project\\java\\task\\src\\test\\mild_weather.jpg";
        } else if (temperature <= 30) {
            return "D:\\java project\\java\\task\\src\\test\\hot_weather.jpg";
        } else {
            return "D:\\java project\\java\\task\\src\\test\\very_hot_weather.jpg";
        }
    }

    private boolean isDarkBackground(double temperature) {
        return temperature <= 20; // Example: Cold and mild temperatures are likely to have darker backgrounds
    }
}

// Custom JPanel for displaying a background image
class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(String imagePath) {
        setBackgroundImage(imagePath);
    }

    public void setBackgroundImage(String imagePath) {
        try {
            backgroundImage = new ImageIcon(imagePath).getImage();
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
