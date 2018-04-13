package com.detectorvision.deltaCluster;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ClusterCompress {
  public static void gzipFile(String from, String to) throws IOException {
    FileInputStream in = new FileInputStream(from);
    GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(to));
    byte[] buffer = new byte[4096];
    int bytesRead;
    while ((bytesRead = in.read(buffer)) != -1)
      out.write(buffer, 0, bytesRead);
    in.close();
    out.close();
  }

  /** Zip the contents of the directory, and save it in the zipfile */
  public static void zipDirectory(String dir, String zipfile)
      throws IOException, IllegalArgumentException {
	  System.out.println("zipping directory:"+dir+"\nto:"+zipfile);
    // Check that the directory is a directory, and get its contents
    File d = new File(dir);
    if (!d.isDirectory()){
      throw new IllegalArgumentException("Not a directory:  "
          + dir);}
    String[] entries = d.list();
    byte[] buffer = new byte[4096]; // Create a buffer for copying
    int bytesRead;

    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile));

    for (int i = 0; i < entries.length; i++) {
      File f = new File(d, entries[i]);
      System.out.println("adding file:"+f.toString());
      if (f.isDirectory())
        continue;//Ignore directory
      FileInputStream in = new FileInputStream(f); // Stream to read file
      ZipEntry entry = new ZipEntry(f.getPath()); // Make a ZipEntry
      out.putNextEntry(entry); // Store entry
      while ((bytesRead = in.read(buffer)) != -1)
        out.write(buffer, 0, bytesRead);
      in.close(); 
    }
    out.close();
  }

  public static void main(String args[]) throws IOException {
    String from = ".";
    File f = new File(from);
    boolean directory = f.isDirectory(); // Is it a file or directory?

    ClusterCompress.zipDirectory(from, from + ".zip");
    ClusterCompress.gzipFile(from, from + ".gz");
  }
}

