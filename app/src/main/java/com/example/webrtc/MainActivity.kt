package com.example.webrtc


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.webrtc.databinding.ActivityMainBinding
import com.example.webrtc.screens.account.AccountViewModel
import com.example.webrtc.screens.encounter.EncounterViewModel
import com.example.webrtc.utils.ahmedId
import com.example.webrtc.utils.mohamedId
import io.ktor.util.*



class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    private val encounterViewModel by viewModels<EncounterViewModel>()
    private val accountViewModel by viewModels<AccountViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)


//        setUpNav()
        accountViewModel.initData()
    }





//    private fun setUpNav() {
//        val navFragment = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
//        NavigationUI.setupWithNavController(binding.homeBottomNavigation, navFragment.navController)
//
//        navFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
//            when (destination.id) {
//                R.id.home -> setBottomNavVisibility(View.VISIBLE)
//                R.id.account -> setBottomNavVisibility(View.VISIBLE)
//                else -> setBottomNavVisibility(View.GONE)
//            }
//        }
//
//    }
//

//    private fun setBottomNavVisibility(visibility: Int) {
//        binding.homeBottomNavigation.visibility = visibility
//    }




    private fun getCurrentUserId(name: String): String{
        return if (name == "Ahmed"){
            ahmedId
        }else{
            mohamedId
        }
    }

}



