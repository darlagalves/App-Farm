package com.example.fazendinha.ui.search.name

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.rememberScaffoldState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import com.example.fazendinha.model.Fazenda
import com.google.firebase.database.*
import com.google.gson.Gson
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import com.example.fazendinha.ui.principal.AppBar
import com.example.fazendinha.ui.principal.DrawerHeader
import com.example.fazendinha.ui.principal.NavigationDrawer
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.fabricasapatos.ui.activities.principal.ui.theme.md_theme_light_tertiary
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import com.example.fabricasapatos.ui.activities.principal.ui.theme.md_theme_light_primary
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import com.example.fabricasapatos.ui.activities.principal.ui.theme.md_theme_light_tertiaryContainer

class SearchByNameActivity : ComponentActivity() {

    private lateinit var referencia : DatabaseReference

    private val farmList = mutableStateOf(emptyList<Fazenda>())

    fun getFarms(){
        lifecycleScope.launch {
            referencia.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val gson = Gson()
                        val farms = mutableListOf<Fazenda>() // Lista temporária para as fazendas

                        for (i in snapshot.children) {
                            val json = gson.toJson(i.value)
                            val farm = gson.fromJson(json, Fazenda::class.java)
                            farms.add(Fazenda(farm.id, farm.name, farm.valorPropriedade, farm.qtdFuncionarios))
                        }

                        farmList.value = farms // Atualiza o valor do MutableState com a nova lista de fazendas
                        //Log.i("Teste", "Array: ${farmList.value}")
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.i("MENSAGEM", "Erro: $error")
                }
            })
        }
    }

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        referencia = FirebaseDatabase.getInstance().getReference("fazendas")
        getFarms()
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
                TelaPesquisaFarm(farmList)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun TelaPesquisaFarm(farmList: MutableState<List<Fazenda>>) {

    val filteredFarmList = mutableStateOf(emptyList<Fazenda>())
    val listState = rememberLazyListState()
    val searchQuery = remember { mutableStateOf("") }
    val searchButtonClicked = remember { mutableStateOf(false) }

    fun filterFarms() {
        if (searchButtonClicked.value) { // Executar a filtragem somente quando o botão "Pesquisar" for clicado
            val query = searchQuery.value.toLowerCase()
            filteredFarmList.value = farmList.value.filter { farm ->
                farm.name.toLowerCase().contains(query)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
                .padding(vertical =8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = searchQuery.value,
                onValueChange = { newValue ->
                    searchQuery.value = newValue
                },
                modifier = Modifier
                    .background(Color.White, shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                label = { Text("Pesquisar") },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = md_theme_light_primary,
                    unfocusedBorderColor = md_theme_light_primary,
                    focusedLabelColor = md_theme_light_primary,
                    unfocusedLabelColor = md_theme_light_primary
                )
            )


            IconButton(
                onClick = {
                    searchButtonClicked.value = true
                    filterFarms()
                },
                modifier = Modifier
                    .padding(top = 8.dp, start = 5.dp)
                    .background(md_theme_light_tertiary, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Pesquisar",
                    tint = Color.White
                )
            }

        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(
                items = filteredFarmList.value,
                itemContent = {
                    ItemDaListaName(farmList, farm = it)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDaListaName(farmList: MutableState<List<Fazenda>>, farm: Fazenda) {

    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(corner = CornerSize(16.dp)),
        backgroundColor = md_theme_light_tertiaryContainer
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = farm.name.toString(),
                    style = typography.titleLarge
                )
                Text(
                    text = "Código: ${farm.id.toString()}",
                    style = typography.bodySmall
                )
                Text(
                    text = "Valor da proriedade: ${farm.valorPropriedade.toString()}",
                    style = typography.bodySmall
                )
                Text(
                    text = "Qtd de funcionários: ${farm.qtdFuncionarios.toString()}",
                    style = typography.bodySmall
                )
            }
        }
    }
}


