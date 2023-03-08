package com.wurstbox.atdit.discount;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

public class DiscountComputerImplementationTest {

  /**
   This method tests whether a customer who does not receive any discount gets no discount.<br/>
   Customer 0 is granted no discount.<br/>
   Tests: {@link DiscountComputerImplementation#computeDiscount(double, int)}<br/>
   Input: base = 100, customer = 0<br/>
   Expected Output: List with one entry (Text | percentage | amount):
   <ol>
   <li>"Aggregate", 0, 0</li>
   </ol>
   */
  @Test
  public void noDiscount() {
    //assemble
    DiscountDataService serviceMock = Mockito.mock( DiscountDataService.class );
    Mockito.when( serviceMock.getDiscountData( 0 ) )
        .thenReturn( new ArrayList<>() );

    var cut = new DiscountComputerImplementation( serviceMock );
    var expected = new ArrayList<Discount>( 1 );
    expected.add( new Discount( "Aggregate", 0, 0 ) );

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
   Expected Output: List with two entries (Text | percentage | amount):
   <ol>
   <li>"Aggregate", 19, 19</li>
   <li>"geschenkte Mehrwertsteuer", 19, 19</li>
   </ol>
   */
  @Test
  public void singleDiscount() {
    //assemble
    ArrayList<DiscountDB> mockDBQueryResult = new ArrayList<>();
    mockDBQueryResult.add( new DiscountDB( 1, 19, "geschenkte Mehrwertsteuer" ) );
    DiscountDataService serviceMock = Mockito.mock( DiscountDataService.class );
    Mockito.when( serviceMock.getDiscountData( 1 ) )
        .thenReturn( mockDBQueryResult );
    var cut = new DiscountComputerImplementation( serviceMock );

    var expected = new ArrayList<Discount>( 2 );
    expected.add( new Discount( "Aggregate", 19, 19 ) );
    expected.add( new Discount( "geschenkte Mehrwertsteuer", 19, 19 ) );

    //act
    var actual = cut.computeDiscount( 100, 1 );

    //assert
    Assertions.assertEquals( expected, actual );
  }

  /**
   This method tests whether a multi-discount customer gets the expected discount.<br/>
   Customer 2 is granted three discounts of 3, 5, and 19%, that is 27% altogether.<br/>
   Tests: {@link DiscountComputerImplementation#computeDiscount(double, int)}<br/>
   Input: base = 100, customer = 2<br/>
   Expected Output: List with four entries (Text | percentage | amount):
   <ol>
   <li>"Aggregate", 19, 19</li>
   <li>"Semesterstart", 5, 5</li>
   <li>"Aktionswochen", 3, 3</li>
   <li>"geschenkte Mehrwertsteuer", 19, 19</li>
   </ol>
   */
  @Test
  public void multipleDiscounts() {
    //assemble
    ArrayList<DiscountDB> mockDBQueryResult = new ArrayList<>();
    mockDBQueryResult.add( new DiscountDB( 1, 5, "Semesterstart" ) );
    mockDBQueryResult.add( new DiscountDB( 2, 3, "Aktionswochen" ) );
    mockDBQueryResult.add( new DiscountDB( 3, 19, "geschenkte Mehrwertsteuer" ) );
    DiscountDataService serviceMock = Mockito.mock( DiscountDataService.class );
    Mockito.when( serviceMock.getDiscountData( 2 ) )
        .thenReturn( mockDBQueryResult );
    var cut = new DiscountComputerImplementation( serviceMock );

    var expected = new ArrayList<Discount>( 4 );
    expected.add( new Discount( "Aggregate", 27, 27 ) );
    expected.add( new Discount( "Semesterstart", 5, 5 ) );
    expected.add( new Discount( "Aktionswochen", 3, 3 ) );
    expected.add( new Discount( "geschenkte Mehrwertsteuer", 19, 19 ) );

    //act
    var actual = cut.computeDiscount( 100, 2 );

    //assert
    Assertions.assertEquals( expected, actual );
  }

}
