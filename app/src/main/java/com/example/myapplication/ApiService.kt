package com.example.myapplication


import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class CurrencyConversionRequest(
    val amount: Double,
    val from_currency: String,
    val to_currency: String
)

data class CurrencyConversionResponse(
    val converted_amount: Double
)

interface ApiService {
    @Multipart
    @POST("api/ocr")
    fun uploadReceipt(
        @Part file: MultipartBody.Part
    ): Call<ResponseBody>

    @POST("api/convert")  // API call for currency conversion
    fun convertCurrency(
        @Body request: CurrencyConversionRequest
    ): Call<CurrencyConversionResponse>
}

