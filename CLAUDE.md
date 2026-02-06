# CLAUDE.md — AI Assistant Guide for YAGSL-Example

## Project Overview

This is a **FIRST Robotics Competition (FRC)** swerve drive example robot project for **Team 3481 (BroncBotz)**, built on the **YAGSL (Yet Another Generic Swerve Library)** framework. It targets the **2024 FRC season (Crescendo)** and demonstrates a fully functional swerve drivetrain with autonomous path following, vision integration, and multiple motor controller configurations.

**Key resource:** [YAGSL Wiki](https://github.com/BroncBotz3481/YAGSL/wiki)

## Build System

- **Build tool:** Gradle 8.5 via GradleRIO 2024.3.2
- **Language:** Java 17
- **Team number:** 3481 (configured in `.wpilib/wpilib_preferences.json`)

### Common Commands

```bash
./gradlew build              # Compile and build the project
./gradlew deploy             # Deploy to the RoboRIO
./gradlew simulateDesktop    # Run desktop simulation with GUI
./gradlew test               # Run JUnit 5 tests
./gradlew clean build        # Clean rebuild
```

### Build Notes

- Desktop simulation support is enabled (`includeDesktopSupport = true`)
- Fat JAR packaging bundles all dependencies for RoboRIO deployment
- Deploy files in `src/main/deploy/` are copied to `/home/lvuser/deploy` on the robot
- JNI debug libraries are configured for both RoboRIO and desktop platforms

## Repository Structure

```
YAGSL-Example/
├── src/main/java/frc/robot/
│   ├── Main.java                          # Entry point (bootstrap only)
│   ├── Robot.java                         # TimedRobot lifecycle (periodic methods)
│   ├── RobotContainer.java               # Subsystems, commands, button bindings
│   ├── Constants.java                     # Global constants (mass, speed, PID, deadbands)
│   ├── commands/swervedrive/
│   │   ├── auto/
│   │   │   └── AutoBalanceCommand.java    # PID-based auto-balance on charge station
│   │   └── drivebase/
│   │       ├── AbsoluteDrive.java         # Field-centric drive with heading target
│   │       ├── AbsoluteDriveAdv.java      # Advanced drive with 4-direction face buttons
│   │       └── AbsoluteFieldDrive.java    # Field-centric drive with continuous heading
│   └── subsystems/swervedrive/
│       └── SwerveSubsystem.java           # Core swerve drive subsystem (~606 lines)
├── src/main/deploy/
│   ├── swerve/                            # Motor controller configurations
│   │   ├── neo/                           # REV SPARK MAX (NEO) — default config
│   │   ├── falcon/                        # CTRE TalonFX (Falcon)
│   │   └── maxSwerve/                     # MaxSwerve modules
│   └── pathplanner/                       # PathPlanner autonomous paths and autos
├── vendordeps/                            # Vendor dependency JSON files (9 libraries)
├── build.gradle                           # Gradle build configuration
├── settings.gradle                        # Plugin repositories
└── .wpilib/wpilib_preferences.json        # WPILib project metadata
```

## Key Entry Points

Execution flows: `Main.java` → `Robot.java` → `RobotContainer.java` → `SwerveSubsystem.java`

| File | Role |
|------|------|
| `Main.java` | Minimal bootstrap — calls `RobotBase.startRobot(Robot::new)` |
| `Robot.java` | TimedRobot lifecycle — instantiates RobotContainer, runs CommandScheduler |
| `RobotContainer.java` | Creates SwerveSubsystem (with NEO config), binds Xbox controller buttons, sets default drive command |
| `SwerveSubsystem.java` | Core subsystem — initializes SwerveDrive from JSON config, PathPlanner setup, drive commands, vision targeting, odometry |
| `Constants.java` | Global constants — robot mass, max speed, PID values, joystick deadbands |

## Swerve Configuration System

YAGSL uses a **JSON-based configuration** system located in `src/main/deploy/swerve/<config>/`. The active config is selected in `RobotContainer.java` (currently `swerve/neo`).

### Config directory structure (per motor type)

```
swerve/neo/
├── swervedrive.json          # IMU type/ID, module file references
├── controllerproperties.json # Heading PID, joystick deadband
└── modules/
    ├── frontleft.json        # Per-module: motor IDs, encoder offsets, location
    ├── frontright.json
    ├── backleft.json
    ├── backright.json
    ├── physicalproperties.json  # Conversion factors, current limits, wheel grip
    └── pidfproperties.json     # Drive/angle motor PID values
```

### Switching motor configurations

Change the constructor argument in `RobotContainer.java`:
```java
// NEO (default):
new SwerveSubsystem(new File(Filesystem.getDeployDirectory(), "swerve/neo"));
// Falcon:
new SwerveSubsystem(new File(Filesystem.getDeployDirectory(), "swerve/falcon"));
// MaxSwerve:
new SwerveSubsystem(new File(Filesystem.getDeployDirectory(), "swerve/maxSwerve"));
```

## Dependencies (Vendor Libraries)

| Library | File | Purpose |
|---------|------|---------|
| **YAGSL** | `yagsl.json` | Generic swerve drive library (core dependency) |
| **Phoenix 6** | `Phoenix6.json` | CTRE TalonFX motors, Pigeon2 IMU |
| **Phoenix 5** | `Phoenix5.json` | Legacy CTRE device support |
| **REVLib** | `REVLib.json` | REV SPARK MAX motor controllers |
| **PathplannerLib** | `PathplannerLib.json` | Autonomous path planning and following |
| **PhotonLib** | `photonlib.json` | PhotonVision camera integration |
| **NavX** | `NavX.json` | NavX gyroscope support |
| **ReduxLib** | `ReduxLib_2024.json` | Redux Robotics data logging |
| **WPILibNewCommands** | `WPILibNewCommands.json` | WPILib command-based framework |

Vendor dependency URL for YAGSL: `https://broncbotz3481.github.io/YAGSL-Lib/yagsl/yagsl.json`

## Code Conventions

### Style
- **Braces:** Allman style (opening brace on its own line for classes/methods)
- **Indentation:** 2-space indentation
- **Style config:** `.styles/BroncBotzStyle.xml` (comprehensive IDE formatter)
- **License header:** WPILib BSD license on all source files

### Naming
- **Classes:** PascalCase (`SwerveSubsystem`, `AbsoluteDriveAdv`)
- **Methods:** camelCase (`getPose()`, `driveFieldOriented()`)
- **Constants:** UPPER_SNAKE_CASE (`MAX_SPEED`, `WHEEL_LOCK_TIME`)
- **Packages:** lowercase dot-separated (`frc.robot.commands.swervedrive.drivebase`)

### Documentation
- JavaDoc on all public classes and methods, with `@param` and `@return` tags
- Inline comments for non-obvious logic
- Constructor comments explaining conversion factor math

### Architecture Patterns
- **WPILib Command-Based** paradigm — subsystems + commands, scheduled via CommandScheduler
- **Functional command composition** — uses `run()`, `Commands.runOnce()`, `Commands.deferredProxy()`, lambda-based DoubleSupplier inputs
- **JSON-driven configuration** — hardware config is separated from code via YAGSL JSON files
- **Alliance-aware** — autonomous paths auto-mirror for red alliance

## Important Constants (in Constants.java)

```java
MAX_SPEED = 4.42 m/s           // ~14.5 ft/s
ROBOT_MASS = 57.6 kg           // (148 - 20.3) lbs converted
LOOP_TIME = 0.13s              // 20ms + 110ms SPARK MAX velocity lag
WHEEL_LOCK_TIME = 10s          // Motor brake hold time when disabled
LEFT_X_DEADBAND = 0.1          // Joystick deadbands
LEFT_Y_DEADBAND = 0.1
RIGHT_X_DEADBAND = 0.1
Auton Translation PID = (0.7, 0, 0)
Auton Angle PID = (0.4, 0, 0.01)
```

## Controller Bindings (in RobotContainer.java)

| Button | Action |
|--------|--------|
| A | Zero gyro |
| X | Add fake vision reading (testing) |
| B (hold) | Drive to pose (4, 4) via PathPlanner pathfinding |
| Y (hold) | Aim at speaker (2° tolerance) |
| Left stick | Translation (X/Y movement) |
| Right stick | Heading direction (direct angle) or angular velocity |

Default command: field-oriented drive with direct angle control (right stick sets heading target). Simulation uses `simDriveCommand` instead.

## Testing

- **Framework:** JUnit 5 (configured in `build.gradle`)
- **Status:** No test files currently exist in the repository
- Run with: `./gradlew test`

## Simulation

Desktop simulation is supported:
```bash
./gradlew simulateDesktop
```
- Simulation GUI and Driver Station are auto-configured
- `SwerveSubsystem` constructor disables cosine compensation for simulation accuracy
- Separate `simDriveCommand` is used when `RobotBase.isSimulation()` returns true
- `simgui.json` and `simgui-ds.json` configure the simulation environment

## Common Modification Patterns

### Adding a new autonomous routine
1. Create the path in PathPlanner and save to `src/main/deploy/pathplanner/`
2. Reference it via `drivebase.getAutonomousCommand("YourAutoName")` in `RobotContainer.getAutonomousCommand()`

### Adding a new command
1. Create a new class in `frc.robot.commands.swervedrive.drivebase` (or appropriate package)
2. Extend `Command` or use functional command composition in `SwerveSubsystem`
3. Bind to a button in `RobotContainer.configureBindings()`

### Tuning PID values
- **Drive/Angle motor PID:** Edit `src/main/deploy/swerve/<config>/modules/pidfproperties.json`
- **Autonomous path following PID:** Edit `Constants.AutonConstants.TRANSLATION_PID` and `ANGLE_PID`
- **Heading controller PID:** Edit `src/main/deploy/swerve/<config>/controllerproperties.json`

### Changing hardware IDs or encoder offsets
- Edit the per-module JSON files in `src/main/deploy/swerve/<config>/modules/`
- Each module file specifies drive motor ID, angle motor ID, encoder ID, and absolute encoder offset

## CI/CD

No CI/CD pipelines are configured. Deployment is manual via `./gradlew deploy` to the RoboRIO.
