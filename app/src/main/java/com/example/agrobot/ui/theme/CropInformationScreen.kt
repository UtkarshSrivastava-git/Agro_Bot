package com.example.agrobot.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.agrobot.R


data class Crop(
    val id: String,
    val name: String,
    @DrawableRes val imageRes: Int,
    val climate: String,
    val soil: String,
    val sowingTime: String,
    val harvestingTime: String,
    val nutrients: String,
    val pestsAndDiseases: String,
    val fertilizers: String,
    val pesticides: String,
    val marketPrice: String
)


object CropData {
    val list = listOf(
        Crop(
            id = "rice",
            name = "Rice",
            imageRes = R.drawable.rice_plant,
            climate = "Hot and humid climate. Requires average temperature of 21 to 37°C.",
            soil = "Clayey loam soils are best suited for rice cultivation.",
            sowingTime = "June-July (Kharif), November-December (Rabi)",
            harvestingTime = "November-December (Kharif), March-April (Rabi)",
            nutrients = "Nitrogen, Phosphorus, and Potassium are crucial. Zinc deficiency is common.",
            pestsAndDiseases = "Stem borer, Brown planthopper, Sheath blight, Blast disease.",
            fertilizers = "Urea, DAP (Diammonium phosphate), Muriate of Potash (MOP).",
            pesticides = "Fipronil, Imidacloprid, Tricyclazole, Propiconazole.",
            marketPrice = "₹1800 - ₹3500 per quintal (varies by variety and region)."
        ),
        Crop(
            id = "wheat",
            name = "Wheat",
            imageRes = R.drawable.wheat,
            climate = "Cool, moist weather during vegetative growth and warm, dry weather for maturity.",
            soil = "Well-drained loams and clay loams with a pH range of 6.0 to 7.5.",
            sowingTime = "October-December",
            harvestingTime = "February-May",
            nutrients = "Nitrogen for leaf growth, Phosphorus for root development, and Potassium for overall health.",
            pestsAndDiseases = "Aphids, Termites, Rusts (leaf, stem, and stripe), Smut.",
            fertilizers = "Urea, DAP, NPK mixtures.",
            pesticides = "Thiamethoxam, Propiconazole, Tebuconazole.",
            marketPrice = "₹1975 - ₹2500 per quintal (MSP and market rates vary)."
        ),
        Crop(
            id = "maize",
            name = "Maize",
            imageRes = R.drawable.maize,
            climate = "Warm weather, sensitive to frost. Needs temperature between 21-27°C.",
            soil = "Well-drained, fertile loamy soil. pH 6.0-7.0.",
            sowingTime = "June-July (Kharif), Oct-Nov (Rabi).",
            harvestingTime = "Sep-Oct (Kharif), Feb-Mar (Rabi).",
            nutrients = "High requirement of Nitrogen and Phosphorus.",
            pestsAndDiseases = "Stem borer, Fall armyworm, Downy mildew.",
            fertilizers = "Urea, SSP (Single Super Phosphate), MOP.",
            pesticides = "Emamectin benzoate, Chlorantraniliprole.",
            marketPrice = "₹1850 - ₹2200 per quintal."
        ),
        Crop(
            id = "sugarcane",
            name = "Sugarcane",
            imageRes = R.drawable.sugarcane,
            climate = "Hot and humid climate, requires high rainfall. Temperature: 20-30°C.",
            soil = "Well-drained, deep, loamy soil.",
            sowingTime = "Oct-Nov (Autumn), Jan-Feb (Spring).",
            harvestingTime = "12-18 months after planting.",
            nutrients = "Heavy feeder, requires N, P, K in large amounts.",
            pestsAndDiseases = "Early shoot borer, Termites, Red rot.",
            fertilizers = "Urea, SSP, MOP, Gypsum.",
            pesticides = "Chlorpyrifos, Carbendazim.",
            marketPrice = "₹300 - ₹350 per quintal (Fair and Remunerative Price varies)."
        ),
        Crop(
            id = "cotton",
            name = "Cotton",
            imageRes = R.drawable.cotton,
            climate = "Warm climate (21-30°C), needs good rainfall during growth and sunny, dry weather during boll opening.",
            soil = "Deep, well-drained black clayey soils are ideal.",
            sowingTime = "April-June.",
            harvestingTime = "October-January (requires multiple pickings).",
            nutrients = "Nitrogen for vegetative growth, Phosphorus and Potassium for boll development.",
            pestsAndDiseases = "Pink bollworm, Whitefly, Jassids, Leaf curl virus.",
            fertilizers = "Urea, DAP, MOP.",
            pesticides = "Acetamiprid, Profenofos, Flonicamid.",
            marketPrice = "₹5500 - ₹7000 per quintal (varies greatly by quality)."
        ),
         Crop(
            id = "soybean",
            name = "Soybean",
            imageRes = R.drawable.soybean,
            climate = "Warm and moist climate. 25-32°C is optimal.",
            soil = "Well-drained, sandy loam to clay soils with pH 6.0-7.5.",
            sowingTime = "Late June to early July.",
            harvestingTime = "Late September to October.",
            nutrients = "Fixes its own nitrogen, but needs Phosphorus and Potassium.",
            pestsAndDiseases = "Girdle beetle, Stem fly, Yellow mosaic virus.",
            fertilizers = "SSP, MOP at the time of sowing. Minimal Nitrogen needed.",
            pesticides = "Thiamethoxam, Lambda-cyhalothrin.",
            marketPrice = "₹4000 - ₹6000 per quintal."
        ),
        Crop(
            id = "tomato",
            name = "Tomato",
            imageRes = R.drawable.tomato,
            climate = "Warm and sunny conditions. Sensitive to frost. Ideal temperature range is 21-24°C.",
            soil = "Well-drained sandy loam soil, rich in organic matter. pH 6.0-6.8.",
            sowingTime = "For spring crop: Nov-Dec. For autumn crop: July-Aug.",
            harvestingTime = "Starts 70-90 days after planting, continues for several weeks.",
            nutrients = "Requires high levels of Potassium, Phosphorus, and Nitrogen. Calcium is important to prevent blossom-end rot.",
            pestsAndDiseases = "Fruit borer, Whitefly, Early blight, Late blight, Leaf curl virus.",
            fertilizers = "NPK complex fertilizers, Calcium Nitrate.",
            pesticides = "Imidacloprid, Spinosad, Mancozeb, Copper oxychloride.",
            marketPrice = "₹1000 - ₹4000 per quintal (highly variable based on season and quality)."
        ),
        Crop(
            id = "potato",
            name = "Potato",
            imageRes = R.drawable.potato, // Add this drawable
            climate = "Cool season crop. Ideal temperature is 15-25°C. Frost is harmful.",
            soil = "Well-drained sandy loam and silt loam soils, rich in organic matter.",
            sowingTime = "October-November in plains, March-May in hills.",
            harvestingTime = "70-120 days after planting, depending on variety.",
            nutrients = "High requirement for Potassium, followed by Nitrogen and Phosphorus.",
            pestsAndDiseases = "Aphids, Potato tuber moth, Late blight, Early blight, Black scurf.",
            fertilizers = "FYM, NPK, Urea.",
            pesticides = "Imidacloprid, Mancozeb, Propamocarb.",
            marketPrice = "₹1000 - ₹2000 per quintal."
        ),
        Crop(
            id = "brinjal",
            name = "Brinjal",
            imageRes = R.drawable.brinjal, // Add this drawable
            climate = "Warm season crop, requires a long warm growing season. Sensitive to frost.",
            soil = "Well-drained silt loam or clay loam soils.",
            sowingTime = "Nursery raised. Transplanting in June-July and Dec-Jan.",
            harvestingTime = "Starts 120-140 days after seeding.",
            nutrients = "N, P, and K are required. Boron deficiency can be an issue.",
            pestsAndDiseases = "Fruit and shoot borer, Jassids, Whitefly, Phomopsis blight, Little leaf disease.",
            fertilizers = "Urea, SSP, MOP.",
            pesticides = "Emamectin benzoate, Cypermethrin, Mancozeb.",
            marketPrice = "₹1500 - ₹3000 per quintal."
        ),
        Crop(
            id = "mushroom",
            name = "Mushroom",
            imageRes = R.drawable.mushroom,
            climate = "Requires specific controlled environment. Button mushroom: 15-18°C. Oyster: 20-30°C.",
            soil = "Grown on a composted substrate, typically based on wheat or paddy straw.",
            sowingTime = "Year-round in controlled environments. Sept-March in seasonal farms.",
            harvestingTime = "Harvesting starts 3-4 weeks after spawning and continues in flushes.",
            nutrients = "Obtains nutrients from the prepared compost substrate.",
            pestsAndDiseases = "Sciarid flies, Phorid flies, Green mould, Wet bubble disease.",
            fertilizers = "Not applicable in the traditional sense. Substrate nutrition is key.",
            pesticides = "Use of chemical pesticides is highly restricted. Sanitation is key.",
            marketPrice = "₹100 - ₹200 per kg (highly perishable)."
        ),
        Crop(
            id = "lady-finger",
            name = "Lady Finger",
            imageRes = R.drawable.ladyfinger,
            climate = "Warm, humid climate. Very sensitive to frost.",
            soil = "Sandy loam to clay loam with good drainage.",
            sowingTime = "Feb-March and June-July.",
            harvestingTime = "Harvesting begins 45-50 days after sowing.",
            nutrients = "Requires moderate amounts of N, P, and K.",
            pestsAndDiseases = "Jassids, Aphids, Fruit borer, Yellow vein mosaic virus.",
            fertilizers = "FYM, NPK.",
            pesticides = "Imidacloprid, Abamectin.",
            marketPrice = "₹2000 - ₹4000 per quintal."
        ),
        Crop(
            id = "cauliflower",
            name = "Cauliflower",
            imageRes = R.drawable.cauliflower,
            climate = "Cool and slightly moist climate. Temperature: 15-20°C.",
            soil = "Sandy loam to clay loam, must be well-drained.",
            sowingTime = "May-June for early season, Sept-Oct for late season.",
            harvestingTime = "90-120 days after transplanting.",
            nutrients = "High requirement for Boron and Molybdenum, besides NPK.",
            pestsAndDiseases = "Diamondback moth, Cabbage butterfly, Aphids, Black rot, Downy mildew.",
            fertilizers = "NPK, Borax, Ammonium molybdate.",
            pesticides = "Spinosad, Emamectin benzoate, Mancozeb.",
            marketPrice = "₹1000 - ₹2500 per quintal."
        ),
        Crop(
            id = "bottlegourd",
            name = "Bottle Gourd",
            imageRes = R.drawable.bottlegourd,
            climate = "Hot and humid climate.",
            soil = "Well-drained loamy soil, pH 6.5-7.5.",
            sowingTime = "Feb-March (Summer), June-July (Monsoon).",
            harvestingTime = "60-70 days after sowing.",
            nutrients = "Requires a balanced supply of NPK.",
            pestsAndDiseases = "Fruit fly, Pumpkin beetles, Powdery mildew, Downy mildew.",
            fertilizers = "FYM, NPK.",
            pesticides = "Malathion, Carbendazim.",
            marketPrice = "₹1500 - ₹2500 per quintal."
        ),
        Crop(
            id = "bittergourd",
            name = "Bitter Gourd",
            imageRes = R.drawable.bittergourd,
            climate = "Warm to hot and humid climate.",
            soil = "Sandy loam soils rich in organic matter.",
            sowingTime = "Jan-March and June-July.",
            harvestingTime = "55-60 days after sowing.",
            nutrients = "Good source of FYM is essential. Balanced NPK required.",
            pestsAndDiseases = "Fruit fly, Aphids, Powdery mildew, Downy mildew.",
            fertilizers = "FYM, NPK.",
            pesticides = "Imidacloprid, Mancozeb, Sulphur.",
            marketPrice = "₹2500 - ₹4000 per quintal."
        ),
        Crop(
            id = "carrot",
            name = "Carrot",
            imageRes = R.drawable.carrot,
            climate = "Cool season crop. Ideal root growth at 15-20°C.",
            soil = "Deep, loose, well-drained sandy loam soils.",
            sowingTime = "August-November in plains.",
            harvestingTime = "90-110 days after sowing.",
            nutrients = "Requires less Nitrogen but more Phosphorus and Potassium.",
            pestsAndDiseases = "Carrot rust fly, Aphids, Alternaria leaf blight, Powdery mildew.",
            fertilizers = "FYM, CAN (Calcium Ammonium Nitrate), SSP, MOP.",
            pesticides = "Not heavily sprayed. Crop rotation is key.",
            marketPrice = "₹1500 - ₹3000 per quintal."
        ),
        Crop(
            id = "radish",
            name = "Radish",
            imageRes = R.drawable.radish,
            climate = "Cool season crop, grows best in mild to cool temperatures.",
            soil = "Light, friable, deep sandy loams.",
            sowingTime = "Can be grown year-round, but best in Sept-Jan.",
            harvestingTime = "Very fast growing, ready in 25-45 days depending on variety.",
            nutrients = "Requires easily available P & K. Moderate N.",
            pestsAndDiseases = "Aphids, Mustard sawfly, Flea beetles.",
            fertilizers = "FYM, NPK.",
            pesticides = "Malathion for sawfly.",
            marketPrice = "₹1000 - ₹2000 per quintal."
        ),
        Crop(
            id = "beetroot",
            name = "Beetroot",
            imageRes = R.drawable.beetroot,
            climate = "Cool season crop. Requires moist soil. Temp: 15-20°C.",
            soil = "Deep, friable loams and sandy loams, rich in organic matter.",
            sowingTime = "October-November.",
            harvestingTime = "70-90 days after sowing.",
            nutrients = "Sensitive to Boron deficiency. Needs balanced NPK.",
            pestsAndDiseases = "Leaf miner, Aphids, Cercospora leaf spot.",
            fertilizers = "NPK, Borax.",
            pesticides = "Pesticide use is generally low.",
            marketPrice = "₹1200 - ₹2500 per quintal."
        ),
        Crop(
            id = "spinach",
            name = "Spinach",
            imageRes = R.drawable.spinach,
            climate = "Cool, moist weather. Can tolerate frost.",
            soil = "Alluvial loam and sandy loam are best.",
            sowingTime = "September-November for plains.",
            harvestingTime = "Ready for first cutting in 25-30 days. Multiple cuttings possible.",
            nutrients = "High nitrogen requirement for leafy growth.",
            pestsAndDiseases = "Aphids, Leaf miners, Downy mildew.",
            fertilizers = "FYM, Ammonium Sulphate, or Urea in split doses.",
            pesticides = "Dimethoate for aphids.",
            marketPrice = "₹1000 - ₹2000 per quintal."
        ),
        Crop(
            id = "yam",
            name = "Yam",
            imageRes = R.drawable.yam,
            climate = "Tropical and subtropical crop. Warm and humid climate.",
            soil = "Fertile, loose, well-drained soil. Cannot tolerate waterlogging.",
            sowingTime = "May-June.",
            harvestingTime = "8-9 months after planting.",
            nutrients = "High Potassium requirement. Also N and P.",
            pestsAndDiseases = "Yam beetle, Mealybugs, Anthracnose, Yam mosaic virus.",
            fertilizers = "FYM, NPK.",
            pesticides = "Crop rotation and clean planting material are the main control methods.",
            marketPrice = "₹2000 - ₹3500 per quintal."
        ),
        Crop(
            id = "cabbage",
            name = "Cabbage",
            imageRes = R.drawable.cabbage,
            climate = "Cool and humid climate. Temperature: 15-20°C.",
            soil = "Well-drained, sandy loam to clay loam soils.",
            sowingTime = "September-October for winter crop.",
            harvestingTime = "60-120 days after transplanting.",
            nutrients = "Heavy feeder. High requirement of Nitrogen and Potassium.",
            pestsAndDiseases = "Diamondback moth, Cabbage looper, Aphids, Black rot.",
            fertilizers = "FYM, NPK, Urea.",
            pesticides = "Spinosad, Bacillus thuringiensis (Bt).",
            marketPrice = "₹800 - ₹1500 per quintal."
        ),
        Crop(
            id = "onion",
            name = "Onion",
            imageRes = R.drawable.onion,
            climate = "Mild climate. Not suited for extreme hot or cold. Best at 15-25°C for vegetative growth.",
            soil = "Well-drained, friable, sandy loam soils.",
            sowingTime = "Oct-Nov (Rabi), May-June (Kharif).",
            harvestingTime = "3-5 months after transplanting.",
            nutrients = "Requires balanced N, P, K. Sulphur is important for pungency.",
            pestsAndDiseases = "Thrips, Maggots, Purple blotch, Downy mildew.",
            fertilizers = "FYM, NPK, Sulphur.",
            pesticides = "Profenofos, Mancozeb, Metalaxyl.",
            marketPrice = "₹1500 - ₹3500 per quintal (highly volatile)."
        ),
        Crop(
            id = "chilli",
            name = "Chilli",
            imageRes = R.drawable.chilli,
            climate = "Warm and humid climate. Not tolerant to frost.",
            soil = "Well-drained loamy soil, rich in organic matter.",
            sowingTime = "Kharif: June-July. Rabi: Sept-Oct. Summer: Jan-Feb.",
            harvestingTime = "Green chillies can be harvested 75-90 days after transplanting.",
            nutrients = "High requirement for N, P, and especially K.",
            pestsAndDiseases = "Thrips, Aphids, Fruit borer, Leaf curl virus, Anthracnose.",
            fertilizers = "FYM, NPK.",
            pesticides = "Imidacloprid, Fipronil, Copper oxychloride.",
            marketPrice = "₹3000 - ₹6000 per quintal for dry chilli."
        ),
        Crop(
            id = "lemon",
            name = "Lemon",
            imageRes = R.drawable.lemon,
            climate = "Sub-tropical climate. Sensitive to frost and very high temperatures.",
            soil = "Well-drained, light loamy soils.",
            sowingTime = "Planting is usually done during the monsoon season (June-August).",
            harvestingTime = "Starts bearing fruits from the 3rd year. Main harvesting season is winter.",
            nutrients = "Regular manuring with FYM and NPK is needed. Zinc and Iron deficiencies are common.",
            pestsAndDiseases = "Citrus psylla, Leaf miner, Citrus canker, Gummosis.",
            fertilizers = "FYM, NPK, Zinc sulphate, Ferrous sulphate.",
            pesticides = "Imidacloprid, Copper oxychloride, Streptocycline.",
            marketPrice = "₹3000 - ₹5000 per quintal."
        ),
        Crop(
            id = "capsicum",
            name = "Capsicum",
            imageRes = R.drawable.capsicum,
            climate = "Warm season crop, but cannot tolerate high heat or frost. Ideal temp: 21-25°C.",
            soil = "Well-drained, sandy loam to loam soil, rich in organic matter.",
            sowingTime = "Nursery can be raised in Aug-Sept and Dec-Jan.",
            harvestingTime = "Green capsicums are ready in 60-75 days after transplanting.",
            nutrients = "Requires heavy manuring. Balanced NPK is crucial.",
            pestsAndDiseases = "Thrips, Mites, Aphids, Fruit borer, Powdery mildew, Cercospora leaf spot.",
            fertilizers = "FYM, NPK, Calcium nitrate.",
            pesticides = "Abamectin, Spiromesifen, Imidacloprid.",
            marketPrice = "₹2500 - ₹5000 per quintal."
        ),
        Crop(
            id = "cucumber",
            name = "Cucumber",
            imageRes = R.drawable.cucumber, // Add this drawable
            climate = "Warm season crop. Requires plenty of sunshine. Sensitive to frost.",
            soil = "Well-drained sandy loam soil, rich in organic matter. pH 6.0-7.0.",
            sowingTime = "January-February for summer crop, June-July for monsoon crop.",
            harvestingTime = "Starts 45-55 days after sowing.",
            nutrients = "Requires steady supply of water and nutrients. NPK in a balanced ratio.",
            pestsAndDiseases = "Cucumber mosaic virus, Powdery mildew, Downy mildew, Fruit fly.",
            fertilizers = "FYM, NPK.",
            pesticides = "Imidacloprid, Mancozeb, Copper oxychloride.",
            marketPrice = "₹1500 - ₹3000 per quintal."
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropListScreen(
    navController: NavController,
    userLang: String,
    translate: suspend (String) -> String
) {
    var title by remember { mutableStateOf("Crop Information") }

    LaunchedEffect(userLang) {
        if (userLang != "en") {
            title = translate("Crop Information")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(CropData.list) { crop ->
                CropListItem(crop = crop, userLang = userLang, translate = translate) {
                    navController.navigate("cropDetail/${crop.id}")
                }
            }
        }
    }
}

@Composable
fun CropListItem(
    crop: Crop,
    userLang: String,
    translate: suspend (String) -> String,
    onClick: () -> Unit
) {
    var translatedName by remember { mutableStateOf(crop.name) }

    LaunchedEffect(userLang) {
        if (userLang != "en") {
            translatedName = translate(crop.name)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = crop.imageRes),
                contentDescription = translatedName,
                modifier = Modifier
                    .size(80.dp)
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Text(text = translatedName, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropDetailScreen(
    navController: NavController,
    cropId: String?,
    userLang: String,
    translate: suspend (String) -> String
) {
    val crop = CropData.list.find { it.id == cropId }

    var translatedCrop by remember { mutableStateOf<Crop?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(crop, userLang) {
        isLoading = true
        if (crop != null && userLang != "en") {
            translatedCrop = crop.copy(
                name = translate(crop.name),
                climate = translate(crop.climate),
                soil = translate(crop.soil),
                sowingTime = translate(crop.sowingTime),
                harvestingTime = translate(crop.harvestingTime),
                nutrients = translate(crop.nutrients),
                pestsAndDiseases = translate(crop.pestsAndDiseases),
                fertilizers = translate(crop.fertilizers),
                pesticides = translate(crop.pesticides),
                marketPrice = translate(crop.marketPrice)
            )
        } else {
            translatedCrop = crop
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(translatedCrop?.name ?: "Crop Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val displayCrop = translatedCrop
            if (displayCrop != null) {
                LazyColumn(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
                    item {
                        Image(
                            painter = painterResource(id = displayCrop.imageRes),
                            contentDescription = displayCrop.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(16.dp))
                        CropDetailItem("Best Climate", "🌦️ ${displayCrop.climate}", userLang, translate)
                        CropDetailItem("Ideal Soil", "🌱 ${displayCrop.soil}", userLang, translate)
                        CropDetailItem("Sowing Time", "⏰ ${displayCrop.sowingTime}", userLang, translate)
                        CropDetailItem("Harvesting Time", "🌾 ${displayCrop.harvestingTime}", userLang, translate)
                        CropDetailItem("Nutrient Requirements", "💧 ${displayCrop.nutrients}", userLang, translate)
                        CropDetailItem("Common Pests & Diseases", "🐛 ${displayCrop.pestsAndDiseases}", userLang, translate)
                        CropDetailItem("Suggested Fertilizers", "💊 ${displayCrop.fertilizers}", userLang, translate)
                        CropDetailItem("Suggested Pesticides", "🛡️ ${displayCrop.pesticides}", userLang, translate)
                        CropDetailItem("Average Market Price", "💰 ${displayCrop.marketPrice}", userLang, translate)
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("Crop not found.")
                }
            }
        }
    }
}

@Composable
fun CropDetailItem(
    label: String,
    value: String,
    userLang: String,
    translate: suspend (String) -> String
) {
    var translatedLabel by remember { mutableStateOf(label) }

    LaunchedEffect(userLang) {
        if (userLang != "en") {
            translatedLabel = translate(label)
        } else {
            translatedLabel = label
        }
    }

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = translatedLabel,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}