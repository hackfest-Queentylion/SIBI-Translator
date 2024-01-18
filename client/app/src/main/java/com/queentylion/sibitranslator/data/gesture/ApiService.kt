package com.queentylion.sibitranslator.data.gesture
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.Response
interface ApiService {
    @POST("endpoint/path")
    suspend fun postData(@Body postData: PostData): Response<ResponseData>
}