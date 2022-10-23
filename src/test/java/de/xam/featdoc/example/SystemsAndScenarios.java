package de.xam.featdoc.example;

import de.xam.featdoc.system.Message;
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
        Message receiveBill = CUSTOMER.eventAsync("Receive bill");
    }
    interface MobileOrderClient {
        Message createOrder = MOC.uiAction("Create Order");
        Message addItemToOrder = MOC.uiAction("Add item to running order");
        Message createBill = MOC.apiCall("Create bill");

        static void define() {
            MOC.feature("Mobile Orders")
                    .rule(createOrder, PosSystem.searchOrdersForTable, PosSystem.createOrder)
                    .rule(addItemToOrder, PosSystem.addItemToOrder);
        }
    }

    interface Waiter {
        Message orderEspresso = WAITER.apiCall("Order Espresso");
        Message orderEspressoDouble = WAITER.apiCall("Order Double Espresso");
        Message orderCappuccino = WAITER.apiCall("Order Cappuccino");
        Message customerWantsToPay = WAITER.apiCall("Customer wants to pay");


        static void define() {
            WAITER.feature("Coffee Serving")
                    .rule(orderEspresso, CoffeeMachine.espresso, MobileOrderClient.createOrder, MobileOrderClient.addItemToOrder)
                    .rule(customerWantsToPay, MobileOrderClient.createBill, Customer.receiveBill);
        }


    }

    interface CoffeeMachine {
        Message espresso = CM.apiCall("Make an espresso");
        Message espressoDouble = CM.apiCall("Make a double espresso");
        Message cappuccino = CM.apiCall("Make a cappuccino");

        Message eventBeansConsumed = CM.eventAsync("20g of coffee beans consumed");
        Message eventMilkConsumed = CM.eventAsync("milk consumed");

        static void define() {
            WAITER.feature("Coffee Making") //
                    .rule(espresso, eventBeansConsumed) //
                    .rule(espressoDouble, eventBeansConsumed, eventBeansConsumed) //
                    .rule(cappuccino, eventBeansConsumed, eventMilkConsumed) //
            ;
        }
    }

    interface PosSystem {

        Message createOrder = POS.apiCall("Create order for table");
        Message searchOrdersForTable = POS.apiCall("Search order of given table");
        Message addItemToOrder = POS.apiCall("Add item");
        Message addTaxesToOrder = POS.apiCall("Add taxes");

        static void define() {
            POS.feature("Tax Integration")
                    .rule(addItemToOrder, AccountingSystem.calculateTax, addTaxesToOrder);
        }
    }

    interface AccountingSystem {

        Message calculateTax = ACC.apiCall("Calculate taxes");
        Message reduceInventory = ACC.apiCall("Reduce inventory");

        static void define() {
            ACC.feature("Inventory")
                    .rule(CoffeeMachine.eventBeansConsumed,reduceInventory)
                    .rule(CoffeeMachine.eventMilkConsumed,reduceInventory);
        }
    }

    /**
     *
     */
    public static void defineScenarios() {
        Systems.UNIVERSE.scenario("Lunch-Customer (in a hurry)") //
                .syncCall(CUSTOMER, WAITER, Waiter.orderEspresso)//
                .syncCall(CUSTOMER, WAITER, Waiter.customerWantsToPay) //
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
