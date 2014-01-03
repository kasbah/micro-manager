///////////////////////////////////////////////////////////////////////////////
//FILE:          Joystick.java
//PROJECT:       Micro-Manager 
//SUBSYSTEM:     ASIdiSPIM plugin
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nico Stuurman, Jon Daniels
//
// COPYRIGHT:    University of California, San Francisco, & ASI, 2013
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.

package org.micromanager.asidispim.Data;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.micromanager.utils.ReportingUtils;

// TODO implement fast axis reverse checkbox (may need device adapter change and possibly even firmware-level change)

/**
 * Class that holds utilities related to joystick function
 * 
 * @author Jon
 */
public class Joystick {
   
   private Devices devices_;   // object holding information about selected/available devices
   private Properties props_;  // object handling all property read/writes
   
   public Joystick(Devices devices, Properties props) {
      devices_ = devices;
      props_ = props;
   }
   
   // useful static constants 

   public static enum Keys {
      JOYSTICK("Joystick"), 
      JOYSTICK_X("JoystickX"),
      JOYSTICK_Y("JoystickY"),
      RIGHT_WHEEL("Right Wheel"),
      LEFT_WHEEL("Left Wheel"),
      NONE("None");
      private final String text;
      Keys(String text) {
         this.text = text;
      }
      public String toString() {
         return text;
      }
   };
   
   public static enum Directions {
      X("X"), Y("Y"), NONE("");
      private final String text;
      Directions(String text) {
         this.text = text;
      }
      public String toString() {
         return text;
      }
   };

   private static final Map<Joystick.Keys, String> VALUES =
         new EnumMap<Keys, String>(Keys.class);
   static {
      VALUES.put(Keys.NONE, Properties.Values.JS_NONE.toString());
      VALUES.put(Keys.JOYSTICK_X, Properties.Values.JS_X.toString());
      VALUES.put(Keys.JOYSTICK_Y, Properties.Values.JS_Y.toString());
      VALUES.put(Keys.RIGHT_WHEEL, Properties.Values.JS_RIGHT_WHEEL.toString());
      VALUES.put(Keys.LEFT_WHEEL, Properties.Values.JS_LEFT_WHEEL.toString());
   }
   
   /**
    * associative class to store information about axis for joystick combo boxes.
    * Contains string shown in combo box, key of corresponding device, and which
    *  direction is to be selected in the case of 1D control of 2D device
    */
   public static class JSAxisData {
      public String displayString;
      public Devices.Keys deviceKey;
      public Directions direction;
      
      /**
       * @param displayString string used in joystick drop-down
       * @param deviceKey enum from Devices.Keys for the device
       * @param direction enum for which axis of multiaxis device, or NONE if single axis or not applicable
       */
      public JSAxisData(String displayString, Devices.Keys deviceKey, Directions direction) {
         this.displayString = displayString;
         this.deviceKey = deviceKey;
         this.direction = direction;
      }
      
      public boolean equals(JSAxisData a) {
         return (this.displayString.equals(a.displayString) && this.direction==a.direction && this.deviceKey==a.deviceKey);
      }
      
      public int hashCode() {
         return (this.displayString.hashCode() + this.deviceKey.toString().hashCode() + this.direction.toString().hashCode());
      }
   }
   
   /**
    * used to generate selection list for joystick wheel combo list
    * order: none, devices in JOYSTICK1DSET, then individual axes of devices in JOYSTICK2DSET
    * @return array with representative strings of 1 axis stages plus individual axes of 2-axis stages/galvos
    */
   public JSAxisData[] getWheelJSAxisData() {
      List<JSAxisData> list = new ArrayList<JSAxisData>();
      list.add(new JSAxisData(devices_.getDeviceDisplayWithAxis(Devices.Keys.NONE), Devices.Keys.NONE, Directions.NONE));  // adds "None" to top of list
      for (Devices.Keys devKey : Devices.STAGES1D) {
         if (devices_.getMMDevice(devKey)!=null) {
            String dispKey = devices_.getDeviceDisplayWithAxis(devKey); 
            list.add(new JSAxisData(dispKey, devKey, Directions.NONE));
         }
      }
      for (Devices.Keys devKey : Devices.STAGES2D) {
         if (devKey == Devices.Keys.XYSTAGE) { 
            continue;  // don't include the XY stage on wheel list since the device adapter doesn't support that
         }
         if (devices_.getMMDevice(devKey)!=null) {
            Directions[] dirs = {Directions.X, Directions.Y};
            for (Directions dir : dirs) {  // gets for X and Y for now
               String dispKey = devices_.getDeviceDisplayWithAxis(devKey) + "-" + dir.toString();
               list.add(new JSAxisData(dispKey, devKey, dir));
            }
         }
      }
      List<JSAxisData> noduplicates = new ArrayList<JSAxisData>(new LinkedHashSet<JSAxisData>(list));
      return noduplicates.toArray(new JSAxisData[0]);
   }
   
   /**
    * used to generate selection list for joystick stick combo list
    * @return array with representative strings of 2 axis stages
    */
   public JSAxisData[] getStickJSAxisData() {
      List<JSAxisData> list = new ArrayList<JSAxisData>();
      list.add(new JSAxisData(devices_.getDeviceDisplayWithAxis(Devices.Keys.NONE), Devices.Keys.NONE, Directions.NONE));
      for (Devices.Keys devKey : Devices.STAGES2D) {
         if (devices_.getMMDevice(devKey)!=null) {
            String dispKey = devices_.getDeviceDisplayWithAxis(devKey);
            list.add(new JSAxisData(dispKey, devKey, Directions.NONE));
         }
      }
      List<JSAxisData> noduplicates = new ArrayList<JSAxisData>(new LinkedHashSet<JSAxisData>(list));
      return noduplicates.toArray(new JSAxisData[0]);
   }
      
   /**
    * Disables all joysticks by going through all devices and disabling joysticks on them.
    * Loops over all devices that could be associated with joysticks and sets appropriate property.
    */
   public void unsetAllJoysticks() {
      try {
         for (Devices.Keys dev : Devices.STAGES1D) {
            props_.setPropValue(dev, Properties.Keys.JOYSTICK_INPUT, VALUES.get(Joystick.Keys.NONE), true);
         }
         for (Devices.Keys dev : Devices.STAGES2D) {
            if (props_.hasProperty(dev, Properties.Keys.JOYSTICK_ENABLED, true)) { // if XY stage
               props_.setPropValue(dev, Properties.Keys.JOYSTICK_ENABLED, Properties.Values.NO.toString(), true);
            }
            else {  // must be micro-mirror
               props_.setPropValue(dev, Properties.Keys.JOYSTICK_INPUT_X, VALUES.get(Joystick.Keys.NONE), true);
               props_.setPropValue(dev, Properties.Keys.JOYSTICK_INPUT_Y, VALUES.get(Joystick.Keys.NONE), true);
            }
         }
      } catch (Exception ex) {
         ReportingUtils.showError("Problem clearing all joysticks");
      }
   }
   
   /**
    * If any device is attached to the specified joystick then it clears that association.
    * Loops over all devices that could be associated with joysticks and sets appropriate property.
    */
   public void unsetJoystick(Joystick.Keys jkey) {
      try {
         for (Devices.Keys dev : Devices.STAGES1D) {
            if (props_.getPropValueString(dev, Properties.Keys.JOYSTICK_INPUT, true).equals(VALUES.get(jkey))) {
               props_.setPropValue(dev, Properties.Keys.JOYSTICK_INPUT, VALUES.get(Joystick.Keys.NONE));
            }
         }
         for (Devices.Keys dev : Devices.STAGES2D) {
            if (props_.hasProperty(dev, Properties.Keys.JOYSTICK_ENABLED, true)) { // if XY stage
               // don't do anything at the moment for XY stages
            }
            else {  // must be micro-mirror
               if (props_.getPropValueString(dev, Properties.Keys.JOYSTICK_INPUT_X, true).equals(VALUES.get(jkey))) {
                  props_.setPropValue(dev, Properties.Keys.JOYSTICK_INPUT_X, VALUES.get(Joystick.Keys.NONE));
               }
               if (props_.getPropValueString(dev, Properties.Keys.JOYSTICK_INPUT_Y, true).equals(VALUES.get(jkey))) {
                  props_.setPropValue(dev, Properties.Keys.JOYSTICK_INPUT_Y, VALUES.get(Joystick.Keys.NONE));
               }
            }
         }
      } catch (Exception ex) {
         ReportingUtils.showError("Problem clearing joystick");
      }
   }
      
   /**
    * De-selects the joystick input according to the specified device and JSAxisData.
    * If this code is modified then probably also need to modify setJoystickOutput
    * @param jkey
    * @param JSAxisData
    */
   public void unsetJoystick(Joystick.Keys jkey, JSAxisData JSAxisData) {
      Devices.Keys dev = JSAxisData.deviceKey;
      if (dev == Devices.Keys.NONE) {
         return;
      }
      try {
         if (jkey==Joystick.Keys.JOYSTICK) {  // setting the joystick, not a wheel
            if (props_.hasProperty(dev, Properties.Keys.JOYSTICK_ENABLED)) { // if XY stage
               props_.setPropValue(dev, Properties.Keys.JOYSTICK_ENABLED, "No");
            }
            else {  // must be micro-mirror
               props_.setPropValue(dev, Properties.Keys.JOYSTICK_INPUT_X, VALUES.get(Joystick.Keys.NONE));
               props_.setPropValue(dev, Properties.Keys.JOYSTICK_INPUT_Y, VALUES.get(Joystick.Keys.NONE));
            }
         }
         else { // must be either right or left wheel
            Properties.Keys prop = null;
            switch (JSAxisData.direction) {
            case X:    prop = Properties.Keys.JOYSTICK_INPUT_X; break;
            case Y:    prop = Properties.Keys.JOYSTICK_INPUT_Y; break;
            case NONE: prop = Properties.Keys.JOYSTICK_INPUT; break;
            default:   prop = Properties.Keys.JOYSTICK_INPUT; break;
            }
            props_.setPropValue(dev, prop, VALUES.get(Joystick.Keys.NONE));
         }
      } catch (Exception ex) {
         ReportingUtils.showError("Problem unsetting joysticks for " + jkey.toString() + " in device " + dev.toString());
      }
   }
   
   /**
    * Selects the joystick input according to the specified device and JSAxisData.
    * If this code is modified then probably also need to modify unsetJoystickOutput
    * @param jkey
    * @param JSAxisData
    */
   public void setJoystick(Joystick.Keys jkey, JSAxisData JSAxisData) {
      Devices.Keys dev = JSAxisData.deviceKey;
      if (dev == Devices.Keys.NONE) {
         return;
      }
      try {
         if (jkey==Joystick.Keys.JOYSTICK) {  // setting the joystick, not a wheel
            if (props_.hasProperty(dev, Properties.Keys.JOYSTICK_ENABLED)) { // if XY stage
               props_.setPropValue(dev, Properties.Keys.JOYSTICK_ENABLED, "Yes");
            }
            else {  // must be micro-mirror
               props_.setPropValue(dev, Properties.Keys.JOYSTICK_INPUT_X, VALUES.get(Joystick.Keys.JOYSTICK_X));
               props_.setPropValue(dev, Properties.Keys.JOYSTICK_INPUT_Y, VALUES.get(Joystick.Keys.JOYSTICK_Y));
            }
         }
         else { // must be either right or left wheel
            Properties.Keys prop;
            switch (JSAxisData.direction) {
               case X:    prop = Properties.Keys.JOYSTICK_INPUT_X; break;
               case Y:    prop = Properties.Keys.JOYSTICK_INPUT_Y; break;
               case NONE: prop = Properties.Keys.JOYSTICK_INPUT; break;
               default:   prop = Properties.Keys.JOYSTICK_INPUT; break;
            }
            props_.setPropValue(dev, prop, VALUES.get(jkey));
         }
      } catch (Exception ex) {
         ReportingUtils.showError("Problem setting joysticks for " + jkey.toString() + " in device " + dev.toString());
      }
   }
  
}
