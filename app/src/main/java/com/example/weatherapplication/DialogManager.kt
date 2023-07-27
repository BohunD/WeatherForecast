package com.example.weatherapplication

import android.app.AlertDialog
import android.content.Context

object DialogManager {
    fun locationSettingsDialog(context: Context, listener: Listener){
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setTitle("Enable location?")
        dialog.setMessage("Location is disabled, do you want to enable it?")
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel"){
            _, _ ->
            dialog.dismiss()
        }
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok"){
            _, _ ->
            dialog.dismiss()
            listener.onClick()
        }
        dialog.show()
    }
    interface Listener{
        fun onClick()
    }
}