import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RuGPT3Generator {
    private static final Logger LOGGER = Logger.getLogger(RuGPT3Generator.class.getName());

    private static final String MODEL_URL = "https://api-inference.huggingface.co/models/sberbank-ai/rugpt3small_based_on_gpt2";
    private static final String GREETING_PROMPT = "Напиши красивое поздравление с днём рождения:";
    private static final int REQUEST_TIMEOUT_SECONDS = 30;
    private static final int MAX_NEW_TOKENS = 100;
    public static String generateGreeting(String apiToken, String userName) {
        try {
            String prompt = String.format("%s %s!\n", GREETING_PROMPT, userName);
            String jsonRequest = buildJsonRequest(prompt);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(MODEL_URL))
                    .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
                    .header("Authorization", "Bearer " + apiToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            LOGGER.info("Sending request to ruGPT-3 model...");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String greeting = parseResponse(response.body());
                LOGGER.info("Greeting generated successfully");
                return greeting;
            } else {
                LOGGER.log(Level.WARNING, "API returned status code: " + response.statusCode());
                return getFallbackGreeting(userName);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating greeting", e);
            return getFallbackGreeting(userName);
        }
    }
    private static String buildJsonRequest(String prompt) {
        return String.format(
                "{\"inputs\":\"%s\",\"parameters\":{\"max_new_tokens\":%d,\"temperature\":0.7}}",
                escapeJson(prompt),
                MAX_NEW_TOKENS
        );
    }
    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    private static String parseResponse(String jsonResponse) {
        try {
            // Простой парсинг JSON без внешних библиотек
            int startIdx = jsonResponse.indexOf("\"generated_text\":\"");
            if (startIdx == -1) {
                startIdx = jsonResponse.indexOf("\\\"generated_text\\\":");
            }

            if (startIdx != -1) {
                int contentStart = jsonResponse.indexOf(":", startIdx) + 1;
                int contentEnd = jsonResponse.indexOf("\"", contentStart + 2);

                if (contentEnd > contentStart) {
                    String text = jsonResponse.substring(contentStart, contentEnd);
                    // Убираем экранирование
                    text = text.replace("\\\"", "\"")
                            .replace("\\n", "\n")
                            .replace("\\\\", "\\");

                    return text.trim();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse response", e);
        }

        return "Поздравляю с днём рождения! ";
    }
    private static String getFallbackGreeting(String userName) {
        String[] greetings = {
                "🎉 Поздравляю, " + userName + "! Желаю здоровья, счастья и процветания! 🎂",
                "🎁 " + userName + ", с днём рождения! Пусть твой день будет наполнен радостью и улыбками! ✨",
                "🎊 С днём рождения, " + userName + "! Пусть исполняются все твои мечты! 🌟",
                "🥳 " + userName + ", давай отметим твой день! Желаю всего самого лучшего! 🎈"
        };
        int index = (int) (System.currentTimeMillis() % greetings.length);
        return greetings[index];
    }
    public static boolean testConnection(String apiToken) {
        try {
            String testGreeting = generateGreeting(apiToken, "Тест");
            LOGGER.info("Connection test passed: " + (testGreeting != null && !testGreeting.isEmpty()));
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Connection test failed", e);
            return false;
        }
    }
}
