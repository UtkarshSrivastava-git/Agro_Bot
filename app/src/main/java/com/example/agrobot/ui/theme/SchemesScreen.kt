package com.example.agrobot.ui.theme

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class Scheme(
    val title: String = "",
    val description: String = "",
    val link: String = ""
)

private const val TAG = "GovernmentSchemesScreen"

@Composable
fun GovernmentSchemesScreen() {
    var schemes by remember { mutableStateOf<List<Scheme>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        Log.d(TAG, "LaunchedEffect started.")
        isLoading = true
        errorMessage = null

        val db = Firebase.firestore
        val collectionRef = db.collection("schemes")

        try {
            Log.d(TAG, "Checking Firestore for existing data.")
            val querySnapshot = collectionRef.get().await()

            if (querySnapshot.isEmpty) {
                Log.d(TAG, "Firestore collection is empty. Adding default schemes...")
                withContext(Dispatchers.IO) {
                    try {
                        val defaultSchemes = getDefaultSchemes()
                        Log.d(TAG, "Adding ${defaultSchemes.size} default schemes to Firestore...")
                        defaultSchemes.forEach { scheme ->
                            collectionRef.add(scheme).await()
                        }
                        Log.d(TAG, "Default schemes stored in Firestore successfully.")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorMessage = "Error storing schemes: ${e.localizedMessage}"
                        Log.e(TAG, "Error storing default schemes: ", e)
                    }
                }
            } else {
                Log.d(TAG, "Firestore collection has data. Skipping scraping.")
            }

            Log.d(TAG, "Fetching schemes from Firestore.")
            schemes = fetchSchemesFromFirestore()
            Log.d(TAG, "Fetched ${schemes.size} schemes from Firestore.")

            if (schemes.isEmpty() && errorMessage == null) {
                errorMessage = "No schemes found in Firestore (and no scraping error)."
                Log.w(TAG, "No schemes found in Firestore after fetching, and no previous scraping error.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = "Error during data process: ${e.localizedMessage}"
            Log.e(TAG, "Error during data process (Firestore or general): ", e)
        } finally {
            Log.d(TAG, "LaunchedEffect finished, isLoading: $isLoading, errorMessage: $errorMessage")
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
        } else {
            errorMessage?.let { msg ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = msg, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center) // Centered text
                }
            } ?: run {
                if (schemes.isNotEmpty()) {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        items(schemes) { scheme ->
                            SchemeItem(scheme)
                        }
                    }
                } else {
                    // This condition is for when schemes are empty AND errorMessage is null.
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No schemes available.", modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SchemeItem(scheme: Scheme) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center) {
            Text(text = scheme.title, style = MaterialTheme.typography.headlineSmall)
            Text(text = scheme.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private suspend fun fetchSchemesFromFirestore(): List<Scheme> {
    val db = Firebase.firestore
    val schemes = mutableListOf<Scheme>()
    withContext(Dispatchers.IO) {
        try {
            val querySnapshot = db.collection("schemes").get().await()
            for (document in querySnapshot.documents) {
                document.toObject(Scheme::class.java)?.let {
                    schemes.add(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Error fetching schemes from Firestore: ", e)
        }
    }
    return schemes
}

private fun getDefaultSchemes(): List<Scheme> {
    return listOf(
        Scheme(
            title = "PM-KISAN (Pradhan Mantri Kisan Samman Nidhi)",
            description = "Direct income support of ₹6,000 per year to small and marginal farmers in three equal installments. All landholding farmers are eligible regardless of land size.",
            link = "https://pmkisan.gov.in/"
        ),
        Scheme(
            title = "PMFBY (Pradhan Mantri Fasal Bima Yojana)",
            description = "Crop insurance scheme providing financial support to farmers suffering crop loss/damage due to natural calamities, pests & diseases. Covers all food & oilseed crops.",
            link = "https://pmfby.gov.in/"
        ),
        Scheme(
            title = "PM-KMY (Pradhan Mantri Kisan Maandhan Yojana)",
            description = "Pension scheme for small and marginal farmers aged 18-40 years. Provides monthly pension of ₹3,000 after attaining the age of 60 years.",
            link = "https://maandhan.in/"
        ),
        Scheme(
            title = "KCC (Kisan Credit Card)",
            description = "Provides farmers with timely access to credit for cultivation and post-harvest expenses. Offers flexible credit limit based on land holding and crop pattern.",
            link = "https://pmkisan.gov.in/"
        ),
        Scheme(
            title = "Soil Health Card Scheme",
            description = "Provides farmers with soil test based recommendations on nutrients and fertilizers required for crops. Helps in improving soil health and productivity.",
            link = "https://soilhealth.dac.gov.in/"
        ),
        Scheme(
            title = "PKVY (Paramparagat Krishi Vikas Yojana)",
            description = "Promotes organic farming through cluster approach. Provides financial assistance of ₹50,000 per hectare for three years including organic inputs, certification, and marketing.",
            link = "https://pgsindia-ncof.gov.in/pkvy/"
        ),
        Scheme(
            title = "PMKSY (Pradhan Mantri Krishi Sinchayee Yojana)",
            description = "Aims to expand cultivable area with assured irrigation, improve water use efficiency, and adopt precision irrigation. Focus on 'Per Drop More Crop'.",
            link = "https://pmksy.gov.in/"
        ),
        Scheme(
            title = "National Agriculture Market (e-NAM)",
            description = "Online trading platform for agricultural commodities in India. Provides better price discovery through transparent auction process and access to nation-wide market.",
            link = "https://www.enam.gov.in/"
        ),
        Scheme(
            title = "PMKVY (Pradhan Mantri Kaushal Vikas Yojana)",
            description = "Skill development scheme for farmers and rural youth. Provides training in agriculture and allied sectors to enhance employability.",
            link = "https://www.pmkvyofficial.org/"
        ),
        Scheme(
            title = "Rashtriya Krishi Vikas Yojana (RKVY)",
            description = "State Plan Scheme for ensuring holistic development of agriculture and allied sectors. Provides flexibility to states in planning and execution.",
            link = "https://rkvy.nic.in/"
        ),
        Scheme(
            title = "Mission for Integrated Development of Horticulture (MIDH)",
            description = "Provides holistic growth of horticulture sector covering fruits, vegetables, root & tuber crops, mushrooms, spices, flowers, aromatic plants, cashew & cocoa.",
            link = "https://midh.gov.in/"
        ),
        Scheme(
            title = "Agricultural Export Policy",
            description = "Aims to double agricultural exports and integrate Indian farmers with global value chains. Provides infrastructure support and market access assistance.",
            link = "https://commerce.gov.in/"
        )
    )
}
