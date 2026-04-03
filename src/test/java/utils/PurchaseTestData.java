package utils;

/**
 * POJO that mirrors purchaseTestData.json.
 *
 * PATTERN: One POJO per feature/test-class.
 *  - Each nested static class matches a JSON section.
 *  - Field names must match JSON keys exactly.
 *  - Jackson fills everything in automatically via TestDataReader.load().
 *
 * To add a new field: add it to the JSON file AND here — that's it.
 */
public class PurchaseTestData {

    public Login    login;
    public Checkout checkout;
    public Payment  payment;

    public static class Login {
        public String username;
        public String password;
    }

    public static class Checkout {
        public String name;
        public String address;
        public String city;
        public String zipCode;
        public String country;
    }

    public static class Payment {
        public String cardName;
        public String cardNumber;
        public String expiryDate;
        public String cvv;
    }
}
