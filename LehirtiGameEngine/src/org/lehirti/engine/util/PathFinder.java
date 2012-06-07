package org.lehirti.engine.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lehirti.engine.res.images.ImageKey;
import org.lehirti.engine.res.text.TextKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathFinder {
  private static final Logger LOGGER = LoggerFactory.getLogger(PathFinder.class);
  
  private static final String VERSION_FILE_LOCATION = "version";
  
  private static final String RES = "res";
  
  private static final File CORE_BASE_DIR = new File("core");
  private static final String CORE_BASE_DIR_ABSOLUTE_PATH = CORE_BASE_DIR.getAbsolutePath();
  private static final int CORE_BASE_DIR_LENGTH = CORE_BASE_DIR_ABSOLUTE_PATH.length();
  private static final File MOD_BASE_DIR = new File("mod");
  
  private static final File MANIFEST_DIR = new File(CORE_BASE_DIR, "manifest");
  
  private static final List<File> CORE_CONTENT_DIRS = new ArrayList<File>(25);
  private static final List<File> MOD_CONTENT_DIRS = new ArrayList<File>(25);
  
  private static final Map<TextKey, File> CORE_TEXT_CACHE = new HashMap<TextKey, File>(1024);
  private static final Map<TextKey, File> MOD_TEXT_CACHE = new HashMap<TextKey, File>(1024);
  
  private static final Map<ImageKey, File[]> CORE_IMAGE_CACHE = new HashMap<ImageKey, File[]>(1024);
  private static final Map<ImageKey, File[]> MOD_IMAGE_CACHE = new HashMap<ImageKey, File[]>(1024);
  
  private static final File CORE_RES_DIR = new File(PathFinder.CORE_BASE_DIR, RES);
  private static final File MOD_RES_DIR = new File(PathFinder.MOD_BASE_DIR, RES);
  
  public static final String PROXY_FILENAME_SUFFIX = ".proxy";
  
  public static File getCoreFile(final TextKey key) {
    return get(key, CORE_TEXT_CACHE, CORE_CONTENT_DIRS);
  }
  
  public static File getModFile(final TextKey key) {
    return get(key, MOD_TEXT_CACHE, MOD_CONTENT_DIRS);
  }
  
  public static File getModFile(final TextKey key, final String contentDir) {
    final Collection<File> contentDirs = new ArrayList<File>(1);
    contentDirs.add(new File(MOD_BASE_DIR, contentDir));
    return get(key, new HashMap<TextKey, File>(), contentDirs);
  }
  
  private static File get(final TextKey key, final Map<TextKey, File> cache, final Collection<File> dirs) {
    File file = cache.get(key);
    if (file == null) {
      final String dirName = key.getClass().getPackage().getName();
      final String fileName = key.getClass().getName().substring(dirName.length() + 1) + "." + key.name();
      file = new File(new File("main", dirName), fileName);
      for (final File dir : dirs) {
        final File parentDir = new File(dir, dirName);
        file = new File(parentDir, fileName);
        if (file.exists()) {
          cache.put(key, file);
          return file;
        }
      }
    }
    return file;
  }
  
  public static File[] getCoreImageProxyFiles(final ImageKey key) {
    return get(key, CORE_IMAGE_CACHE, CORE_CONTENT_DIRS);
  }
  
  public static File[] getModImageProxyFiles(final ImageKey key) {
    return get(key, MOD_IMAGE_CACHE, MOD_CONTENT_DIRS);
  }
  
  public static File getModImageProxyFile(final ImageKey key, final String contentDir) {
    return getDir(new File(MOD_BASE_DIR, contentDir), key);
  }
  
  private static File[] get(final ImageKey key, final Map<ImageKey, File[]> cache, final Collection<File> dirs) {
    File[] files = cache.get(key);
    if (files == null) {
      final Collection<File> proxyFiles = new ArrayList<File>(25);
      for (final File parentDir : dirs) {
        final File dir = getDir(parentDir, key);
        if (!dir.isDirectory()) {
          continue;
        }
        final File[] imageProxies = dir.listFiles(new FileFilter() {
          @Override
          public boolean accept(final File pathname) {
            return pathname.getName().endsWith(PathFinder.PROXY_FILENAME_SUFFIX);
          }
        });
        for (final File proxy : imageProxies) {
          proxyFiles.add(proxy);
        }
      }
      files = proxyFiles.toArray(new File[proxyFiles.size()]);
      cache.put(key, files);
    }
    return files;
  }
  
  private static File getDir(final File baseDir, final ImageKey key) {
    final File moduleDir = new File(baseDir, key.getClass().getName());
    final File keyDir = new File(moduleDir, key.name());
    return keyDir;
  }
  
  public static File getCoreImageFile(final String imageBaseName) {
    final File tmp = new File(CORE_RES_DIR, imageBaseName.substring(0, 2));
    LOGGER.debug("core/res file dir: {}", tmp.getAbsolutePath());
    return new File(tmp, imageBaseName);
  }
  
  public static File getModImageFile(final String imageBaseName) {
    final File tmp = new File(MOD_RES_DIR, imageBaseName.substring(0, 2));
    return new File(tmp, imageBaseName);
  }
  
  /**
   * @param coreFile
   *          file inside the core sub-directory
   * @return <p>
   *         if coreFile is inside the core sub-directory: corresponding file in the mod sub-directory
   *         </p>
   *         <p>
   *         else coreFile will be returned as-is
   *         </p>
   */
  public static File toModFile(final File coreFile) {
    if (coreFile.getAbsolutePath().startsWith(CORE_BASE_DIR_ABSOLUTE_PATH)) {
      final String relPath = coreFile.getAbsolutePath().substring(CORE_BASE_DIR_LENGTH);
      return new File(MOD_BASE_DIR.getAbsolutePath() + relPath);
    }
    return coreFile;
  }
  
  /**
   * @param imageProxyFile
   * @return corresponding real file in core sub-directory
   */
  public static File imageProxyToCoreReal(final File imageProxyFile) {
    final int realImageFileNamelength = imageProxyFile.getName().length() - PROXY_FILENAME_SUFFIX.length();
    return getCoreImageFile(imageProxyFile.getName().substring(0, realImageFileNamelength));
  }
  
  public static File getProxyFile(final File baseDir, final String baseName) {
    return new File(baseDir, baseName + PathFinder.PROXY_FILENAME_SUFFIX);
  }
  
  public static String getLocationOfVerionsFileOnClasspath() {
    return VERSION_FILE_LOCATION;
  }
  
  public static File getManifest(final String contentKey, final int version) {
    return new File(MANIFEST_DIR, contentKey + "-" + version);
  }
  
  public static File getContentZipFile(final String contentKey, final int version) {
    return new File(contentKey + "-" + version + ".zip");
  }
  
  public static File getCoreContentDir(final String contentKey) {
    return new File(CORE_BASE_DIR, contentKey);
  }
  
  public static void registerContentDir(final String contentKey) {
    CORE_CONTENT_DIRS.add(new File(CORE_BASE_DIR, contentKey));
    MOD_CONTENT_DIRS.add(new File(MOD_BASE_DIR, contentKey));
  }
  
  public static String[] getContentDirs() {
    final String[] contentDirs = new String[CORE_CONTENT_DIRS.size()];
    for (int i = 0; i < CORE_CONTENT_DIRS.size(); i++) {
      contentDirs[i] = CORE_CONTENT_DIRS.get(i).getName();
    }
    return contentDirs;
  }
  
  public static File[] getContentPaths() {
    final File[] contentDirs = new File[CORE_CONTENT_DIRS.size() + MOD_CONTENT_DIRS.size()];
    for (int i = 0; i < CORE_CONTENT_DIRS.size(); i++) {
      contentDirs[i] = CORE_CONTENT_DIRS.get(i);
    }
    for (int i = 0; i < MOD_CONTENT_DIRS.size(); i++) {
      contentDirs[CORE_CONTENT_DIRS.size() + i] = MOD_CONTENT_DIRS.get(i);
    }
    return contentDirs;
  }
}
