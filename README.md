# nearby-discovery
This is a simple experiment to test peer discovery with the Google Nearby
Connections API. It is written in Kotlin and uses Jetpack.

For simplicity, the entire program is implemented in the MainActivity.
Note that a real application would/should not be written this way. For
example, changing the app orientation will restart Nearby (or it would
without `configChanges` customization in the manifest).

The app allows Nearby Connections to be turned on and off, and it lists
peers discovered on `onEndpointFound()` and unlists them on `onEndpointLost()`.
No actual connections are made.
