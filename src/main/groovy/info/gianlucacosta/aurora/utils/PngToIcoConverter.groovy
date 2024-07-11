package info.gianlucacosta.aurora.utils

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.ByteBuffer

public class PngToIcoConverter {

  public static void convert(File pngFile, File icoFile) {
    FileOutputStream fos = null
    try {
      // Read PNG image
      BufferedImage originalImage = ImageIO.read(pngFile)

      // Prepare ICO header
      ByteBuffer header = ByteBuffer.allocate(6)
      header.putShort(0, 0) // Reserved (must be 0)
      header.putShort(2, 1) // Type (1 for ICO, 2 for CUR)
      header.putShort(4, 1) // Number of images in ICO

      // Prepare ICO directory entry
      ByteBuffer directoryEntry = ByteBuffer.allocate(16)
      directoryEntry.put(0, originalImage.getWidth() as byte) // Width in pixels
      directoryEntry.put(1, originalImage.getHeight() as byte) // Height in pixels
      directoryEntry.put(2, 0) // Colors (0 means using as many as possible)
      directoryEntry.put(3, 0) // Reserved (must be 0)
      directoryEntry.putShort(4, 1) // Color planes
      directoryEntry.putShort(6, 32) // Bits per pixel (PNG is 32-bit)
      directoryEntry.putInt(8, (int) pngFile.length()) // Image size
      directoryEntry.putInt(12, 22) // Offset of image data

      // Write ICO file
      fos = new FileOutputStream(icoFile)
      // Write header
      fos.write(header.array())

      // Write directory entry
      fos.write(directoryEntry.array())

      // Write PNG image data
      ImageIO.write(originalImage, 'PNG', fos)
    } catch (IOException e) {
      throw new RuntimeException(e)
    } finally {
      if (fos != null) {
        try {
          fos.close()
            } catch (IOException e) {
          throw new RuntimeException(e)
        }
      }
    }
  }

  private PngToIcoConverter() { }

}
