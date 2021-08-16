package my.edu.utar.person.ui

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import my.edu.utar.person.R
import my.edu.utar.person.databinding.FragmentForgotPasswordBinding
import my.edu.utar.person.utils.showLongToast
import my.edu.utar.person.utils.showToast

class ForgotPasswordFragment : Fragment(R.layout.fragment_forgot_password) {
    private val binding by viewBinding(FragmentForgotPasswordBinding::bind)

    private val auth = FirebaseAuth.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSentLink.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()

            if(email.isEmpty()){
                showToast("Email is empty")
                return@setOnClickListener
            }

            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                showToast("Invalid Email Format")
                return@setOnClickListener
            }

            showLoading()

            auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                hideLoading()
                if(task.isSuccessful){
                    showLongToast("Password Reset Email send to $email")
                    findNavController().navigateUp()
                } else {
                    showLongToast(task.exception?.message ?: "Failed to sent password reset email")
                }
            }
        }
    }

    private fun showLoading(){
        binding.progressBar.isVisible = true
        binding.btnSentLink.isEnabled = false
    }

    private fun hideLoading(){
        binding.progressBar.isVisible = false
        binding.btnSentLink.isEnabled = true
    }
}