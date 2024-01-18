package com.queentylion.sibitranslator.presentation.profile


import android.bluetooth.BluetoothAdapter
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.queentylion.sibitranslator.presentation.permissions.SystemBroadcastReceiver
import com.queentylion.sibitranslator.presentation.sign_in.UserData
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.queentylion.sibitranslator.data.ConnectionState
import com.queentylion.sibitranslator.presentation.permissions.PermissionUtils

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(
        userData: UserData?,
        onSignOut: () -> Unit,
        onTranslate: () -> Unit,
        onBluetooth: () -> Unit,
        isBluetoothConnected: Boolean,
        onBluetoothStateChanged:()->Unit,
        viewModel: GloveSensorsViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
) {
    Column(
            modifier = Modifier
                .fillMaxSize(),
//                .background(Color(0xFF191F28)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(userData?.profilePictureUrl != null) {
            AsyncImage(
                    model = userData.profilePictureUrl,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        if(userData?.username != null) {
            Text(
                    text = userData.username,
                    textAlign = TextAlign.Center,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = onTranslate,
//            colors = ButtonDefaults.buttonColors(
//                backgroundColor = Color(0xFFc69f68),
//                contentColor = Color(0xFF191F28)
//            )
        ) {
            Text(
                text = "Start Translation",
//                color = Color(0xFF191F28)
            )
        }

        if(!isBluetoothConnected) {
            Button(onClick = onBluetooth,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFFc69f68),
                    contentColor = Color(0xFF191F28)
                )
            ) {
                Text(text = "Start Connection", color = Color(0xFF191F28))
            }
        } else {
            SystemBroadcastReceiver(systemAction = BluetoothAdapter.ACTION_STATE_CHANGED){ bluetoothState ->
                val action = bluetoothState?.action ?: return@SystemBroadcastReceiver
                if(action == BluetoothAdapter.ACTION_STATE_CHANGED){
                    onBluetoothStateChanged()
                }
            }

            val permissionState = rememberMultiplePermissionsState(permissions = PermissionUtils.permissions)
            val lifecycleOwner = LocalLifecycleOwner.current
            val bleConnectionState = viewModel.connectionState

            DisposableEffect(
                key1 = lifecycleOwner,
                effect = {
                    val observer = LifecycleEventObserver{_,event ->
                        if(event == Lifecycle.Event.ON_START){
                            permissionState.launchMultiplePermissionRequest()
                            if(permissionState.allPermissionsGranted && bleConnectionState == ConnectionState.Disconnected){
                                viewModel.reconnect()
                            }
                        }
                        if(event == Lifecycle.Event.ON_STOP){
                            if (bleConnectionState == ConnectionState.Connected){
                                viewModel.disconnect()
                            }
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)

                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }
            )

            LaunchedEffect(key1 = permissionState.allPermissionsGranted){
                if(permissionState.allPermissionsGranted){
                    if(bleConnectionState == ConnectionState.Uninitialized){
                        viewModel.initializeConnection()
                    }
                }
            }

            Box(
//                modifier = Modifier
//                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .aspectRatio(1f)
                        .border(
                            BorderStroke(
                                5.dp, Color(0xFFc69f68)
                            ),
                            RoundedCornerShape(10.dp)
                        ),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    if(bleConnectionState == ConnectionState.CurrentlyInitializing){
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            CircularProgressIndicator()
                            if(viewModel.initializingMessage != null){
                                Text(
                                    text = viewModel.initializingMessage!!,
                                    color = Color(0xFFc69f68),
                                )
                            }
                        }
                    }else if(!permissionState.allPermissionsGranted){
                        Text(
                            text = "Go to the app setting and allow the missing permissions.",
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.padding(10.dp),
                            textAlign = TextAlign.Center,
                            color = Color(0xFF191F28)
                        )
                    }else if(viewModel.errorMessage != null){
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = viewModel.errorMessage!!
                            )
                            Button(
                                onClick = {
                                    if(permissionState.allPermissionsGranted){
                                        viewModel.initializeConnection()
                                    }
                                }
                            ) {
                                Text(
                                    text = "Try again", color = Color(0xFF191F28)
                                )
                            }
                        }
                    }else if(bleConnectionState == ConnectionState.Connected){
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ){
                            Text(
                                text = "Finger 1: ${viewModel.flexResistance[0]}",
                                style = MaterialTheme.typography.h6,
                                color = Color(0xFFc69f68)
                            )
                            Text(
                                text = "Finger 2: ${viewModel.flexResistance[1]}",
                                style = MaterialTheme.typography.h6,
                                color = Color(0xFFc69f68)
                            )
                            Text(
                                text = "Finger 3: ${viewModel.flexResistance[2]}",
                                style = MaterialTheme.typography.h6,
                                color = Color(0xFFc69f68)
                            )
                            Text(
                                text = "Finger 4: ${viewModel.flexResistance[3]}",
                                style = MaterialTheme.typography.h6,
                                color = Color(0xFFc69f68)
                            )
                            Text(
                                text = "Finger 5: ${viewModel.flexResistance[4]}",
                                style = MaterialTheme.typography.h6,
                                color = Color(0xFFc69f68)
                            )
                        }
                    }else if(bleConnectionState == ConnectionState.Disconnected){
                        Button(onClick = {
                            viewModel.initializeConnection()
                        }) {
                            Text(text = "Initialize again", color = Color(0xFF191F28))
                        }
                    }
                }
            }
        }



        Button(
            onClick = onSignOut,
//            colors = ButtonDefaults.buttonColors(
//                backgroundColor = Color(0xFFc69f68),
//                contentColor = Color(0xFF191F28)
//            )
        ) {
            Text(text = "Sign out",
//                color = Color(0xFF191F28)
            )
        }
    }
}