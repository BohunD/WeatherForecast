package com.example.weatherapplication

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weatherapplication.Adapters.WeatherModel

class MainViewModel: ViewModel() {
    val currentLD = MutableLiveData<WeatherModel>()
    val listLD = MutableLiveData<List<WeatherModel>>()
}