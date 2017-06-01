package com.kotlinandroidbot.data.network

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Created by serhii_slobodyanuk on 5/31/17.
 */
interface ApiEndpoints {

    @FormUrlEncoded
    @Headers("X-Requested-With: XMLHttpRequest")
    @POST("/login.php")
    fun login(@Field("do_cmd") cmd: String, @Field("server") server: Int,
              @Field("email") email: String, @Field("password") password: String,
              @Field("remember") remember: Int, @Field("do_content_as_json") json: Int) : Observable<ResponseBody>

}