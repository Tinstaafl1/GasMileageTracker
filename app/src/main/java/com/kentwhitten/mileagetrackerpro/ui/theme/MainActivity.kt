package com.kentwhitten.mileagetrackerpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import org.json.JSONArray
import org.json.JSONObject

private val AppBackground = Color(0xFF000000)
private val ScreenBackground = Color(0xFF050505)
private val CardBackground = Color(0xFF111111)
private val CardBorder = Color(0xFF4B5563)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFD1D5DB)
private val OrangeAccent = Color(0xFFF97316)
private val DarkPanel = Color(0xFF1F2937)
private val InputBackground = Color(0xFF0A0A0A)
private val ErrorColor = Color(0xFFEF4444)

private const val PREFS_NAME = "mileage_tracker_pro_storage"
private const val FUEL_UPS_KEY = "fuel_ups_json"
private const val VEHICLES_KEY = "vehicles_json"
private const val CURRENT_VEHICLE_KEY = "current_vehicle"

private val DATE_FORMAT = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.US)

fun today(): String = DATE_FORMAT.format(java.util.Date())

enum class AppScreen {
    Home,
    Add,
    History,
    Reports,
    Vehicles
}

data class Vehicle(
    val id: String = java.util.UUID.randomUUID().toString(),
    val emoji: String,
    val name: String,
    val year: String
)

data class FuelUpEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val date: String,
    val odometer: String,
    val gallons: String,
    val pricePerGallon: String,
    val totalCost: String,
    val notes: String,
    val mpg: String,
    val vehicleId: String
)

fun saveVehicles(context: Context, vehicles: List<Vehicle>) {
    val array = JSONArray()
    vehicles.forEach { vehicle ->
        val obj = JSONObject()
        obj.put("id", vehicle.id)
        obj.put("emoji", vehicle.emoji)
        obj.put("name", vehicle.name)
        obj.put("year", vehicle.year)
        array.put(obj)
    }
    context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(VEHICLES_KEY, array.toString())
        .apply()
}

fun loadVehicles(context: Context): List<Vehicle> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val json = prefs.getString(VEHICLES_KEY, null) ?: return getDefaultVehicles()

    return try {
        val array = JSONArray(json)
        val vehicles = mutableListOf<Vehicle>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            vehicles.add(
                Vehicle(
                    id = obj.optString("id").takeIf { it.isNotEmpty() } ?: java.util.UUID.randomUUID().toString(),
                    emoji = obj.optString("emoji"),
                    name = obj.optString("name"),
                    year = obj.optString("year")
                )
            )
        }
        vehicles
    } catch (e: Exception) {
        getDefaultVehicles()
    }
}

fun getDefaultVehicles(): List<Vehicle> {
    return listOf(
        Vehicle(emoji = "🚗", name = "Ford F-150", year = "2019")
    )
}

fun getCurrentVehicleId(context: Context): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getString(CURRENT_VEHICLE_KEY, "") ?: ""
}

fun setCurrentVehicleId(context: Context, vehicleId: String) {
    context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(CURRENT_VEHICLE_KEY, vehicleId)
        .apply()
}

fun saveFuelUps(context: Context, fuelUps: List<FuelUpEntry>) {
    val array = JSONArray()

    fuelUps.forEach { entry ->
        val obj = JSONObject()
        obj.put("id", entry.id)
        obj.put("date", entry.date)
        obj.put("odometer", entry.odometer)
        obj.put("gallons", entry.gallons)
        obj.put("pricePerGallon", entry.pricePerGallon)
        obj.put("totalCost", entry.totalCost)
        obj.put("notes", entry.notes)
        obj.put("mpg", entry.mpg)
        obj.put("vehicleId", entry.vehicleId)
        array.put(obj)
    }

    context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(FUEL_UPS_KEY, array.toString())
        .apply()
}

fun loadFuelUps(context: Context): List<FuelUpEntry> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val json = prefs.getString(FUEL_UPS_KEY, null) ?: return emptyList()

    return try {
        val array = JSONArray(json)
        val entries = mutableListOf<FuelUpEntry>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)

            entries.add(
                FuelUpEntry(
                    id = obj.optString("id").takeIf { it.isNotEmpty() } ?: java.util.UUID.randomUUID().toString(),
                    date = obj.optString("date"),
                    odometer = obj.optString("odometer"),
                    gallons = obj.optString("gallons"),
                    pricePerGallon = obj.optString("pricePerGallon"),
                    totalCost = obj.optString("totalCost"),
                    notes = obj.optString("notes"),
                    mpg = obj.optString("mpg", "--"),
                    vehicleId = obj.optString("vehicleId")
                )
            )
        }

        entries
    } catch (e: Exception) {
        emptyList()
    }
}

fun calculateTotalCost(
    gallons: String,
    pricePerGallon: String
): String {
    val gallonsNumber = gallons.toDoubleOrNull()
    val priceNumber = pricePerGallon.toDoubleOrNull()

    if (gallonsNumber == null || priceNumber == null) {
        return ""
    }

    return String.format("%.2f", gallonsNumber * priceNumber)
}

fun calculateMPG(
    currentOdometer: String,
    previousOdometer: String,
    gallons: String
): String {
    val currentOdo = currentOdometer.toDoubleOrNull() ?: return "--"
    val previousOdo = previousOdometer.toDoubleOrNull() ?: return "--"
    val gallonsNumber = gallons.toDoubleOrNull() ?: return "--"

    if (gallonsNumber == 0.0) return "--"

    val distance = currentOdo - previousOdo
    if (distance <= 0) return "--"

    val mpg = distance / gallonsNumber
    return String.format("%.1f", mpg)
}

fun calculateCostPerMile(totalCost: Double, distance: Double): String {
    if (distance == 0.0) return "--"
    val costPerMile = totalCost / distance
    return String.format("$%.2f", costPerMile)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MileageTrackerProApp()
        }
    }
}

@Composable
fun MileageTrackerProApp() {
    var currentScreen by remember { mutableStateOf(AppScreen.Home) }
    var editingEntryId by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val fuelUps = remember {
        loadFuelUps(context).toMutableStateList()
    }

    val vehicles = remember {
        loadVehicles(context).toMutableStateList()
    }

    var currentVehicleId by remember { mutableStateOf(getCurrentVehicleId(context)) }

    fun persistFuelUps() {
        saveFuelUps(context, fuelUps)
    }

    fun persistVehicles() {
        saveVehicles(context, vehicles)
    }

    if (currentVehicleId.isEmpty() && vehicles.isNotEmpty()) {
        currentVehicleId = vehicles.first().id
        setCurrentVehicleId(context, currentVehicleId)
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ScreenBackground)
                    .navigationBarsPadding()
            ) {
                TopBar()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (currentScreen) {
                        AppScreen.Home -> HomeScreen(
                            fuelUps = fuelUps,
                            vehicles = vehicles,
                            currentVehicleId = currentVehicleId,
                            onAddFuelUpClick = {
                                editingEntryId = null
                                currentScreen = AppScreen.Add
                            }
                        )

                        AppScreen.Add -> AddFuelUpScreen(
                            fuelUps = fuelUps,
                            vehicles = vehicles,
                            currentVehicleId = currentVehicleId,
                            editingEntryId = editingEntryId,
                            onSaveFuelUp = { entry ->
                                if (editingEntryId != null) {
                                    val index = fuelUps.indexOfFirst { it.id == editingEntryId }
                                    if (index >= 0) {
                                        fuelUps[index] = entry
                                    }
                                } else {
                                    fuelUps.add(0, entry)
                                }
                                persistFuelUps()
                                editingEntryId = null
                                currentScreen = AppScreen.History
                            }
                        )

                        AppScreen.History -> HistoryScreen(
                            fuelUps = fuelUps,
                            vehicles = vehicles,
                            currentVehicleId = currentVehicleId,
                            onEditClick = { entryId ->
                                editingEntryId = entryId
                                currentScreen = AppScreen.Add
                            },
                            onDeleteClick = { entryId ->
                                fuelUps.removeAll { it.id == entryId }
                                persistFuelUps()
                            }
                        )

                        AppScreen.Reports -> ReportsScreen(
                            fuelUps = fuelUps,
                            currentVehicleId = currentVehicleId
                        )

                        AppScreen.Vehicles -> VehiclesScreen(
                            vehicles = vehicles,
                            currentVehicleId = currentVehicleId,
                            onAddVehicle = { vehicle ->
                                vehicles.add(vehicle)
                                currentVehicleId = vehicle.id
                                persistVehicles()
                                setCurrentVehicleId(context, currentVehicleId)
                            },
                            onSelectVehicle = { vehicleId ->
                                currentVehicleId = vehicleId
                                setCurrentVehicleId(context, currentVehicleId)
                            },
                            onDeleteVehicle = { vehicleId ->
                                vehicles.removeAll { it.id == vehicleId }
                                fuelUps.removeAll { it.vehicleId == vehicleId }
                                persistVehicles()
                                persistFuelUps()
                                if (currentVehicleId == vehicleId) {
                                    currentVehicleId = vehicles.firstOrNull()?.id ?: ""
                                    setCurrentVehicleId(context, currentVehicleId)
                                }
                            }
                        )
                    }
                }

                BottomNavigationBar(
                    currentScreen = currentScreen,
                    onScreenSelected = { selectedScreen ->
                        currentScreen = selectedScreen
                    }
                )
            }
        }
    }
}

@Composable
fun TopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .border(width = 1.dp, color = Color(0xFF374151))
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Mileage Tracker Pro",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ScreenContainer(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        content = content
    )
}

@Composable
fun HomeScreen(
    fuelUps: List<FuelUpEntry>,
    vehicles: List<Vehicle>,
    currentVehicleId: String,
    onAddFuelUpClick: () -> Unit
) {
    val vehicleFuelUps = fuelUps.filter { it.vehicleId == currentVehicleId }
    val latest = vehicleFuelUps.firstOrNull()
    val totalCost = vehicleFuelUps.sumOf { it.totalCost.toDoubleOrNull() ?: 0.0 }
    val averageMpg = vehicleFuelUps.mapNotNull { it.mpg.toDoubleOrNull() }.average()
    val currentVehicle = vehicles.firstOrNull { it.id == currentVehicleId }

    val odometerReadings = vehicleFuelUps.mapNotNull { it.odometer.toDoubleOrNull() }
    val totalDistance = if (odometerReadings.size >= 2) odometerReadings.max() - odometerReadings.min() else 0.0
    val costPerMile = calculateCostPerMile(totalCost, totalDistance)

    ScreenContainer {
        VehicleSummaryCard(
            vehicle = currentVehicle,
            lifetimeMpg = if (averageMpg.isNaN()) "--" else String.format("%.1f", averageMpg),
            costPerMile = costPerMile
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Last Fill-Up",
                value = latest?.mpg ?: "--",
                subtitle = if (latest == null) "No entries yet" else "MPG on ${latest.date}",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Fuel Cost",
                value = "$${String.format("%.0f", totalCost)}",
                subtitle = "This vehicle",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Entries",
                value = vehicleFuelUps.size.toString(),
                subtitle = "Fuel-ups saved",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Avg MPG",
                value = if (averageMpg.isNaN()) "--" else String.format("%.1f", averageMpg),
                subtitle = "This vehicle",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryActionButton(
            text = "+ Add Fuel-Up",
            onClick = onAddFuelUpClick
        )

        Spacer(modifier = Modifier.height(22.dp))

        SectionTitle("Recent Fuel-Ups")

        Spacer(modifier = Modifier.height(12.dp))

        if (vehicleFuelUps.isEmpty()) {
            CardShell {
                Text(
                    text = "No fuel-ups yet. Add one to get started!",
                    color = TextSecondary,
                    fontSize = 16.sp
                )
            }
        } else {
            vehicleFuelUps.take(3).forEach { entry ->
                FuelUpRow(
                    date = entry.date,
                    details = "${entry.gallons} gal · Odometer ${entry.odometer} · $${entry.totalCost}",
                    mpg = entry.mpg
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun AddFuelUpScreen(
    fuelUps: List<FuelUpEntry>,
    vehicles: List<Vehicle>,
    currentVehicleId: String,
    editingEntryId: String?,
    onSaveFuelUp: (FuelUpEntry) -> Unit
) {
    val editingEntry = fuelUps.firstOrNull { it.id == editingEntryId }

    var date by remember { mutableStateOf(editingEntry?.date ?: today()) }
    var odometer by remember { mutableStateOf(editingEntry?.odometer ?: "") }
    var gallons by remember { mutableStateOf(editingEntry?.gallons ?: "") }
    var pricePerGallon by remember { mutableStateOf(editingEntry?.pricePerGallon ?: "") }
    val totalCost = calculateTotalCost(gallons, pricePerGallon)
    var stationNotes by remember { mutableStateOf(editingEntry?.notes ?: "") }
    var message by remember { mutableStateOf("") }

    val currentVehicle = vehicles.firstOrNull { it.id == currentVehicleId }
    val vehicleFuelUps = fuelUps
        .filter { it.vehicleId == currentVehicleId }
        .sortedByDescending { runCatching { DATE_FORMAT.parse(it.date) }.getOrNull() }
    val previousEntry = vehicleFuelUps.firstOrNull { it.id != editingEntryId }

    val mpgValue = if (previousEntry != null && odometer.isNotBlank()) {
        calculateMPG(odometer, previousEntry.odometer, gallons)
    } else {
        "--"
    }

    ScreenContainer {
        PageTitle(
            title = if (editingEntry != null) "Edit Fuel-Up" else "Add Fuel-Up",
            subtitle = "Log your fuel-up details"
        )

        Spacer(modifier = Modifier.height(16.dp))

        CardShell {
            FieldLabel("Vehicle")
            StaticFieldText("${currentVehicle?.emoji ?: "🚗"} ${currentVehicle?.name ?: "Unknown"}")

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                label = "Date",
                value = date,
                onValueChange = { date = it },
                placeholder = "Example: May 1, 2026"
            )

            AppTextField(
                label = "Odometer",
                value = odometer,
                onValueChange = { odometer = it },
                placeholder = "Example: 52340",
                keyboardType = KeyboardType.Decimal
            )

            AppTextField(
                label = "Gallons",
                value = gallons,
                onValueChange = { gallons = it },
                placeholder = "Example: 18.40",
                keyboardType = KeyboardType.Decimal
            )

            AppTextField(
                label = "Price Per Gallon",
                value = pricePerGallon,
                onValueChange = { pricePerGallon = it },
                placeholder = "Example: 3.42",
                keyboardType = KeyboardType.Decimal
            )

            FieldLabel("Total Cost")
            Spacer(modifier = Modifier.height(8.dp))
            StaticFieldText(
                if (totalCost.isBlank()) {
                    "Calculated after gallons and price"
                } else {
                    "$$totalCost"
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            FieldLabel("Calculated MPG")
            Spacer(modifier = Modifier.height(8.dp))
            StaticFieldText(
                if (previousEntry == null) {
                    "First fill-up (no MPG yet)"
                } else {
                    "$mpgValue MPG"
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                label = "Station / Notes",
                value = stationNotes,
                onValueChange = { stationNotes = it },
                placeholder = "Optional notes"
            )
        }

        if (message.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                color = ErrorColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryActionButton(
            text = if (editingEntry != null) "Update Fuel-Up" else "Save Fuel-Up",
            onClick = {
                val gallonsNumber = gallons.toDoubleOrNull()
                val priceNumber = pricePerGallon.toDoubleOrNull()

                if (date.isBlank() || odometer.isBlank() || gallonsNumber == null || priceNumber == null || totalCost.isBlank()) {
                    message = "Enter a date, odometer, gallons, and price per gallon before saving."
                } else {
                    onSaveFuelUp(
                        FuelUpEntry(
                            id = editingEntry?.id ?: java.util.UUID.randomUUID().toString(),
                            date = date,
                            odometer = odometer,
                            gallons = gallons,
                            pricePerGallon = pricePerGallon,
                            totalCost = totalCost,
                            notes = stationNotes,
                            mpg = if (previousEntry == null) "--" else mpgValue,
                            vehicleId = currentVehicleId
                        )
                    )

                    date = today()
                    odometer = ""
                    gallons = ""
                    pricePerGallon = ""
                    stationNotes = ""
                    message = ""
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun HistoryScreen(
    fuelUps: List<FuelUpEntry>,
    vehicles: List<Vehicle>,
    currentVehicleId: String,
    onEditClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    val vehicleFuelUps = fuelUps.filter { it.vehicleId == currentVehicleId }
    val currentVehicle = vehicles.firstOrNull { it.id == currentVehicleId }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }

    if (pendingDeleteId != null) {
        ConfirmDeleteDialog(
            title = "Delete Entry",
            message = "This fuel-up will be permanently removed.",
            onConfirm = {
                onDeleteClick(pendingDeleteId!!)
                pendingDeleteId = null
            },
            onDismiss = { pendingDeleteId = null }
        )
    }

    ScreenContainer {
        PageTitle(
            title = "Fuel History",
            subtitle = "${currentVehicle?.name ?: "This vehicle"}'s fuel-ups"
        )

        Spacer(modifier = Modifier.height(18.dp))

        SectionTitle("Saved Fuel-Ups")

        Spacer(modifier = Modifier.height(12.dp))

        if (vehicleFuelUps.isEmpty()) {
            CardShell {
                Text(
                    text = "No fuel-ups saved yet.",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            vehicleFuelUps.forEach { entry ->
                HistoryEntryCard(
                    entry = entry,
                    vehicle = currentVehicle,
                    onEditClick = { onEditClick(entry.id) },
                    onDeleteClick = { pendingDeleteId = entry.id }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun ReportsScreen(
    fuelUps: List<FuelUpEntry>,
    currentVehicleId: String
) {
    val vehicleFuelUps = fuelUps.filter { it.vehicleId == currentVehicleId }
    val totalCost = vehicleFuelUps.sumOf { it.totalCost.toDoubleOrNull() ?: 0.0 }
    val avgMpg = vehicleFuelUps.mapNotNull { it.mpg.toDoubleOrNull() }.average()

    val chartData = vehicleFuelUps
        .sortedByDescending { runCatching { DATE_FORMAT.parse(it.date) }.getOrNull() }
        .take(6)
        .reversed()
        .mapNotNull { it.mpg.toDoubleOrNull()?.toInt()?.coerceIn(30, 100) }

    ScreenContainer {
        PageTitle(
            title = "Reports",
            subtitle = "Fuel economy and cost trends"
        )

        Spacer(modifier = Modifier.height(16.dp))

        CardShell {
            Text(
                text = "MPG Trend",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (avgMpg.isNaN()) "--" else String.format("%.1f", avgMpg),
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (chartData.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF0A0A0A))
                        .border(
                            width = 2.dp,
                            color = Color(0xFF3F3F46),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    chartData.forEach { mpg ->
                        ChartBar((mpg * 2), Modifier.weight(1f))
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF0A0A0A))
                        .border(
                            width = 2.dp,
                            color = Color(0xFF3F3F46),
                            shape = RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add fuel-ups to see trends",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Total Entries",
                value = vehicleFuelUps.size.toString(),
                subtitle = "This vehicle",
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Total Cost",
                value = "$${String.format("%.0f", totalCost)}",
                subtitle = "This vehicle",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun VehiclesScreen(
    vehicles: List<Vehicle>,
    currentVehicleId: String,
    onAddVehicle: (Vehicle) -> Unit,
    onSelectVehicle: (String) -> Unit,
    onDeleteVehicle: (String) -> Unit
) {
    var showAddVehicle by remember { mutableStateOf(false) }
    var pendingDeleteVehicleId by remember { mutableStateOf<String?>(null) }

    if (showAddVehicle) {
        AddVehicleDialog(
            onAdd = { vehicle ->
                onAddVehicle(vehicle)
                showAddVehicle = false
            },
            onDismiss = { showAddVehicle = false }
        )
    }

    if (pendingDeleteVehicleId != null) {
        ConfirmDeleteDialog(
            title = "Delete Vehicle",
            message = "This vehicle and all its fuel entries will be permanently removed.",
            onConfirm = {
                onDeleteVehicle(pendingDeleteVehicleId!!)
                pendingDeleteVehicleId = null
            },
            onDismiss = { pendingDeleteVehicleId = null }
        )
    }

    ScreenContainer {
        PageTitle(
            title = "Vehicles",
            subtitle = "Manage your vehicles"
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (vehicles.isEmpty()) {
            CardShell {
                Text(
                    text = "No vehicles yet. Add one to get started!",
                    color = TextSecondary,
                    fontSize = 16.sp
                )
            }
        } else {
            vehicles.forEach { vehicle ->
                VehicleRow(
                    emoji = vehicle.emoji,
                    name = "${vehicle.year} ${vehicle.name}",
                    subtitle = if (vehicle.id == currentVehicleId) "Current vehicle" else "Tap to select",
                    isSelected = vehicle.id == currentVehicleId,
                    onSelect = { onSelectVehicle(vehicle.id) },
                    onDelete = { pendingDeleteVehicleId = vehicle.id }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryActionButton(
            text = "+ Add Vehicle",
            onClick = { showAddVehicle = true }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun AddVehicleDialog(
    onAdd: (Vehicle) -> Unit,
    onDismiss: () -> Unit
) {
    var emoji by remember { mutableStateOf("🚗") }
    var name by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("2024") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clickable(enabled = false) {}
                .border(width = 2.dp, color = CardBorder, shape = RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Add Vehicle",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                AppTextField(
                    label = "Emoji",
                    value = emoji,
                    onValueChange = { emoji = it },
                    placeholder = "e.g., 🚗 🚙 🛻 🏎"
                )

                AppTextField(
                    label = "Vehicle Name",
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "e.g., Ford F-150"
                )

                AppTextField(
                    label = "Year",
                    value = year,
                    onValueChange = { year = it },
                    placeholder = "e.g., 2024",
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PrimaryActionButton(
                        text = "Add",
                        onClick = {
                            if (name.isNotBlank() && year.isNotBlank()) {
                                onAdd(Vehicle(emoji = emoji, name = name, year = year))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    SecondaryActionButton(
                        text = "Cancel",
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clickable(enabled = false) {}
                .border(width = 2.dp, color = CardBorder, shape = RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = title, color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = message, color = TextSecondary, fontSize = 16.sp, lineHeight = 22.sp)

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ErrorColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(text = "Delete", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    SecondaryActionButton(
                        text = "Cancel",
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
fun VehicleSummaryCard(
    vehicle: Vehicle?,
    lifetimeMpg: String,
    costPerMile: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = CardBorder,
                shape = RoundedCornerShape(26.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(26.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(134.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(OrangeAccent)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Current vehicle",
                    color = TextSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "${vehicle?.year ?: "Unknown"} ${vehicle?.name ?: "Vehicle"}",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HeroStat(
                        label = "Lifetime MPG",
                        value = lifetimeMpg,
                        modifier = Modifier.weight(1f)
                    )
                    HeroStat(
                        label = "Cost / Mile",
                        value = costPerMile,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(DarkPanel)
                    .border(
                        width = 2.dp,
                        color = CardBorder,
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = vehicle?.emoji ?: "🚗",
                    fontSize = 28.sp
                )
            }
        }
    }
}

@Composable
fun HeroStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF171717))
            .border(
                width = 2.dp,
                color = Color(0xFF3F3F46),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(14.dp)
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            color = TextPrimary,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(
                width = 2.dp,
                color = CardBorder,
                shape = RoundedCornerShape(22.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )

            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = OrangeAccent,
            contentColor = Color(0xFF111111)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun FuelUpRow(
    date: String,
    details: String,
    mpg: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = CardBorder,
                shape = RoundedCornerShape(22.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = date,
                    color = TextPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = details,
                    color = TextSecondary,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = mpg,
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "MPG",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground)
            .border(width = 2.dp, color = CardBorder)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        BottomNavItem("Home", "⌂", currentScreen == AppScreen.Home, { onScreenSelected(AppScreen.Home) }, Modifier.weight(1f))
        BottomNavItem("Add", "+", currentScreen == AppScreen.Add, { onScreenSelected(AppScreen.Add) }, Modifier.weight(1f))
        BottomNavItem("History", "≡", currentScreen == AppScreen.History, { onScreenSelected(AppScreen.History) }, Modifier.weight(1f))
        BottomNavItem("Reports", "▥", currentScreen == AppScreen.Reports, { onScreenSelected(AppScreen.Reports) }, Modifier.weight(1f))
        BottomNavItem("Vehicles", "⚙", currentScreen == AppScreen.Vehicles, { onScreenSelected(AppScreen.Vehicles) }, Modifier.weight(1f))
    }
}

@Composable
fun BottomNavItem(
    label: String,
    icon: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) OrangeAccent else Color.Transparent
    val textColor = if (selected) Color(0xFF111111) else TextSecondary

    TextButton(
        onClick = onClick,
        modifier = modifier
            .height(68.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(icon, color = textColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(3.dp))
            Text(label, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PageTitle(title: String, subtitle: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 34.sp
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = subtitle,
        color = TextSecondary,
        fontSize = 16.sp,
        lineHeight = 22.sp
    )
}

@Composable
fun SectionTitle(text: String) {
    Text(text = text, color = TextPrimary, fontSize = 23.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun CardShell(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 2.dp, color = CardBorder, shape = RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun FieldLabel(text: String) {
    Text(text = text, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
}

@Composable
fun StaticFieldText(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(InputBackground)
            .border(width = 2.dp, color = Color(0xFF6B7280), shape = RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(text = text, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AppTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    FieldLabel(label)
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(text = placeholder, color = Color(0xFFCBD5E1), fontSize = 16.sp)
        },
        textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 18.sp),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(18.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = OrangeAccent,
            unfocusedBorderColor = Color(0xFF6B7280),
            focusedContainerColor = InputBackground,
            unfocusedContainerColor = InputBackground,
            cursorColor = OrangeAccent
        ),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun HistoryEntryCard(
    entry: FuelUpEntry,
    vehicle: Vehicle?,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    CardShell {
        Text(text = entry.date, color = TextPrimary, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFF2B1A10))
                .border(width = 2.dp, color = Color(0xFF7C2D12), shape = RoundedCornerShape(50.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(text = "${entry.mpg} MPG", color = Color(0xFFFFD7BF), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "${vehicle?.emoji ?: "🚗"} ${vehicle?.year ?: "Unknown"} ${vehicle?.name ?: "Vehicle"}", color = TextSecondary, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(14.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Gallons", entry.gallons, "", Modifier.weight(1f))
            StatCard("Odometer", entry.odometer, "", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))

        StatCard(
            title = "Total Cost",
            value = "$${entry.totalCost}",
            subtitle = if (entry.notes.isBlank()) "No notes" else entry.notes,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryActionButton("Edit", Modifier.weight(1f), onClick = onEditClick)
            SecondaryActionButton("Delete", Modifier.weight(1f), onClick = onDeleteClick)
        }
    }
}

@Composable
fun SecondaryActionButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CardBackground, contentColor = TextPrimary),
        shape = RoundedCornerShape(18.dp)
    ) {
        Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ChartBar(heightPercent: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightPercent.dp)
            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
            .background(OrangeAccent)
    )
}

@Composable
fun VehicleRow(
    emoji: String,
    name: String,
    subtitle: String,
    isSelected: Boolean = false,
    onSelect: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(
                width = 2.dp,
                color = if (isSelected) OrangeAccent else CardBorder,
                shape = RoundedCornerShape(22.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1C1410) else CardBackground
        ),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkPanel)
                        .border(
                            width = 2.dp,
                            color = if (isSelected) OrangeAccent else CardBorder,
                            shape = RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 26.sp)
                }

                Column {
                    Text(
                        text = name,
                        color = TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 23.sp
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(text = subtitle, color = TextSecondary, fontSize = 15.sp)
                }
            }

            if (isSelected) {
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(text = "✕", color = ErrorColor, fontSize = 20.sp)
                }
            } else {
                Text(text = "›", color = OrangeAccent, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
