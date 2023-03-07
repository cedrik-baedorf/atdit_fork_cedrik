package com.wurstbox.atdit.discount;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 Current Implementation for discount computation (see {@link DiscountComputer}).
 */
public class DiscountComputerImplementation implements DiscountComputer {
  private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );
  public static final String URL = "url";
  public static final String USER = "user";
  public static final String PASSWORD = "password";

  DiscountComputerImplementation() {
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

    var rawData = getDiscountsFromDB( customer );
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

  private List<DiscountDB> getDiscountsFromDB( int customer ) {
    List<DiscountDB> result = new ArrayList<>();

    Properties dbAccessProperties = getDbAccessProperties();

    String url = dbAccessProperties.getProperty( "url" );
    String user = dbAccessProperties.getProperty( "user" );
    String password = dbAccessProperties.getProperty( "password" );
    logMissingParameters( url, user, password );

    try( Connection connection = getConnection( url, user, password );
        PreparedStatement statement = prepareStatement( connection, customer );
        ResultSet dbQueryResult = statement.executeQuery() ) {
      fillResultList( result, dbQueryResult );
    }
    catch( SQLException e ) {
      final String msg = "database access failed";
      log.error( msg, e );
      throw new RuntimeException( msg );
    }

    return result;
  }

  private void logMissingParameters( String url, String user, String password ) {
    String msg = "database access property not maintained: {}";
    if( url == null )
      log.error( msg, URL );
    if( user == null )
      log.error( msg, USER );
    if( password == null )
      log.error( msg, PASSWORD );
  }

  private void fillResultList( List<DiscountDB> result, ResultSet dbQueryResult ) throws SQLException {
    while( dbQueryResult.next() ) {
      int discountID = dbQueryResult.getInt( "discount_id" );
      double discount = dbQueryResult.getDouble( "discount" );
      String discountText = dbQueryResult.getString( "discount_text" );

      DiscountDB resultLine = new DiscountDB( discountID, discount, discountText );
      result.add( resultLine );
    }
  }

  private PreparedStatement prepareStatement( Connection connection, int customer ) throws SQLException {
    PreparedStatement result = connection.prepareStatement(
        """
        SELECT d.discount_id, d.discount, d.discount_text
          FROM discount AS d
            INNER JOIN customer_discount cd ON d.discount_id = cd.discount_id
          WHERE customer_id = ?
        """
    );
    result.setInt( 1, customer );
    return result;
  }

  private Connection getConnection( String url, String user, String password ) throws SQLException {
    return DriverManager.getConnection( url, user, password );
  }

  private Properties getDbAccessProperties() {
    Properties dbAccessProperties;

    try( InputStream is = getClass().getClassLoader().getResourceAsStream( "db.properties" ) ) {
      dbAccessProperties = new Properties();
      dbAccessProperties.load( is );
    }
    catch( IOException | IllegalArgumentException | NullPointerException e ) {
      final String msg = "Loading database connection properties failed";
      log.error( msg, e );
      throw new RuntimeException( msg );
    }
    return dbAccessProperties;
  }

}