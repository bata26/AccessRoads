package it.unipi.accessroads

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import it.unipi.accessroads.model.AccessibilityPoint
import it.unipi.accessroads.model.Position
import it.unipi.accessroads.ui.theme.AccessRoadsTheme
import java.sql.Timestamp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccessRoadsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
    val point = AccessibilityPoint(
        Db.getUUID(),
        position = Position(0.0,0.0),
        counter = 1,
        timestamp = Timestamp(0),
        type = "elevator"
    )
    Db.postPoint(point)
    Log.d(TAG, "POST DONE")
    Db.getPoints()
    Log.d(TAG, "DONE GET")
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AccessRoadsTheme {
        Greeting("Android")
    }
}