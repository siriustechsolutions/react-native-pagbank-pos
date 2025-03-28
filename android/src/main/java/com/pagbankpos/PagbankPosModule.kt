package com.pagbankpos

import android.content.pm.PackageInfo
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log

import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule

import com.izettle.html2bitmap.Html2Bitmap
import com.izettle.html2bitmap.content.WebViewContent

import com.pagbankpos.helpers.JsonParseUtils

import br.com.uol.pagseguro.plugpagservice.wrapper.*

@ReactModule(name = PagbankPosModule.NAME)
class PagbankPosModule(reactContext: ReactApplicationContext) :
  NativePagbankPosSpec(reactContext) {

    override fun getName(): String {
      return NAME
  }

  private val reactContext = reactApplicationContext
  private var plugPag: PlugPag? = null
  private var messageCard: String? = null
  private val appVersion: String

  init {
      appVersion = try {
          getPackageInfo().versionName ?: "unknown"
      } catch (e: Exception) {
          "unknown"
      }
  }

  @Throws(Exception::class)
  private fun getPackageInfo(): PackageInfo {
      return reactContext.packageManager.getPackageInfo(
          reactContext.packageName,
          0
      )
  }

  override fun getConstants(): Map<String, Any> {
      val constants = HashMap<String, Any>()

      constants["PAYMENT_CREDITO"] = PlugPag.TYPE_CREDITO
      constants["PAYMENT_DEBITO"] = PlugPag.TYPE_DEBITO
      constants["PAYMENT_VOUCHER"] = PlugPag.TYPE_VOUCHER

      constants["INSTALLMENT_TYPE_A_VISTA"] = PlugPag.INSTALLMENT_TYPE_A_VISTA
      constants["INSTALLMENT_TYPE_PARC_VENDEDOR"] = PlugPag.INSTALLMENT_TYPE_PARC_VENDEDOR
      constants["INSTALLMENT_TYPE_PARC_COMPRADOR"] = PlugPag.INSTALLMENT_TYPE_PARC_COMPRADOR

      constants["OPERATION_ABORTED"] = PlugPag.OPERATION_ABORTED

      constants["ACTION_POST_OPERATION"] = PlugPag.ACTION_POST_OPERATION
      constants["ACTION_PRE_OPERATION"] = PlugPag.ACTION_PRE_OPERATION
      constants["ACTION_UPDATE"] = PlugPag.ACTION_UPDATE

      constants["AUTHENTICATION_FAILED"] = PlugPag.AUTHENTICATION_FAILED
      constants["COMMUNICATION_ERROR"] = PlugPag.COMMUNICATION_ERROR
      constants["ERROR_CODE_OK"] = PlugPag.ERROR_CODE_OK
      constants["MIN_PRINTER_STEPS"] = PlugPag.MIN_PRINTER_STEPS

      constants["NO_PRINTER_DEVICE"] = PlugPag.NO_PRINTER_DEVICE
      constants["NO_TRANSACTION_DATA"] = PlugPag.NO_TRANSACTION_DATA
      constants["SERVICE_CLASS_NAME"] = PlugPag.SERVICE_CLASS_NAME
      constants["SERVICE_PACKAGE_NAME"] = PlugPag.SERVICE_PACKAGE_NAME

      constants["RET_OK"] = PlugPag.RET_OK
      constants["appVersion"] = appVersion
      return constants
  }

  @ReactMethod
  override fun setAppIdentification() {
      try {
          plugPag = PlugPag(reactContext)
      } catch (e: Exception) {
          throw RuntimeException(e)
      }
  }

  @ReactMethod
  override fun initializeAndActivatePinPad(activationCode: String, promise: Promise) {
      setAppIdentification()

      val activationData = PlugPagActivationData(activationCode)

      val executor: ExecutorService = Executors.newSingleThreadExecutor()
      val callable = Callable<PlugPagInitializationResult> {
          plugPag?.initializeAndActivatePinpad(activationData)
      }

      val future: Future<PlugPagInitializationResult> = executor.submit(callable)
      executor.shutdown()

      try {
          val initResult = future.get()

          val map = Arguments.createMap()
          map.putInt("result", initResult.result)
          map.putString("errorCode", initResult.errorCode)
          map.putString("errorMessage", initResult.errorMessage)

          promise.resolve(map)
      } catch (e: ExecutionException) {
          Log.d("PlugPag", e.message ?: "Unknown error")
          promise.reject("error", e.message)
      } catch (e: InterruptedException) {
          Log.d("PlugPag", e.message ?: "Unknown error")
          promise.reject("error", e.message)
      }
  }

  @ReactMethod
  override fun doPayment(jsonStr: String, promise: Promise) {
      setAppIdentification()

      val paymentData = JsonParseUtils.getPlugPagPaymentDataFromJson(jsonStr)

      plugPag?.setEventListener(object : PlugPagEventListener {
          override fun onEvent(plugPagEventData: PlugPagEventData) {
              messageCard = plugPagEventData.customMessage

              val params = Arguments.createMap()
              params.putInt("status", plugPagEventData.eventCode)
              params.putString("message", messageCard)
              reactContext
                  .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                  .emit("MAKE_TRANSACTION_PROGRESS", params)
          }
      })

      val executor: ExecutorService = Executors.newSingleThreadExecutor()

      val runnableTask = Runnable {
          try {
              val transactionResult = plugPag?.doPayment(paymentData!!)
              val map = Arguments.createMap()
              map.putInt("result", transactionResult?.result ?: 0)
              map.putString("errorCode", transactionResult?.errorCode)
              map.putString("message", transactionResult?.message)
              map.putString("transactionCode", transactionResult?.transactionCode)
              map.putString("transactionId", transactionResult?.transactionId)
              map.putString("hostNsu", transactionResult?.hostNsu)
              map.putString("date", transactionResult?.date)
              map.putString("time", transactionResult?.time)
              map.putString("cardBrand", transactionResult?.cardBrand)
              map.putString("bin", transactionResult?.bin)
              map.putString("holder", transactionResult?.holder)
              map.putString("userReference", transactionResult?.userReference)
              map.putString("terminalSerialNumber", transactionResult?.terminalSerialNumber)
              map.putString("amount", transactionResult?.amount)
              map.putString("availableBalance", transactionResult?.availableBalance)
              map.putString("cardApplication", transactionResult?.cardApplication)
              map.putString("label", transactionResult?.label)
              map.putString("holderName", transactionResult?.holderName)
              map.putString("extendedHolderName", transactionResult?.extendedHolderName)

              promise.resolve(map)
              executor.isTerminated
              System.gc()
          } catch (error: Exception) {
              Log.v("DoPaymentError", error.message ?: "Unknown error")
              promise.reject("DoPaymentPlugPagError", error)
              executor.isTerminated
              System.gc()
          }
      }
      executor.execute(runnableTask)
      executor.shutdown()
  }

  @ReactMethod
  override fun printByHtml(htmlContent: String, promise: Promise) {
      setAppIdentification();

      val executor: ExecutorService = Executors.newSingleThreadExecutor()

      val runnableTask = Runnable {
          val FILE_NAME = "ticket.jpg"
          val context = reactApplicationContext

          try {
              val html2Bitmap =
                  Html2Bitmap.Builder(context, WebViewContent.html(htmlContent)).build()

              val bitmap = html2Bitmap.bitmap
              if (bitmap != null) {
                  val folder = File(
                      context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                      "print_files"
                  )
                  if (!folder.exists()) folder.mkdirs()

                  val file = File(folder, FILE_NAME)
                  if (file.exists()) file.delete()

                  if (file.createNewFile()) {
                      FileOutputStream(file).use { fos ->
                          bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                      }

                      if (!file.exists()) {
                          promise.reject("ERRO", "Falha ao salvar o arquivo.")
                      }

                      val printerData = PlugPagPrinterData(
                          file.absolutePath,
                          printerQuality = 4,
                          steps = 0
                      )

                      val result = plugPag?.printFromFile(printerData)

                      if (result?.result == PlugPag.RET_OK) {
                          val map = Arguments.createMap()

                          map.putString("message", result.message)
                          promise.resolve("Impressão concluída com sucesso.")
                      } else {
                          promise.reject(
                              "ERRO_IMPRESSAO",
                              "Erro ao imprimir: ${result?.errorCode}"
                          )
                      }
                  }
              } else {
                  promise.reject("ERRO", "Falha ao gerar o Bitmap.")
              }
          } catch (e: Exception) {
              Log.v("PrintByHtmlError", e.message ?: "Unknown error")
              promise.reject("ERRO", "Falha ao processar o HTML.")
              executor.isTerminated
              System.gc()
          }
      }
      executor.execute(runnableTask);
      executor.shutdown()
  }

  @ReactMethod
  override fun reprintEstablishmentReceipt(promise: Promise) {
      setAppIdentification();

      val executor: ExecutorService = Executors.newSingleThreadExecutor()

      val runnableTask = Runnable {
          try {
              val result = plugPag?.reprintStablishmentReceipt()
              if (result?.result == PlugPag.RET_OK) {
                  val map = Arguments.createMap()

                  map.putString("message", result.message)
                  promise.resolve(map)
              } else {
                  promise.reject(
                      "ERRO_IMPRESSAO",
                      "Erro ao imprimir: ${result?.errorCode}"
                  )
              }
          } catch (error: Exception) {
              Log.v("ReprintEstablishmentReceiptError", error.message ?: "Unknown error")
              promise.reject("ReprintEstablishmentReceiptError", error)
              executor.isTerminated
              System.gc()
          }
      }
      executor.execute(runnableTask);
      executor.shutdown()
  }

  @ReactMethod
  override fun reprintCustomerReceipt(promise: Promise) {
      setAppIdentification();

      val executor: ExecutorService = Executors.newSingleThreadExecutor()

      val runnableTask = Runnable {
          try {
              val result = plugPag?.reprintCustomerReceipt()
              if (result?.result == PlugPag.RET_OK) {
                  val map = Arguments.createMap()

                  map.putString("message", result.message)
                  promise.resolve("Impressão concluída com sucesso.")
              } else {
                  promise.reject(
                      "ERRO_IMPRESSAO",
                      "Erro ao imprimir: ${result?.errorCode}"
                  )
              }
          } catch (error: Exception) {
              Log.v("ReprintCustomerReceiptError", error.message ?: "Unknown error")
              promise.reject("ReprintCustomerReceiptError", error)
              executor.isTerminated
              System.gc()
          }
      }
      executor.execute(runnableTask);
      executor.shutdown()
  }


  @ReactMethod
  override fun voidPayment(dataJSON: String, promise: Promise) {
      setAppIdentification()

      val voidPaymentData: PlugPagVoidData? = JsonParseUtils.getPlugPagVoidDataFromJson(dataJSON)

      plugPag!!.setEventListener(object : PlugPagEventListener {
          override fun onEvent(plugPagEventData: PlugPagEventData) {
              messageCard = plugPagEventData.customMessage
              val code = plugPagEventData.eventCode

              val params = Arguments.createMap()
              params.putInt("code", plugPagEventData.eventCode)
              params.putString("message", messageCard)
              reactContext
                  .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                  .emit("VOID_TRANSACTION_PROGRESS", params)
          }
      })

      val executor = Executors.newSingleThreadExecutor()

      val runnableTask = Runnable {
          try {
              val voidPaymentResult = plugPag!!.voidPayment(voidPaymentData!!)
              val map = Arguments.createMap()

              voidPaymentResult.result?.let { Log.v("result", it.toString()) }
              voidPaymentResult.errorCode?.let { Log.v("errorCode", it) }
              voidPaymentResult.message?.let { Log.v("message", it) }

              if (voidPaymentResult.result != PlugPag.RET_OK) {
                  map.putString("message", voidPaymentResult.message)
                  promise.reject(
                      "ERRO_VOID_PAYMENT",
                      "Erro ao cancelar venda: ${voidPaymentResult.errorCode}"
                  )
              }

              voidPaymentResult.result?.let { map.putInt("result", it) }
              map.putString("errorCode", voidPaymentResult.errorCode)
              map.putString("message", voidPaymentResult.message)
              map.putString("transactionCode", voidPaymentResult.transactionCode)
              map.putString("transactionId", voidPaymentResult.transactionId)
              map.putString("hostNsu", voidPaymentResult.hostNsu)
              map.putString("date", voidPaymentResult.date)
              map.putString("time", voidPaymentResult.time)
              map.putString("cardBrand", voidPaymentResult.cardBrand)
              map.putString("bin", voidPaymentResult.bin)
              map.putString("holder", voidPaymentResult.holder)
              map.putString("userReference", voidPaymentResult.userReference)
              map.putString("terminalSerialNumber", voidPaymentResult.terminalSerialNumber)
              map.putString("amount", voidPaymentResult.amount)
              map.putString("availableBalance", voidPaymentResult.availableBalance)
              map.putString("cardApplication", voidPaymentResult.cardApplication)
              map.putString("label", voidPaymentResult.label)
              map.putString("holderName", voidPaymentResult.holderName)
              map.putString("extendedHolderName", voidPaymentResult.extendedHolderName)

              promise.resolve(map)
              executor.isTerminated
              System.gc()
          } catch (error: java.lang.Exception) {
              Log.v("VoidPaymentError", error.message!!)

              promise.reject("VoidPaymentError", error)
              executor.isTerminated
              System.gc()
          }
      }

      executor.execute(runnableTask)
      executor.shutdown()
  }

  @ReactMethod
  override fun cancelRunningTransaction(promise: Promise) {
      setAppIdentification();

      val executor: ExecutorService = Executors.newSingleThreadExecutor()
      val runnableTask = Runnable {

          try {
              val result = plugPag?.abort()

              if (result?.result == PlugPag.RET_OK) {
                  val map = Arguments.createMap()
                  map.putString("message", "Sucesso ao cancelar a transação")
                  promise.resolve(map)
              } else {
                  promise.reject(
                      "ERRO_CANCEL",
                      "Erro ao cancelar a transação"
                  )
              }
          } catch (error: java.lang.Exception) {
              Log.v("CancelRunningTransactionError", error.message!!)

              promise.reject("CancelRunningTransactionError", error)
              executor.isTerminated
              System.gc()
          }
      }
      executor.execute(runnableTask);
      executor.shutdown()
  }

  override fun addListener(eventName: String?) = Unit;

  override fun removeListeners(count: Double) = Unit;

  companion object {
      const val NAME = "PagbankPos"
  }
}