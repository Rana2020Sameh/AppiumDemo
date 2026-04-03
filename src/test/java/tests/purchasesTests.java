package tests;

import org.testng.annotations.Test;
import pages.Purchasepage;
import utils.PurchaseTestData;
import utils.TestDataReader;

public class purchasesTests extends BaseTests {

    // One line loads and parses the JSON into a typed object — IDE autocomplete works on every field
    private static final PurchaseTestData data =
            TestDataReader.load("purchaseTestData", PurchaseTestData.class);

    @Test
    public void additems() {
        Purchasepage prr = new Purchasepage(driver);
        prr.additems();
        prr.proceedpurch();
    }

    @Test(dependsOnMethods = {"additems"})
    public void insertcredintals() {
        Purchasepage prr = new Purchasepage(driver);
        prr.insertcredintails(
                data.login.username,
                data.login.password
        );
    }

    @Test(dependsOnMethods = {"insertcredintals"})
    public void checkoutTest() {
        Purchasepage prr = new Purchasepage(driver);
        prr.checkoutscreen(
                data.checkout.name,
                data.checkout.address,
                data.checkout.city,
                data.checkout.zipCode,
                data.checkout.country
        );
    }

    @Test(dependsOnMethods = {"checkoutTest"})
    public void checkpayment() {
        Purchasepage prr = new Purchasepage(driver);
        prr.checkoutToReviewOrder(
                data.payment.cardName,
                data.payment.cardNumber,
                data.payment.expiryDate,
                data.payment.cvv
        );
    }
}
