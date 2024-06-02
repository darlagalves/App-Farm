package com.example.fazendinha.ui.farm

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fazendinha.model.Fazenda
import com.example.fazendinha.ui.principal.AppBar
import com.example.fazendinha.ui.principal.DrawerHeader
import com.example.fazendinha.ui.principal.NavigationDrawer
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import com.example.fabricasapatos.ui.activities.principal.ui.theme.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.reflect.KFunction1

class CreateFarmsActivity : ComponentActivity() {

    private lateinit var referencia : DatabaseReference

    //salvar tudo em um arquivo txt
    private val caminhoFile = "fileFarm"
    private var externoFile : File? = null
    private val armazenamentoExternoSomenteLeitura: Boolean get() {
        var armazSomLeitRet = false //Armazenamento externo somente leitura - retorno.
        val armazenamentoExterno = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED_READ_ONLY == armazenamentoExterno) {
            armazSomLeitRet = true
        }
        return (armazSomLeitRet)
    }
    private val armazenamentoExternoDisponivel: Boolean get() {
        var armazExtDispRet = false //Armazenamento externo disponível - retorno.
        val extStorageState = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == extStorageState) {
            armazExtDispRet = true
        }
        return(armazExtDispRet)
    }

    fun salvarTxt(farm : Fazenda){
        externoFile = File(getExternalFilesDir(caminhoFile), "fileFarm")
        try{
            val fileOutputStream = FileOutputStream(externoFile)
            fileOutputStream.write(farm.toString().toByteArray())
            fileOutputStream.close()
        } catch (e: IOException) {
            Log.i("Erro File Farm", e.printStackTrace().toString())
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            referencia = Firebase.database.reference.child("fazendas")


            val scaffoldState = rememberScaffoldState()
            val scope = rememberCoroutineScope()
            androidx.compose.material.Scaffold(
                scaffoldState = scaffoldState,
                topBar = {
                    AppBar(
                        onNavigationIconClick = {
                            scope.launch {
                                scaffoldState.drawerState.open()
                            }
                        }
                    )
                },
                drawerGesturesEnabled = scaffoldState.drawerState.isOpen,
                drawerContent = {
                    DrawerHeader()
                    NavigationDrawer()
                }
            ) {
                TelaCriarFarm(referencia , ::salvarTxt)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCriarFarm(referencia: DatabaseReference, salvarTxt: KFunction1<Fazenda, Unit>) {
    // Campos de texto para armazenar as informações digitadas pelo usuário
    val textField1Value = remember { mutableStateOf("") }
    val textField2Value = remember { mutableStateOf("") }
    val textField3Value = remember { mutableStateOf("") }
    val textField4Value = remember { mutableStateOf("") }

    //função para customizar a cor do text field
    val customTextFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        focusedBorderColor = md_theme_light_primary, // Define a cor da borda quando o campo está em foco
        unfocusedBorderColor = md_theme_light_primary, // Define a cor da borda quando o campo não está em foco
        disabledBorderColor = Color.Gray // Define a cor da borda quando o campo está desativado
    )

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp, vertical = 16.dp),
        //verticalArrangement = Arrangement.SpaceBetween, // Ajuste o arranjo vertical aqui
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = textField1Value.value,
            onValueChange = { textField1Value.value = it },
            label = { Text("Código") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = customTextFieldColors
        )

        OutlinedTextField(
            value = textField2Value.value,
            onValueChange = { textField2Value.value = it },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth(),
            colors = customTextFieldColors
        )

        OutlinedTextField(
            value = textField3Value.value,
            onValueChange = { textField3Value.value = it },
            label = { Text("Valor da Propriedade") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = customTextFieldColors
        )

        OutlinedTextField(
            value = textField4Value.value,
            onValueChange = { textField4Value.value = it },
            label = { Text("Qtd de Funcionários") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = customTextFieldColors
        )


        // Botão "Salvar"
        Button(
            onClick = {
                if (
                    textField1Value.value.isNotBlank() &&
                    textField2Value.value.isNotBlank() &&
                    textField3Value.value.isNotBlank() &&
                    textField4Value.value.isNotBlank()
                ) {
                    // Ação do botão
                    val farm = Fazenda(
                        textField1Value.value,
                        textField2Value.value,
                        textField3Value.value.toDouble(),
                        textField4Value.value.toInt()
                    )
                    referencia.child(farm.id.toString()).setValue(farm)

                    //mandando a fazenda para o arquivo txt
                    salvarTxt(farm)

                    // Limpar os campos de texto
                    textField1Value.value = ""
                    textField2Value.value = ""
                    textField3Value.value = ""
                    textField4Value.value = ""

                    context.startActivity(Intent(context, GetFarmsActivity::class.java))
                } else {
                    Toast.makeText(context, "Todos os campos devem estar preenchidos!", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(contentColor = Color.White, containerColor = md_theme_light_tertiaryContainer),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Start)
                .padding(2.dp)
        ) {
            Text(text = "Salvar")
        }

    }
}