package com.example.weatherapplication.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapplication.Adapters.WeatherAdapter
import com.example.weatherapplication.Adapters.WeatherModel
import com.example.weatherapplication.MainViewModel
import com.example.weatherapplication.R
import com.example.weatherapplication.databinding.FragmentHoursBinding
import com.example.weatherapplication.databinding.FragmentMainBinding
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt


class HoursFragment : Fragment() {

    private lateinit var binding: FragmentHoursBinding
    private lateinit var rvAdapter: WeatherAdapter
    private val viewModel by activityViewModels<MainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHoursBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRv()

    }

    private fun getHoursList(item: WeatherModel): List<WeatherModel>{
        val hoursArray = JSONArray(item.hours)
        val list = ArrayList<WeatherModel>()
        for(i in 0 until hoursArray.length()){

            val hourItem = WeatherModel(
                item.city,
                (hoursArray[i] as JSONObject).getString("time")
                    .substring(
                        (hoursArray[i] as JSONObject).getString("time").indexOf(" ")+1,
                        (hoursArray[i] as JSONObject).getString("time").length
                    ),
                (hoursArray[i] as JSONObject).getJSONObject("condition").getString("text"),
                (hoursArray[i] as JSONObject).getJSONObject("condition").getString("icon"),
                (hoursArray[i] as JSONObject).getString("temp_c").toDouble().roundToInt().toString(),
                "",
                "",
                ""
            )
            list.add(hourItem)
        }
        return list
    }

    private fun initRv() = with(binding){
        rvHours.layoutManager = LinearLayoutManager(activity)
        rvAdapter = WeatherAdapter(null)
        rvHours.adapter = rvAdapter
        viewModel.currentLD.observe(viewLifecycleOwner){
            rvAdapter.submitList(getHoursList(it))
        }

    }

    companion object {

        @JvmStatic
        fun newInstance() = HoursFragment()
    }
}