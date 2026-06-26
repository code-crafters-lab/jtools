import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LicenseIDGeneratorTest {

    @Test
    public void LicenseIDGenerate() {
        UUID testUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        String licenseId = LicenseIDGenerator.generate(testUuid);
        assertEquals("YD7UO0X7HV", licenseId);
    }
}
