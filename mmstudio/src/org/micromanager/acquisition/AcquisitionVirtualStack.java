/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.micromanager.acquisition;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import java.awt.image.ColorModel;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmcorej.TaggedImage;
import org.micromanager.utils.ImageUtils;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.ReportingUtils;

/**
 *
 * @author arthur
 */
public class AcquisitionVirtualStack extends ij.VirtualStack {

   private MMImageCache imageCache_;
   private HashMap<Integer,UUID> uuids_ = new HashMap();
   
   protected int width_, height_, type_;
   private int nSlices_;
   private ImagePlus imagePlus_;

   public AcquisitionVirtualStack(int width, int height, ColorModel cm, MMImageCache imageCache, int nSlices)
   {
      super(width, height, cm, "");
      imageCache_ = imageCache;
      width_ = width;
      height_ = height;
      nSlices_ = nSlices;
   }


   public void setImagePlus(ImagePlus imagePlus) {
      imagePlus_ = imagePlus;
   }

   
   public void setType(int type) {
      type_ = type;
   }

   public Object getPixels(int flatIndex) {
      if (!uuids_.containsKey(flatIndex))
         return ImageUtils.makeProcessor(type_, width_, height_).getPixels();
      else {
         try {
            TaggedImage image = getTaggedImage(flatIndex);
            if (MDUtils.isGRAY(image)) {
               return image.pix;
            } else if (MDUtils.isRGB32(image)) {
               return ImageUtils.singleChannelFromRGB32((byte []) image.pix, (flatIndex-1) % 3);
            } else if (MDUtils.isRGB64(image)) {
               return ImageUtils.singleChannelFromRGB64((short []) image.pix, (flatIndex-1) % 3);
            }
         } catch (Exception ex) {
            ReportingUtils.logError(ex);
            return null;
         }
         return null;
      }
   }

   public ImageProcessor getProcessor(int flatIndex) {
      return ImageUtils.makeProcessor(type_, width_, height_, getPixels(flatIndex));
   }

   public TaggedImage getTaggedImage(int flatIndex) {
      if (uuids_.containsKey(flatIndex))
         return imageCache_.getImage(uuids_.get(flatIndex));
      else
         return null;
   }

   public int getSize() {
      return nSlices_;
   }

   void insertImage(int flatIndex, TaggedImage taggedImg) {
      try {
         UUID uuid = imageCache_.putImage(taggedImg);
         uuids_.put(flatIndex, uuid);
         if (MDUtils.isRGB(taggedImg)) {
            uuids_.put(flatIndex + 1, uuid);
            uuids_.put(flatIndex + 2, uuid);
         }
         
      } catch (Exception ex) {
         ReportingUtils.logError(ex);
      }
   }

   void insertImage(TaggedImage taggedImg) {
      int flatIndex = getFlatIndex(taggedImg.tags);
      insertImage(flatIndex, taggedImg);
   }

   public String getSliceLabel(int n) {
      if (uuids_.containsKey(n)) {
         Map<String,String> md = imageCache_.getImage(uuids_.get(n)).tags;
         try {
            return MDUtils.getChannelName(md) + ", " + md.get("Acquisition-ZPositionUm") + " um(z), " + md.get("Acquisition-TimeMs") + " s";
         } catch (Exception ex) {
            return "";
         }
      } else {
         return "";
      }
   }

   public void rememberImage(Map<String,String> md) {
      try {
         int flatIndex = getFlatIndex(md);
         UUID uuid = MDUtils.getUUID(md);
         uuids_.put(flatIndex, uuid);
         if (MDUtils.isRGB(md)) {
            uuids_.put(flatIndex + 1, uuid);
            uuids_.put(flatIndex + 2, uuid);
         }
      } catch (Exception ex) {
         ReportingUtils.logError(ex);
      }
   }

   private int getFlatIndex(Map<String,String> md) {
      try {
         int slice = MDUtils.getSliceIndex(md);
         int frame = MDUtils.getFrameIndex(md);
         int channel = MDUtils.getChannelIndex(md);
         if (imagePlus_ == null && slice == 0 && frame == 0 && channel == 0) {
            return 1;
         } else {
            return imagePlus_.getStackIndex(1 + channel, 1 + slice, 1 + frame);
         }
      } catch (Exception ex) {
         ReportingUtils.logError(ex);
         return 0;
      }
   }

   

   MMImageCache getCache() {
      return this.imageCache_;
   }

}
