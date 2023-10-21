package com.example.noveljson.image

import com.alibaba.fastjson.JSON
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileWriter
import java.util.Collections.synchronizedList
import java.util.concurrent.CountDownLatch
import javax.imageio.ImageIO


enum class resizeType {
    FILL,
    RESIZE
}

data class ImageInfo(
    val filename:String,
    val oriWidth:Int,
    val oriHeight:Int,
    val startX:Int,
    val startY:Int,
    val startWidth:Int,
    val startHeight:Int,
    var midWidth:Int,
    var midHeight:Int,
    var endX:Int,
    var endY:Int,
    var endWidth:Int,
    var endHeight:Int,
)


class ImageFileHandler {
    private val NEW_SIZE = Pair(960, 1472)
    private val THRESHOLD = 1.0
    private val HeightWidth = 1.2
    private var imageInfos:MutableList<ImageInfo>? = null
    private val INFO = "info.txt"
    private val THREAD_NUM=13

    fun handleImages(srcFolderPath:String, dstFolderPath:String) {
        imageInfos = synchronizedList<ImageInfo>(mutableListOf())
        val srcFolder = File(srcFolderPath)
        val files = srcFolder.listFiles().filter { it.extension == "png" }
        if (files.size > THREAD_NUM*10) {
            val cd = CountDownLatch(THREAD_NUM)
            val threadsFiles = files.chunked(files.size/THREAD_NUM+1)
            assert(threadsFiles.size == THREAD_NUM)
            for(listFiles in threadsFiles) {
                Thread {
                    for (file in listFiles) {
                        val image = resizeImage(file)
                        if (image != null) ImageIO.write(image, "png", File(dstFolderPath + File.separator + file.name))
                    }
                    cd.countDown()
                }.start()
            }
            cd.await()
        } else {
            for (file in files) {
                val image = resizeImage(file)
                if (image != null) ImageIO.write(image, "png", File(dstFolderPath + File.separator + file.name))
            }
        }
        writeImageInfo(dstFolderPath + File.separator + INFO)
    }

    fun restoreImages(srcFolderPath:String, dstFolderPath:String) {
        readImageInfo(srcFolderPath + File.separator + INFO)
        val srcFolder = File(srcFolderPath)
        val files = srcFolder.listFiles().filter { it.extension == "png" }
        if (files.size > THREAD_NUM*10) {
            val cd = CountDownLatch(THREAD_NUM)
            val threadsFiles = files.chunked(files.size/THREAD_NUM+1)
            assert(threadsFiles.size == THREAD_NUM)
            for(listFiles in threadsFiles) {
                Thread {
                    for (file in listFiles) {
                        val image = restoreImage(file)
                        ImageIO.write(image, "png", File(dstFolderPath + File.separator + file.name))
                    }
                    cd.countDown()
                }.start()
            }
            cd.await()
        } else {
            for (file in files) {
                val image = restoreImage(file)
                ImageIO.write(image, "png", File(dstFolderPath + File.separator + file.name))
            }
        }
    }

    private fun resizeImage(file: File):BufferedImage? {
        try {
            // 读取图片
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

            var subWidth = maxX-minX+1
            var subHeight = maxY-minY+1
            if (subHeight < 400 || subWidth < 200) return null
            var subImage = image.getSubimage(minX, minY, subWidth, subHeight)
            val info = ImageInfo(file.name, width,height, minX, minY, subWidth, subHeight,
                0,0, 0, 0, 0, 0)
            val reScale = getMinScale(subImage, NEW_SIZE.first, NEW_SIZE.second, HeightWidth)
            if (reScale > THRESHOLD || reScale < (1.0/THRESHOLD)) {
                info.midWidth = (subWidth*reScale+0.5).toInt()
                info.midHeight = (subHeight*reScale+0.5).toInt()
                if (info.midHeight and 1 == 1) info.midHeight++
                if (info.midWidth and 1 == 1) info.midWidth++
                subImage = resizeImage(subImage, info.midWidth, info.midHeight, resizeType.RESIZE, null)
            }
            val reImage = resizeImage(subImage, NEW_SIZE.first, NEW_SIZE.second, resizeType.FILL, info)
            imageInfos!!.add(info)
            return reImage
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun restoreImage(file: File):BufferedImage {
        val info = imageInfos!!.find { it.filename == file.name }!!

        val image = ImageIO.read(file)
        var subImage = image.getSubimage(info.endX, info.endY, info.endWidth, info.endHeight)
        subImage = resizeImage(subImage, info.startWidth, info.startHeight, resizeType.RESIZE, null)

        val resizedImage = BufferedImage(info.oriWidth, info.oriHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = resizedImage.createGraphics()
        graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )
        graphics.drawImage(subImage, info.startX, info.startY, null)
        graphics.dispose()
        return resizedImage
    }

    private fun writeImageInfo(path: String) {
        val writer = FileWriter(path)
        writer.write(JSON.toJSONString(imageInfos))
        writer.flush()
        writer.close()
    }

    private fun readImageInfo(path: String) {
        val jsonStr:String = File(path).readText()
        imageInfos = JSON.parseArray(jsonStr, ImageInfo::class.java)
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
        type: resizeType,
        info: ImageInfo?
    ): BufferedImage {
        val resizedImage = BufferedImage(newWidth, newHeight, originalImage.type)
        val graphics = resizedImage.createGraphics()
        // fill the entire picture with white
        //graphics.color = Color.WHITE
        //graphics.fillRect(0, 0, newWidth, newHeight)
        if (type == resizeType.FILL) {
            var oriWidth = originalImage.width
            var oriHeight = originalImage.height

            // calculate the x value with which the original image is centred
            val centerX = (resizedImage.width - oriWidth) / 2
            // calculate the y value with which the original image is centred
            val centerY = (resizedImage.height - oriHeight) / 2
            // draw the original image
            graphics.drawImage(originalImage, centerX, centerY, null)
            info?.endX = centerX
            info?.endY = centerY
            info?.endWidth = oriWidth
            info?.endHeight = oriHeight
        } else if (type == resizeType.RESIZE) {
            graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON )

            // calculate the x value with which the original image is centred
            val centerX = (resizedImage.width - newWidth) / 2
            // calculate the y value with which the original image is centred
            val centerY = (resizedImage.height - newHeight) / 2
            // draw the original image
            graphics.drawImage(originalImage.getScaledInstance(newWidth, newHeight, BufferedImage.SCALE_SMOOTH), centerX, centerY, null)
        }
        graphics.dispose()
        return resizedImage
    }

    fun sharpImage(image:BufferedImage):BufferedImage {
        val imageSharpen = ImageSharpen.lapLaceSharpDeal(image)
        val graphics = imageSharpen.createGraphics()
        //设置为透明覆盖
   //     graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.2f));
        //在背景图片上添加锐化的边缘
        graphics.drawImage(imageSharpen, 0, 0, imageSharpen.getWidth(), imageSharpen.getHeight(), null);
        // 释放对象 透明度设置结束
    //    graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        graphics.dispose()
        return imageSharpen
    }
}