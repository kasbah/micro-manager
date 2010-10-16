/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.micromanager.acquisition;

import java.util.HashMap;
import mmcorej.TaggedImage;
import org.json.JSONObject;
import org.micromanager.api.TaggedImageStorage;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.MMException;

/**
 *
 * @author arthur
 *
 * This class stores a collection of TaggedImages, all in ram.
 */
public class TaggedImageStorageRam implements TaggedImageStorage {
   HashMap<String, TaggedImage> imageMap_;
   private JSONObject summaryMetadata_;
   private String comment_;

   TaggedImageStorageRam(JSONObject summaryMetadata) {
      imageMap_ = new HashMap<String,TaggedImage>();
      summaryMetadata_ = summaryMetadata;
   }

   public String putImage(TaggedImage taggedImage) throws MMException {
      String label = MDUtils.getLabel(taggedImage.tags);
      imageMap_.put(label, taggedImage);
      return label;
   }

   public TaggedImage getImage(int channel, int slice, int frame, int position) {
      return imageMap_.get(MDUtils.generateLabel(channel, slice, frame, position));
   }

   public void finished() {
      // Do nothing.
   }

   public void setSummaryMetadata(JSONObject md) {
      summaryMetadata_ = md;
   }

   public JSONObject getSummaryMetadata() {
      return summaryMetadata_;
   }

   public void setComment(String text) {
      comment_ = text;
   }

   public String getComment() {
      return comment_;
   }

   public void setDisplaySettings(JSONObject settings) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public JSONObject getDisplaySettings() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

}
