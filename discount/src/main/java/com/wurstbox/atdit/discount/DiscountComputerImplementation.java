package com.wurstbox.atdit.discount;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 Current Implementation for discount computation (see {@link DiscountComputer}).
 */
public class DiscountComputerImplementation implements DiscountComputer {
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
    List<Discount> result = new ArrayList<>();

    var dbData = getDiscountsFromDB( customer );
    convertDBDataToResult( base, result, dbData );
    addAggregateDiscounts( result );

    return result;
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

    try( Connection connection = getConnection( url, user, password );
         PreparedStatement statement = prepareStatement( connection, customer );
         ResultSet dbQueryResult = statement.executeQuery() ) {
      fillResultList( result, dbQueryResult );
    }
    catch( SQLException e ) {
      throw new RuntimeException( "database access failed" );
    }

    return result;
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
    catch( IOException e ) {
      throw new RuntimeException( "database connection properties could not be found" );
    }
    return dbAccessProperties;
  }

}