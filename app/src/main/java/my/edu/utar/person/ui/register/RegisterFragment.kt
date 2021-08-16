package my.edu.utar.person.ui.register

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import my.edu.utar.person.R
import my.edu.utar.person.databinding.FragmentRegisterBinding
import my.edu.utar.person.utils.showLongToast
import my.edu.utar.person.utils.showToast


class RegisterFragment : Fragment(R.layout.fragment_register) {
    private val binding by viewBinding(FragmentRegisterBinding::bind)

    private lateinit var viewModel: RegisterViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)

        binding.btnBackToLogin.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnRegister.setOnClickListener {
            validateAndRegister()
        }

        viewModel.loading.observe(viewLifecycleOwner){ loading ->
            binding.progressBar.isVisible = loading
            binding.btnRegister.isVisible = !loading
            binding.btnBackToLogin.isEnabled = !loading
            binding.etUsername.isEnabled = !loading
            binding.etEmail.isEnabled = !loading
            binding.etPhone.isEnabled = !loading
            binding.etPassword.isEnabled = !loading
        }

        viewModel.registrationStatus.observe(viewLifecycleOwner){
            it?.let { registrationResult ->
                when(registrationResult){
                    is RegistrationResult.RegistrationFailed -> {
                        showLongToast(registrationResult.errorMsg)
                        viewModel.onRegistrationResultHandled()
                    }
                    RegistrationResult.RegistrationSuccessful -> {
                        showToast("Login Successful")
                        findNavController().navigate(R.id.action_registerFragment_to_mainFragment)
                        viewModel.onRegistrationResultHandled()
                    }
                }
            }
        }
    }

    private fun validateAndRegister() {
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if(username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()){
            showToast("All Fields Required")
            return
        }

        if(username.contains(" ")){
            showToast("Username shouldn't contain spaces")
            return
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            showToast("Invalid Email Format")
            return
        }

        if(password.length < 8){
            showToast("Password should be 8 or more characters")
            return
        }

        viewModel.register(username, email, phone, password)
    }
}