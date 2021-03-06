package com.me.cl.letschat.ui.client

import android.app.Activity
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Intent
import com.me.cl.letschat.R
import com.me.cl.letschat.base.*
import com.me.cl.letschat.ui.client.ClientInteractorImpl.Companion.CACHE_WRITEABLE_CHARACTER
import com.me.cl.letschat.ui.client.base.ClientInteractor
import com.me.cl.letschat.ui.client.base.ClientPresenter
import com.me.cl.letschat.ui.client.base.ClientView
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.TimeUnit
import javax.inject.Inject



/**
 * Created by CL on 3/8/18.
 */
class ClientPresenterImpl @Inject constructor(var interactor: ClientInteractor?): ClientPresenter {
    var clientView: ClientView?=null

    override val disposables= CompositeDisposable()

    val REQUEST_ENABLE_BT = 1

    override fun handleInit(){
        noBleFinish()
        noBlueToothFinish()
    }

    override fun handleResume(){
        notEnableRequest()
        clientView?.run {
            initDeviceList()
            startDiscoverLimited()
        }
    }

    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            clientView?.finishSelf()
        }
    }

    override fun handleDevicesClick(event: ClickDevicesItem){
        event.currentDevice?.let {
            clientView?.connectToGattServer(it)
        }
    }

    override fun handleConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int){
        if (newState== BluetoothProfile.STATE_CONNECTED) {
                clientView?.apply {
                    maintainConnectState(STATE_CONNECTED)
                    discoverServices()
                }
        }else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                clientView?.apply {
                    maintainConnectState(STATE_DISCONNECTED)
                    showTitleProgressBar(false)
                    Completable.create {  showToast("disconnected")}.subscribeOn(AndroidSchedulers.mainThread()).subscribe()
                }
        }
    }

    override fun handleServicesDiscovered(gatt: BluetoothGatt, status: Int){
        Completable.create {  clientView?.showTitleProgressBar(false) }.subscribeOn(AndroidSchedulers.mainThread()).subscribe()


        if (status == BluetoothGatt.GATT_SUCCESS){
            interactor?.run {
                val writable=getWriteAbleCharacteristic(gatt)
                val readable=getReadAbleCharacteristic(gatt)
                writable?.let {
                    saveToStrongCache(CACHE_WRITEABLE_CHARACTER,it)
                }
                readable?.let {
                    clientView?.setCharacteristicNotification(gatt,it,true)
                }
                if(writable!=null&&readable!=null){
                    clientView?.startChatActivity()
                    return
                }
            }
        }
            Completable.create {  clientView?.showToast("Can't create session") }.subscribeOn(AndroidSchedulers.mainThread()).subscribe()


    }

    override fun handleCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?){
        if (CHARACTERISTIC_READABLE_UUID == characteristic?.uuid) {
            characteristic?.getStringValue(0)?.let {
                EventBus.getDefault().postSticky(AddNewChat(DIRECTION_RECEIVE,it))
            }
        }
    }

    override fun handleSendMessage(event:SendMessageUseBle){
        if (event.fromWhere==FROM_CLIENT){
            interactor?.run {
                val characteristic=getFromStrongCache(CACHE_WRITEABLE_CHARACTER)
                if(characteristic is BluetoothGattCharacteristic){
                    if (clientView?.writeCharacteristic(characteristic,event.message)==true)
                        EventBus.getDefault().postSticky(AddNewChat(DIRECTION_SEND,event.message))
                }
            }
        }


    }

    private fun startDiscoverLimited(){
        addDisposable(Single.timer(SCAN_PERIOD,TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe { t1, t2 ->
            clientView?.stopDiscover()
        })
        clientView?.startDiscover()
    }

    private fun notEnableRequest() {
        if (clientView?.checkBleEnable() != true) {
            clientView?.requestEnableBlueTooth(REQUEST_ENABLE_BT)
        }
    }

    override fun setView(view: ClientView) {
        clientView=view
    }


    fun noBleFinish(){
        if (clientView?.checkBleEnable() != true) {
            interactor?.run {
                clientView?.showToast(getStringFromResource(R.string.ble_not_supported))
            }
            clientView?.finishSelf()
        }
    }
    fun noBlueToothFinish(){
        if (clientView?.checkBlueToothSupport() != true) {
            interactor?.run {
                clientView?.showToast(getStringFromResource(R.string.error_bluetooth_not_supported))
            }
            clientView?.finishSelf()
        }
    }
}