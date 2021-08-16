package my.edu.utar.person.utils

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment

fun Context.showToast(msg: String){
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Context.showLongToast(msg: String){
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

fun Fragment.showToast(msg: String){
    requireActivity().showToast(msg)
}

fun Fragment.showLongToast(msg: String){
    requireActivity().showLongToast(msg)
}