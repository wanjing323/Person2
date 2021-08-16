package my.edu.utar.person.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.huawei.agconnect.AGConnectApp
import com.huawei.agconnect.AGConnectInstance
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import my.edu.utar.person.R

class SplashFragment : Fragment(R.layout.fragment_splash) {

    private val handler = Handler(Looper.getMainLooper())

    private val auth = FirebaseAuth.getInstance()

    private lateinit var sharedPrefs: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPrefs = requireActivity().getSharedPreferences("default", Context.MODE_PRIVATE)
        val authType = sharedPrefs.getString("loginType", "") ?: ""

        val authParams =  HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setAuthorizationCode().createParams()
        val service = HuaweiIdAuthManager.getService(requireActivity(), authParams)

        handler.postDelayed({
            val user = auth.currentUser

            if(user != null){
                findNavController().navigate(R.id.action_splashFragment_to_mainFragment)
            } else {
                if (authType == "HUAWEI"){
                    findNavController().navigate(R.id.action_splashFragment_to_mainFragment)
                } else {
                    findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
                }
            }

        }, 1500L)
    }
}