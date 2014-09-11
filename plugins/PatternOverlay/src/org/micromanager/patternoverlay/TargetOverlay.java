package org.micromanager.patternoverlay;

import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.ShapeRoi;

import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.util.prefs.Preferences;


/**
 *  Since this creates an overlay, stored images will not be affected, and
 *  no persistent change is made to the actual image; rather, this adds another
 *  layer on top of the life view window.
 *
 *  @author Jon
 */
public class TargetOverlay extends GenericOverlay {
   
   public TargetOverlay(Preferences prefs, String prefPrefix) {
      super(prefs, prefPrefix);
   }

   @Override
   protected Overlay getOverlay(int width, int height) {
      
      // makes diameters of relative size 1, 3, and 5
      
      double radius = java.lang.Math.min(width, height)/2 * size_/100;
      
      GeneralPath path = new GeneralPath();
      Ellipse2D.Double circle = new Ellipse2D.Double(width/2 - radius, height/2 - radius, 2*radius, 2*radius);
      path.append(circle, false);
      radius = radius*3/5;
      Ellipse2D.Double circle2 = new Ellipse2D.Double(width/2 - radius, height/2 - radius, 2*radius, 2*radius);
      path.append(circle2, false);
      radius = radius/3;
      Ellipse2D.Double circle3 = new Ellipse2D.Double(width/2 - radius, height/2 - radius, 2*radius, 2*radius);
      path.append(circle3, false);
      
      Roi roi = new ShapeRoi(path);
      roi.setStrokeColor(color_);
      return new Overlay(roi);
   }


}

