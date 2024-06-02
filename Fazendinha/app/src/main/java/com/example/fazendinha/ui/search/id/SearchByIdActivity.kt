package com.example.fazendinha.ui.search.id

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.fabricasapatos.ui.activities.principal.ui.theme.md_theme_light_primary
import com.example.fabricasapatos.ui.activities.principal.ui.theme.md_theme_light_tertiary
import com.example.fazendinha.model.Fazenda
import com.example.fazendinha.ui.principal.AppBar
import com.example.fazendinha.ui.principal.DrawerHeader
import com.example.fazendinha.ui.principal.NavigationDrawer
import com.example.fazendinha.ui.search.name.ItemDaListaName
import com.google.firebase.database.*
import com.google.gson.Gson
import kotlinx.coroutines.launch

class SearchByIdActivity : ComponentActivity() {

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
                TelaPesquisaFarmById(farmList)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun TelaPesquisaFarmById(farmList: MutableState<List<Fazenda>>) {

    val filteredFarmList = mutableStateOf(emptyList<Fazenda>())
    val listState = rememberLazyListState()
    val searchQuery = remember { mutableStateOf("") }
    val searchButtonClicked = remember { mutableStateOf(false) }

    fun filterFarms() {
        if (searchButtonClicked.value) { // Executar a filtragem somente quando o botão "Pesquisar" for clicado
            val query = searchQuery.value
            filteredFarmList.value = farmList.value.filter { farm ->
                farm.id.contains(query)
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
