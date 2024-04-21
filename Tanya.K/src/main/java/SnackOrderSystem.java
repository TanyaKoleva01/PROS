import java.util.*;
import java.text.DecimalFormat;
import java.math.BigDecimal;
import java.math.RoundingMode;

class Product {
    int id;
    String name;
    double baseCost;
    double markup;
    String promotion;

    public Product(int id, String name, double baseCost, double markup, String promotion) {
        this.id = id;
        this.name = name;
        this.baseCost = baseCost;
        this.markup = markup;
        this.promotion = promotion;
    }

    BigDecimal getStandardPrice() {
        BigDecimal cost = BigDecimal.valueOf(baseCost);
        if (markup <= 1) {
            return cost.add(BigDecimal.valueOf(markup)).setScale(2, RoundingMode.HALF_UP);
        } else {
            // Otherwise, treat markup as a percentage
            BigDecimal markupPercent = BigDecimal.valueOf(markup).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP); // Convert percentage to a decimal
            BigDecimal markupAmount = cost.multiply(markupPercent);
            return cost.add(markupAmount).setScale(2, RoundingMode.HALF_UP);
        }

    }

    BigDecimal calculatePromotionalPrice(int quantity) {
        BigDecimal standardPrice = getStandardPrice();
        if (promotion.equals("")) {
            return standardPrice;
        } else if (promotion.contains("% off")) {
            BigDecimal discount = new BigDecimal(promotion.replace("% off", "")).divide(new BigDecimal("100"), RoundingMode.HALF_UP);
            return standardPrice.multiply(BigDecimal.ONE.subtract(discount)).setScale(5, RoundingMode.HALF_UP);
        } else if (promotion.equals("Buy 2, get 3rd free")) {
            int paidItems =  2 * (quantity / 3) + (quantity % 3);
            BigDecimal totalCost = standardPrice.multiply(new BigDecimal(paidItems));
            return totalCost.divide(new BigDecimal(quantity), 5, RoundingMode.HALF_UP);
        }
        return standardPrice;
    }
}

class Client {
    int id;
    String name;
    double basicDiscount;
    double additionalDiscount10k;
    double additionalDiscount30k;

    public Client(int id, String name, double basicDiscount, double additionalDiscount10k, double additionalDiscount30k) {
        this.id = id;
        this.name = name;
        this.basicDiscount = basicDiscount;
        this.additionalDiscount10k = additionalDiscount10k;
        this.additionalDiscount30k = additionalDiscount30k;
    }
}

public class SnackOrderSystem {

    private static final DecimalFormat priceFormat = new DecimalFormat("#,##0.00");
    private static final DecimalFormat promoPriceFormat = new DecimalFormat("#,##0.00000");

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter order details (format: ClientID,ProductID=Quantity,...):");
        String input = scanner.nextLine();

        // Assuming you have filled in the correct baseCost and markup values
        List<Product> products = Arrays.asList(
                new Product(1, "Danish Muffin", 0.52, 80, " "),
                new Product(2, "Granny’s Cup Cake", 0.38, 120, "30% off"),
                new Product(3, "Frenchy’s Croissant", 0.41, 0.90, " "),
                new Product(4, "Crispy Chips", 0.60, 1.00, "Buy 2, get 3rd free")
        );

        List<Client> clients = Arrays.asList(
                new Client(1, "ABC Distribution", 5, 0, 2),
                new Client(2, "DEF Foods", 4, 1, 2),
                new Client(3, "GHI Trade", 3, 1, 3),
                new Client(4, "JKL Kiosks", 2, 3, 5),
                new Client(5, "MNO Vending", 0, 5, 7)
        );

        try {
            String[] parts = input.split(",");
            int clientId = Integer.parseInt(parts[0].trim());
            Client client = clients.get(clientId - 1);
            System.out.println("\nClient: " + client.name);

            LinkedHashMap<Product, Integer> orderedProducts = new LinkedHashMap<>();
            for (int i = 1; i < parts.length; i++) {
                String[] detail = parts[i].split("=");
                int productId = Integer.parseInt(detail[0].trim());
                int quantity = Integer.parseInt(detail[1].trim());
                Product product = products.get(productId - 1);
                orderedProducts.put(product, quantity);
            }

            printOrderSummary(client, orderedProducts);
        } catch (NumberFormatException e) {
            System.out.println("There was an error in the format of your input. Please check and try again.");
            e.printStackTrace();
        }
    }

    private static void printOrderSummary(Client client, LinkedHashMap<Product, Integer> orderedProducts) {
        System.out.println("Client: " + client.name);
        System.out.printf("%-15s %-10s %-15s %-18s %s\n", "Product", "Quantity", "Standard Unit Price", "Promotional Unit Price", "Line Total");

        BigDecimal totalBeforeDiscounts = BigDecimal.ZERO;
        for (Map.Entry<Product, Integer> entry : orderedProducts.entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            BigDecimal standardPrice = product.getStandardPrice();
            BigDecimal promotionalPrice = product.calculatePromotionalPrice(quantity);
            BigDecimal lineTotal = promotionalPrice.multiply(BigDecimal.valueOf(quantity));

            totalBeforeDiscounts = totalBeforeDiscounts.add(lineTotal);

            System.out.printf("%-15s %-10d EUR %-13s EUR %-16s EUR %s\n",
                    product.name, quantity,
                    priceFormat.format(standardPrice),
                    promoPriceFormat.format(promotionalPrice),
                    priceFormat.format(lineTotal));
        }

        BigDecimal basicDiscountAmount = totalBeforeDiscounts.multiply(BigDecimal.valueOf(client.basicDiscount / 100.0)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAfterBasicDiscount = totalBeforeDiscounts.subtract(basicDiscountAmount);
        BigDecimal additionalDiscount = calculateAdditionalVolumeDiscount(totalAfterBasicDiscount, client);
        BigDecimal orderTotalAmount = totalAfterBasicDiscount.subtract(additionalDiscount);

        System.out.printf("\nTotal Before Client Discounts: EUR %s\n", priceFormat.format(totalBeforeDiscounts));
        System.out.printf("Additional Volume Discount at 7%%: EUR %s\n", priceFormat.format(additionalDiscount));
        System.out.printf("Order Total Amount: EUR %s\n", priceFormat.format(orderTotalAmount));
    }

    static BigDecimal calculateAdditionalVolumeDiscount(BigDecimal totalBeforeDiscounts, Client client) {
        BigDecimal discountRate = BigDecimal.ZERO;
        if (totalBeforeDiscounts.compareTo(BigDecimal.valueOf(30000)) >= 0) {
            discountRate = BigDecimal.valueOf(client.additionalDiscount30k / 100.0);
        } else if (totalBeforeDiscounts.compareTo(BigDecimal.valueOf(10000)) >= 0) {
            discountRate = BigDecimal.valueOf(client.additionalDiscount10k / 100.0);
        }
        return totalBeforeDiscounts.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
    }
}