package com.wurstbox.atdit.discount;

import java.util.List;

/**
 Represents the API for a discount computation provided by this module.
 */
public interface DiscountComputer {

  /**
   Factory method. This method instantiates a DiscountComputer. It guarantees that the correct DiscountComputer corresponding to
   the release is instantiated.

   @return The appropriate DiscountComputer
   */
  @SuppressWarnings( "unused" )
  static DiscountComputer get() {
    return new DiscountComputerImplementation(
        new DatabaseDiscountDataService() );
  }

  /**
   <p>Calculates the discount a customer receives on a given base price. The discounts are determined by customer number. Invalid
   customer numbers are granted no discount.<br/>
   Discounts are returned in a list of {@link Discount discounts}. The first index contains the aggregated discount, all following
   lines contain single discounts of which the aggregated discount consists.</p>

   <p>Example: Two individual discounts are granted for a net price of 200.
   <ul>
   <li>1% Discount A</li>
   <li>3% Discount B</li>
   </ul>
   The result will contain three lines (text | percentage | amount).
   <ol>
   <li>Aggregate | 4 | 8 </li>
   <li>Discount A | 1 | 2 </li>
   <li>Discount B | 3 | 6 </li>
   </ol>
   </p>

   @param base the base price
   @param customer the customer number

   @return the discounts: index 0 is the aggregation of index 1..(size-1).
   */
  List<Discount> computeDiscount( double base, int customer );
}
