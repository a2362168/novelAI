package com.example.noveljson

import com.example.noveljson.image.ImageFileHandler
import java.io.File

val outputDir = "D:\\novalAI\\temp"


val path1 = """D:\mmd\UserFile\Motion\君色に染まる(足太ぺんたさん)モーション\face.txt"""
suspend fun main(args: Array<String>) {
    //NovelAPIService.txt2img("")
    val ss = GenerateVMD()
    //ss.generateVMD(0, 2802)
    //renameFiles("""D:\ccc3""")
    ImageFileHandler().handleImages("""D:\cccend""", """D:\ccc2""")
    //ImageFileHandler().restoreImages("""D:\ccc4""", """D:\ccc3""")
    //renameFiles("""D:\Koikatu-Game\BepInEx\plugins\VideoExport\Frames\2023-10-21T11-13-13""", """D:\cccend""")
}

fun renameFiles(path:String) {
    val dir = File(path)

    val files = dir.listFiles()
    files.sortBy { it.nameWithoutExtension.toInt() }
    for (i in files.indices) {
        val file = files[i]
        print(file.name)
        file.renameTo(File(file.parentFile.absolutePath + File.separator + "0" + (i+579).toString() + "." + file.extension))
    }
}

fun renameFiles(src:String, dst:String) {
    val dir = File(src)

    val files = dir.listFiles().filter { it.extension == "png" }
    for (file in files) {
        file.renameTo(File(dst + File.separator + String.format("%04d", file.nameWithoutExtension.toInt()) + "." + file.extension))
    }
}
