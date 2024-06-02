package com.example.fazendinha.ui.farm

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fabricasapatos.ui.activities.principal.ui.theme.md_theme_light_primary
import com.example.fabricasapatos.ui.activities.principal.ui.theme.md_theme_light_tertiaryContainer
import com.example.fazendinha.model.Fazenda
import com.example.fazendinha.ui.principal.AppBar
import com.example.fazendinha.ui.principal.DrawerHeader
import com.example.fazendinha.ui.principal.NavigationDrawer
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlin.reflect.KFunction1

class UpdateFarmActivity : ComponentActivity() {

    private lateinit var referencia : DatabaseReference


    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        referencia = FirebaseDatabase.getInstance().getReference("fazendas")
        val farm = intent.getParcelableExtra<Fazenda>("farm")
        setContent {
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
                if (farm != null) {
                    TelaUpdateFarm(farm , referencia)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaUpdateFarm(farm: Fazenda , referencia: DatabaseReference){
    // Campos de texto para armazenar as informações digitadas pelo usuário
    val textField1Value = remember { mutableStateOf(farm.id) }
    val textField2Value = remember { mutableStateOf(farm.name) }
    val textField3Value = remember { mutableStateOf(farm.valorPropriedade.toString()) }
    val textField4Value = remember { mutableStateOf(farm.qtdFuncionarios.toString()) }

    //função para customizar a cor do text field
    val customTextFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        focusedBorderColor = md_theme_light_primary, // Define a cor da borda quando o campo está em foco
        unfocusedBorderColor = md_theme_light_primary, // Define a cor da borda quando o campo não está em foco
        disabledBorderColor = Color.Gray // Define a cor da borda quando o campo está desativado
    )

    val enabled = remember { mutableStateOf(false) }

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
            colors = customTextFieldColors,
            enabled = enabled.value
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
                    //val farmId = referencia.push().key // Gera uma chave única para a nova fazenda
                    referencia.child(farm.id.toString()).setValue(farm)

                    //Log.i("teste", farm.toString())

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
                .align(Alignment.Start) // Ajuste o alinhamento vertical aqui
                .padding(2.dp)
        ) {
            Text(text = "Salvar")
        }

    }

}