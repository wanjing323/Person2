package my.edu.utar.person.ui.register

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import my.edu.utar.person.application.PersonApp
import my.edu.utar.person.ui.register.RegistrationResult.RegistrationSuccessful

class RegisterViewModel(application: Application): AndroidViewModel(application) {

    private val sharedPrefs = this.getApplication<PersonApp>().getSharedPreferences("default", Context.MODE_PRIVATE)

    private val auth = FirebaseAuth.getInstance()
    private val usersRef = FirebaseDatabase.getInstance().getReference("Users")

    val loading = MutableLiveData(false)
    val registrationStatus = MutableLiveData<RegistrationResult?>(null)

    fun register(userName: String, email: String, phone: String, password: String){
        loading.postValue(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                loading.postValue(false)

                if(task.isSuccessful){
                    saveDataToDatabase(task.result?.user?.uid, userName, email, phone)
                    registrationStatus.postValue(RegistrationSuccessful)
                } else {
                    registrationStatus.postValue(RegistrationResult.RegistrationFailed(task.exception?.message ?: "Registration Failed"))
                }
            }
    }

    private fun saveDataToDatabase(
        uid: String?,
        userName: String,
        email: String,
        phone: String,
    ) {
        uid?.let {
            sharedPrefs.edit(true){
                putString("username", userName)
                putString("email", email)
                putString("phone", phone)
                putString("loginType", "EmailAndPassword")
            }
            usersRef.child(it).updateChildren(
                mapOf(
                    "username" to userName,
                    "email" to email,
                    "phone" to phone,
                    "uid" to it,
                    "loginType" to "EmailAndPassword"
                )
            )
        }
    }

    fun onRegistrationResultHandled(){
        registrationStatus.postValue(null)
    }
}

sealed class RegistrationResult{
    object RegistrationSuccessful: RegistrationResult()
    data class RegistrationFailed(val errorMsg: String): RegistrationResult()
}