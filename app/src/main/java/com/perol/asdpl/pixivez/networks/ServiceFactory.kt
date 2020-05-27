package com.perol.asdpl.pixivez.networks

import android.util.Log
import com.perol.asdpl.pixivez.BuildConfig
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit

object ServiceFactory {

    /**
     * API declarations([T]) must be interfaces.
     */
    inline fun <reified T : Any> create(
        httpUrl: HttpUrl = "https://0.0.0.0/".toHttpUrl(),
        httpClient: OkHttpClient = HttpClient.DEFAULT,
        callAdapterFactory: CallAdapter.Factory? = RxJava2CallAdapterFactory.create(),
        converterFactory: Converter.Factory? = GsonConverterFactory.create()
    ): T {
        require(T::class.java.isInterface && T::class.java.interfaces.isEmpty()) {
            "API declarations must be interfaces and API interfaces must not extend other interfaces."
        }

        val retrofit = Retrofit.Builder()
            .apply {
                callAdapterFactory?.let { addCallAdapterFactory(it) }
                converterFactory?.let { addConverterFactory(it) }
            }
            .baseUrl(httpUrl)
            .client(httpClient)
            .validateEagerly(BuildConfig.DEBUG)
            .build()

        return retrofit.create()
    }

    object HttpClient {
        val DEFAULT: OkHttpClient by lazy {
            OkHttpClient.Builder()
//                .addInterceptor(httpLoggingInterceptor)
                .connectTimeout(8L, TimeUnit.SECONDS)
                .readTimeout(8L, TimeUnit.SECONDS)
                .writeTimeout(8L, TimeUnit.SECONDS)
                .build()
        }

        private val httpLoggingInterceptor by lazy {
            HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Log.d("GlideInterceptor", message)
                }
            }).apply {
                level =
                    if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            }
        }
    }
}
