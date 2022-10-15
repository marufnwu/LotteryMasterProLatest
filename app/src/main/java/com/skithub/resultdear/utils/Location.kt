package com.skithub.resultdear.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.location.Location
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.Global.getString
import android.system.Os
import android.util.Log
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.common.api.internal.LifecycleCallback
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.skithub.resultdear.BuildConfig
import com.skithub.resultdear.R
import com.skithub.resultdear.model.LocationCheck
import com.skithub.resultdear.utils.GenericDialog.context
import com.skithub.resultdear.utils.MyExtensions.lifecycleOwner
import com.skithub.resultdear.utils.MyExtensions.shortToast
import java.util.*


class Location(private val ctx: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val audioLoadingDialog = AudioLoadingDialog(ctx as Activity, false)
    private var isPause: Boolean = false
    private var isPermissionOverride = false
    private var permissionRequest = 0

    init {
        initActivityLifeCycle()
        initAudioPlayer()
    }

    private fun initActivityLifeCycle() {
        ctx.lifecycleOwner()?.lifecycle
            ?.addObserver(object: LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {

                    if(event== Lifecycle.Event.ON_START){
                        //onStart
                    }else if(event == Lifecycle.Event.ON_CREATE){
                        //on create
                    }else if(event == Lifecycle.Event.ON_DESTROY){
                        //onDestroyed

                    }else if(event == Lifecycle.Event.ON_PAUSE){
                        //onPause
                        mediaPlayer?.release()
                        mediaPlayer = null
                        audioLoadingDialog.hide()
                    }else if(event == Lifecycle.Event.ON_RESUME){
                        //onResume

                    }


                    when (event) {
                        Lifecycle.Event.ON_CREATE -> Log.d("", "")
                        else -> {}
                    }
                }


            })
    }

    private fun initAudioPlayer() {
        if(mediaPlayer==null){
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
            }


            mediaPlayer!!.setOnPreparedListener {
                it.start()
                audioLoadingDialog.show()
            }

            mediaPlayer!!.setOnErrorListener { p0, p1, p2 ->
                Toast.makeText(ctx, "error", Toast.LENGTH_SHORT).show()
                audioLoadingDialog.hide()
                requestPermission()
                true
            }

            mediaPlayer!!.setOnCompletionListener {
                requestPermission()
                audioLoadingDialog.hide()
            }

        }

    }


    companion object{
        var locationCheck : LocationCheck? = null
        private var locationUpdated = false

        var blockAreaList = mutableListOf<BlockLocation>()
        public  fun isPermissionGranted(ctx : Context) : Boolean{
            if (
                ActivityCompat.checkSelfPermission(ctx,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                    (ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                return true
            }
            return false

        }


        fun isGpsEnable(ctx: Context) : Boolean{
            var gps_enabled = false
            var network_enabled = false
            val locationManager = ctx.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager



            try {
                network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            } catch (ex: Exception) {
            }

            return network_enabled
        }
    }

    var genericDialog : GenericDialog = GenericDialog.make(ctx)

    interface OnAreaBlockListener{
        fun onSuccess(isBlock : Boolean)
    }

    var areaBlockListener : ((Boolean)->Unit)? = {  }


    private fun getBlockList(): MutableList<BlockLocation>{
        blockAreaList.add(BlockLocation(22.778734030802077, 88.38398102917022))
        blockAreaList.add(BlockLocation(22.7893404803951, 88.38626351131613))
        blockAreaList.add(BlockLocation(22.791620, 88.385736))
        blockAreaList.add(BlockLocation(22.790127572028727, 88.38076834547499))
        blockAreaList.add(BlockLocation(22.785259147700597, 88.37688248874915))
        blockAreaList.add(BlockLocation(22.784286809953272, 88.3788868425374))

        blockAreaList.add(BlockLocation(22.78909095087294, 88.38902425811052))
        blockAreaList.add(BlockLocation(22.78830419157986, 88.38716351352163))
        blockAreaList.add(BlockLocation(22.786711133493583, 88.38603593604948))
        blockAreaList.add(BlockLocation(22.78464970956465, 88.38469284703775))

        blockAreaList.add(BlockLocation(22.78619982226388, 88.38456144713732))
        blockAreaList.add(BlockLocation(22.79058791034023, 88.38492359152754))
        blockAreaList.add(BlockLocation(22.794646686280355, 88.3826607358921))
        blockAreaList.add(BlockLocation(22.782590539670718, 88.37019398949589))

        //blockAreaList.add(BlockLocation(22.78680664, 88.37458549))

        return blockAreaList
    }





    private fun isLocationCheckEnable():Boolean{

        if(locationCheck!=null){

            return locationCheck!!.enable
        }

        return false
    }

     fun isBlockArea(){

         return

         Log.d("Location", "LocationCheck ${isLocationCheckEnable()}")


        if(isLocationCheckEnable()){
            if(isPermissionGranted(ctx)){
                Log.d("Location", "Permission granted")

                checkGpsEnable()



            }else{
                Log.d("Location", "Permission not granted")
                if(locationCheck!=null){
                    if( locationCheck!!.voice!=null){
                        mediaPlayer!!.setDataSource(locationCheck!!.voice)
                        mediaPlayer!!.prepareAsync()
                        return

                    }else{
                        requestPermission()
                    }
                    return
                }
                requestPermission()
            }
        }else{
            areaBlockListener?.invoke(false)
        }
    }



    private fun checkGpsEnable() {

        if (!isGpsEnable(ctx)) {
            // notify user

            genericDialog.setBodyText("Your location service is disable. Please enable it")
                .setNegativeButtonText("Cancel")
                .setPositiveButtonText("Enable")
                .setOnGenericDialogListener(object : GenericDialog.OnGenericDialogListener {
                    override fun onPositiveButtonClick(dialog: GenericDialog?) {
                        dialog?.hideDialog()
                        context.startActivity(
                            Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS
                            )
                        )
                    }

                    override fun onNegativeButtonClick(dialog: GenericDialog?) {
                        areaBlockListener?.invoke(true)
                        dialog?.hideDialog()
                        (ctx as Activity).finish()

                    }


                    override fun onToast(message: String?) {}
                }).init()
                .showDialog()

        }else{
            checkLocation()
        }

    }

    @SuppressLint("MissingPermission")
    private fun updateLocation(){
        val locationManager = ctx.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000L,500.0f, object : LocationListener{
            override fun onLocationChanged(loc: Location) {


                locationUpdated = true

                getCompleteAddressString(loc.latitude, loc.longitude)

                val centerLoc = Location("")


                val listOfArea = getBlockList()
                var isInsideBlockArea = true;

                for (area in listOfArea){
                    centerLoc.latitude = area.latitude
                    centerLoc.longitude = area.longitude



                    val distanceInMeter = centerLoc.distanceTo(loc)
                    if(distanceInMeter<1000){
                        //within the 1km


                        isInsideBlockArea = true
                        //areaBlockListener?.invoke(true)
                        Log.d("Location", "inside ${area.latitude} ${area.longitude}")
                        break

                    }else{
                        //outside of 1km
                        //areaBlockListener?.invoke(false)
                        Log.d("Location", "outside ${area.latitude} ${area.longitude}")

                        isInsideBlockArea = false

                    }




                }

                Log.d("Location", "IsAreBlock $isInsideBlockArea")

                if (isInsideBlockArea){
                    showAreaBlockMessage()
                    areaBlockListener?.invoke(true)
                }else{
                    areaBlockListener?.invoke(false)
                }


            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {


            }

            override fun onProviderDisabled(provider: String) {
                super.onProviderDisabled(provider)
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun lastLocation(){
        val locationManager = ctx.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        val loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if(loc!=null){
            getCompleteAddressString(loc.latitude, loc.longitude)

            val centerLoc = Location("")


            val listOfArea = getBlockList()
            var isInsideBlockArea = true;

            for (area in listOfArea){
                centerLoc.latitude = area.latitude
                centerLoc.longitude = area.longitude



                val distanceInMeter = centerLoc.distanceTo(loc)
                if(distanceInMeter<1000){
                    //within the 1km


                    isInsideBlockArea = true
                    //areaBlockListener?.invoke(true)
                    Log.d("Location", "inside ${area.latitude} ${area.longitude}")
                    break

                }else{
                    //outside of 1km
                    //areaBlockListener?.invoke(false)
                    Log.d("Location", "outside ${area.latitude} ${area.longitude}")

                    isInsideBlockArea = false

                }




            }

            Log.d("Location", "IsAreBlock $isInsideBlockArea")

            if (isInsideBlockArea){
                showAreaBlockMessage()
                areaBlockListener?.invoke(true)
            }else{
                areaBlockListener?.invoke(false)
            }



        }else{

        }


    }

    @SuppressLint("MissingPermission")
    private fun checkLocation() {
        if(locationUpdated){
            lastLocation()
        }else{
            updateLocation()
        }
    }

    public fun showAreaBlockMessage() {

        genericDialog
            .setNegativeButtonText("Exit")
            .setPositiveButtonText("Ok")
            .setBodyText(ctx.getString(R.string.location_block_msg))
            .setCancelable(false)
            .setShowPositiveButton(false)
            .setOnGenericDialogListener(object : GenericDialog.OnGenericDialogListener {
            override fun onPositiveButtonClick(dialog: GenericDialog?) {
                dialog?.hideDialog()
            }

            override fun onNegativeButtonClick(dialog: GenericDialog?) {

                dialog?.hideDialog()
                areaBlockListener?.invoke(true)
                (ctx as Activity).finish()

            }


            override fun onToast(message: String?) {}
        }).init()
            .showDialog()
    }


    private fun requestPermission(){
        permissionRequest++
        genericDialog
            .setNegativeButtonText("Exit")
            .setPositiveButtonText("Ok")
            .setBodyText(locationCheck?.body)
            .setCancelable(false)
            .setImage(GenericDialog.Image(locationCheck?.image, locationCheck?.link))
            .setOnGenericDialogListener(object : GenericDialog.OnGenericDialogListener {
                override fun onPositiveButtonClick(dialog: GenericDialog?) {
                    dialog?.hideDialog()
                    Dexter.withContext(ctx)
                        .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        .withListener(object : PermissionListener {
                            override fun onPermissionGranted(response: PermissionGrantedResponse) {
                                checkGpsEnable()
                            }

                            override fun onPermissionDenied(response: PermissionDeniedResponse) {
                                ctx.shortToast("Permission Denied")


                                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q ) {
                                    requestPermission()
                                    return
                                }
                                context.shortToast(isPermissionOverride.toString())

                                showPermissionSettingsPopup()
                            }

                            override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                                context.shortToast("overrride")
                                isPermissionOverride = true
                                token?.continuePermissionRequest()
                            }

                        }).onSameThread().check()
                }

                override fun onNegativeButtonClick(dialog: GenericDialog?) {
                    areaBlockListener?.invoke(true)
                    dialog?.hideDialog()
                    (ctx as Activity).finish()

                }


                override fun onToast(message: String?) {}
            }).init()
            .showDialog()





    }

    private fun showPermissionSettingsPopup() {
        genericDialog.setBodyText(locationCheck?.gotoSettingBody)
            .setNegativeButtonText("Exit")
            .setImage(GenericDialog.Image(locationCheck?.image2, locationCheck?.link2))
            .setPositiveButtonText("Ok")
            .setOnGenericDialogListener(object : GenericDialog.OnGenericDialogListener {
                override fun onPositiveButtonClick(dialog: GenericDialog?) {
                    dialog?.hideDialog()
                    openLocationPermissionSettings()
                }

                override fun onNegativeButtonClick(dialog: GenericDialog?) {
                    areaBlockListener?.invoke(true)
                    dialog?.hideDialog()
                    (ctx as Activity).finish()

                }


                override fun onToast(message: String?) {}
            }).init()
            .showDialog()
    }

    private fun openLocationPermissionSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
        intent.data = uri
        ctx.startActivity(intent)
    }

    @SuppressLint("LongLogTag")
    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double): String? {
        Log.d("Location", "$LATITUDE $LONGITUDE")
        var strAdd = ""
        val geocoder = Geocoder(ctx, Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            if (addresses != null) {
                val returnedAddress: Address = addresses[0]

                Log.d("Location", returnedAddress.postalCode)

                val strReturnedAddress = StringBuilder("")
                for (i in 0..returnedAddress.getMaxAddressLineIndex()) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                strAdd = strReturnedAddress.toString()
                Log.w("My Current loction address", strReturnedAddress.toString())

            } else {
                Log.w("My Current loction address", "No Address returned!")

            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.w("My Current loction address", "Canont get Address!")

        }

        Log.d("Location", strAdd)


        return strAdd
    }

    inner class BlockLocation( lat: Double, long: Double){
        val latitude : Double = lat
        val longitude : Double = long
    }
}