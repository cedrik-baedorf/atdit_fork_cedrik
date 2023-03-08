package com.wurstbox.atdit.discount;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;


/**
 Current Implementation for discount computation (see {@link DiscountComputer}).
 */
public class DiscountComputerImplementation implements DiscountComputer {
  private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );
  private final DiscountDataService discountDataService;

  DiscountComputerImplementation( DiscountDataService discountDataService ) {
    this.discountDataService = discountDataService;
  }

  /**
   @implSpec To compute the discount, the customer number is looked up in database. Associated discounts are retrieved and
   computed according to the rules defined in {@link DiscountComputer}.
   A database must be available and running. Connection details must be provided in "db.properties". Required properties are:
   <ul>
   <li>url: database connection string except for user and passwort</li>
   <li>user: db user name</li>
   <li>password: db password</li>
   </ul>
   */
  @Override
  public List<Discount> computeDiscount( double base, int customer ) {
    logMethodCall( base, customer );
    List<Discount> result = new ArrayList<>();

    var rawData = discountDataService.getDiscountData( customer );
    logDiscountRawData( rawData );

    convertDBDataToResult( base, result, rawData );
    logComputedDiscounts( result );

    addAggregateDiscounts( result );
    logAggregatedDiscount( result );

    return result;
  }

  private void logAggregatedDiscount( List<Discount> result ) {
    if( !log.isInfoEnabled() )
      return;

    log.info( "customer receives {}% discount or {}€",
              result.get( 0 ).percentage(),
              result.get( 0 ).amount() );
  }

  private void logComputedDiscounts( List<Discount> result ) {
    if( !log.isDebugEnabled() )
      return;

    log.debug( "raw data conversion result: " );
    result.forEach( d -> log.debug( d.toString() ) );
  }


  private void logDiscountRawData( List<DiscountDB> rawData ) {
    if( !log.isDebugEnabled() )
      return;

    log.debug( "discount raw data found: " );
    rawData.forEach( d -> log.debug( d.toString() ) );
  }

  private void logMethodCall( double base, int customer ) {
    if( !log.isInfoEnabled() )
      return;

    log.info( "compute discount for customer {} and base price {}", customer, base );
  }

  private void addAggregateDiscounts( List<Discount> result ) {
    Discount aggregate = new Discount( "Aggregate", 0, 0 );
    for( Discount d : result ) {
      aggregate = new Discount(
          aggregate.description(),
          aggregate.percentage() + d.percentage(),
          aggregate.amount() + d.amount() );
    }
    result.add( 0, aggregate );
  }

  private void convertDBDataToResult( double base, List<Discount> result, List<DiscountDB> dbData ) {
    for( DiscountDB discountDB : dbData ) {
      double discountValue = base * discountDB.discount() / 100;
      Discount discount = new Discount(
          discountDB.discountText(),
          discountDB.discount(),
          discountValue );
      result.add( discount );
    }
  }

}