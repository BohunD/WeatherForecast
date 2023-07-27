package com.example.weatherapplication.fragments

import android.Manifest
import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapplication.Adapters.VpAdapter
import com.example.weatherapplication.Adapters.WeatherModel
import com.example.weatherapplication.DialogManager
import com.example.weatherapplication.MainViewModel
import com.example.weatherapplication.databinding.FragmentMainBinding
import com.example.weatherapplication.isPermissionGranted
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import org.json.JSONObject
import kotlin.math.roundToInt

const val API_KEY = "280b3647c7e743c6858160642232307&"

class MainFragment : Fragment() {

    private lateinit var pLauncher: ActivityResultLauncher<String>
    private lateinit var binding: FragmentMainBinding
    private val viewModel by activityViewModels<MainViewModel>()
    private lateinit var fLocationClient: FusedLocationProviderClient

    private val fList = listOf( HoursFragment.newInstance(), DaysFragment.newInstance())
    private val tabList = listOf(
        "Hours",
        "Days"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        init()
        updateCurrentCard()
        checkLocation()
    }

    private fun updateCurrentCard()= with(binding){
        viewModel.currentLD.observe(viewLifecycleOwner){
            val maxMinTemp = it.maxTemp + " Cº" +
                    "/" + it.minTemp + " Cº"
            tvCurrentTemp.text = (it.currentTemp ).ifEmpty { maxMinTemp}
            tvLastUpd.text = it.dateTime.substring(it.dateTime.indexOf("-")+1, it.dateTime.length)
            tvCondition.text = it.condition
            tvCity.text = it.city
            tvMaxMinTemp.text = if(it.currentTemp.isEmpty()){""} else{maxMinTemp}
            Picasso.get().load("https:"+it.imageUrl).into(ivCondition)
        }
    }

    private fun checkPermission(){
        if(!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun permissionListener(){
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()){
            Toast.makeText(activity, "Permission is $it", Toast.LENGTH_SHORT).show()
        }
    }

    private fun init() = with(binding){
        fLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val vpAdapter = VpAdapter(activity as FragmentActivity, fList)
        vp.adapter = vpAdapter
        TabLayoutMediator(tabLayout,vp){
            tab, position -> tab.text = tabList[position]
        }.attach()
        btnRefresh.setOnClickListener {
            checkLocation()
            tabLayout.selectTab(tabLayout.getTabAt(0))
        }
    }

    private fun requestWeatherData(city: String){
        val url = "https://api.weatherapi.com/v1/forecast.json?key=$API_KEY" +
                "&q=$city" +
                "&days" +
                "=10&aqi=no&alerts=no"
        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            url,
            {
                result->parseWeatherData(result)
            },
            {
                error-> Log.d("MyLog", "Error is $error")
            })
        queue.add(request)
    }

    private fun isLocationEnabled(): Boolean{
        val locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onResume() {
        super.onResume()
        checkLocation()
    }

    private fun checkLocation(){
        if(isLocationEnabled()){
            getLocation()
        }
        else{
            DialogManager.locationSettingsDialog(requireContext(),object: DialogManager.Listener{
                override fun onClick() {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }

            })
        }
    }

    private fun getLocation(){
        val ct = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,ct.token)
            .addOnCompleteListener{
            requestWeatherData("${it.result.latitude},${it.result.longitude}")
        }
    }

    private fun parseWeatherData(result: String){
        val mainObject = JSONObject(result)
        val list = parseDays(mainObject)
        parseCurrentData(mainObject, list[0])

    }

    private fun parseCurrentData(mainObject: JSONObject, currentItem: WeatherModel){
        val weatherItem = WeatherModel(
            mainObject.getJSONObject("location").getString("name"),
            mainObject.getJSONObject("current").getString("last_updated"),
            mainObject.getJSONObject("current")
                .getJSONObject("condition").getString("text"),
            mainObject.getJSONObject("current")
                .getJSONObject("condition").getString("icon"),
            roundToStr(mainObject.getJSONObject("current").getString("temp_c"))+ " ºC",
            currentItem.maxTemp,
            currentItem.minTemp,
            currentItem.hours
        )
        viewModel.currentLD.value = weatherItem

    }

    private fun roundToStr(value: String) : String{
        return value.toDouble().roundToInt().toString()
    }

    private fun parseDays(mainObject: JSONObject): List<WeatherModel>{
        val list = ArrayList<WeatherModel>()
        val daysArray = mainObject.getJSONObject("forecast").getJSONArray("forecastday")
        val name = mainObject.getJSONObject("location").getString("name")
        for(i in 0 until daysArray.length()){
            val day = daysArray[i] as JSONObject
            val item = WeatherModel(
                name,
                day.getString("date"),
                day.getJSONObject("day").getJSONObject("condition").getString("text"),
                day.getJSONObject("day").getJSONObject("condition").getString("icon"),
                "",
                roundToStr(day.getJSONObject("day").getString("maxtemp_c")),
                roundToStr(day.getJSONObject("day").getString("mintemp_c")),
                day.getJSONArray("hour").toString()
            )
            list.add(item)
        }
        viewModel.listLD.value = list
        return list
    }

    companion object {

        @JvmStatic
        fun newInstance() = MainFragment()

    }
}