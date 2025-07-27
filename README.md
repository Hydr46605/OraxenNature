<h1 align="center">🌱 OraxenNature</h1>
<p align="center">A powerful Spigot plugin for enhancing natural world generation with Oraxen custom blocks and trees.</p>

---

## Links

- [ModRinth](https://modrinth.com/plugin/oraxennature)
- [Oraxen](https://polymart.org/product/629/oraxen)

## Features

-   **Custom Block Population:** Generate Oraxen custom blocks as ores or other natural formations.
-   **Custom Tree Population:** Populate your worlds with unique Oraxen custom trees.
-   **Configurable Growth System:** Implement custom growth and decay mechanics for Oraxen blocks.
-   **In-Game Editor:** Easily modify and debug configuration values directly in-game.
-   **Multi-Version Compatibility:** Supports Minecraft versions from 1.16 to 1.21.8 (experimental).

## Commands

-   `/on reload <all|block_populator|tree_populator|growth_config>`: Reloads plugin configurations.
-   `/on set <config_file> <path> <value>`: Sets a configuration value (e.g., `/on set block_populator blocks.my_block.enabled true`).
-   `/on get <config_file> <path>`: Gets a configuration value (e.g., `/on get block_populator blocks.my_block.enabled`).
-   `/on debug <true|false>`: Toggles debug mode for more verbose logging.
-   `/on help`: Displays the help message.

## Permissions

-   `oraxennature.admin`: Allows access to all OraxenNature admin commands (default: op).
-   `oraxennature.reload`: Allows reloading OraxenNature configurations (default: op).
-   `oraxennature.set`: Allows setting OraxenNature configuration values in-game (default: op).
-   `oraxennature.get`: Allows getting OraxenNature configuration values in-game (default: op).
-   `oraxennature.debug`: Allows toggling OraxenNature debug mode (default: op).

## Installation

1.  Download the latest `OraxenNature.jar` from the [GitHub Releases](https://github.com/Hydr46605/OraxenNature/releases) or from [Modrinth](https://modrinth.com/plugin/oraxennature).
2.  Place the `OraxenNature.jar` file into your server's `plugins` folder.
3.  Restart your server.
4.  (Optional) Edit the generated configuration files in the `plugins/OraxenNature` folder.

## Support

If you encounter any issues or have suggestions, please open an issue.
