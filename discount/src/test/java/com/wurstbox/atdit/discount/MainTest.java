package com.wurstbox.atdit.discount;

import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MainTest {

  /**
   This method tests whether a customer who does not receive any discount gets no discount.<br/>
   Customer 0 is granted no discount.<br/>
   Tests: {@link DiscountComputerImplementation#computeDiscount(double, int)}<br/>
   Input: base = 100, customer = 0<br/>
   Expected Output: 0
   */
  @Test
  public void noDiscount() {
    //assemble
    var cut = new DiscountComputerImplementation();
    var expected = new ArrayList<>();
    expected.add(0, new Discount("Aggregate", 0.0, 0.0));

    //act
    var actual = cut.computeDiscount( 100, 0 );

    //assert
    Assertions.assertEquals( expected, actual );
  }

  /**
   This method tests whether a single-discount customer gets the expected discount.<br/>
   Customer 1 is granted one single discount of 19%.<br/>
   Tests: {@link DiscountComputerImplementation#computeDiscount(double, int)}<br/>
   Input: base = 100, customer = 1<br/>
   Expected Output: 19
   */
  @Test
  public void singleDiscount() {
    //assemble
    var cut = new DiscountComputerImplementation();
    var expected = new ArrayList<>();
    expected.add(new Discount("geschenkte Mehrwertsteuer",19.0, 19.0));
    expected.add(0, new Discount("Aggregate", 19.0, 19.0));

    //act
    var actual = cut.computeDiscount( 100.0, 1 );

    //assert
    Assertions.assertEquals( expected, actual );
  }

  /**
   This method tests whether a multi-discount customer gets the expected discount.<br/>
   Customer 2 is granted three discounts of 3, 5, and 19%, that is 27% altogether.<br/>
   Tests: {@link DiscountComputerImplementation#computeDiscount(double, int)}<br/>
   Input: base = 100, customer = 2<br/>
   Expected Output: 27
   */
  @Test
  public void multipleDiscounts() {
    //assemble
    var cut = new DiscountComputerImplementation();
    var expected = new ArrayList<>();
    expected.add(new Discount("Semesterstart", 5.0, 5.0));
    expected.add(new Discount("Aktionswochen", 3.0, 3.0));
    expected.add(new Discount("geschenkte Mehrwertsteuer", 19.0, 19.0));
    expected.add(0, new Discount("Aggregate", 27.0, 27.0));

    //act
    var actual = cut.computeDiscount( 100, 2 );

    //assert
    Assertions.assertEquals( expected, actual );
  }

}
