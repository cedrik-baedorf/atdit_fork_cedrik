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

public class DatabaseDiscountDataService implements DiscountDataService {
  private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );
  public static final String URL = "url";
  public static final String USER = "user";
  public static final String PASSWORD = "password";

  @Override
  public List<DiscountDB> getDiscountData( int customer ) {
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
}
