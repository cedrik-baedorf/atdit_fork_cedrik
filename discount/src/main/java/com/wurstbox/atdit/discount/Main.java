package com.wurstbox.atdit.discount;

import java.sql.*;

public class Main {
  private static final String url = "jdbc:mariadb://localhost:3306/atdit";
  private static final String user = "atdit_user";
  private static final String password = "LaXI8i3abe1aluYoRAC3nAhIyiJ6hi";

  public double computeDiscount( double base, int customer ) {
    Connection c = null;
    PreparedStatement s = null;
    double result = 0;
    ResultSet r = null;

    try {
      c = DriverManager.getConnection( url, user, password );

      s = c.prepareStatement(
          """
            SELECT d.discount_id, d.discount, d.discount_text
              FROM discount AS d
                INNER JOIN customer_discount cd ON d.discount_id = cd.discount_id
              WHERE customer_id = ?
          """ );
      s.setInt( 1, customer );
      r = s.executeQuery();

      while( r.next() ) {
        int id = r.getInt( 1 );
        double val1 = r.getDouble( 2 ); // discount_text
        String val2 = r.getString( 3 ); // discount

        var discount = base * val1 / 100;
        System.out.println( "Granted " + val1 + " % " + val2 + " discount: " + discount + " €" );
        result += discount;
      }

      r.close();
      s.close();
      c.close();
    }
    catch( Exception sql ) { }

    return result;
  }
}
