package com.kotlinandroidbot.data.network

import com.google.gson.GsonBuilder
import com.kotlinandroidbot.data.network.cookies.CustomCookieStore
import io.reactivex.Observable
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy


/**
 * Created by serhii_slobodyanuk on 5/31/17.
 */
class RestClient {

    companion object {

        val SERVER_URL: String = "http://g1.botva.ru/"

        val gson = GsonBuilder()
                .setDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'")
                .setLenient()
                .create()

        private var api: ApiEndpoints

        init {
            val cookieStore = CustomCookieStore()
            val cookieManager = CookieManager(cookieStore, CookiePolicy.ACCEPT_ALL)
            CookieHandler.setDefault(cookieManager)

            val interceptor = HttpLoggingInterceptor()

            interceptor.level = HttpLoggingInterceptor.Level.BODY

            val client = OkHttpClient.Builder()
                    .cookieJar(JavaNetCookieJar(CookieHandler.getDefault()))
                    .addInterceptor({ chain -> headerInterceptor(chain) })
                    .addInterceptor(interceptor)
                    .build()

            val retrofit = Retrofit.Builder()
                    .baseUrl(SERVER_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(client)
                    .build()

            api = retrofit.create(ApiEndpoints::class.java)
        }

        fun headerInterceptor(chain: Interceptor.Chain): Response {
            val original = chain.request()
            val request = original.newBuilder()
                    .method(original.method(), original.body())
                    .build()

            return chain.proceed(request)
        }

    }

    fun login(username: String, password: String): Observable<ResponseBody> {
        return api.login("login", 1, username, password, 1, 1)
    }

}

