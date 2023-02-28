package com.wurstbox.atdit.discount;

/**
 <p>Represents discount information</p>
 <p><b>Example:</b> A customer receives a 15% "loyalty discount" on their original purchase value of €200. Accordingly, the
 discount record looks like this:
 <ul>
 <li>description: Loyalty discount</li>
 <li>percentage: 15</li>
 <li>amount: 30</li>
 </ul></p>

 @param description Short description
 @param percentage the percentage discount
 @param amount the calculated discount
 */
public record Discount(String description, double percentage, double amount) { }
