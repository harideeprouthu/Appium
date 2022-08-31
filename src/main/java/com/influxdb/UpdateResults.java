package com.influxdb;


import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

public class UpdateResults {

  private static final InfluxDB INFLXUDB = InfluxDBFactory.connect(Constants.DB_URL, Constants.DB_USER_NAME, Constants.DB_PASSWORD);

  static {
    INFLXUDB.setDatabase(Constants.DB_NAME);
  }

  public static void post(final Point point) {
    INFLXUDB.write(point);
  }

}
