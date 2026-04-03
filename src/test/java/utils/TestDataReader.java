package utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

/**
 * Generic JSON → POJO loader for data-driven tests.
 *
 * PATTERN (apply to every new test class):
 *
 *   Step 1 – create  src/test/java/resources/myFeatureTestData.json
 *   Step 2 – create  src/test/java/utils/MyFeatureTestData.java  (POJO)
 *   Step 3 – in your test class, add one line:
 *
 *       private static final MyFeatureTestData data =
 *               TestDataReader.load("myFeatureTestData", MyFeatureTestData.class);
 *
 *   Step 4 – use  data.sectionName.fieldName  — fully type-safe, IDE-autocomplete.
 *
 * That's it. TestDataReader never needs to change again.
 */
public class TestDataReader {

    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final String DATA_DIR =
            System.getProperty("user.dir") + "/src/test/java/resources/";

    /**
     * Deserialises a JSON file into the given POJO type.
     *
     * @param fileName  file name without extension, e.g. "purchaseTestData"
     * @param type      the POJO class to map into, e.g. PurchaseTestData.class
     * @param <T>       inferred from the class you pass in
     * @return          a fully-populated instance of T
     */
    public static <T> T load(String fileName, Class<T> type) {
        String filePath = DATA_DIR + fileName + ".json";
        try {
            return mapper.readValue(new File(filePath), type);
        } catch (IOException e) {
            throw new RuntimeException("Could not load test data file: " + filePath, e);
        }
    }
}
