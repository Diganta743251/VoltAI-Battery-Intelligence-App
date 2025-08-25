# TensorFlow Lite Models

## Battery Prediction Model (battery_model.tflite)

This directory should contain trained TensorFlow Lite models for battery analysis.

### Model Requirements:
- **Input**: [1, 6] - battery%, voltage, temperature, current, time_since_last_charge, charging_status
- **Output**: [1, 3] - predicted_drain_rate, health_score, time_to_empty

### Health Analysis Model (battery_health_model.tflite)

- **Input**: [1, 10, 6] - last 10 battery readings with 6 features each
- **Output**: [1, 4] - health_score, degradation_rate, cycle_count_estimate, lifespan_months

### Training Data Features:
1. Battery percentage (0-100)
2. Voltage (mV)
3. Temperature (°C * 10)
4. Current (µA)
5. Time since last charge (hours)
6. Charging status (0=discharging, 1=charging)

### Model Training:
Models should be trained using historical battery data from various devices and usage patterns.
The current implementation includes placeholder models that will gracefully fallback to rule-based analysis.

### Deployment:
Replace the placeholder files with actual trained models before production deployment.