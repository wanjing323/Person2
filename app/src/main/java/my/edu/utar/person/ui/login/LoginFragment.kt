package my.edu.utar.person.ui.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.FirebaseDatabase
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.huawei.hms.support.hwid.result.AuthHuaweiId
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import my.edu.utar.person.R
import my.edu.utar.person.databinding.FragmentLoginBinding
import my.edu.utar.person.ui.register.RegistrationResult
import my.edu.utar.person.utils.showLongToast
import my.edu.utar.person.utils.showToast

class LoginFragment : Fragment(R.layout.fragment_login) {
    private val binding by viewBinding(FragmentLoginBinding::bind)

    private lateinit var viewModel: LoginViewModel

    private lateinit var sharedPrefs: SharedPreferences

    private val usersRef = FirebaseDatabase.getInstance().getReference("Users")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPrefs = requireActivity().getSharedPreferences("default", Context.MODE_PRIVATE)

        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        binding.btnGotoSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.btnForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }

        binding.btnLogin.setOnClickListener {
            validateAndLogin()
        }

        viewModel.loading.observe(viewLifecycleOwner){ loading ->
            binding.progressBar.isVisible = loading
            binding.btnLogin.isVisible = !loading
            binding.btnHuaweiLogin.isEnabled = !loading
            binding.btnForgotPassword.isEnabled = !loading
            binding.etUsername.isEnabled = !loading
            binding.etPassword.isEnabled = !loading
        }

        viewModel.loginStatus.observe(viewLifecycleOwner){
            it?.let { loginResult ->
                when(loginResult){
                    is LoginResult.LoginFailed -> {
                        showLongToast(loginResult.errorMsg)
                        viewModel.onLoginResultHandled()
                    }
                    LoginResult.LoginSuccessful -> {
                        showToast("Login Successful")
                        findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
                        viewModel.onLoginResultHandled()
                    }
                }
            }
        }

        binding.btnHuaweiLogin.setOnClickListener {
            val authParams =  HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setAuthorizationCode().createParams()
            val service = HuaweiIdAuthManager.getService(requireActivity(), authParams)
            startActivityForResult(service.signInIntent, 8888)
        }
    }

    private fun validateAndLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if(username.isEmpty() || password.isEmpty()){
            showToast("All Fields Required")
            return
        }

        if(username.contains(" ")){
            showToast("Username shouldn't contain spaces")
            return
        }

        if(password.length < 8){
            showToast("Password should be 8 or more characters")
            return
        }

        viewModel.login(username, password)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int,  data: Intent?) {
        // Process the authorization result and obtain the authorization code from AuthHuaweiId.
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 8888) {
            val authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data)
            if (authHuaweiIdTask.isSuccessful) {
                // The sign-in is successful, and the user's HUAWEI ID information and authorization code are obtained.
                val huaweiAccount = authHuaweiIdTask.result
                saveToDatabase(huaweiAccount)
                findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
                Log.i("112233", "Authorization code:" + huaweiAccount.authorizationCode)
            } else {
                // The sign-in failed.
                Log.e("112233", "sign in failed : " + (authHuaweiIdTask.exception as ApiException).statusCode)
            }
        }
    }

    private fun saveToDatabase(huaweiAccount: AuthHuaweiId?) {
        huaweiAccount?.let {
            sharedPrefs.edit(true){
                putString("username", it.displayName ?: "")
                putString("email", it.email ?: "")
                putString("phone", "")
                putString("loginType", "HUAWEI")
            }
            usersRef.child(it.displayName).updateChildren(
                mapOf(
                    "username" to it.displayName,
                    "email" to it.email,
                    "phone" to "",
                    "uid" to it.uid,
                    "loginType" to "HUAWEI"
                )
            )
        }
    }

}