package com.pagbankpos.helpers

import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPaymentData
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagVoidData

object JsonParseUtils {

    @JvmStatic
    fun getPlugPagPaymentDataFromJson(jsonStr: String): PlugPagPaymentData? {
        return try {
            val jsonObject = JSONObject(jsonStr)
            val amount = jsonObject.getInt("amount")
            val installmentType = jsonObject.getInt("installmentType")
            val installments = jsonObject.getInt("installments")
            val type = jsonObject.getInt("type")
            val userReference = jsonObject.getString("userReference")
            val printReceipt = jsonObject.getBoolean("printReceipt")

            val paymentData = PlugPagPaymentData(
                type, amount, installmentType, installments, userReference, printReceipt
            )
            Log.d("PlugPag Json Parse", "PlugPagPaymentData parse success")

            paymentData
        } catch (e: JSONException) {
            Log.d("PlugPag Json Parse", "PlugPagPaymentData parse error")
            null
        }
    }

    @JvmStatic
    fun getPlugPagVoidDataFromJson(jsonStr: String): PlugPagVoidData? {
        return try {
            val jsonObject = JSONObject(jsonStr)
            val transactionCode = jsonObject.getString("transactionCode")
            val transactionId = jsonObject.getString("transactionId")

            val voidPayment = PlugPagVoidData(transactionCode, transactionId, true)
            Log.d("transactionCode", transactionCode)
            Log.d("transactionId", transactionId)

            voidPayment
        } catch (e: JSONException) {
            Log.d("PlugPag Json Parse", "PlugPagVoidData parse error")
            null
        }
    }
}
