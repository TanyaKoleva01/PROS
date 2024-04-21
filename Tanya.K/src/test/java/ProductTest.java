import static org.junit.Assert.*;
import org.junit.Test;
import java.math.BigDecimal;


public class ProductTest {

    @Test
    public void testGetStandardPrice_PercentageMarkup() {
        Product product = new Product(1, "Danish Muffin", 0.52, 80, "");
        assertEquals(new BigDecimal("0.94"), product.getStandardPrice());
    }

    @Test
    public void testGetStandardPrice_FixedMarkup() {
        Product product = new Product(3, "Frenchy’s Croissant", 0.41, 0.90, "");
        assertEquals(new BigDecimal("1.31"), product.getStandardPrice());
    }

    @Test
    public void testCalculatePromotionalPrice_NoPromotion() {
        Product product = new Product(1, "Danish Muffin", 0.52, 80, "");
        assertEquals(new BigDecimal("0.94"), product.calculatePromotionalPrice(100));
    }


    @Test
    public void testCalculatePromotionalPrice_Buy2Get1Free() {
        Product product = new Product(4, "Crispy Chips", 0.60, 1.00, "Buy 2, get 3rd free");
        assertEquals(new BigDecimal("1.06667"), product.calculatePromotionalPrice(3));
    }
}
