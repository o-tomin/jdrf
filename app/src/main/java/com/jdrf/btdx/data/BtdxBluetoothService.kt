package com.jdrf.btdx.data

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import java.util.UUID

data class BtdxBluetoothService(
    val uuid: UUID,
    val type: Type,
    val characteristics: List<BtdxCharacteristic>,
    val includedServices: List<BtdxBluetoothService>,
) {
    sealed class Type(val type: Int) {

        data object Primary : Type(0)
        data object Secondary : Type(1)
        data class Unknown(val unknownType: Int) : Type(unknownType)

        companion object {
            fun toType(type: Int): Type {
                return when (type) {
                    0 -> Primary
                    1 -> Secondary
                    else -> Unknown(type)
                }
            }
        }
    }

    companion object {
        fun create(
            bluetoothService: BluetoothGattService?
        ): BtdxBluetoothService {
            return BtdxBluetoothService(
                uuid = bluetoothService?.uuid!!,
                type = Type.toType(bluetoothService.type),
                characteristics = bluetoothService.characteristics!!.map(BtdxCharacteristic::create),
                includedServices = bluetoothService.includedServices!!.map(::create)
            )
        }
    }
}

data class BtdxCharacteristic(
    val bluetoothGattCharacteristic: BluetoothGattCharacteristic,
    val instanceId: Int,
    val uuid: UUID,
    val properties: List<Property>,
    val btdxPermissions: List<BtdxPermission>,
    val value: String,
    val writeType: WriteType,
    val descriptors: List<BtdxDescriptor>,
) {

    enum class Property {
        READ, WRITE, NOTIFY, INDICATE, NONE
    }

    sealed class WriteType {
        data object WriteTypeDefault : WriteType()
        data object WriteTypeNoResponse : WriteType()
        data object WriteTypeSigned : WriteType()
        data class Unknown(val writeType: Int) : WriteType()

        companion object {
            fun toWriteType(writeType: Int): WriteType {
                return when (writeType) {
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT -> WriteTypeDefault
                    BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE -> WriteTypeNoResponse
                    BluetoothGattCharacteristic.WRITE_TYPE_SIGNED -> WriteTypeSigned
                    else -> Unknown(writeType)
                }
            }
        }
    }

    companion object {
        fun create(bluetoothGattCharacteristic: BluetoothGattCharacteristic?): BtdxCharacteristic {
            return BtdxCharacteristic(
                bluetoothGattCharacteristic = bluetoothGattCharacteristic!!,
                uuid = bluetoothGattCharacteristic.uuid!!,
                properties = bluetoothGattCharacteristic.properties.let {
                    mutableListOf<Property>().apply {
                        if (it and BluetoothGattCharacteristic.PROPERTY_READ != 0) {
                            add(Property.READ)
                        }
                        if (it and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) {
                            add(Property.WRITE)
                        }
                        if (it and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) {
                            add(Property.NOTIFY)
                        }
                        if (it and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) {
                            add(Property.INDICATE)
                        }
                        if (this.isEmpty()) {
                            add(Property.NONE)
                        }
                    }
                }.toList(),
                btdxPermissions = toBtdxPermissions(bluetoothGattCharacteristic.permissions),
                value = bluetoothGattCharacteristic.value?.decodeToString() ?: "",
                writeType = WriteType.toWriteType(bluetoothGattCharacteristic.writeType),
                descriptors = bluetoothGattCharacteristic.descriptors.map(BtdxDescriptor::create),
                instanceId = bluetoothGattCharacteristic.instanceId
            )
        }
    }
}

data class BtdxDescriptor(
    val bluetoothGattDescriptor: BluetoothGattDescriptor,
    val uuid: UUID,
    val value: String,
    val permissions: List<BtdxPermission>
) {

    companion object {
        fun create(
            bluetoothGattDescriptor: BluetoothGattDescriptor
        ): BtdxDescriptor {
            return BtdxDescriptor(
                bluetoothGattDescriptor = bluetoothGattDescriptor,
                uuid = bluetoothGattDescriptor.uuid,
                value = bluetoothGattDescriptor.value?.decodeToString() ?: "",
                permissions = toBtdxPermissions(bluetoothGattDescriptor.permissions)
            )
        }
    }
}

enum class BtdxPermission {
    READ, READ_ENCRYPTED, READ_ENCRYPTED_MITM, WRITE, WRITE_ENCRYPTED_MITM, WRITE_SIGNED, WRITE_SIGNED_MITM, NONE
}

private fun toBtdxPermissions(permission: Int): List<BtdxPermission> {
    return mutableListOf<BtdxPermission>().apply {
        if (permission and BluetoothGattCharacteristic.PERMISSION_READ != 0) {
            add(BtdxPermission.READ)
        }
        if (permission and BluetoothGattCharacteristic.PERMISSION_WRITE != 0) {
            add(BtdxPermission.WRITE)
        }

        if (permission and BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED != 0) {
            add(BtdxPermission.READ_ENCRYPTED)
        }

        if (permission and BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM != 0) {
            add(BtdxPermission.READ_ENCRYPTED_MITM)
        }

        if (permission and BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED != 0) {
            add(BtdxPermission.WRITE_SIGNED)
        }

        if (permission and BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM != 0) {
            add(BtdxPermission.WRITE_ENCRYPTED_MITM)
        }

        if (permission and BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED_MITM != 0) {
            add(BtdxPermission.WRITE_SIGNED_MITM)
        }

        if (this.isEmpty()) {
            add(BtdxPermission.NONE)
        }
    }.toList()
}
