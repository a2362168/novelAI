package com.example.noveljson

import java.io.ByteArrayInputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO


object NovelAPIService {
    private val retrofit = HttpFunctions.createRetrofit(NovelAPI.baseUrl).create(NovelAPI::class.java)
    private var count = 0

    suspend fun txt2img(say:String) {
        val requset = RequestBody()
        print("111")
        try {
            val response = retrofit.txt2img(requset)
        if (response.images != null && response.info != null) {
            var name:String = "${count}_"
            val parameters = response.info.split(", ")
            for (parameter in parameters) {
                val keys = parameter.split(": ")
                if (keys[0] == "Seed" || keys[0] == "Eta" || keys[0] == "CFG scale") {
                    name += "${keys[0]}_${keys[1]}"
                }
            }
            decodeAndSave(response.images[0], name)
        } else
            assert(false)
    } catch (e:Exception) {
        print("333")

    }
    print("222")
    }

    fun decodeAndSave(image:String, name:String) {
        val image1 = Base64.getDecoder().decode(image.split(',', limit=1)[0])
        val bufferedImage = ImageIO.read(ByteArrayInputStream(image1))
        val otufile = File(outputDir + "\\$name.png")
        ImageIO.write(bufferedImage, "png", otufile);
        count++
    }
}