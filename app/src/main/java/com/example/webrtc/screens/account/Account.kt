package com.example.webrtc.screens.account

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.example.webrtc.R
import com.example.webrtc.databinding.AccountBinding
import com.example.webrtc.utils.SelectUser

class Account : Fragment() {


//    private lateinit var viewModel: AccountViewModel

    private val viewModel by activityViewModels<AccountViewModel>()
    private lateinit var binding : AccountBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

//        viewModel = ViewModelProvider(this)[AccountViewModel::class.java]

        binding = DataBindingUtil.inflate(inflater,R.layout.account,container,false)




        setViews()
        setObserves()


        return binding.root
    }

    override fun onResume() {
        super.onResume()

        viewModel.initData()
    }

    private fun setObserves(){

        /** live data user **/
        viewModel.user.observe(viewLifecycleOwner){ user ->
            if (user != null){
                binding.userName.text = user.name
                binding.phoneNum.text = user.phone.toString()
                binding.email.text = user.email

            }
        }
    }

    private fun setViews() {


        binding.apply {


            /** radio group **/
            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                when(checkedId){
                    R.id.radio_Ahmed -> {
                        viewModel.updateUser("Ahmed")
                    }
                    R.id.radio_Mohamed -> {
                        viewModel.updateUser("Mohamed")
                    }
                }
            }



//// To listen for a radio button's checked/unchecked state changes
//        radioButton.setOnCheckedChangeListener { buttonView, isChecked
//            // Responds to radio button being checked/unchecked
//        }



        }

    }


}