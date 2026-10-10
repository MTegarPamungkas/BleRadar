package com.example.bleradar.data.ble

import com.juul.kable.Advertisement
import com.juul.kable.Scanner

expect fun createBleScanner(): Scanner<Advertisement>
