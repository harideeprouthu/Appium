package com.influxdb;


import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

public class UpdateResults {

  private static final InfluxDB INFLXUDB = InfluxDBFactory.connect("http://10.102.97.210:8086", "root", "root");
  private static final String DB_NAME = "Browserstack";

  static {
    INFLXUDB.setDatabase(DB_NAME);
  }

  public static void post(final Point point) {
    INFLXUDB.write(point);
  }

}
