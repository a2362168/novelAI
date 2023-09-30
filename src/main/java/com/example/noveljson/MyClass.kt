package com.example.noveljson

import java.io.File

val outputDir = "D:\\novalAI\\temp"


val path1 = """D:\mmd\UserFile\Motion\君色に染まる(足太ぺんたさん)モーション\face.txt"""
suspend fun main(args: Array<String>) {
    //NovelAPIService.txt2img("")
    //val ss = GenerateVMD()
    //ss.generateVMD(0, 2835)
    renameFiles("""D:\ccc3""")
    //ss.generateVideo(0, 2700)
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
