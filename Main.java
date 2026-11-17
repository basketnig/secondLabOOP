
import delivery.*;
import discounts.*;
import factories.*;
import orders.*;
import products.Product;
import products.ProductType;
import warehouse.Warehouse;

import java.util.Arrays;
import java.util.Collections;

public class Main {
    public static void main(String[] args) {
        ProductFactory phoneFactory = new SmartphoneFactory();
        ProductFactory laptopFactory = new LaptopFactory();
        ProductFactory jacketFactory = new JacketFactory();
        ProductFactory breadFactory = new BreadFactory();
        ProductFactory milkFactory = new MilkFactory();
        ProductFactory tShirtFactory = new TShirtFactory();

        Warehouse warehouse = new Warehouse(20);
        Product iphone = phoneFactory.createProduct("iPhone 17", 100000, 1, 0.2);
        Product macbook = laptopFactory.createProduct("MacBook Air", 120000, 1, 1.5);
        Product jacket = jacketFactory.createProduct("Кожанка HD", 20000, 2, 1.5);
        Product heavyServer = laptopFactory.createProduct("Серверная стойка", 300000, 1, 25.0);
        Product giftShirt = tShirtFactory.createProduct("Фирменная футболка (Подарок)", 1500, 1, 0.2);

        warehouse.addProduct(iphone);
        warehouse.addProduct(macbook);
        warehouse.addProduct(jacket);
        warehouse.addProduct(heavyServer);

        OrderManager manager = new OrderManager(2, 10);

        System.out.println("сценарий 1: создание заказа и применение процентной скидки");
        Order order1 = manager.createOrder("Марат Матрецов", Arrays.asList(iphone, jacket), "Москва");
        System.out.println("до скидки: " + order1);

        Discount tenPercentOff = new PercentDiscount(10);
        order1.applyDiscount(tenPercentOff);
        System.out.println("после скидки: " + order1);

        System.out.println("\nсценарий 2: выбор доставки через assignTo и работа DeliveryProxy");
        Delivery courierProxy = new DeliveryProxy(new CourierDelivery());
        courierProxy.assignTo(order1);
        System.out.println("повторный запрос цены: " + order1.getDeliveryCost() + " руб.");
        System.out.println("итог по заказу #1: " + order1);

        System.out.println("\nпроверка автопереключения Proxy при перевесе");
        Order heavyOrder = manager.createOrder("Максим Синицын", Collections.singletonList(heavyServer), "Москва");
        Delivery heavyCourierProxy = new DeliveryProxy(new CourierDelivery());
        heavyCourierProxy.assignTo(heavyOrder);
        System.out.println("итог по тяжелому заказу #2: " + heavyOrder);

        System.out.println("\nсценарий 3: смена статуса заказа, уведомление OrderManager, обновление оборота");
        manager.updateStatus(order1.getId(), OrderStatus.PAID);
        manager.updateStatus(order1.getId(), OrderStatus.SHIPPED);
        manager.updateStatus(order1.getId(), OrderStatus.DELIVERED);
        System.out.printf("оборот: %.2f руб.%n",
                manager.getTurnover());

        System.out.println("\nсценарий 4: применение всех 4 видов скидок к одному заказу и проверка итоговой цены");
        Order order3 = manager.createOrder("Никодим Волошин", Collections.singletonList(macbook), "Санкт-Петербург");
        Delivery postProxy = new DeliveryProxy(new PostDelivery());
        postProxy.assignTo(order3);
        System.out.println("до акций: " + order3);

        order3.applyDiscount(new PercentDiscount(10))
                .applyDiscount(new CashbackDiscount(5))
                .applyDiscount(new FreeDeliveryDiscount())
                .applyDiscount(new GiftDiscount(giftShirt));

        System.out.println("после комплекса акций: " + order3);
        System.out.println("состав заказа #3:");
        for (Product item : order3.getItems()) {
            System.out.println("   " + item);
        }
        manager.updateStatus(order3.getId(), OrderStatus.PAID);

        System.out.println("\nсценарий 5: ограничение лимита активных заказов, отмена заказа и освобождение лимита");
        Order o4 = manager.createOrder("Федос Манчестер", Collections.singletonList(breadFactory.createProduct("Хлеб", 50, 2, 0.4)));
        Order o5 = manager.createOrder("Федос Манчестер", Collections.singletonList(milkFactory.createProduct("Молоко", 80, 1, 1.0)));

        try {
            System.out.println("попытка создать 3-й активный заказ для Федоса:");
            manager.createOrder("Федос Манчестер", Collections.singletonList(iphone));
        } catch (IllegalStateException e) {
            System.out.println("перехвачено ожидаемое исключение: " + e.getMessage());
        }

        System.out.println("отменяем заказ #" + o4.getId());
        manager.updateStatus(o4.getId(), OrderStatus.CANCELLED);

        System.out.println("повторная попытка создать новый заказ после отмены предыдущего:");
        Order o6 = manager.createOrder("Федос Манчестер", Collections.singletonList(iphone));
        System.out.println("успешно создан: " + o6);
    }
}