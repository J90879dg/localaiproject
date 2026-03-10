from __future__ import annotations

import math

C_LIGHT = 299_792_458.0
PLANCK = 6.626_070_15e-34


def kinetic_displacement_m(initial_velocity_mps: float, acceleration_mps2: float, time_s: float) -> float:
    if time_s < 0:
        raise ValueError("time_s cannot be negative")
    return initial_velocity_mps * time_s + 0.5 * acceleration_mps2 * (time_s**2)


def electric_current_amps(voltage_v: float, resistance_ohm: float) -> tuple[float, float]:
    if resistance_ohm <= 0:
        raise ValueError("resistance_ohm must be positive")
    current = voltage_v / resistance_ohm
    power = voltage_v * current
    return current, power


def magnetic_force_newtons(
    charge_c: float,
    velocity_mps: float,
    magnetic_field_t: float,
    angle_deg: float = 90.0,
) -> float:
    theta = math.radians(angle_deg)
    return abs(charge_c * velocity_mps * magnetic_field_t * math.sin(theta))


def drag_force_newtons(
    fluid_density_kg_m3: float,
    velocity_mps: float,
    drag_coefficient: float,
    reference_area_m2: float,
) -> float:
    if fluid_density_kg_m3 <= 0 or drag_coefficient <= 0 or reference_area_m2 <= 0:
        raise ValueError("density, drag coefficient, and reference area must be positive")
    return 0.5 * fluid_density_kg_m3 * (velocity_mps**2) * drag_coefficient * reference_area_m2


def lorentz_factor(velocity_mps: float) -> float:
    if abs(velocity_mps) >= C_LIGHT:
        raise ValueError("velocity_mps must be less than speed of light")
    return 1.0 / math.sqrt(1.0 - (velocity_mps**2 / C_LIGHT**2))


def de_broglie_wavelength_m(momentum_kg_mps: float) -> float:
    if momentum_kg_mps <= 0:
        raise ValueError("momentum_kg_mps must be positive")
    return PLANCK / momentum_kg_mps
