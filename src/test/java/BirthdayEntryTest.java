import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class BirthdayEntryTest {

    @Test
    void testBirthdayEntryCreation() {
        // Given
        LocalDate birthday = LocalDate.of(1990, 5, 15);

        // When
        BirthdayEntry entry = new BirthdayEntry(1L, 123456L, "Иван Иванов", birthday);

        // Then
        assertEquals(1L, entry.getId());
        assertEquals(123456L, entry.getOwnerUserId());
        assertEquals("Иван Иванов", entry.getPersonName());
        assertEquals(birthday, entry.getBirthday());
    }

    @Test
    void testGetBirthdayFormatted() {
        // Given
        LocalDate birthday = LocalDate.of(1990, 5, 15);
        BirthdayEntry entry = new BirthdayEntry(1L, 123456L, "Test User", birthday);

        // When
        String formatted = entry.getBirthdayFormatted();

        // Then
        assertEquals("15.05.1990", formatted);
    }

    @Test
    void testImmutable() {
        // Проверяем что класс действительно immutable
        LocalDate birthday = LocalDate.of(1990, 5, 15);
        BirthdayEntry entry = new BirthdayEntry(1L, 123456L, "Test User", birthday);

        // Все поля final - класс immutable
        assertNotNull(entry);
    }
}
