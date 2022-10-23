package de.xam.featdoc.example;

import de.xam.featdoc.system.Event;
import de.xam.featdoc.system.System;
import de.xam.featdoc.system.Universe;

import static de.xam.featdoc.example.SystemsAndScenarios.Systems.ACC;
import static de.xam.featdoc.example.SystemsAndScenarios.Systems.CM;
import static de.xam.featdoc.example.SystemsAndScenarios.Systems.CUSTOMER;
import static de.xam.featdoc.example.SystemsAndScenarios.Systems.MOC;
import static de.xam.featdoc.example.SystemsAndScenarios.Systems.POS;
import static de.xam.featdoc.example.SystemsAndScenarios.Systems.WAITER;


public class SystemsAndScenarios {

    interface Systems {
        Universe UNIVERSE = new Universe();
        System CUSTOMER = UNIVERSE.system("customer", "Customer", "Customer");
        System WAITER = UNIVERSE.system("waiter", "Waiter", "Waiter");
        System CM = UNIVERSE.system("coffee", "Coffee Machine", "CoffeeMachine");
        System MOC = UNIVERSE.system("moc", "Mobile Order Client", "Mobile");
        System POS = UNIVERSE.system("pos", "Point of Sale System", "POS");
        System ACC = UNIVERSE.system("accounting", "Accounting System", "Accounting");
    }

    interface Customer {
        Event receiveBill = CUSTOMER.eventAsync("Receive bill");
    }
    interface MobileOrderClient {
        Event createOrder = MOC.uiAction("Create Order");
        Event addItemToOrder = MOC.uiAction("Add item to running order");
        Event createBill = MOC.apiCall("Create bill");

        static void define() {
            MOC.feature("Mobile Orders")
                    .rule(createOrder, PosSystem.searchOrdersForTable, PosSystem.createOrder)
                    .rule(addItemToOrder, PosSystem.addItemToOrder);
        }
    }

    interface Waiter {
        Event orderEspresso = WAITER.apiCall("Order Espresso");
        Event orderEspressoDouble = WAITER.apiCall("Order Double Espresso");
        Event orderCappuccino = WAITER.apiCall("Order Cappuccino");
        Event event_customerWantsToPay = WAITER.apiCall("Customer wants to pay");


        static void define() {
            WAITER.feature("Coffee Serving")
                    .rule(orderEspresso, CoffeeMachine.coffee_espresso, MobileOrderClient.createOrder, MobileOrderClient.addItemToOrder)
                    .rule(event_customerWantsToPay, MobileOrderClient.createBill, Customer.receiveBill);
        }


    }

    interface CoffeeMachine {
        Event coffee_espresso = CM.apiCall("Make an espresso");
        Event coffee_espressoDouble = CM.apiCall("Make a double espresso");
        Event coffee_cappuccino = CM.apiCall("Make a cappuccino");

        Event coffee_event_beansConsumed = CM.eventAsync("20g of coffee beans consumed");
        Event coffee_event_milkConsumed = CM.eventAsync("milk consumed");

        static void define() {
            WAITER.feature("Coffee Making") //
                    .rule(coffee_espresso, coffee_event_beansConsumed) //
                    .rule(coffee_espressoDouble, coffee_event_beansConsumed, coffee_event_beansConsumed) //
                    .rule(coffee_cappuccino, coffee_event_beansConsumed, coffee_event_milkConsumed) //
            ;
        }
    }

    interface PosSystem {

        Event createOrder = POS.apiCall("Create order for table");
        Event searchOrdersForTable = POS.apiCall("Search order of given table");
        Event addItemToOrder = POS.apiCall("Add item");
        Event addTaxesToOrder = POS.apiCall("Add taxes");

        static void define() {
            POS.feature("Tax Integration")
                    .rule(addItemToOrder, AccountingSystem.calculateTax, addTaxesToOrder);
        }
    }

    interface AccountingSystem {

        Event calculateTax = ACC.apiCall("Calculate taxes");
        Event reduceInventory = ACC.apiCall("Reduce inventory");

        static void define() {
            ACC.feature("Inventory")
                    .rule(CoffeeMachine.coffee_event_beansConsumed,reduceInventory)
                    .rule(CoffeeMachine.coffee_event_milkConsumed,reduceInventory);
        }
    }

    /**
     *
     */
    public static void defineScenarios() {
        Systems.UNIVERSE.scenario("Lunch-Customer (in a hurry)") //
                .syncCall(CUSTOMER, WAITER, Waiter.orderEspresso)//
                .syncCall(CUSTOMER, WAITER, Waiter.event_customerWantsToPay) //
        ;
    }

    public static void defineSystems() {
        // this could later be simplified by using annotations, e.g. @Feature (not yet available)
        MobileOrderClient.define();
        Waiter.define();
        CoffeeMachine.define();
        PosSystem.define();
        AccountingSystem.define();
    }

}
