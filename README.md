# Application Template MVVM

# Application Template

[![Kotlin](https://img.shields.io/badge/Kotlin-1.5.20-blue.svg)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-SDK%2021%2B-green.svg)](https://developer.android.com/)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM-orange.svg)](https://developer.android.com/jetpack/guide?gclid=Cj0KCQiA6Or_BRC_ARIsAPzuer_1YlgAtJ-8YN_Brdq9RAwvfr-6eSd9GpR5rxqwmjy7BX2On8UqpwgaAsNTEALw_wcB&gclsrc=aw.ds)
[![Room](https://img.shields.io/badge/Room-2.3.0-blueviolet.svg)](https://developer.android.com/training/data-storage/room)
[![Coroutine](https://img.shields.io/badge/Coroutine-1.5.0-orange.svg)](https://kotlinlang.org/docs/coroutines-overview.html)
[![LiveData](https://img.shields.io/badge/LiveData-2.3.1-green.svg)](https://developer.android.com/topic/libraries/architecture/livedata)
[![RecyclerView](https://img.shields.io/badge/RecyclerView-1.2.1-brightgreen.svg)](https://developer.android.com/jetpack/androidx/releases/recyclerview)
[![Dagger Hilt](https://img.shields.io/badge/Dagger%20Hilt-2.39.1-red.svg)](https://dagger.dev/hilt/)

Application Template is a sample Android application written in Kotlin, 
following the MVVM (Model-View-ViewModel) architecture. 
It provides a starting point for developers who are building a Banking Application. 
The application integrates various libraries and technologies commonly used in Android development.

## Features

- Sale, Void, Refund, and Batch Close operations for a POS device.
- Printing a slip after performing the above operations.
- Stores operations in a local database thanks to Room.
- Support for making requests from Postman to perform the operations.
- Includes example usages of some library functions below the example menu
- Shows how the usage CardService Library for reading a card and emv Configuration.
- Shows how to upload parameters for a bin table, allowed operations, supported AIDs and config files.

## Prerequisites

- Android SDK 28 or higher.
- Kotlin 1.8.0 or higher.
- Gradle 8.0.1 or higher

## Dependencies

The application includes the following dependencies:

- Kotlin Coroutines: For managing background threads with simplified code and reducing callback-based code.
- Android Room: Provides an abstraction layer over SQLite to handle database operations.
- Android LiveData: A data holder class that allows observing changes in data across the application.
- Android RecyclerView: A powerful UI component for displaying large datasets.
- Dagger Hilt: A dependency injection framework for managing dependencies and enhancing testability.

## Usage

1. Clone the repository:
2. Sync Gradle settings with [Gradle Assistant](https://developer.android.com/build/agp-upgrade-assistant)
3. Run the Template
