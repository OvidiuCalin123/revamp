package com.example.msa_getpet

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.net.HttpHeaders
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.util.InternalAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager
import okhttp3.OkHttpClient
import okhttp3.FormBody
import okhttp3.Request
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.call.*
import io.ktor.http.content.TextContent
import coil.compose.AsyncImage



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Msa_getPetTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("login_input") { LoginInputScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("search") { SearchScreen(navController) }
        composable("cityDetail/{cityID}") { backStackEntry ->
            CityDetailScreen(navController, cityID = backStackEntry.arguments?.getString("cityID") ?: "")
        }
        composable("petShopList/{cityID}") {backStackEntry ->
            PetShopListScreen(navController, cityID = backStackEntry.arguments?.getString("cityID") ?: "") }
        composable("petList/{shopID}") {backStackEntry ->
            PetListScreen(navController, shopID = backStackEntry.arguments?.getString("shopID") ?: "")}
    }
}

@Composable
fun LoginScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "GetPet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = { navController.navigate("login_input") }, // Navigate to LoginInputScreen
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Login", color = MaterialTheme.colorScheme.onPrimary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Don't have an account?", color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Register",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
                modifier = Modifier.clickable { navController.navigate("register") }
            )
        }
    }
}

@Composable
fun LoginInputScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isEmailValid by remember { mutableStateOf(true) }
    var isPasswordValid by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Login",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            isError = !isEmailValid,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
        )
        if (!isEmailValid) {
            Text(
                text = "Invalid email address",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            isError = !isPasswordValid,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
        )
        if (!isPasswordValid) {
            Text(
                text = "Password must be at least 6 characters",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                isPasswordValid = password.length >= 6

                if (isEmailValid && isPasswordValid) {
                    coroutineScope.launch {
                        val result = loginUser(email, password)
                        if (result) {
                            Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                            navController.navigate("search")
                        } else {
                            Toast.makeText(context, "Login failed. Try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Login", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}


suspend fun loginUser(email: String, password: String): Boolean {
    val client = HttpClient(CIO) {
        engine {
            https {
                trustManager = object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate>? = null
                    override fun checkClientTrusted(chain: Array<X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<X509Certificate>?, authType: String?) {}
                }
            }
        }
    }

    return try {
        val queryParams = "email=$email&password=$password"

        val response = client.get("https://10.0.2.2:7239/api/Login?$queryParams") {
            contentType(ContentType.Application.Json)
        }

        response.status == HttpStatusCode.OK
    } catch (e: Exception) {
        println("Login failed: ${e.message}")
        false
    } finally {
        client.close()
    }
}


@Composable
fun RegisterScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isEmailValid by remember { mutableStateOf(true) }
    var isPasswordValid by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Register",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Email TextField
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            isError = !isEmailValid,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
        )
        if (!isEmailValid) {
            Text(
                text = "Invalid email address",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Password TextField
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            isError = !isPasswordValid,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
        )
        if (!isPasswordValid) {
            Text(
                text = "Password must be at least 6 characters",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Register Button
        Button(
            onClick = {
                isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                isPasswordValid = password.length >= 6

                if (isEmailValid && isPasswordValid) {
                    // Proceed with registration logic here
                    coroutineScope.launch {
                        val result = registerUser(email, password)
                        if (result) {
                            // On successful registration, navigate to login screen
                            navController.navigate("login")
                        } else {
                            // Handle failure (you can show an error message)
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Register", color = MaterialTheme.colorScheme.onPrimary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Back to Login Button
        Button(
            onClick = { navController.navigate("login") },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Back to Login", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

suspend fun registerUser(email: String, password: String): Boolean {
    val client = HttpClient(CIO) {
        engine {
            https {
                trustManager = object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate>? = null
                    override fun checkClientTrusted(chain: Array<X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<X509Certificate>?, authType: String?) {}
                }
            }
        }
    }

    return try {
        // Create the query string with email and password
        val queryParams = "email=$email&password=$password"

        // Make the GET request with query parameters in the URL
        val response = client.post("https://10.0.2.2:7239/api/Login?$queryParams") {
            contentType(ContentType.Application.Json) // Set content type (Optional)
        }

        // Handle the response
        if (response.status == HttpStatusCode.OK) {
            println("Successfully registered: ${response.status}")
            true
        } else {
            println("Failed registration with status: ${response.status}")
            false
        }
    } catch (e: Exception) {
        // Log the error
        println("Error during registration: ${e.message}")
        false
    } finally {
        // Close the client
        client.close()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var cities by remember { mutableStateOf(listOf<List<String>>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val response = fetchCitiesFromApi()
            println("Fetched Cities: $response")
            cities = response
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Failed to load cities: ${e.message}"
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Cities with GetPet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.contentColorFor(Color.White),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else if (errorMessage != null) {
            Text(
                text = errorMessage ?: "Unknown error",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(0.8f),
                placeholder = { Text("Search for a city", color = MaterialTheme.colorScheme.onBackground) },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = MaterialTheme.colorScheme.contentColorFor(Color.White),
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            val filteredCities = cities.filter { it[1].contains(query.text, ignoreCase = true) }
            println(filteredCities)
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                filteredCities.forEach { city ->
                    val cityID = city[0]  // Extract the ID (which is at index 0)
                    val cityName = city[1]  // Extract the city name (which is at index 1)

                    Text(
                        text = cityName,  // Display the city name
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clickable { navController.navigate("petShopList/$cityID") }  // Use cityId for navigation
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("login") },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Back to Login", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

suspend fun fetchCitiesFromApi(): List<List<String>> {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }

        engine {
            https {
                trustManager = object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate>? = null
                    override fun checkClientTrusted(chain: Array<X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<X509Certificate>?, authType: String?) {}
                }
            }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30000  // Set a longer timeout (30 seconds)
        }
    }

    return try {
        // Attempt to fetch data from the API
        val response = client.get("https://10.0.2.2:7239/api/Cities")
        val rawResponse = response.bodyAsText()
        println("Raw Response: $rawResponse")

        if (rawResponse.isNotEmpty()) {
            val cityData = mutableListOf<List<String>>()
            val cityID = "\"id\":"
            val cityNamePrefix = "\"cityName\":\""

            var currentIndex = 0
            while (true) {
                // Find "id"
                val idStart = rawResponse.indexOf(cityID, currentIndex)
                if (idStart == -1) break // No more cities found

                val idEnd = rawResponse.indexOf(",", idStart + cityID.length)
                val id = rawResponse.substring(idStart + cityID.length, idEnd).trim()

                // Find "cityName"
                val cityNameStart = rawResponse.indexOf(cityNamePrefix, idEnd)
                if (cityNameStart == -1) break // No cityName found after id

                val cityNameEnd = rawResponse.indexOf("\"", cityNameStart + cityNamePrefix.length)
                if (cityNameEnd != -1) {
                    val cityName = rawResponse.substring(cityNameStart + cityNamePrefix.length, cityNameEnd)
                    cityData.add(listOf(id, cityName)) // Add ID and city name as a pair to the list
                    currentIndex = cityNameEnd // Move past the current city
                } else {
                    break // End of city name not found (malformed data)
                }
            }

            cityData
        } else {
            emptyList() // Return empty if response is empty
        }
    } catch (e: Exception) {
        println("Error fetching cities: ${e.message}")
        emptyList() // Return an empty list in case of error
    } finally {
        client.close()
    }
}


@Composable
fun CityDetailScreen(navController: NavController, cityID: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("City: $cityID", color = MaterialTheme.colorScheme.onBackground)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("petShopList/$cityID") },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Pet Shops", color = MaterialTheme.colorScheme.onPrimary)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { navController.navigate("petList") },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Pets", color = MaterialTheme.colorScheme.onPrimary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Back", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetShopListScreen(navController: NavController, cityID: String) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var petShops by remember { mutableStateOf(listOf<List<String>>())}
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch pet shops from the API
    LaunchedEffect(Unit) {
        try {
            // Fetch pet shops from the API
            val response = fetchPetShopsFromApi(cityID)
            println("Fetched Pet Shops: $response")
            petShops = response
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Failed to load pet shops: ${e.message}"
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pet Shops",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else if (errorMessage != null) {
            Text(
                text = errorMessage ?: "Unknown error",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(0.8f),
                placeholder = { Text("Search Pet Shops", color = MaterialTheme.colorScheme.onBackground) },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = MaterialTheme.colorScheme.contentColorFor(Color.White),
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            println("ovi"+petShops)
            val filteredPetShops = petShops.filter { innerList ->
                innerList.size > 1 && innerList[1].contains(query.text, ignoreCase = true)
            }
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                filteredPetShops.forEach { shop ->
                    val shopID = shop[0]
                    println("alin"+shop[1])
                    Text(
                        text = shop[1],

                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clickable { navController.navigate("petList/$shopID") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Back", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

suspend fun fetchPetShopsFromApi(cityID: String): List<List<String>> {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }

        engine {
            https {
                trustManager = object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate>? = null
                    override fun checkClientTrusted(chain: Array<X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<X509Certificate>?, authType: String?) {}
                }
            }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30000
        }
    }

    return try {
        // Attempt to fetch data from the API (replace localhost with 10.0.2.2 for the emulator)
        val response = client.get("https://10.0.2.2:7239/api/PetShop/$cityID")
        val rawResponse = response.bodyAsText()
        println("Raw Response: $rawResponse")

        if (rawResponse.isNotEmpty()) {
            val petShopData = mutableListOf<List<String>>()
            val shopIDPrefix = "\"id\":"
            val petShopNamePrefix = "\"petShopName\":\""

            // Parse the response for pet shop details
            var currentIndex = 0
            while (true) {
                // Find the start of the next "id"
                val idStart = rawResponse.indexOf(shopIDPrefix, currentIndex)
                if (idStart == -1) break // No more pet shop IDs found

                val idEnd = rawResponse.indexOf(",", idStart + shopIDPrefix.length)
                val shopID = rawResponse.substring(idStart + shopIDPrefix.length, idEnd).trim()

                // Find the start of the next "petShopName"
                val petShopNameStart = rawResponse.indexOf(petShopNamePrefix, idEnd)
                if (petShopNameStart == -1) break // No petShopName found after ID

                val petShopNameEnd = rawResponse.indexOf("\"", petShopNameStart + petShopNamePrefix.length)
                if (petShopNameEnd != -1) {
                    val petShopName = rawResponse.substring(petShopNameStart + petShopNamePrefix.length, petShopNameEnd)
                    petShopData.add(listOf(shopID, petShopName)) // Add ID and name as a pair to the list
                    currentIndex = petShopNameEnd // Move past the current pet shop name
                } else {
                    break // End of pet shop name not found (malformed data)
                }
            }

            petShopData
        } else {
            emptyList() // Return empty if response is empty
        }
    } catch (e: Exception) {
        println("Error fetching pet shops: ${e.message}")
        emptyList() // Return an empty list in case of error
    } finally {
        client.close()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetListScreen(navController: NavController, shopID: String) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var pets by remember { mutableStateOf(listOf<Map<String, String>>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch pets from API
    LaunchedEffect(Unit) {
        try {
            val response = fetchPetsFromApi(shopID)
            pets = response
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Failed to load pets: ${e.message}"
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pets Available",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else if (errorMessage != null) {
            Text(
                text = errorMessage ?: "Unknown error",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(0.8f),
                placeholder = { Text("Search by type or breed", color = MaterialTheme.colorScheme.onBackground) },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter pets by type or breed
            val filteredPets = pets.filter {
                it["type"]?.contains(query.text, ignoreCase = true) == true ||
                        it["breed"]?.contains(query.text, ignoreCase = true) == true
            }

            LazyColumn {
                items(filteredPets) { pet ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            // Display pet image if available
                            if (pet["animalImage"] != null) {
                                AsyncImage(
                                    model = pet["animalImage"],
                                    contentDescription = "Pet Image",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No Image",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "Name: ${pet["animalName"]}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text("Type: ${pet["type"]}", color = MaterialTheme.colorScheme.onBackground)
                                Text("Breed: ${pet["breed"]}", color = MaterialTheme.colorScheme.onBackground)
                                Text("Gender: ${pet["gender"]}", color = MaterialTheme.colorScheme.onBackground)
                                Text("Age: ${pet["age"]} years", color = MaterialTheme.colorScheme.onBackground)
                                Text("Price: \$${pet["price"]}", color = MaterialTheme.colorScheme.onBackground)
                                Text("Contact: ${pet["contactNumber"]}", color = MaterialTheme.colorScheme.onBackground)
                                Text(
                                    text = "Description: ${pet["description"]}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Back", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}



suspend fun fetchPetsFromApi(shopID: String): List<Map<String, String>> {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }

        engine {
            https {
                trustManager = object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate>? = null
                    override fun checkClientTrusted(chain: Array<X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<X509Certificate>?, authType: String?) {}
                }
            }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30000
        }
    }

    return try {
        val response = client.get("https://10.0.2.2:7239/api/Animal/$shopID")
        val rawResponse = response.bodyAsText()
        println("Raw Response: $rawResponse")

        if (rawResponse.isNotEmpty()) {
            val animals = mutableListOf<Map<String, String>>()
            val animalEntries = rawResponse.trim('[', ']').split("},{") // Split individual JSON objects

            animalEntries.forEach { entry ->
                val cleanedEntry = entry.replace("{", "").replace("}", "")
                val fields = cleanedEntry.split(",")
                val animalDetails = mutableMapOf<String, String>()

                fields.forEach { field ->
                    val parts = field.split(":").map { it.trim().replace("\"", "") }
                    if (parts.size == 2) {
                        val (key, value) = parts
                        animalDetails[key] = value
                    }
                }
                animals.add(animalDetails)
            }
            animals
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        println("Error fetching pets: ${e.message}")
        emptyList()
    } finally {
        client.close()
    }
}


@Composable
fun Msa_getPetTheme(content: @Composable () -> Unit) {
    val darkColorScheme = darkColorScheme(
        primary = Color(0xFFFFEB3B),
        onPrimary = Color.Black,
        background = Color.Black,
        onBackground = Color(0xFFFFEB3B)
    )

    MaterialTheme(colorScheme = darkColorScheme, content = content)
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    Msa_getPetTheme {
        AppNavigation()
    }
}
