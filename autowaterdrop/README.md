# Auto WaterDrop - Minecraft Fabric Mod

## Description

Auto WaterDrop is a client-side Fabric mod for Minecraft 1.21.4 that automatically performs water bucket MLG to prevent fall damage. The mod ensures 100% success rate when falling from heights greater than 5 blocks with a water bucket in your hotbar.

## Features

- **Automatic Water Placement**: Detects falls and automatically places water before impact
- **Smart Timing**: Calculates optimal water placement timing with ping compensation
- **Intelligent Block Detection**: Finds valid water placement positions, including nearby blocks
- **Configurable Settings**: Customize fall height threshold, timing, and behavior
- **HUD Indicator**: Visual indicator showing mod status and readiness
- **Hotbar Management**: Automatically switches to water bucket and returns to previous slot
- **Safety Features**: Won't activate in Creative/Spectator modes or while using Elytra

## Installation

1. Install Minecraft 1.21.4
2. Install Fabric Loader 0.16.10 or higher
3. Install Fabric API for 1.21.4
4. Place the mod JAR file in your `.minecraft/mods` folder
5. Launch Minecraft with the Fabric profile

## Configuration

Press `V` to toggle the mod on/off (default keybind).

The mod creates a configuration file at `.minecraft/config/autowaterdrop.json` with the following options:

- **Min Fall Height**: Minimum blocks to fall before activation (3-20, default: 5)
- **Water Placement Ticks**: Ticks before impact to place water (1-6, default: 3)
- **Auto Return Slot**: Return to previous hotbar slot after landing (default: true)
- **Auto Pickup Water**: Automatically pick up placed water (default: false)
- **Search Nearby Blocks**: Find alternative placement if direct placement fails (default: true)
- **Show HUD Indicator**: Display status indicator on screen (default: true)
- **Safe Mode**: Add extra tick for safety margin (default: false)
- **Show Notifications**: Display chat messages on activation (default: false)

## Usage

1. Keep a water bucket in your hotbar (slots 1-9)
2. The mod will automatically activate when falling from sufficient height
3. Water will be placed just before impact to prevent damage
4. The HUD indicator shows:
   - **Green**: Ready (water bucket available)
   - **Yellow**: Currently falling
   - **Red**: No water bucket in hotbar

## Requirements

- Minecraft 1.21.4
- Fabric Loader 0.16.10+
- Fabric API
- Java 21+

## Building from Source

```bash
git clone https://github.com/autowaterdrop/autowaterdrop.git
cd autowaterdrop
./gradlew build
```

The compiled JAR will be in `build/libs/`

## License

MIT License

## Disclaimer

This is a client-side mod that only simulates legitimate player actions (slot switching and bucket usage). It does not modify game mechanics or send non-standard packets to servers.