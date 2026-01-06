import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BirthdayUserTest {

    @Test
    void testBirthdayUserCreation() {
        // Given
        LocalDate birthday = LocalDate.of(1990, 5, 15);

        // When
        BirthdayUser user = new BirthdayUser(1, 123456L, "Иван Иванов", birthday);

        // Then
        assertEquals(123456L, user.getTelegramId());
        assertEquals("Иван Иванов", user.getName());
        assertEquals(birthday, user.getBirthday());
    }

    @Test
    void testGetAge() {
        // Given
        LocalDate birthday = LocalDate.now().minusYears(25);
        BirthdayUser user = new BirthdayUser(1, 123456L, "Test User", birthday);

        // When
        int age = user.getAge();

        // Then
        assertEquals(25, age);
    }

    @Test
    void testGetBirthdayFormatted() {
        // Given
        LocalDate birthday = LocalDate.of(1990, 5, 15);
        BirthdayUser user = new BirthdayUser(1, 123456L, "Test User", birthday);

        // When
        String formatted = user.getBirthdayFormatted();

        // Then
        assertEquals("15.05.1990", formatted);
    }

    @Test
    void testSetters() {
        // Given
        BirthdayUser user = new BirthdayUser(1, 123456L, "Old Name", LocalDate.now());

        // When
        user.setName("New Name");
        user.setTelegramId(999999L);
        LocalDate newBirthday = LocalDate.of(2000, 1, 1);
        user.setBirthday(newBirthday);

        // Then
        assertEquals("New Name", user.getName());
        assertEquals(999999L, user.getTelegramId());
        assertEquals(newBirthday, user.getBirthday());
    }

    @Test
    void testToString() {
        // Given
        LocalDate birthday = LocalDate.of(1990, 5, 15);
        BirthdayUser user = new BirthdayUser(1, 123456L, "Test User", birthday);

        // When
        String result = user.toString();

        // Then
        assertTrue(result.contains("Test User"));
        assertTrue(result.contains("123456"));
        assertTrue(result.contains("1990-05-15"));
    }
}
