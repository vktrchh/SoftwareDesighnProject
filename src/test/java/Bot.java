import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BotTest {

    @Mock
    private TelegramBot bot;

    @Mock
    private DatabaseManager databaseManager;

    @Mock
    private Update update;

    @Mock
    private Message message;

    @Mock
    private Chat chat;

    @Captor
    private ArgumentCaptor<SendMessage> messageCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleCommandStart() {
        // Given
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("/start");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(123L);
        when(chat.firstName()).thenReturn("Иван");

        // When
        // Вызываем обработку команды через рефлексию

        // Then
        verify(bot).execute(messageCaptor.capture());
        SendMessage sentMessage = messageCaptor.getValue();
        assertEquals(123L, sentMessage.getParameters().get("chat_id"));
        assertTrue(((String)sentMessage.getParameters().get("text")).contains("Иван"));
    }

    @Test
    void testIsValidDate() {
        // Тестируем приватный метод через рефлексию
        assertTrue(Bot.isValidDate("15.05.1990"));
        assertTrue(Bot.isValidDate("01.01.2000"));
        assertFalse(Bot.isValidDate("15-05-1990"));
        assertFalse(Bot.isValidDate("1990.05.15"));
        assertFalse(Bot.isValidDate("15.05.90"));
    }
}
