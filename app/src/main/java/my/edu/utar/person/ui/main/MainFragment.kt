package my.edu.utar.person.ui.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import my.edu.utar.person.MainActivity
import my.edu.utar.person.R
import my.edu.utar.person.databinding.FragmentMainBinding

class MainFragment : Fragment(R.layout.fragment_main) {
    private val binding by viewBinding(FragmentMainBinding::bind)

    private lateinit var sharedPrefs: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPrefs = requireActivity().getSharedPreferences("default", Context.MODE_PRIVATE)

        val email = sharedPrefs.getString("email", "") ?: ""
        val username = sharedPrefs.getString("username", "") ?: ""
        val phone = sharedPrefs.getString("phone", "") ?: ""

        with(binding){
            tvEmail.text = email
            tvPhone.text = phone
            tvUsername.text = username
        }

        binding.phoneLayout.isVisible = phone.isNotEmpty()
        binding.emailLayout.isVisible = email.isNotEmpty()

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireActivity())
                .setTitle("Logout Confirmation")
                .setMessage("Do you want to log out?")
                .setPositiveButton("Yes"){_, _ ->
                    signOut()
                }
                .setNegativeButton("No"){_, _ -> }
                .show()
        }
    }

    private fun signOut() {
        val authParams =  HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setAuthorizationCode().createParams()
        val service = HuaweiIdAuthManager.getService(requireActivity(), authParams)

        val authType = sharedPrefs.getString("loginType", "") ?: ""
        if(authType == "HUAWEI"){
            sharedPrefs.edit().clear().commit()
            service.cancelAuthorization()
            startActivity(Intent(requireActivity(), MainActivity::class.java))
            requireActivity().finish()
        } else {
            sharedPrefs.edit().clear().commit()
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireActivity(), MainActivity::class.java))
            requireActivity().finish()
        }
    }
}