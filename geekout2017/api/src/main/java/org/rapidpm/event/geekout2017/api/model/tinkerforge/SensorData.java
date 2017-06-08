package org.rapidpm.event.geekout2017.api.model.tinkerforge;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 *
 */
public class SensorData implements Serializable {

  private String masterUID;      //master abc
  private String brickletUID;    // sensor xyz
  public int deviceIdentifier;   // type
  private ZonedDateTime zonedDateTime;
  private Integer sensorValue;


  public SensorData(String masterUID, String brickletUID, int deviceIdentifier,
                    ZonedDateTime zonedDateTime, Integer sensorValue) {
    this.masterUID = masterUID;
    this.brickletUID = brickletUID;
    this.deviceIdentifier = deviceIdentifier;
    this.zonedDateTime = zonedDateTime;
    this.sensorValue = sensorValue;
  }

  public String getMasterUID() {
    return masterUID;
  }

  public String getBrickletUID() {
    return brickletUID;
  }

  public int getDeviceIdentifier() {
    return deviceIdentifier;
  }

  public ZonedDateTime getZonedDateTime() {
    return zonedDateTime;
  }

  public Integer getSensorValue() {
    return sensorValue;
  }

  @Override
  public String toString() {
    return "SensorData{" +
           "masterUID='" + masterUID + '\'' +
           ", brickletUID='" + brickletUID + '\'' +
           ", deviceIdentifier=" + deviceIdentifier +
           ", zonedDateTime=" + zonedDateTime +
           ", sensorValue=" + sensorValue +
           '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (! (o instanceof SensorData)) return false;

    SensorData that = (SensorData) o;

    if (deviceIdentifier != that.deviceIdentifier) return false;
    if (masterUID != null ? ! masterUID.equals(that.masterUID) : that.masterUID != null) return false;
    if (brickletUID != null ? ! brickletUID.equals(that.brickletUID) : that.brickletUID != null) return false;
    if (zonedDateTime != null ? ! zonedDateTime.equals(that.zonedDateTime) : that.zonedDateTime != null) return false;
    return sensorValue != null ? sensorValue.equals(that.sensorValue) : that.sensorValue == null;
  }

  @Override
  public int hashCode() {
    int result = masterUID != null ? masterUID.hashCode() : 0;
    result = 31 * result + (brickletUID != null ? brickletUID.hashCode() : 0);
    result = 31 * result + deviceIdentifier;
    result = 31 * result + (zonedDateTime != null ? zonedDateTime.hashCode() : 0);
    result = 31 * result + (sensorValue != null ? sensorValue.hashCode() : 0);
    return result;
  }
}
