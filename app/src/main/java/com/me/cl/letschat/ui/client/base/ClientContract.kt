package com.me.cl.letschat.ui.client.base

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Intent
import com.me.cl.letschat.base.BaseInteractor
import com.me.cl.letschat.base.BasePresenter
import com.me.cl.letschat.base.BaseView
import com.me.cl.letschat.base.ClickDevicesItem

/**
 * Created by CL on 3/8/18.
 */
interface ClientView: BaseView{
    fun showToast(message:String)
    fun checkBleEnable(): Boolean
    fun checkBlueToothSupport(): Boolean
    fun finishSelf()
    fun checkBlueToothEnabled(): Boolean
    fun requestEnableBlueTooth(requestCode: Int)
    fun initDeviceList()
    fun startDiscover()
    fun stopDiscover()
    fun connectToGattServer(device: BluetoothDevice?)
    fun maintainConnectState(state: Int)
    fun discoverServices()
    fun setCharacteristicNotification(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic?, enable: Boolean)
}
interface ClientPresenter: BasePresenter<ClientView> {
    fun handleInit()
    fun handleResume()
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    fun handleDevicesClick(event: ClickDevicesItem)
    fun handleConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int)
    fun handleServicesDiscovered(gatt: BluetoothGatt, status: Int)
    fun handleCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?)
}
interface ClientInteractor: BaseInteractor{
    fun getStringFromResource(resId: Int): String
    fun getReadAbleCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic?
    fun getWriteAbleCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic?
}