package com.example.noveljson.image

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

enum class resizeType {
    FILL,
    RESIZE
}

class ImageFileHandler {
    private val NEW_SIZE = Pair(2000, 2400)
    private val THRESHOLD = 1.5
    private val HeightWidth = 1.2

    fun readImage() {
        try {
            // 读取图片
            val file = File("""D:\新建文件夹\2023-09-30T15-15-27\97.png""")
            val ss = file.exists()
            val image = ImageIO.read(file)

            // 获取图片宽度和高度
            val width = image.getWidth()
            val height = image.getHeight()

            val pixels = IntArray(width * height)
            image.getRGB(0, 0, width, height, pixels, 0, width)

            var minX=Int.MAX_VALUE
            var minY=Int.MAX_VALUE
            var maxX=Int.MIN_VALUE
            var maxY=Int.MIN_VALUE
            for (i in 0 until height) {
                for (j in 0 until width) {
                    val value = pixels[i*width + j]
                    if ((value and 0xFF000000.toInt()) != 0) {
                        if (i < minY) minY = i
                        if (i > maxY) maxY = i
                        if (j < minX) minX = j
                        if (j > maxX) maxX = j
                    }
                }
            }

            val subWidth = maxX-minX+1
            val subHeight = maxY-minY+1
            var subImage = image.getSubimage(minX, minY, subWidth, subHeight)
            val reScale = getMinScale(subImage, NEW_SIZE.first, NEW_SIZE.second, HeightWidth)
            if (reScale > THRESHOLD || reScale < (1.0/THRESHOLD)) {
                subImage = resizeImage(subImage, (subWidth*reScale).toInt(), (subHeight*reScale).toInt(), resizeType.RESIZE)
            }
            val reImage = resizeImage(subImage, NEW_SIZE.first, NEW_SIZE.second, resizeType.FILL)
            ImageIO.write(reImage, "png", File("""D:\新建文件夹\1.png"""))
        } catch (e:IOException) {
            e.printStackTrace()
        }
    }

    private fun getMinScale(image: BufferedImage, desWidth:Int, desHeight:Int, exHeight:Double):Double {
        val oriWidthScale = desWidth.toDouble()/image.width
        val oriHeightScale = desHeight.toDouble()/exHeight/image.height

        return Math.min(oriWidthScale, oriHeightScale)
    }

    private fun resizeImage(
        originalImage: BufferedImage,
        newWidth: Int,
        newHeight: Int,
        type: resizeType
    ): BufferedImage? {
        val resizedImage = BufferedImage(newWidth, newHeight, originalImage.type)
        val graphics = resizedImage.createGraphics()
        graphics.color = Color.WHITE
        // fill the entire picture with white
        graphics.fillRect(0, 0, newWidth, newHeight)
        if (type == resizeType.FILL) {
            var oriWidth = originalImage.width
            var oriHeight = originalImage.height

            // calculate the x value with which the original image is centred
            val centerX = (resizedImage.width - oriWidth) / 2
            // calculate the y value with which the original image is centred
            val centerY = (resizedImage.height - oriHeight) / 2
            // draw the original image
            graphics.drawImage(originalImage, centerX, centerY, oriWidth, oriHeight, null)
        } else if (type == resizeType.RESIZE) {
            var scale = getMinScale(originalImage, resizedImage.width, resizedImage.height, 1.0)

            var oriWidth = (originalImage.width * scale).toInt()
            var oriHeight = (originalImage.height * scale).toInt()
            // calculate the x value with which the original image is centred
            val centerX = (resizedImage.width - oriWidth) / 2
            // calculate the y value with which the original image is centred
            val centerY = (resizedImage.height - oriHeight) / 2
            // draw the original image
            graphics.drawImage(originalImage, centerX, centerY, oriWidth, oriHeight, null)
        }
        graphics.dispose()
        return resizedImage
    }
}