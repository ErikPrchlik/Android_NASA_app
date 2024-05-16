# Meteorite Info App

Welcome to the Meteorite Info App! This Android application provides information about meteorites that have fallen to Earth, powered by data from NASA's open API.

## Table of Contents
- [Introduction](#introduction)
- [Features](#features)
- [Getting Started](#getting-started)
- [Installation](#installation)
- [Usage](#usage)
- [API Documentation](#api-documentation)
- [Contributing](#contributing)

## Introduction

The Meteorite Info App is designed to help users learn about meteorites that have impacted Earth. By utilizing NASA's open API, the app fetches detailed information about these meteorites, including their names, locations, and fall dates. This project is a showcase of Android development skills, emphasizing code readability, work organization, and platform knowledge.

## Features

- **Meteorite Data**: Fetch and display detailed information about meteorites.
- **Search Functionality**: Find meteorites by name, year, or location. (TODO)
- **Interactive Map**: Visualize meteorite locations on a map.
- **Detail View**: View comprehensive details about each meteorite. (IN PROGRESS)

## Getting Started

To get started with the Meteorite Info App, you will need to register for access to NASA's API. This provides an X-App-Token necessary for making API requests.

1. Visit [NASA's Data Portal](https://data.nasa.gov/login) to register.
2. Create an application to receive your API token.
3. Note your API token for use in the app.

## Installation

Clone this repository to your local machine using:

```bash
git clone https://github.com/ErikPrchlik/Android_NASA_app.git
```

Open the project in Android Studio and build the project.

## Usage

1. Launch the app on your Android device or emulator.
2. Enter your NASA API token in the settings.
3. Explore meteorite data by searching or browsing the interactive map.

## API Documentation

This app uses the NASA Meteorite Landings API. Full documentation can be found [here](https://dev.socrata.com/foundry/data.nasa.gov/y77d-th95).

Example JSON response: [NASA Meteorite Data](https://data.nasa.gov/resource/y77d-th95.json)

## Contributing

We welcome contributions to improve the app! If you have suggestions or bug reports, please open an issue or submit a pull request.

1. Fork the repository.
2. Create a new branch: `git checkout -b feature/YourFeature`
3. Commit your changes: `git commit -m 'Add some feature'`
4. Push to the branch: `git push origin feature/YourFeature`
5. Open a pull request.

---

We hope you enjoy exploring meteorite data with our app! If you have any questions or need further assistance, feel free to contact us.

Happy coding! 🚀
