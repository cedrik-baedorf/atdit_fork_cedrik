package com.wurstbox.atdit.discount;

import java.util.List;

public interface DiscountDataService {
  List<DiscountDB> getDiscountData( int customer );
}
