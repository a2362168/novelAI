package com.example.noveljson

import org.apache.commons.io.FileUtils
import java.io.File
import kotlin.math.absoluteValue

data class FrameData (
    val frame:Int,
    val value:Double
)

data class FacialData (
    var facial:String,
    var value:Double
)

class GenerateVMD {
    val face = """D:\mmd\UserFile\Model\Is the Order a Rabbit\Poppin Jump (Short Ver.)\megu_face.txt"""
    val bone = """D:\mmd\UserFile\Model\Is the Order a Rabbit\Poppin Jump (Short Ver.)\megu_bone.txt"""
    val eyes = ",red eyes"
    val front = "blush"
    val lora = ",(light smile:0.6),<lora:Gochuumon_all_resized:0.7:MIDD>,<lora:animixAnimeScreenshotLikeStyleMixLora_v10:0.6:OUTALL>,white_background,simple_background"
    val half_eye = 0.35
    val closed_eye = 0.72
    val step = 1
    val disableMouth = false
    val disableEyes = false
    val disabletrun = true

    val CASL = HashMap<String,String>()

    val allFical =  HashMap<String, ArrayList<FrameData>>()
    val allBone =  HashMap<String, ArrayList<FrameData>>()
    init {
        parseFical(face, allFical)
        parseBone(bone, allBone)
        CASL["あ"] = ":D,open mouth"
        //CASL["あ"] = "open mouth"
        CASL["う"] = "parted_lips"
        CASL["え"] = "^_^,open mouth"
        //CASL["え"] = "teeth"
        CASL["い"] = "grin,parted_lips"
        //CASL["い"] = "clenched teeth,parted_lips"
        CASL["お"] = ":o,open mouth"
        CASL["笑い"] = "smile"
        CASL["はぅ"] = ">_<"
        CASL["はちゅ目"] = "wide-eyed,surprised"
        CASL["赤面"] = "embarrassed"
       // CASL["照れ"] = "embarrassed"
        CASL["にこり"] = "light_smile:0.7"
        CASL["にやけ"] = "light_smile:0.7"
        CASL["困る"] = "Surprised,wide-eyed:0.7"
        CASL["怒り"] = "annoyed"
        CASL["びっくり"] = "Surprised,wide-eyed"
    }

    private fun parseFical(path:String, values:HashMap<String, ArrayList<FrameData>>) {
        val file = File(path)

        val lines = file.readLines()
        lines.forEach {
            val temp = it.split(',')
            if (temp.size == 3) {
                val data = FrameData(temp[1].toInt(), temp[2].toDouble())
                var curdata = values[temp[0]]
                if (curdata == null) {
                    curdata = ArrayList<FrameData>()
                    values[temp[0]] = curdata
                }
                curdata.add(data)
            }
        }
    }

    private fun parseBone(path:String, values:HashMap<String, ArrayList<FrameData>>) {
        val file = File(path)

        val lines = file.readLines()
        lines.forEach {
            val temp = it.split(',')
            if (temp.size >= 24) {
                val data = FrameData(temp[1].toInt(), temp[6].toDouble())
                var curdata = values[temp[0]]
                if (curdata == null) {
                    curdata = ArrayList<FrameData>()
                    values[temp[0]] = curdata
                }
                curdata.add(data)
            }
        }
    }

    fun generateVMD(start:Int, end:Int) {
        //val output = File(path2)

        //output.writer().use {
            for (i in start..end step step) {
                var curprpmt = String()

                val output = File(String.format("D:\\ccc6\\%04d.txt", (i-start)/step+1))
                output.writer().use {

                    var facialData = getBoneValue(allBone, i, listOf("上半身", "上半身2", "首" ,"頭", "全ての親", "グルーブ", "センター"), true)
                    if (facialData.value.absoluteValue > 100 && !disabletrun) {
                        curprpmt += ",from_back"
                    } else if (facialData.value.absoluteValue > 65 && !disabletrun) {
                        curprpmt += ",from_side"
                    } else {
                        curprpmt += front
                        val eyeData = getBoneValue(allFical, i, listOf("まばたき", "笑い", "なごみ" ,"はぅ", "はちゅ目"), true)
                        if (!disableEyes) {
                            when {
                                eyeData.value > closed_eye -> {
                                    curprpmt += ",closed eyes"
                                    curprpmt += fillCommonFacial(eyeData)
                                }
                                eyeData.value > half_eye && eyeData.value <= closed_eye -> {
                                    curprpmt += ",half-closed eyes"
                                    curprpmt += fillCommonFacial(eyeData)
                                }
                            }
                            if(eyeData.value <= closed_eye) curprpmt += eyes
                        }

                        if (!disableMouth) {
                            facialData = getBoneValue(allFical, i, listOf("あ", "お", "え", "う", "い"), true)
                            val mouthData = converMouthValue(facialData.value)
                            curprpmt += if (mouthData < 0.6) ",closed mouth"
                            else getPromtWithWeight(CASL[facialData.facial]!!, mouthData)
                        }

                        facialData = getBoneValue(allFical, i, listOf("ウィンク", "ウィンク右", "ウィンク２", "ｳｨﾝｸ２右"), false)
                        if (facialData.value > closed_eye) curprpmt += ",one eye closed"

                        if (!disableMouth) {
                            facialData = getBoneValue(allFical, i, listOf("赤面", "照れ"), true)
                            curprpmt += fillCommonFacial(facialData)
                            facialData = getBoneValue(allFical, i, listOf("にこり", "にやけ"), true)
                            curprpmt += fillCommonFacial(facialData)
                            if (eyeData.value <= half_eye) {
                                facialData =
                                    getBoneValue(allFical, i, listOf("困る", "怒り", "びっくり"), true)
                                curprpmt += fillCommonFacial(facialData)
                            }
                        }
                    }
                    it.write(curprpmt + lora)
            }
        }
    }

    private fun fillCommonFacial(facialData: FacialData):String {
        val mouthData = converMouthValue(facialData.value)
        return if(CASL[facialData.facial] != null) getPromtWithWeight(CASL[facialData.facial]!!, mouthData)
        else ""
    }

    private fun getBoneValue(bones:HashMap<String, ArrayList<FrameData>>, frame:Int, facials:List<String>, plus:Boolean):FacialData {
        val maxValue:FacialData = FacialData("..", 0.0)

        facials.forEach {
            val data = bones[it]
            if (data != null) {
                val value = getFacialValue(frame,data)
                if (value > maxValue.value) {
                    maxValue.facial = it
                }
                if (plus)
                    maxValue.value += value
                else if(value > maxValue.value)
                    maxValue.value = value
            }
        }
        return maxValue
    }

    private fun getFacialValue(frame:Int, frameData:List<FrameData>):Double {
        val index = frameData.indexOfFirst { it.frame >= frame }
        if (index == -1 || index == 0) {
            if (frame <= frameData[0].frame)
                return frameData[0].value
            else if (frame >= frameData[frameData.size-1].frame)
                return frameData[frameData.size-1].value
            else
                assert(false)
        }
        val first = frameData[index-1]
        val sencond = frameData[index]
        return (frame-first.frame).toDouble()/(sencond.frame-first.frame)*(sencond.value-first.value)+first.value
    }

    private fun converMouthValue(value:Double) : Double {
        val min = 0.6
        val max = 1.0

        val min1 = 0.2
        var tt = value
        if (tt > max) tt = max

        return (tt-min1)/(max-min1)*(max-min)+min
    }

    private fun getPromtWithWeight(promt:String, weight:Double):String {
        val temp = promt.split(':')
        var value = weight
        var temppromt = promt
        try {
            if (temp.size == 2) {
                value *= temp[1].toDouble()
                temppromt = temp[0]
            }
            return String.format(",(%s:%.2f)", temppromt, value)
        } catch (e:NumberFormatException) {
            return String.format(",(%s:%.2f)", temppromt, value)
        }
    }

    enum class EYES(val filename:String) {
        OPEN("blue_eyes"),
        CLOSE("closed_eyes"),
        SMILE("smile_open"),
        SMILE_CLOSE("smile_closed"),
    }

    enum class MOUTH(val filename:String) {
        A("a"),
        O("o"),
        E("e"),
        I("i"),
        U("u"),
        CLOSE("closed_mouth"),
        SMILE_CLOSE("smile_closed_mouth"),
    }

    fun generateVideo(start:Int, end:Int) {
        var prieye:EYES? = null
        var count = 0

        for (i in start..end step step) {
            val eyeoutput = File(String.format("D:\\ccc7\\%04d.png", (i-start)/step+1))
            val mouthoutput = File(String.format("D:\\ccc8\\%04d.png", (i-start)/step+1))

            var facialData:FacialData
            var eye = EYES.OPEN
            var mouth = MOUTH.CLOSE

            val eyeData = getBoneValue(allFical, i, listOf("まばたき", "笑い", "なごみ" ,"はぅ", "はちゅ目"), true)
            when {
                eyeData.value > closed_eye -> {
                    eye = if (eyeData.facial == "笑い") EYES.SMILE_CLOSE else EYES.CLOSE
                }
                eyeData.value < half_eye -> {
                    eye = if (eyeData.facial == "笑い") EYES.SMILE else EYES.OPEN
                }
            }

            facialData = getBoneValue(allFical, i, listOf("あ", "お", "え", "う", "い"), true)
            val mouthData = converMouthValue(facialData.value)
            if (mouthData > 0.6) {
                when(facialData.facial) {
                    "あ" -> mouth = MOUTH.A
                    "お" -> mouth = MOUTH.O
                    "え" -> mouth = MOUTH.E
                    "う" -> mouth = MOUTH.U
                    "い" -> mouth = MOUTH.I
                }
            } else {
                mouth = if (eyeData.facial == "笑い") MOUTH.SMILE_CLOSE else MOUTH.CLOSE
            }
            if (prieye != null) {
                if (prieye == EYES.SMILE && count < 60) {
                    eye = prieye
                    count++
                } else {
                    count = 0
                }
            }
            prieye = eye

            val orieyefile = File("L:\\chinoloop\\sharo\\${eye.filename}.png")
            val orimouthfile = File("L:\\chinoloop\\raw\\${mouth.filename}.png")

            FileUtils.copyFile(orieyefile, eyeoutput);
           // FileUtils.copyFile(orimouthfile, mouthoutput);
        }
    }
}

