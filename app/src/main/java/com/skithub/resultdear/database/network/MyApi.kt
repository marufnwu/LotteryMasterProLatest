package com.skithub.resultdear.database.network

import android.os.Build
import android.util.Base64
import android.util.Log
import com.google.android.exoplayer2.metadata.id3.ApicFrame
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.skithub.resultdear.BuildConfig
import com.skithub.resultdear.model.*
import com.skithub.resultdear.model.FirstPrizeLastDigitPage
import com.skithub.resultdear.model.MiddlePartPage
import com.skithub.resultdear.model.page.MiddlePage
import com.skithub.resultdear.model.response.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit
interface MyApi {
    @GET("get_similar_lottery_number_list.php?")
    suspend fun findSimilarLotteryNumberList(
        @Query("PageNumber") pageNumber: String,
        @Query("ItemCount") itemCount: String,
        @Query("LotteryNumber") lotteryNumber: String
    ): Response<LotteryNumberResponse>

    @GET("get_lottery_number_list_by_lottery_number.php?")
    suspend fun getLotteryNumberListUsingLotteryNumber(
        @Query("LotteryNumber") lotteryNumber: String
    ): Response<LotteryNumberResponse>

    @GET("get_middle_list_by_lottery_number.php?")
    suspend fun getMiddleListUsingLotteryNumber(
        @Query("LotteryNumber") lotteryNumber: String
    ): Response<LotteryNumberResponse>

     @FormUrlEncoded
     @POST("api/lottery.getMiddlePartDeatils.php")
     suspend fun getMiddlePartDeatils(
         @Field("userId") userId: String,
         @Field("middle") middle: String,
         @Field("range") range: String,
     ): Response<LotteryNumberResponse>

    @GET("get_2nd_middle_list_by_lottery_number.php?")
    suspend fun get2ndMiddleListUsingLotteryNumber(
        @Query("LotteryNumber") lotteryNumber: String
    ): Response<LotteryNumberResponse>

    @GET("get_1st_middle_list_by_lottery_number.php?")
    suspend fun get1stMiddleListUsingLotteryNumber(
        @Query("LotteryNumber") lotteryNumber: String
    ): Response<LotteryNumberResponse>

    @GET("get_lottery_number_list_by_win_date_time.php?")
    suspend fun getLotteryNumberListByDateTime(
        @Query("WinDate") winDate: String,
        @Query("WinTime") winTime: String,
        @Query("userId") userId: String
    ): Response<LotteryNumberResponse>

    @GET("get_lottery_number_list_by_win_date_slot.php?")
    suspend fun getLotteryNumberListByDateSlot(
        @Query("WinDate") winDate: String,
        @Query("SlotId") slotId: Int,
        @Query("userId") userId: String
    ): Response<LotteryNumberResponse>

    @GET("get_lottery_number_list_by_win_type.php?")
    suspend fun getLotteryNumberListByWinType(
        @Query("PageNumber") pageNumber: String,
        @Query("ItemCount") itemCount: String,
        @Query("WinType") winType: String
    ): Response<LotteryNumberResponse>

    @GET("get_duplicate_lottery_number_list.php?")
    suspend fun getDuplicateLotteryNumberList(
        @Query("PageNumber") pageNumber: String,
        @Query("ItemCount") itemCount: String
    ): Response<LotteryNumberResponse>

    @GET("get_duplicate_lottery_number_list_paging.php?")
    suspend fun getDuplicateLotteryNumberListPaging(
        @Query("PageNumber") pageNumber: String,
        @Query("ItemCount") itemCount: String
    ): Response<LotteryNumberPagingResponse>


    @GET("get_not_played_number.php")
    suspend fun getNotPlayedNumByMiddle(
        @Query("PageNumber") pageNumber: String,
        @Query("ItemCount") itemCount: String,
        @Query("Middle") middle: String,
    ): Response<LotteryNumberPagingResponse>

    @GET("get_lottery_number_plays_more.php?")
    suspend fun getMiddlePlaysMoreNumberList(
        @Query("PageNumber") pageNumber: String,
        @Query("ItemCount") itemCount: String
    ): Response<LotteryNumberResponse>

    @GET("get_lottery_number_plays_more_days.php")
    suspend fun getMiddlePlaysMoreNumberListInDays(
        @Query("PageNumber") pageNumber: String,
        @Query("ItemCount") itemCount: String,
        @Query("days") day: Int,
    ): Response<LotteryNumberResponse>


    @FormUrlEncoded
    @POST("api/page.firstPrizeLastDigit.php")
    suspend fun firstPrizeLastDigitPage(
        @Field("userId") userId: String,
    ): Response<FirstPrizeLastDigitPage>



    @FormUrlEncoded
    @POST("api/page.middlePart.php")
    suspend fun middlePartPage(
        @Field("userId") userId: String,
    ): Response<MiddlePartPage>

    @FormUrlEncoded
    @POST("api/page.maxMiddleByDays.php")
    suspend fun maxMiddleByDays(
        @Field("userId") userId: String,
    ): Response<MiddlePage>

    @FormUrlEncoded
    @POST("api/lottery.getMiddlePart.php")
    suspend fun getPatByMiddle(
        @Field("userId") userId: String,
        @Field("middle") middle: String,
    ): Response<MiddlePartResponse>


    @FormUrlEncoded
    @POST("api/lottery.firstPrizeLastDigitDetails.php")
    suspend fun firstPrizeLastDigitDetails(
        @Field("userId") userId: String,
        @Field("digit") digit: String,
        @Field("totalPages") totalPages: Int,
        @Field("pageNumber") pageNumber: Int,
    ): Response<NumberResponse>

    @GET("get_two_nd_middle_plays_more.php?")
    suspend fun gettwoNdMiddlePlaysMoreList(
        @Query("PageNumber") pageNumber: String,
        @Query("ItemCount") itemCount: String
    ): Response<LotteryNumberResponse>

    @GET("get_one_st_middle_plays_more.php?")
    suspend fun get1stNdMiddlePlaysMoreList(
        @Query("PageNumber") pageNumber: String,
        @Query("ItemCount") itemCount: String
    ): Response<LotteryNumberResponse>

    @GET("get_lottery_number_less.php?")
    suspend fun getMiddleLessNumberList(
        @Query("PageNumber") pageNumber: String,
        @Query("ItemCount") itemCount: String
    ): Response<LotteryNumberResponse>

    @GET("get_old_result_in_user_app.php?")
    suspend fun getLotteryResultList(
        @Query("PageNumber") pageNumber: String,
        @Query("ItemCount") itemCount: String
    ): Response<LotteryNumberResponse>

    @GET("get_bumper_result_list.php?")
    suspend fun getBumperLotteryResultList(
        @Query("PageNumber") pageNumber: String,
        @Query("ItemCount") itemCount: String
    ): Response<LotteryPdfResponse>

    @GET("get_paid_for_contact_info.php?")
    suspend fun getPaidContInfoList(
        @Query("pageId") pageNumber: String,
        @Query("userId") itemCount: String
    ): Response<PaidForContactModel>

    @GET("get_special_number_in_user_app.php?")
    suspend fun getSpecialNumberList(
        @Query("userId") itemCount: String
    ): Response<SpecialNumberModel>

    @GET("get_video_in_user_app.php?")
    suspend fun getVideoList(
        @Query("userId") itemCount: String
    ): Response<VideoTutorResponse>


    @GET("get_facebook_video_in_user_app.php?")
    suspend fun getFacebookVideoList(
        @Query("userId") itemCount: String
    ): Response<VideoTutorResponse>


    @GET("get_facebook_video_in_user_app.php?")
    suspend fun getFacebookVideoListPaging(
        @Query("page") page: Int = 1
    ): Response<VideoTutorResponse>

    @GET("get_facebook_video_with_view_count_in_user_app.php?")
    suspend fun getFacebookVideoListWithCountPaging(
        @Query("page") page: Int = 1
    ): Response<FacebookVideoResponse>

    @GET("get_video_in_result_info.php?")
    suspend fun getVideoListInResultInfo(
        @Query("userId") itemCount: String
    ): Response<VideoTutorResponse>

    @GET("get_ads_info.php?")
    suspend fun getAdsInfo(): Response<AdsImageResponse>


    @GET("get_service_info.php?")
    suspend fun getServiceInfo(
        @Query("userId") userId: String
    ): Response<ServiceInfoModel>

    @GET("get_home_tutorial.php?")
    suspend fun getHomeTutorialInfo(): Response<AdsImageResponse>

    @POST("upload_user_info.php?")
    suspend fun uploadUserInfo(
        @Query("Token") token: String,
        @Query("Phone") phone: String,
        @Query("RegistrationDate") registrationDate: String,
        @Query("ActiveStatus") activeStatus: String,
        @Query("countryCode") countryCode: String
    ): Response<UserInfoResponse>

    @POST("logout_user.php?")
    suspend fun logouUser(
        @Query("userId") token: String
    ): Response<UserInfoResponse>

    @GET("get_user_info_by_token.php?")
     fun getUserInfoByToken(
        @Query("userId") userId: String,
        @Query("Token") token: String
    ): Response<UserInfoResponse>


    //Maruf's works start

    @GET("api/helper.getBanner.php")
    suspend fun getBanner(
        @Query("bannerName") bannerName: String,
    ): Response<BannerRes>


    @GET("api/banner.getAddUserInfoBanner.php")
    fun getAddUserInfoBanner(
        @Query("bannerName") bannerName: String,
    ): Call<AddUserInfoDataResponse>

    @GET("api/helper.getWhatsapp.php")
    suspend fun getWhatsapp(
        @Query("place") place: String,
    ): Response<WhatsappResponse>

    @GET("api/audio.getAudio.php")
     fun getAudio(
        @Query("name") name: String,
    ): Call<AudioResponse>


     @GET("api/helper.addDeviceMetadata.php")
     fun addDeviceMetadata(
        @Query("userId") userId: String,
        @Query("phone") phone: String,
        @Query("versionCode") versionCode: Int,
        @Query("versionName") versionName: String,
        @Query("androidVersion") androidVersion: String,
        @Query("device") device: String,
        @Query("manufacturer") manufacturer: String,
        @Query("screenDensity") screenDensity: String,
        @Query("screenSize") screenSize: String
    ): Call<LotterySlotResponse>


     @GET("api/helper.searchDeviceMetadata.php")
     fun searchDeviceMetadata(
        @Query("androidVersion") androidVersion: String,
        @Query("device") device: String,
        @Query("manufacturer") manufacturer: String,
        @Query("screenDensity") screenDensity: String,
        @Query("screenSize") screenSize: String
    ): Call<MetadataSearchResponse>


    @FormUrlEncoded
    @POST("api/lottery.getLotteryResultTime.php")
    fun getLotteryResultTime(
        @Field("checkActive") checkActive: Boolean
    ): Call<LotterySlotResponse>

    @Multipart
    @POST("/convert/upload.php")
    fun uploadImage(@Part file: Part?): Call<JsonObject?>?


    @FormUrlEncoded
    @POST("api/contact.getCustContactNumber.php")
    fun getCustContactNumber(
        @Field("page") page: String
    ): Response<ContactListResponse>


    @FormUrlEncoded
    @POST("api/contact.getCustContactNumberWithBanner.php")
    fun getCustContactNumberWithBanner(
        @Field("page") page: String,
        @Field("call") banner: String,
    ): Call<ContactListBannerResponse>


    @FormUrlEncoded
    @POST("api/dialog.getDialogInfo.php")
    fun getDialogInfo(
        @Field("activity") activity: String,
    ): Call<ActivityDialogResponse>

    @FormUrlEncoded
    @POST("getVideos.php")
    fun getVideos(
        @Field("page") page: Int,
    ): Call<VideoResponse>

    @GET("getVideosInResultInfo.php")
    fun getVideosInResultInfo(
    ): Call<VideoResponse>

    @GET("api/helper.getVideoType.php")
    fun getVideoType(
    ): Call<VideoTypeResposne>

    @GET("api/helper.fbShareContent.php")
    fun fbShareContent(
    ): Call<FbShareContentResponse>

    @FormUrlEncoded
    @POST("api/get10DaysOldNumber.php")
    fun get10DaysOldNumber(
        @Field("startNumber") startNumber: String,
        @Field("endNumber") endNumber: String,
    ): Call<LotterySerialCheckResponse>

    @FormUrlEncoded
    @POST("api/get10DaysOldNumber_v2.php")
    fun get10DaysOldNumberV2(
        @Field("startNumber") startNumber: String,
        @Field("endNumber") endNumber: String,
        @Field("userId") userId: Int,
    ): Call<LotterySerialCheckResponse>

    @FormUrlEncoded
    @POST("api/license.getSerialCheckLicense.php")
    fun getSerialCheckLicense(
        @Field("userId") userId: String
    ): Call<SerialCheckLicenseResponse>

    @FormUrlEncoded
    @POST("api/lotterySerialCheckInfo.php")
    fun lotterySerialCheckInfo(
        @Field("userId") userId: String
    ): Call<LotterySerialCheckInfoResponse>

    @FormUrlEncoded
    @POST("api/audio.getAudioTutorial.php")
    fun getAudioTutorial(
        @Field("page") page: Int
    ): Call<AudioTutorialResponse>

    @GET("api/video.getRandomVideo.php")
    suspend fun getRandomVideo(
    ): Response<GenericApiResponse<List<FacebookVideoModel>>>

    @GET("api/getButtonBuyInfo.php")
    fun getButtonBuyInfo(
    ): Call<ButtonBuyRuleResponse>

    @GET("api/lmpclass.getVideo.php")
    fun getLmpClassVideo(
        @Query("page") page: Int,
    ): Call<lmpVideoResponse>


    @GET("api/lmpclass.getSpecialVideo.php")
    fun getSpecialVideo(
        @Query("page") page: Int,
    ): Call<SpecialVideoResponse>


    @GET("add_facebook_video_views.php")
    suspend fun addFbVideoView(
        @Query("userId") userId: String,
        @Query("videoId") videoId: String,
    ): Response<GenericResponse>


    @FormUrlEncoded
    @POST("api/user.addUserDetails.php")
    fun addUserDetails(
        @Field("userId") userId: String,
        @Field("name") name: String,
        @Field("zila") zila: String,
        @Field("thana") thana: String,
        @Field("village") village: String,
        @Field("postOffice") postOffice: String,
        @Field("pinCode") pinCode: String,
        @Field("phone") phone: String,
        @Field("whatsApp") whatsApp: String,
    ): Call<GenericResponse>


    @FormUrlEncoded
    @POST("add_location.php")
    fun addLocation(
        @Field("userId") userId: String,
        @Field("deviceId") deviceId: String,
        @Field("district") district: String,
        @Field("city") city: String,
    ): Call<GenericResponse>


    @FormUrlEncoded
    @POST("check_device_block.php")
    fun checkDeviceBlock(
        @Field("deviceId") deviceId: String,
    ): Call<Int>


    //Maruf's work end here

    companion object {

        @Volatile
        private var myApiInstance: MyApi? = null
        private val LOCK = Any()

        operator fun invoke() = myApiInstance ?: synchronized(LOCK) {
            myApiInstance ?: createClient().also {
                myApiInstance = it
            }
        }


        private fun createClient(): MyApi {

            val firebaseCrashlytics = Firebase.crashlytics

            val AUTH: String = "Basic ${
                Base64.encodeToString(
                    ("${BuildConfig.USER_NAME}:${BuildConfig.USER_PASSWORD}").toByteArray(),
                    Base64.NO_WRAP
                )
            }"



            val interceptor = run {
                val httpLoggingInterceptor = HttpLoggingInterceptor()
                httpLoggingInterceptor.apply {
                    if(BuildConfig.DEBUG){
                        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
                    }else{
                        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.NONE
                    }
                }
            }




            val okHttpClient: OkHttpClient = OkHttpClient.Builder()
                .readTimeout(2, TimeUnit.MINUTES)
                .connectTimeout(2, TimeUnit.MINUTES)
                .callTimeout(2, TimeUnit.MINUTES)
                .addInterceptor(interceptor)
                .addInterceptor { chain ->
                    try {
                        val request = chain.request()
                        val response = chain.proceed(request)

                        response
                    }catch (e :Exception){
                        e.message?.let {
                            firebaseCrashlytics.log(chain.request().url.toUri().toString())
                            firebaseCrashlytics.log(e.stackTraceToString())

                            Log.d("OkHttpError", e.stackTraceToString())
                        }
                        chain.proceed(chain.request())
                    }
                }
                .addInterceptor { chain ->
                    val original: Request = chain.request()
                    val requestBuilder: Request.Builder = original.newBuilder()
                        .addHeader("Authorization", AUTH)
                        .method(original.method, original.body)
                    val request: Request = requestBuilder.build()
                    chain.proceed(request)
                }
                .build()

            val gsonBuilder = GsonBuilder()
            gsonBuilder.setLenient()
            val gson = gsonBuilder.create()

            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(okHttpClient)
                .build()
                .create(MyApi::class.java)
        }


    }


}