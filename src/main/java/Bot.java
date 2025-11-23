import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bot {
    private static final Map<Long, String> userStates = new HashMap<>();
    private static final Map<Long, String> tempNames = new HashMap<>();

    public static void start(String botToken, String url, String username, String password, String apiToken) {
        TelegramBot bot = new TelegramBot(botToken);
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.initialize(url, username, password);

        List<User> users = dbManager.getAllUsers();
        BirthdayScheduler scheduler = new BirthdayScheduler(bot, dbManager);
        scheduler.start();

        bot.setUpdatesListener(updates -> {
            for (Update update: updates) {
                if (update.message() != null && update.message().text() != null) {
                    Long chatId = update.message().chat().id();
                    String messageText = update.message().text();
                    String userName = update.message().chat().firstName();

                    if (messageText.equals("/start")) {
                        sendMessage(bot, chatId, "Привет, " + userName + "!\n"
                                + "Я ваш бот и я умею поздравлять с днем рождения.\n"
                                + "Как мной пользоваться:\n"
                                + "/newBirthday - добавить день рождения в базу\n"
                                + "/allBirthdays - посмотреть все дни рождения в базе\n"
                                + "/deleteBirthday - удалить день рождения из базы\n"
                                + "/поздравь - получить сгенерированное поздравление\n");
                    } else {
                        handleCommand(bot, chatId, messageText, dbManager, apiToken, userName);
                    }
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private static void handleCommand(TelegramBot bot, Long chatId, String command,
                                      DatabaseManager dbManager, String apiToken, String userName) {
        String userState = userStates.get(chatId);

        if (userState != null) {
            switch (userState) {
                case "WAITING_FOR_NAME":
                    tempNames.put(chatId, command);
                    userStates.put(chatId, "WAITING_FOR_DATE");
                    sendMessage(bot, chatId, "Когда поздравляем? (дата рождения вида DD.MM.YYYY)");
                    return;

                case "WAITING_FOR_DATE":
                    String name = tempNames.get(chatId);
                    String dateStr = command;

                    if (isValidDate(dateStr)) {
                        try{
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                            LocalDate birthdate = LocalDate.parse(dateStr, formatter);

                            dbManager.addUser(chatId, name, birthdate);
                            sendMessage(bot, chatId, "Ура, день рождения добавлен!");
                        } catch (Exception e){
                            sendMessage(bot, chatId, "Что-то сломалось при добавлении.");
                        }
                    } else {
                        sendMessage(bot, chatId, "Неверный формат даты. Используйте DD.MM.YYYY");
                    }

                    userStates.remove(chatId);
                    tempNames.remove(chatId);
                    return;


                case "WAITING_FOR_ID_TO_DELETE":
                    try {
                        long telegramId = Long.parseLong(command);
                        if (dbManager.deleteUserByTelegramId(telegramId)) {
                            sendMessage(bot, chatId, "Пользователь удалён из базы.");
                        } else {
                            sendMessage(bot, chatId, "Пользователь не найден.");
                        }
                    } catch (NumberFormatException e) {
                        sendMessage(bot, chatId, "Неверный id для удаления.");
                    }

                    userStates.remove(chatId);
                    tempNames.remove(chatId);
                    return;
            }
        }

        switch (command.toLowerCase()) {
            case "/newbirthday":
                userStates.put(chatId, "WAITING_FOR_NAME");
                sendMessage(bot, chatId, "Кого поздравляем? (введите имя)");
                break;

            case "/allbirthdays":
                List<User> users = dbManager.getAllUsers();
                if (users.isEmpty()) {
                    sendMessage(bot, chatId, "В базе нет пользователей.");
                } else {
                    StringBuilder response = new StringBuilder("Пользователи в базе:\n");
                    for (int i = 0; i < users.size(); i++) {
                        User user = users.get(i);
                        response.append(i + 1).append(". ").append(user.getName())
                                .append(" - ").append(user.getBirthdayFormatted()).append("\n");
                    }
                    sendMessage(bot, chatId, response.toString());
                }
                break;


            case "/deletebirthday":
                List<User> usersForDelete = dbManager.getAllUsers();
                if (usersForDelete.isEmpty()) {
                    sendMessage(bot, chatId, "В базе нет пользователей для удаления.");
                } else {
                    StringBuilder response = new StringBuilder("Пользователи в базе:\n");
                    for (int i = 0; i < usersForDelete.size(); i++) {
                        User user = usersForDelete.get(i);
                        response.append(i + 1).append(". ").append(user.getName())
                                .append(" - ").append(user.getBirthdayFormatted()).append("\n");
                    }
                    sendMessage(bot, chatId, response.toString());
                    userStates.put(chatId, "WAITING_FOR_ID_TO_DELETE");
                    sendMessage(bot, chatId, "Напишите telegram_id пользователя, которого хотите удалить");
                }
                break;
            case "/поздравь":
                sendMessage(bot, chatId, "🎉 Генерируем поздравление... Пожалуйста, подождите.");
                String greeting = RuGPT3Generator.generateGreeting(apiToken, userName);
                sendMessage(bot, chatId, greeting);
                break;

            default:
                if (command.startsWith("/")) {
                    sendMessage(bot, chatId, "Неизвестная команда: " + command);
                }
                break;
        }
    }

    private static boolean isValidDate(String date) {
        return date.matches("\\d{2}\\.\\d{2}\\.\\d{4}");
    }

    private static void sendMessage(TelegramBot bot, Long chatId, String text) {
        SendMessage request = new SendMessage(chatId, text);
        bot.execute(request);
    }

    private static String generateGreeting(String prompt, String apiToken) {
        try {
            String jsonRequest = "{\"inputs\":\"" + prompt + "\"}";
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api-inference.huggingface.co/models/microsoft/DialoGPT-large"))
                    .header("Authorization", "Bearer " + apiToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка при генерации поздравления";
        }
    }

    private static String parseGeneratedText(String jsonResponse) {
        try {
            JsonElement element = JsonParser.parseString(jsonResponse);
            JsonArray array = element.getAsJsonArray();
            if (array.size() > 0) {
                return array.get(0).getAsJsonObject().get("generated_text").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Не удалось получить сгенерированный текст";
    }
}
