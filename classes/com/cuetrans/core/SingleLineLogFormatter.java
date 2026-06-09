package com.cuetrans.core;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SingleLineLogFormatter extends Formatter
{
  public String format(LogRecord record)
  {
    return new Date() + "," + record.getMessage() + System.getProperty("line.separator");
  }
}