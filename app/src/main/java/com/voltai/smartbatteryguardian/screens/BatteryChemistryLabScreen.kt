package com.voltai.smartbatteryguardian.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

// Future-ready data models with extensible architecture
data class BatteryChemistryProfile(
    val chemistryType: ChemistryType,
    val nominalVoltage: Float,
    val energyDensity: Float,
    val powerDensity: Float,
    val cyclability: Int,
    val safetyRating: Float,
    val temperatureRange: TemperatureRange,
    val ionicConductivity: Float,
    val electrolyte: ElectrolyteInfo
)

data class MolecularAnalysis(
    val lithiumConcentration: Float,
    val electrolytePurity: Float,
    val electrodeIntegrity: Float,
    val separatorHealth: Float,
    val thermalStability: Float,
    val ionMobility: Float,
    val crystalStructure: CrystalStructureInfo
)

data class ElectrochemicalData(
    val openCircuitVoltage: Float,
    val internalResistance: Float,
    val impedanceSpectrum: List<ImpedancePoint>,
    val cyclicVoltammetry: List<CVPoint>,
    val galvanostaticProfile: List<GalvanoPoint>,
    val pulseResponse: List<PulsePoint>
)

// Scalable enums for future battery technologies
enum class ChemistryType {
    LITHIUM_ION_COBALT,
    LITHIUM_ION_MANGANESE,
    LITHIUM_ION_PHOSPHATE,
    LITHIUM_POLYMER,
    LITHIUM_TITANATE,
    SOLID_STATE_LITHIUM,
    SODIUM_ION,
    ALUMINUM_ION,
    QUANTUM_DOT,
    GRAPHENE_HYBRID
}

data class TemperatureRange(val min: Float, val max: Float, val optimal: Float)
data class ElectrolyteInfo(val type: String, val concentration: Float, val conductivity: Float)
data class CrystalStructureInfo(val phase: String, val latticeConstant: Float, val crystallinity: Float)
data class ImpedancePoint(val frequency: Float, val impedance: Float, val phase: Float)
data class CVPoint(val voltage: Float, val current: Float)
data class GalvanoPoint(val time: Float, val voltage: Float, val capacity: Float)
data class PulsePoint(val time: Float, val voltage: Float, val current: Float)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryChemistryLabScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("🔬 Analysis", "⚛️ Molecular", "📈 Electrochemical", "🧪 Testing", "🔮 Future")
    
    val chemistryProfile = remember { getCurrentBatteryChemistry() }
    val molecularData = remember { getMolecularAnalysis() }
    val electrochemicalData = remember { getElectrochemicalData() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FA),
                        Color(0xFFE8F4FD),
                        Color(0xFFDCE7FF)
                    )
                )
            )
    ) {
        // Scientific Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF6366F1),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.3f),
                                        Color.White.copy(alpha = 0.1f)
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🔬", fontSize = 32.sp)
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Battery Chemistry Lab",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Molecular-level battery analysis & insights",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    
                    ChemistryTypeIndicator(chemistryProfile.chemistryType)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Scientific Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ScientificMetricCard("⚛️", "${chemistryProfile.energyDensity.toInt()}", "Wh/kg")
                    ScientificMetricCard("⚡", "${chemistryProfile.powerDensity.toInt()}", "W/kg")
                    ScientificMetricCard("🔄", "${chemistryProfile.cyclability}", "Cycles")
                    ScientificMetricCard("🌡️", "${chemistryProfile.temperatureRange.optimal}°C", "Optimal")
                }
            }
        }
        
        // Scientific Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF6366F1),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
        
        // Scientific Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    item { ChemistryOverviewCard(chemistryProfile) }
                    item { SafetyAnalysisCard(chemistryProfile) }
                    item { PerformanceMetricsCard(chemistryProfile) }
                }
                1 -> {
                    item { MolecularStructureCard(molecularData) }
                    item { IonicAnalysisCard(molecularData) }
                    item { ElectrolyteHealthCard(molecularData) }
                }
                2 -> {
                    item { ImpedanceSpectroscopyCard(electrochemicalData) }
                    item { CyclicVoltammetryCard(electrochemicalData) }
                    item { GalvanostaticAnalysisCard(electrochemicalData) }
                }
                3 -> {
                    item { ChemistryDiagnosticTestsCard() }
                    item { CalibrationProtocolsCard() }
                    item { QualityAssuranceCard() }
                }
                4 -> {
                    item { FutureTechnologiesCard() }
                    item { ResearchInsightsCard() }
                    item { InnovationRoadmapCard() }
                }
            }
        }
    }
}

@Composable
fun ChemistryTypeIndicator(chemistryType: ChemistryType) {
    val displayName = when (chemistryType) {
        ChemistryType.LITHIUM_ION_COBALT -> "Li-Co"
        ChemistryType.LITHIUM_ION_MANGANESE -> "Li-Mn"
        ChemistryType.LITHIUM_ION_PHOSPHATE -> "LiFePO4"
        ChemistryType.LITHIUM_POLYMER -> "Li-Po"
        ChemistryType.LITHIUM_TITANATE -> "Li-Ti"
        ChemistryType.SOLID_STATE_LITHIUM -> "SS-Li"
        ChemistryType.SODIUM_ION -> "Na-ion"
        ChemistryType.ALUMINUM_ION -> "Al-ion"
        ChemistryType.QUANTUM_DOT -> "QD-Li"
        ChemistryType.GRAPHENE_HYBRID -> "Gr-Li"
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = displayName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Chemistry",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun ScientificMetricCard(icon: String, value: String, unit: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 16.sp)
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = unit,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun ChemistryOverviewCard(profile: BatteryChemistryProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "🔬 Chemistry Analysis",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Chemistry Visualization
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    ChemistryProperty("Type", profile.chemistryType.name.replace("_", " "))
                    ChemistryProperty("Nominal Voltage", "${profile.nominalVoltage}V")
                    ChemistryProperty("Energy Density", "${profile.energyDensity} Wh/kg")
                    ChemistryProperty("Power Density", "${profile.powerDensity} W/kg")
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        progress = profile.safetyRating / 10f,
                        modifier = Modifier.size(80.dp),
                        strokeWidth = 8.dp,
                        strokeCap = StrokeCap.Round,
                        color = when {
                            profile.safetyRating >= 8.5f -> Color(0xFF4CAF50)
                            profile.safetyRating >= 7.0f -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${(profile.safetyRating * 10).toInt()}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Safety Rating",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
}

@Composable
fun MolecularStructureCard(molecularData: MolecularAnalysis) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "⚛️ Molecular Structure Analysis",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MolecularMetric("Li⁺ Concentration", molecularData.lithiumConcentration, "mol/L")
                MolecularMetric("Electrolyte Purity", molecularData.electrolytePurity, "%")
                MolecularMetric("Electrode Integrity", molecularData.electrodeIntegrity, "%")
                MolecularMetric("Separator Health", molecularData.separatorHealth, "%")
                MolecularMetric("Thermal Stability", molecularData.thermalStability, "°C")
                MolecularMetric("Ion Mobility", molecularData.ionMobility, "cm²/Vs")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Crystal Structure Info
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF6366F1).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Crystal Structure: ${molecularData.crystalStructure.phase}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6366F1)
                    )
                    Text(
                        text = "Lattice Constant: ${molecularData.crystalStructure.latticeConstant}Å",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                    Text(
                        text = "Crystallinity: ${molecularData.crystalStructure.crystallinity}%",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
}

@Composable
fun MolecularMetric(label: String, value: Float, unit: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                progress = (value / 100f).coerceIn(0f, 1f),
                modifier = Modifier
                    .width(80.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFF6366F1)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${value.toInt()}$unit",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6366F1)
            )
        }
    }
}

@Composable
fun ChemistryProperty(label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF666666),
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// Future-ready data generation functions
fun getCurrentBatteryChemistry(): BatteryChemistryProfile {
    return BatteryChemistryProfile(
        chemistryType = ChemistryType.LITHIUM_ION_COBALT,
        nominalVoltage = 3.7f,
        energyDensity = 265f,
        powerDensity = 1800f,
        cyclability = 1000,
        safetyRating = 8.5f,
        temperatureRange = TemperatureRange(-20f, 60f, 25f),
        ionicConductivity = 10.5f,
        electrolyte = ElectrolyteInfo("LiPF6 in EC/DMC", 1.2f, 12.5f)
    )
}

fun getMolecularAnalysis(): MolecularAnalysis {
    return MolecularAnalysis(
        lithiumConcentration = 1.15f,
        electrolytePurity = 99.2f,
        electrodeIntegrity = 94.8f,
        separatorHealth = 97.1f,
        thermalStability = 180f,
        ionMobility = 2.3f,
        crystalStructure = CrystalStructureInfo("Cubic", 4.15f, 89.5f)
    )
}

fun getElectrochemicalData(): ElectrochemicalData {
    return ElectrochemicalData(
        openCircuitVoltage = 3.82f,
        internalResistance = 85f,
        impedanceSpectrum = generateImpedanceSpectrum(),
        cyclicVoltammetry = generateCVData(),
        galvanostaticProfile = generateGalvanostaticData(),
        pulseResponse = generatePulseData()
    )
}

// Helper functions for generating scientific data
fun generateImpedanceSpectrum(): List<ImpedancePoint> = 
    (1..100).map { i ->
        val freq = 10f.pow(i / 25f)
        ImpedancePoint(freq, 100f / sqrt(freq), atan(freq) * 57.3f)
    }

fun generateCVData(): List<CVPoint> = 
    (0..100).map { i ->
        val voltage = 2.5f + (i / 100f) * 2f
        CVPoint(voltage, sin(voltage * PI.toFloat()) * 0.1f)
    }

fun generateGalvanostaticData(): List<GalvanoPoint> = 
    (0..100).map { i ->
        val time = i * 60f
        GalvanoPoint(time, 4.2f - (i / 100f) * 1.5f, i * 10f)
    }

fun generatePulseData(): List<PulsePoint> = 
    (0..50).map { i ->
        val time = i * 10f
        PulsePoint(time, 3.7f + sin(time * 0.1f) * 0.2f, if (i % 10 < 5) 1f else 0f)
    }

// Placeholder composables for future implementation
@Composable fun SafetyAnalysisCard(profile: BatteryChemistryProfile) { /* Future implementation */ }
@Composable fun PerformanceMetricsCard(profile: BatteryChemistryProfile) { /* Future implementation */ }
@Composable fun IonicAnalysisCard(molecularData: MolecularAnalysis) { /* Future implementation */ }
@Composable fun ElectrolyteHealthCard(molecularData: MolecularAnalysis) { /* Future implementation */ }
@Composable fun ImpedanceSpectroscopyCard(data: ElectrochemicalData) { /* Future implementation */ }
@Composable fun CyclicVoltammetryCard(data: ElectrochemicalData) { /* Future implementation */ }
@Composable fun GalvanostaticAnalysisCard(data: ElectrochemicalData) { /* Future implementation */ }
@Composable fun ChemistryDiagnosticTestsCard() { /* Future implementation */ }
@Composable fun CalibrationProtocolsCard() { /* Future implementation */ }
@Composable fun QualityAssuranceCard() { /* Future implementation */ }
@Composable fun FutureTechnologiesCard() { /* Future implementation */ }
@Composable fun ResearchInsightsCard() { /* Future implementation */ }
@Composable fun InnovationRoadmapCard() { /* Future implementation */ }
