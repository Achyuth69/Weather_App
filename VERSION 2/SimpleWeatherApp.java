package seryproject;

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

    public SimpleWeatherGUI() {
        // Setting up the frame
        frame = new JFrame("Weather App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);
        frame.setLayout(new BorderLayout());

        // Input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());

        JLabel cityLabel = new JLabel("City:");
        cityField = new JTextField(10);
        JButton fetchButton = new JButton("Get Weather");

        inputPanel.add(cityLabel);
        inputPanel.add(cityField);
        inputPanel.add(fetchButton);

        // Result label
        resultLabel = new JLabel("Enter a city to get weather info.", SwingConstants.CENTER);

        // Adding components to frame
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(resultLabel, BorderLayout.CENTER);

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

        frame.setVisible(true);
    }

    private void fetchWeather(String city) {
        // Replace with your actual API key
        String apiKey = "c373608ebe5aab99a7400841401b652a";
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

                System.out.println("API Response: " + response.toString());
                parseAndDisplayWeather(response.toString());
            } else {
                resultLabel.setText("Error: Unable to fetch weather data. Response code: " + responseCode);
                System.out.println("Error Response Code: " + responseCode);
            }
        } catch (Exception e) {
            resultLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void parseAndDisplayWeather(String jsonResponse) {
        try {
            String cityName = extractValue(jsonResponse, "\"name\":\"", "\"");
            String temp = extractValue(jsonResponse, "\"temp\":", ",");
            String description = extractValue(jsonResponse, "\"description\":\"", "\"");

            if (cityName != null && temp != null && description != null) {
                String result = String.format("City: %s\nTemperature: %s °C\nCondition: %s", cityName, temp, description);
                resultLabel.setText("<html>" + result.replace("\n", "<br>") + "</html>");
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
}
