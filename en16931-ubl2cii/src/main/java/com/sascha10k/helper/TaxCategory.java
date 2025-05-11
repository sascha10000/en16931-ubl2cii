package com.sascha10k.helper;

import java.math.BigDecimal;

public class TaxCategory {
  public String typeCode; // S
  public String taxScheme; // VAT
  public BigDecimal taxPercentage; // e.g. 19
  public BigDecimal taxAmountQuotient; // Quotient of the whole billings sum that is part of this tax category
}
