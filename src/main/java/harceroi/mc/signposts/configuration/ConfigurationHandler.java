package harceroi.mc.signposts.configuration;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class ConfigurationHandler {
  private static Configuration config;
  private static int distancePerPayment;
  private static int maximumPayment;
  private static int markerMaxUsage;

  public static void init(File file) {
    config = new Configuration(file);
    String category;
    
    category = "Payment";
    config.addCustomCategoryComment(category, "Payments for travelling");
    setDistancePerPayment(config.getInt("distancePerPayment", "Payment", 10000, 0, Integer.MAX_VALUE, "Distance the player can travel for 1 Foodlevel (= half a drumstick in the hungerbar)"));
    setMaximumPayment(config.getInt("maximumPayment", "Payment", 10, 0, 20, "The maximum payment per travel. 0 makes traveling free."));
    
    category = "Marker";
    config.addCustomCategoryComment(category, "Marker settings");
    // markerMaxUsage
    // -1 infinity
    // 0 no usage at all
    setMarkerMaxUsage(config.getInt("makerMaxUsage", category, 5, -1, 10, "How often you can use a marker. -1 for Infinity, 0 for disabling markers."));
    config.save();
  }

  public static int getDistancePerPayment() {
    return distancePerPayment;
  }

  private static void setDistancePerPayment(int distancePerPayment) {
    ConfigurationHandler.distancePerPayment = distancePerPayment;
  }

  public static int getMaximumPayment() {
    return maximumPayment;
  }

  private static void setMaximumPayment(int maximumPayment) {
    ConfigurationHandler.maximumPayment = maximumPayment;
  }

  public static int getMarkerMaxUsage() {
    return markerMaxUsage;
  }

  private static void setMarkerMaxUsage(int markerMaxUsage) {
    ConfigurationHandler.markerMaxUsage = markerMaxUsage;
  }
 
  
}
