package com.example.agrobot.ui.theme

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.agrobot.BuildConfig
import com.example.agrobot.R
import com.google.android.gms.location.*
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherResponse(
    @Json(name = "weather") val weather: List<WeatherDescription>?,
    @Json(name = "main") val main: MainDetails?,
    @Json(name = "wind") val wind: Wind?,
    @Json(name = "sys") val sys: Sys?,
    @Json(name = "name") val cityName: String?,
    @Json(name = "dt") val dt: Long?
)

data class WeatherDescription(
    @Json(name = "id") val id: Int?,
    @Json(name = "main") val main: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "icon") val icon: String?
)

data class MainDetails(
    @Json(name = "temp") val temp: Double?,
    @Json(name = "feels_like") val feelsLike: Double?,
    @Json(name = "humidity") val humidity: Int?
)

data class Wind(@Json(name = "speed") val speed: Double?)

data class Sys(
    @Json(name = "sunrise") val sunrise: Long?,
    @Json(name = "sunset") val sunset: Long?
)

interface WeatherApiService {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}

object RetrofitClient {
    // Reverted to the 2.5 base URL
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val okHttpClient = OkHttpClient.Builder().build()

    val instance: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(WeatherApiService::class.java)
    }
}

sealed interface WeatherUiState {
    object Loading : WeatherUiState
    data class Success(val data: WeatherResponse) : WeatherUiState // Reverted to WeatherResponse
    data class Error(val message: String) : WeatherUiState
    object Idle : WeatherUiState
    object LocationPermissionNeeded : WeatherUiState
}

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val uiState: StateFlow<WeatherUiState> = _uiState

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application.applicationContext)

    fun fetchWeatherForCurrentLocation() {
        _uiState.value = WeatherUiState.Loading
        getCurrentLocationAndFetchWeather()
    }

    fun requestLocationPermission() {
        _uiState.value = WeatherUiState.LocationPermissionNeeded
    }

    fun setIdleState() {
        _uiState.value = WeatherUiState.Idle
    }
    
    private fun getCurrentLocationAndFetchWeather() {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    _uiState.value = WeatherUiState.Error("Location permission not granted")
                    return@launch
                }
                
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        fetchWeatherData(location.latitude, location.longitude)
                    } else {
                        requestCurrentLocation()
                    }
                }.addOnFailureListener {
                    requestCurrentLocation()
                }
                
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error("Failed to get location: ${e.message}")
            }
        }
    }
    
    private fun requestCurrentLocation() {
        try {
            val context = getApplication<Application>().applicationContext
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L).build()
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        fetchWeatherData(location.latitude, location.longitude)
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            } else {
                _uiState.value = WeatherUiState.Error("Location permission not granted")
            }
        } catch (e: Exception) {
            _uiState.value = WeatherUiState.Error("Failed to request location: ${e.message}")
        }
    }
    
    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.OPENWEATHERMAP_API_KEY
                if (apiKey.isBlank() || apiKey.startsWith("YOUR")) {
                    _uiState.value = WeatherUiState.Error("API key not configured.")
                    return@launch
                }
                
                // Call the getCurrentWeather endpoint
                val weatherData = RetrofitClient.instance.getCurrentWeather(latitude, longitude, apiKey)
                _uiState.value = WeatherUiState.Success(weatherData)
                
            } catch (e: HttpException) {
                Log.e("WeatherViewModel", "HTTP Exception: ${e.code()} - ${e.message()}")
                _uiState.value = WeatherUiState.Error("HTTP Error: ${e.code()} - ${e.message()}")
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Network error: ${e.message}", e)
                _uiState.value = WeatherUiState.Error("Network error: ${e.message}")
            }
        }
    }
}

@Composable
private fun getWeatherDrawables(mainWeather: String?, dt: Long?, sys: Sys?): Pair<Int, Int> {
    val isNight = dt != null && sys?.sunrise != null && sys.sunset != null && (dt < sys.sunrise || dt > sys.sunset)

    return when (mainWeather) {
        "Clear" -> if (isNight) Pair(R.drawable.night, R.drawable.night_icon) else Pair(R.drawable.sunny, R.drawable.sun_icon)
        "Clouds" -> Pair(R.drawable.cloudy, R.drawable.cloud_icon)
        "Rain", "Drizzle" -> Pair(R.drawable.raindrops, R.drawable.rain_icon)
        "Thunderstorm" -> Pair(R.drawable.thunderstorm, R.drawable.lightning_bolt__icon)
        else -> Pair(R.drawable.black, R.drawable.india_icon)
    }
}

@Composable
fun WeatherScreen(
    lang: String,
    navController: NavController, 
    translate: suspend (String) -> String,
    viewModel: WeatherViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_animation))

    // Translated strings state
    var backText by remember { mutableStateOf("Back") }
    var getCurrentWeatherText by remember { mutableStateOf("Get Current Weather") }
    var grantPermissionText by remember { mutableStateOf("Grant Permission") }
    var locationNeededText by remember { mutableStateOf("Location permission needed.")}
    var currentLocText by remember { mutableStateOf("Current Location") }
    var weatherIconText by remember { mutableStateOf("Weather icon") }
    var humidityText by remember { mutableStateOf("Humidity") }
    var windSpeedText by remember { mutableStateOf("Wind Speed") }
    var feelsLikeText by remember { mutableStateOf("Feels Like") }
    var tryAgainText by remember { mutableStateOf("Try Again") }

    LaunchedEffect(lang) {
        if (lang != "en") {
            backText = translate("Back")
            getCurrentWeatherText = translate("Get Current Weather")
            grantPermissionText = translate("Grant Permission")
            locationNeededText = translate("Location permission needed.")
            currentLocText = translate("Current Location")
            weatherIconText = translate("Weather icon")
            humidityText = translate("Humidity")
            windSpeedText = translate("Wind Speed")
            feelsLikeText = translate("Feels Like")
            tryAgainText = translate("Try Again")
        } else {
            backText = "Back"
            getCurrentWeatherText = "Get Current Weather"
            grantPermissionText = "Grant Permission"
            locationNeededText = "Location permission needed."
            currentLocText = "Current Location"
            weatherIconText = "Weather icon"
            humidityText = "Humidity"
            windSpeedText = "Wind Speed"
            feelsLikeText = "Feels Like"
            tryAgainText = "Try Again"
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            viewModel.fetchWeatherForCurrentLocation()
        } else {
            viewModel.setIdleState()
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            viewModel.fetchWeatherForCurrentLocation()
        } else {
            viewModel.requestLocationPermission()
        }
    }

    val (backgroundRes, _) = if (uiState is WeatherUiState.Success) {
        val successState = uiState as WeatherUiState.Success
        getWeatherDrawables(
            mainWeather = successState.data.weather?.firstOrNull()?.main,
            dt = successState.data.dt,
            sys = successState.data.sys
        )
    } else {
        Pair(R.drawable.lettuce_field_dawn, R.drawable.india_icon)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = "Weather background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.align(Alignment.TopStart).padding(top=36.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = backText, 
                tint = Color.Unspecified
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val state = uiState) {
                is WeatherUiState.Idle -> {
                     Button(onClick = { locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)) },
                         colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black)) {
                        Text(getCurrentWeatherText)
                    }
                }
                is WeatherUiState.LocationPermissionNeeded -> {
                    Text(locationNeededText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Button(onClick = { locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)) }) {
                        Text(grantPermissionText)
                    }
                }
                is WeatherUiState.Loading -> {
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(200.dp)
                    )
                }
                is WeatherUiState.Success -> {
                    val weatherData = state.data
                    val (_, iconRes) = getWeatherDrawables(weatherData.weather?.firstOrNull()?.main, weatherData.dt, weatherData.sys)
                    
                    var translatedDescription by remember { mutableStateOf("") }

                    LaunchedEffect(weatherData.weather, lang) {
                        val originalDescription = weatherData.weather?.firstOrNull()?.description ?: ""
                        translatedDescription = if (lang != "en" && originalDescription.isNotEmpty()) {
                            translate(originalDescription)
                        } else {
                            originalDescription
                        }
                    }
                    
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(weatherData.cityName ?: currentLocText, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${weatherData.main?.temp?.toInt() ?: 0}°C", style = MaterialTheme.typography.displayLarge, color = Color.White, fontWeight = FontWeight.ExtraBold)
                                Spacer(Modifier.width(16.dp))
                                Image(painterResource(id = iconRes), contentDescription = weatherIconText, modifier = Modifier.size(72.dp))
                            }
                            Text(translatedDescription.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }, style = MaterialTheme.typography.titleLarge, color = Color.White)
                            Spacer(Modifier.height(24.dp))
                            WeatherDetailRow(R.drawable.humidity_icon, humidityText, "${weatherData.main?.humidity ?: "--"}%")
                            WeatherDetailRow(R.drawable.wind_icon, windSpeedText, "${weatherData.wind?.speed ?: "--"} m/s")
                            WeatherDetailRow(R.drawable.thermometer_icon, feelsLikeText, "${weatherData.main?.feelsLike?.toInt() ?: "--"}°C")
                        }
                    }

                }
                is WeatherUiState.Error -> {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                    Button(onClick = { viewModel.fetchWeatherForCurrentLocation() }) {
                        Text(tryAgainText)
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherDetailRow(iconRes: Int, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(id = iconRes), contentDescription = "$label icon", modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text(text = label, color = Color.White, fontSize = 16.sp)
        }
        Text(text = value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
