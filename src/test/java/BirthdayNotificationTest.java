import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BirthdayNotificationTest {

    @Test
    void testNotificationCreation() {
        // When
        BirthdayNotification notification = new BirthdayNotification(123456L, "Иван Иванов");

        // Then
        assertEquals(123456L, notification.getNotifyChatId());
        assertEquals("Иван Иванов", notification.getPersonName());
    }
}
