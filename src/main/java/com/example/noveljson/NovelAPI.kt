package com.example.noveljson

import retrofit2.http.*


data class NovelAIImage(
    val images: List<String>?,
    val parameters : NovelAIParameters?,
    val info : String?,
    val detail : NovelAIFailedDetail?,
)

data class NovelAIParameters(
    val prompt: String,
)

data class NovelAIFailedDetail(
    val detail: List<NovelAIFailedDetailLoc>
)

data class NovelAIFailedDetailLoc(
    val loc: List<String>,
    val msg: String,
    val type: String
)

data class RequestBody(
    val prompt: String = "(((masterpiece))), (((best quality))), ",
    val negative_prompt: String = "lowres, bad anatomy, bad hands, text, error, missing fingers, extra digit, fewer digits, cropped, worst quality, low quality, normal quality, jpeg artifacts, signature, watermark, username, blurry",
    val styles: List<String> = listOf(""),
    val seed: Long = 2226649862,
    val subseed: Int = -1,
    val subseed_strength: Int = 0,
    val seed_resize_from_h: Int = -1,
    val seed_resize_from_w: Int = -1,
    val batch_size: Int = 1,
    val n_iter: Int = 1,
    val steps: Int = 28,
    val cfg_scale: Int = 9,
    val width: Int = 512,
    val height: Int = 768,
    val restore_faces: Boolean = false,
    val tiling: Boolean = false,
    val eta: Double = 0.56,
    val sampler_index: String = "Euler a",
    //val override_settings : Settings,
)

data class Settings(
    val filter_nsfw: Boolean = false,
    val CLIP_stop_at_last_layers: Int = 2,
)

interface NovelAPI {
    companion object {
        const val baseUrl = "http://127.0.0.1:7860/"
    }

    /**
     * 查询图片
     */
    @Headers("user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.63 Safari/537.36",
        "Content-Type: application/json"
    )
    @POST("sdapi/v1/txt2img")
    suspend fun txt2img(
        @Body info : RequestBody
    ): NovelAIImage
}