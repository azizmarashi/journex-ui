package com.example.journexui.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.journexui.model.LoginRequest
import com.example.journexui.model.RegisterRequest
import com.example.journexui.network.RetrofitClient
import com.example.journexui.network.extractErrorMessage
import kotlinx.coroutines.launch

@Composable
fun AuthScreens(onLogin:(String)->Unit,onRegister:()->Unit) {
    var register by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Ready") }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Card(Modifier.width(430.dp), shape=RoundedCornerShape(24.dp)) {
                if(register) Register(
                    onSuccess={register=false; status="Registration successful"; onRegister()},
                    onBack={register=false},
                    notify={status=it}
                )
                else Login(
                    onSuccess=onLogin,
                    onRegister={register=true},
                    notify={status=it}
                )
            }
        }
        JournexStatusBar(status)
    }
}

@Composable private fun Login(onSuccess:(String)->Unit,onRegister:()->Unit,notify:(String)->Unit){
    var username by remember{mutableStateOf("")}; var password by remember{mutableStateOf("")}; var loading by remember{mutableStateOf(false)}; val scope=rememberCoroutineScope()
    Column(Modifier.padding(32.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        Text("Journex",style=MaterialTheme.typography.displaySmall); Text("Trading journal",style=MaterialTheme.typography.titleMedium)
        OutlinedTextField(username,{username=it},label={Text("Username")},singleLine=true,modifier=Modifier.fillMaxWidth())
        OutlinedTextField(password,{password=it},label={Text("Password")},visualTransformation=PasswordVisualTransformation(),singleLine=true,modifier=Modifier.fillMaxWidth())
        Button(enabled=!loading && username.isNotBlank() && password.isNotBlank(),modifier=Modifier.fillMaxWidth(),onClick={scope.launch{loading=true;try{onSuccess(RetrofitClient.api.login(LoginRequest(username,password)))}catch(e:Exception){notify(extractErrorMessage(e))}finally{loading=false}}}){Text(if(loading)"Signing in…" else "Sign in")}
        TextButton(onClick=onRegister,modifier=Modifier.fillMaxWidth()){Text("Create an account")}
    }
}

@Composable private fun Register(onSuccess:()->Unit,onBack:()->Unit,notify:(String)->Unit){
    var username by remember{mutableStateOf("")};var nickname by remember{mutableStateOf("")};var email by remember{mutableStateOf("")};var password by remember{mutableStateOf("")};var loading by remember{mutableStateOf(false)};val scope=rememberCoroutineScope()
    Column(Modifier.padding(32.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        Text("Create account",style=MaterialTheme.typography.headlineMedium)
        OutlinedTextField(username,{username=it},label={Text("Username")},singleLine=true,modifier=Modifier.fillMaxWidth())
        OutlinedTextField(nickname,{nickname=it},label={Text("Nickname")},singleLine=true,modifier=Modifier.fillMaxWidth())
        OutlinedTextField(email,{email=it},label={Text("Email")},singleLine=true,modifier=Modifier.fillMaxWidth())
        OutlinedTextField(password,{password=it},label={Text("Password")},visualTransformation=PasswordVisualTransformation(),singleLine=true,modifier=Modifier.fillMaxWidth())
        Button(enabled=!loading && username.isNotBlank()&&nickname.isNotBlank()&&email.isNotBlank()&&password.isNotBlank(),modifier=Modifier.fillMaxWidth(),onClick={scope.launch{loading=true;try{RetrofitClient.api.register(RegisterRequest(username,nickname,email,password));onSuccess()}catch(e:Exception){notify(extractErrorMessage(e))}finally{loading=false}}}){Text(if(loading)"Creating…" else "Create account")}
        TextButton(onClick=onBack,modifier=Modifier.fillMaxWidth()){Text("Back to sign in")}
    }
}
