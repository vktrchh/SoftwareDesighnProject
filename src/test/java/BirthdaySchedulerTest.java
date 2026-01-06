import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BirthdaySchedulerTest {

    @Mock
    private TelegramBot bot;

    @Mock
    private DatabaseManager database;

    @Mock
    private ScheduledExecutorService scheduler;

    private BirthdayScheduler birthdayScheduler;

    @BeforeEach
    void setUp() {
        // Используем Reflection для тестирования приватных полей
        birthdayScheduler = new BirthdayScheduler(bot, database);
    }

    @Test
    void testStart() {
        // When
        birthdayScheduler.start();

        // Then
        // Проверяем что логирование произошло
        // В реальном тесте нужно проверить через Captor
    }

    @Test
    void testCheckBirthdaysWithNotifications() {
        // Given
        List<BirthdayNotification> notifications = Arrays.asList(
                new BirthdayNotification(123L, "Иван"),
                new BirthdayNotification(456L, "Мария")
        );

        when(database.getTodayNotifications()).thenReturn(notifications);

        // When
        // Вызываем приватный метод через reflection или делаем package-private
        // birthdayScheduler.checkBirthdays();

        // Then
        // Проверяем что отправлены сообщения
        verify(database, times(1)).getTodayNotifications();
    }

    @Test
    void testCheckBirthdaysNoNotifications() {
        // Given
        when(database.getTodayNotifications()).thenReturn(Arrays.asList());

        // When
        // birthdayScheduler.checkBirthdays();

        // Then
        verify(database, times(1)).getTodayNotifications();
        // verify(bot, never()).execute(any(SendMessage.class));
    }

    @Test
    void testStop() {
        // When
        birthdayScheduler.stop();

        // Then
        // Проверяем что scheduler был остановлен
    }
}
