package io.shubham0204.smollmandroid

import android.content.Context
import android.content.Intent
import android.net.Uri

class AndroidIntentHandler(private val context: Context) {

    fun processAndExecute(llmResponse: String) {
        when {
            llmResponse.contains("call:") -> handlePhoneCall(llmResponse)
            llmResponse.contains("sms:") -> handleSMS(llmResponse)
            llmResponse.contains("open:") -> handleAppLaunch(llmResponse)
            llmResponse.contains("setting:") -> handleSettings(llmResponse)
        }
    }

    private fun handlePhoneCall(response: String) {
        val phoneNumber = extractPhoneNumber(response)
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        context.startActivity(intent)
    }

    private fun handleSMS(response: String) {
        val (number, message) = extractSMSDetails(response)
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$number")
            putExtra("sms_body", message)
        }
        context.startActivity(intent)
    }

    private fun handleAppLaunch(response: String) {
        val packageName = response.substringAfter("open:").trim()
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        context.startActivity(intent)
    }

    private fun handleSettings(response: String) {
        // This is a simplified example. A more robust solution would handle various settings.
        val setting = response.substringAfter("setting:").trim()
        when (setting) {
            "wifi_on" -> {
                // To toggle Wi-Fi, you might need to direct the user to the Wi-Fi settings screen.
                context.startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS))
            }
            "bluetooth_off" -> {
                context.startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))
            }
        }
    }

    private fun extractPhoneNumber(response: String): String {
        return response.substringAfter("call:").trim()
    }

    private fun extractSMSDetails(response: String): Pair<String, String> {
        val parts = response.substringAfter("sms:").split(":")
        val number = parts[0].trim()
        val message = parts.getOrNull(1)?.trim() ?: ""
        return Pair(number, message)
    }
}
