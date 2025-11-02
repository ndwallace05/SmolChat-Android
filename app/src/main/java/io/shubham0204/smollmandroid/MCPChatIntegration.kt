package io.shubham0204.smollmandroid

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import dev.jasonpearson.mcp.mcp_tool.MCPTool
import java.io.File


class MCPChatIntegration(private val context: Context) {

    fun createMCPTools(): List<MCPTool> {
        return listOf(
            createAndroidTool(),
            createFileTool(),
            createContactTool()
        )
    }

    private fun createAndroidTool(): MCPTool {
        return MCPTool(
            name = "android_control",
            description = "Control Android device functions",
            parameters = mapOf(
                "action" to "string",
                "parameters" to "map"
            )
        ) { params ->
            when (params["action"]) {
                "launch_app" -> launchApp(params["packageName"] as String)
                "send_sms" -> sendSMS(params)
                "make_call" -> makeCall(params["number"] as String)
                else -> "Unknown action"
            }
        }
    }

    private fun createFileTool(): MCPTool {
        return MCPTool(
            name = "file_system",
            description = "Interact with the file system",
            parameters = mapOf(
                "action" to "string",
                "path" to "string"
            )
        ) { params ->
            when (params["action"]) {
                "read_file" -> readFile(params["path"] as String)
                else -> "Unknown action"
            }
        }
    }

    private fun createContactTool(): MCPTool {
        return MCPTool(
            name = "contact_tool",
            description = "Search for contacts",
            parameters = mapOf(
                "name" to "string"
            )
        ) { params ->
            searchContact(params["name"] as String)
        }
    }

    private fun launchApp(packageName: String): String {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        context.startActivity(intent)
        return "Launched $packageName"
    }

    private fun sendSMS(params: Map<String, Any>): String {
        val number = params["number"] as String
        val message = params["message"] as String
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$number")
            putExtra("sms_body", message)
        }
        context.startActivity(intent)
        return "SMS sent to $number"
    }

    private fun makeCall(number: String): String {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$number")
        }
        context.startActivity(intent)
        return "Calling $number"
    }

    private fun readFile(path: String): String {
        return try {
            File(path).readText()
        } catch (e: Exception) {
            "Error reading file: ${e.message}"
        }
    }

    private fun searchContact(name: String): String {
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$name%"),
            null
        )
        val contacts = mutableListOf<String>()
        cursor?.use {
            while (it.moveToNext()) {
                val contactName = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                contacts.add("$contactName: $phoneNumber")
            }
        }
        return if (contacts.isNotEmpty()) {
            contacts.joinToString("\n")
        } else {
            "No contacts found for $name"
        }
    }

    fun processMCPCommand(command: String) {
        // This is a simplified approach. A more robust solution would involve a proper command parser.
        val parts = command.split(":")
        val toolName = parts[1].trim()
        val action = parts[2].trim()
        val params = parts.getOrNull(3)?.trim()

        val tools = createMCPTools()
        val tool = tools.find { it.name == toolName }

        if (tool != null) {
            val toolParams = mutableMapOf<String, Any>()
            toolParams["action"] = action
            if (params != null) {
                // This is a very basic parameter parser. A real implementation would be more complex.
                toolParams[tool.parameters.keys.last()] = params
            }
            tool.execute(toolParams)
        }
    }
}
