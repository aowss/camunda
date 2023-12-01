package com.micasa.tutorial.services;


public class CustomerService {

    public double deductCredit(double customerCredit, double amount) {
        System.out.println(STR. "Deducting \{ amount } from available credit \{ customerCredit }" );
        return customerCredit > amount ? 0.0 : amount - customerCredit;
    }

}
