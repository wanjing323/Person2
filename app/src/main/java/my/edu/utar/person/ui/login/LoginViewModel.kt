package my.edu.utar.person.ui.login

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import my.edu.utar.person.application.PersonApp
import my.edu.utar.person.application.UserModel
import my.edu.utar.person.ui.register.RegistrationResult

class LoginViewModel(application: Application): AndroidViewModel(application) {
    private val sharedPrefs = this.getApplication<PersonApp>().getSharedPreferences("default", Context.MODE_PRIVATE)

    private val auth = FirebaseAuth.getInstance()
    private val usersRef = FirebaseDatabase.getInstance().getReference("Users")

    val loading = MutableLiveData(false)
    val loginStatus = MutableLiveData<LoginResult?>(null)

    fun login(username: String, password: String){
        loading.postValue(true)

        usersRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    snapshot.children.forEach { userSnap ->
                        val user = userSnap.getValue(UserModel::class.java)
                        if(user != null && user.username == username){
                            auth.signInWithEmailAndPassword(user.email, password).addOnCompleteListener { task ->
                                loading.postValue(false)
                                if(task.isSuccessful){
                                    saveDataToDatabase(task.result?.user?.uid, username, user.email, user.phone)
                                    loginStatus.postValue(LoginResult.LoginSuccessful)
                                } else {
                                    loginStatus.postValue(LoginResult.LoginFailed(task.exception?.message ?: "Registration Failed"))
                                }
                            }
                            return@forEach
                        } else {
                            loading.postValue(false)
                            loginStatus.postValue(LoginResult.LoginFailed("Username not found"))
                        }
                    }
                } else {
                    loading.postValue(false)
                    loginStatus.postValue(LoginResult.LoginFailed("Username not found"))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                loading.postValue(false)
                loginStatus.postValue(LoginResult.LoginFailed(error.message))
            }
        })
    }

    private fun saveDataToDatabase(uid: String?, username: String, email: String, phone: String) {
        uid?.let {
            sharedPrefs.edit(true){
                putString("username", username)
                putString("email", email)
                putString("phone", phone)
                putString("loginType", "EmailAndPassword")
            }
            usersRef.child(it).updateChildren(
                mapOf(
                    "username" to username,
                    "email" to email,
                    "phone" to phone,
                    "uid" to it,
                    "loginType" to "EmailAndPassword"
                )
            )
        }
    }

    fun onLoginResultHandled(){
        loginStatus.postValue(null)
    }
}

sealed class LoginResult{
    object LoginSuccessful: LoginResult()
    data class LoginFailed(val errorMsg: String): LoginResult()
}