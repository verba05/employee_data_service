package verba.employee_data_service.converters;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SystemStubsExtension.class)
class EncryptionConverterTests {

    @SystemStub
    private EnvironmentVariables environmentVariables;

    private static String randomBase64Key() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    @Test
    void convertToDatabaseColumn_encryptsValueSuccessfully() {
        environmentVariables.set("ENCRYPTION_KEY", randomBase64Key());
        EncryptionConverter converter = new EncryptionConverter();

        String plainText = "123-45-6789";
        String encrypted = converter.convertToDatabaseColumn(plainText);

        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);
    }

    @Test
    void convertToEntityAttribute_decryptsValueSuccessfully() {
        environmentVariables.set("ENCRYPTION_KEY", randomBase64Key());
        EncryptionConverter converter = new EncryptionConverter();

        String plainText = "123-45-6789";
        String encrypted = converter.convertToDatabaseColumn(plainText);
        String decrypted = converter.convertToEntityAttribute(encrypted);

        assertEquals(plainText, decrypted);
    }

    @Test
    void convertToDatabaseColumn_withInvalidKeyLength_throwsIllegalStateException() {
        byte[] invalidKey = new byte[10];
        environmentVariables.set("ENCRYPTION_KEY", Base64.getEncoder().encodeToString(invalidKey));

        EncryptionConverter converter = new EncryptionConverter();

        assertThrows(IllegalStateException.class,
                () -> converter.convertToDatabaseColumn("123-45-6789"));
    }

    @Test
    void convertToEntityAttribute_withTamperedCiphertext_throwsIllegalStateException() {
        environmentVariables.set("ENCRYPTION_KEY", randomBase64Key());
        EncryptionConverter converter = new EncryptionConverter();

        String encrypted = converter.convertToDatabaseColumn("123-45-6789");
        byte[] raw = Base64.getDecoder().decode(encrypted);
        raw[raw.length - 1] ^= 0x01; // flip a bit, breaks the GCM auth tag
        String tampered = Base64.getEncoder().encodeToString(raw);

        assertThrows(IllegalStateException.class,
                () -> converter.convertToEntityAttribute(tampered));
    }

    @Test
    void constructor_withMissingKey_throwsIllegalStateException() {
        environmentVariables.set("ENCRYPTION_KEY", null);
        assertThrows(IllegalStateException.class, EncryptionConverter::new);
    }
}