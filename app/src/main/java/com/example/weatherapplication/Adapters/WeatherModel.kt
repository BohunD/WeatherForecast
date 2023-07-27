package com.example.weatherapplication.Adapters

data class WeatherModel(
    val city: String,
    val dateTime: String,
    val condition: String,
    val imageUrl: String,
    val currentTemp: String,
    val maxTemp: String,
    val minTemp: String,
    val hours: String
)
