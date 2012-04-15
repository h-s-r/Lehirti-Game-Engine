package org.lehirti.engine.gui;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.VolatileImage;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.lehirti.engine.res.ResourceCache;
import org.lehirti.engine.res.images.ImageKey;
import org.lehirti.engine.res.images.ImageWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageArea extends JComponent implements Externalizable {
  private static final long serialVersionUID = 1L;
  
  private static final Logger LOGGER = LoggerFactory.getLogger(ImageArea.class);
  
  private static final String IMAGE_AREA_END_OBJECT = "IMAGE_AREA_END_OBJECT";
  
  private static final int WIDTH = 1000;
  private static final int HEIGHT = 800;
  
  private VolatileImage backBuffer;
  
  private ImageWrapper backgroundImage = null;
  
  private final List<ImageWrapper> foregroundImages = new ArrayList<ImageWrapper>(15);
  
  private final Object interpolation;
  private final Object renderQuality;
  private final Object antiAliasing;
  
  public ImageArea() {
    final String interpolation = DisplayOptions.getDisplayOptionFor("INTERPOLATION", "BILINEAR");
    if ("NEAREST_NEIGHBOR".equals(interpolation)) {
      this.interpolation = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
    } else if ("BILINEAR".equals(interpolation)) {
      this.interpolation = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
    } else {
      this.interpolation = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
    }
    
    final String renderQuality = DisplayOptions.getDisplayOptionFor("RENDERING", "DEFAULT");
    if ("SPEED".equals(renderQuality)) {
      this.renderQuality = RenderingHints.VALUE_RENDER_SPEED;
    } else if ("DEFAULT".equals(renderQuality)) {
      this.renderQuality = RenderingHints.VALUE_RENDER_DEFAULT;
    } else {
      this.renderQuality = RenderingHints.VALUE_RENDER_QUALITY;
    }
    
    final String antiAliasing = DisplayOptions.getDisplayOptionFor("ANTIALIASING", "OFF");
    if ("DEFAULT".equals(antiAliasing)) {
      this.antiAliasing = RenderingHints.VALUE_ANTIALIAS_DEFAULT;
    } else if ("OF".equals(antiAliasing)) {
      this.antiAliasing = RenderingHints.VALUE_ANTIALIAS_OFF;
    } else {
      this.antiAliasing = RenderingHints.VALUE_ANTIALIAS_ON;
    }
  }
  
  @Override
  public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    final long serialVersionUniqueID = in.readLong();
    if (serialVersionUniqueID == 1L) {
      readExternal1(in);
    } else {
      throw new RuntimeException("Unknown StateObject serialVersionUID: " + serialVersionUniqueID);
    }
  }
  
  private void readExternal1(final ObjectInput in) throws ClassNotFoundException, IOException {
    this.backgroundImage = null;
    this.foregroundImages.clear();
    
    final boolean hasBackgroundImage = in.readBoolean();
    if (hasBackgroundImage) {
      final String className = (String) in.readObject();
      final ImageKey backgroundImageKey = readImageKey(in, className);
      if (backgroundImageKey != null) {
        setBackgroundImage(backgroundImageKey);
      }
    }
    String className = (String) in.readObject();
    while (!className.equals(IMAGE_AREA_END_OBJECT)) {
      final ImageKey imgKey = readImageKey(in, className);
      if (imgKey != null) {
        addImage(imgKey);
      }
      className = (String) in.readObject();
    }
    repaint();
  }
  
  private static ImageKey readImageKey(final ObjectInput in, final String className) throws ClassNotFoundException,
      IOException {
    final String key = (String) in.readObject();
    try {
      return (ImageKey) Enum.valueOf((Class<? extends Enum>) Class.forName(className), key);
    } catch (final ClassNotFoundException e) {
      LOGGER.warn("Failed to reconstruct ImageKey from className " + className + " and key " + key, e);
    }
    return null;
  }
  
  @Override
  public void writeExternal(final ObjectOutput out) throws IOException {
    out.writeLong(serialVersionUID);
    if (this.backgroundImage != null) {
      out.writeBoolean(true);
      writeImageKey(out, this.backgroundImage.getKey());
    } else {
      out.writeBoolean(false);
    }
    for (final ImageWrapper img : this.foregroundImages) {
      writeImageKey(out, img.getKey());
    }
    out.writeObject(IMAGE_AREA_END_OBJECT);
  }
  
  private static void writeImageKey(final ObjectOutput out, final ImageKey key) throws IOException {
    out.writeObject(key.getClass().getName());
    out.writeObject(key.name());
  }
  
  @Override
  public void paintComponent(final Graphics g) {
    do {
      if (this.backBuffer != null) {
        this.backBuffer.flush();
        this.backBuffer = null;
      }
      this.backBuffer = createVolatileImage(getWidth(), getHeight());
      
      // rendering to the back buffer:
      final Graphics2D g2d = (Graphics2D) this.backBuffer.getGraphics();
      g2d.setComposite(AlphaComposite.SrcAtop);
      
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, this.interpolation);
      g2d.setRenderingHint(RenderingHints.KEY_RENDERING, this.renderQuality);
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, this.antiAliasing);
      
      if (this.backgroundImage != null) {
        final int[] coords = this.backgroundImage.calculateCoordinates(getWidth(), getHeight());
        g2d.drawImage(this.backgroundImage.getImage(), coords[0], coords[1], coords[2], coords[3], null);
      }
      
      for (final ImageWrapper image : this.foregroundImages) {
        final int[] coords = image.calculateCoordinates(getWidth(), getHeight());
        g2d.drawImage(image.getImage(), coords[0], coords[1], coords[2], coords[3], null);
      }
      
      // copy to front buffer
      g.drawImage(this.backBuffer, 0, 0, this);
      
    } while (this.backBuffer.contentsLost());
  }
  
  @Override
  public Dimension getPreferredSize() {
    return new Dimension(WIDTH, HEIGHT);
  }
  
  /**
   * @param backgroundImage
   *          replace current background image with this one (null to remove background image)
   */
  public void setBackgroundImage(final ImageKey imageKey) {
    LOGGER.debug("Setting background image to {}", imageKey);
    final ImageWrapper backgroundImage;
    if (imageKey != null) {
      backgroundImage = ResourceCache.get(imageKey);
      backgroundImage.pinRandomImage();
    } else {
      backgroundImage = null;
    }
    this.backgroundImage = backgroundImage;
  }
  
  public void setImage(final ImageKey imageKey) {
    this.foregroundImages.clear();
    addImage(imageKey);
  }
  
  public void addImage(final ImageKey imageKey) {
    if (imageKey == null) {
      return;
    }
    final ImageWrapper image = ResourceCache.get(imageKey);
    image.pinRandomImage();
    this.foregroundImages.add(image);
  }
  
  public void removeImage(final ImageKey imageKey) {
    this.foregroundImages.remove(imageKey);
  }
  
  void setImage(final ImageWrapper images) {
    this.foregroundImages.clear();
    this.foregroundImages.add(images);
  }
  
  void setImages(final List<ImageWrapper> allImages) {
    this.foregroundImages.clear();
    this.foregroundImages.addAll(allImages);
  }
  
  public List<ImageWrapper> getAllImages() {
    final List<ImageWrapper> allImages = new ArrayList<ImageWrapper>(16);
    if (this.backgroundImage != null) {
      allImages.add(this.backgroundImage);
    }
    allImages.addAll(this.foregroundImages);
    return allImages;
  }
}
