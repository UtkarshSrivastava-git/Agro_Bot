package com.example.agrobot.ui.theme

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.agrobot.ChatViewModel
import com.example.agrobot.TranslatorHelper
import com.example.agrobot.ui.theme.SchemeItem
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.content.Context
import androidx.compose.ui.platform.LocalContext

data class Scheme(
    val title: String = "",
    val description: String = "",
    val link: String = ""
)

private const val TAG = "GovernmentSchemesScreen"

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun GovernmentSchemesScreen(
    viewModel: ChatViewModel
) {
    val schemes = remember { getDefaultSchemes() }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(schemes) { scheme ->
            SchemeItem(scheme,viewModel.userLangTag)
        }
    }
}

//@Composable
//fun GovernmentSchemesScreen() {
//    var schemes by remember { mutableStateOf<List<Scheme>>(emptyList()) }
//    var isLoading by remember { mutableStateOf(true) }
//    var errorMessage by remember { mutableStateOf<String?>(null) }
//
//    LaunchedEffect(Unit) {
//        Log.d(TAG, "LaunchedEffect started.")
//        isLoading = true
//        errorMessage = null
//
//        try {
//            // Use a timeout to prevent infinite loading if network/rules are hanging
//            kotlinx.coroutines.withTimeout(10000) {
//                val db = Firebase.firestore
//                val collectionRef = db.collection("schemes")
//
//                Log.d(TAG, "Checking Firestore for existing data.")
//                val querySnapshot = collectionRef.get().await()
//                val defaultSchemes = getDefaultSchemes()
//
//                if (querySnapshot.size() < defaultSchemes.size) {
//                    Log.d(TAG, "Firestore collection incomplete. Syncing ${defaultSchemes.size} schemes...")
//                    val batch = db.batch()
//                    defaultSchemes.forEach { scheme ->
//                        val docRef = collectionRef.document(scheme.title)
//                        batch.set(docRef, scheme)
//                    }
//                    batch.commit().await()
//                }
//
//                val fetched = collectionRef.get().await().documents.mapNotNull {
//                    it.toObject(Scheme::class.java)
//                }
//
//                schemes = fetched
//                if (fetched.isEmpty()) {
//                    errorMessage = "No schemes found in database."
//                }
//            }
//        } catch (_: kotlinx.coroutines.TimeoutCancellationException) {
//            errorMessage = "Connection timed out. Please check your internet."
//            Log.e(TAG, "Firestore request timed out")
//        } catch (e: Exception) {
//            if (e is kotlinx.coroutines.CancellationException) throw e
//            Log.e(TAG, "Error processing schemes: ", e)
//            errorMessage = "Error: ${e.localizedMessage}"
//        } finally {
//            isLoading = false
//            Log.d(TAG, "LaunchedEffect finished. isLoading=false")
//        }
//    }
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        if (isLoading) {
//            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
//            }
//        } else {
//            errorMessage?.let { msg ->
//                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                    Text(text = msg, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center) // Centered text
//                }
//            } ?: run {
//                if (schemes.isNotEmpty()) {
//                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//                        items(schemes) { scheme ->
//                            SchemeItem(scheme)
//                        }
//                    }
//                } else {
//                    // This condition is for when schemes are empty AND errorMessage is null.
//                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                        Text("No schemes available.", modifier = Modifier.padding(16.dp))
//                    }
//                }
//            }
//        }
//    }
//}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun SchemeItem(scheme: Scheme,
               userLangTag: String
) {
    var translatedTitle by remember { mutableStateOf(scheme.title) }
    var translatedDesc by remember { mutableStateOf(scheme.description) }
    val context = LocalContext

    LaunchedEffect(userLangTag) {
        if (userLangTag != "en") {
            try {
                translatedTitle = TranslatorHelper.translateText(
                    scheme.title,
                    "en",
                    userLangTag
                )
                translatedDesc = TranslatorHelper.translateText(
                    scheme.description,
                    "en",
                    userLangTag
                )
            } catch (e: Exception) {
                translatedTitle = scheme.title
                translatedDesc = scheme.description
            }
        } else {
            translatedTitle = scheme.title
            translatedDesc = scheme.description
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = translatedTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = translatedDesc,
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (scheme.link.isNotBlank()) {
                val context = LocalContext.current


                TextButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scheme.link))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Log.e(TAG, "Could not open link", e)
                        }
                    },
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "Visit Official Website",
                        style = MaterialTheme.typography.bodySmall.copy(
                            textDecoration = TextDecoration.Underline,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
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
