package com.example.fazendinha.ui.farm

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.fabricasapatos.ui.activities.principal.ui.theme.md_theme_light_primary
import com.example.fabricasapatos.ui.activities.principal.ui.theme.md_theme_light_tertiaryContainer
import com.example.fazendinha.model.Fazenda
import com.example.fazendinha.ui.principal.AppBar
import com.example.fazendinha.ui.principal.DrawerHeader
import com.example.fazendinha.ui.principal.NavigationDrawer
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlin.reflect.KFunction0
import kotlin.reflect.KFunction1

class GetFarmsActivity : ComponentActivity() {
    private lateinit var referencia : DatabaseReference

    //val farmList = ArrayList<Fazenda>()
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
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.i("MENSAGEM", "Erro: $error")
                }
            })
        }
    }


    fun deleteFarm(id : String){
        referencia.child(id).removeValue()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
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
                TelaListarFarm(farmList , ::deleteFarm , ::getFarms)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TelaListarFarm(farmList: State<List<Fazenda>>, deleteFarm: KFunction1<String, Unit>, getFarms: KFunction0<Unit>) {
    val listState = rememberLazyListState()

    val context = LocalContext.current

    Column() {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        //OnClick Method
                        context.startActivity(Intent(context, CreateFarmsActivity::class.java))
                    },
                    containerColor = md_theme_light_primary,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add FAB",
                        tint = Color.White,
                    )
                }
            },
            content = {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(
                        items = farmList.value,
                        itemContent = {
                            ItemDaLista(farmList, farm = it , deleteFarm , getFarms)
                        })
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDaLista(farmList: State<List<Fazenda>>, farm: Fazenda, deleteFarm: (String) -> Unit, getFarms: () -> Unit) {

    val context = LocalContext.current

    val showDialog = remember { mutableStateOf(false) }
    if (showDialog.value){
        DialogDeleteFarm(farm , deleteFarm, getFarms) { showDialog.value = it }
    }

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
            }

            Box(
                modifier = Modifier.wrapContentSize(Alignment.CenterEnd)
            ) {
                IconButton(
                    onClick = {
                        var mochila = Bundle()
                        mochila.putParcelable("farm", farm)
                        context.startActivity((Intent(context, UpdateFarmActivity::class.java)).putExtras(mochila))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Lápis"
                    )
                }
            }

            Box(
                modifier = Modifier.wrapContentSize(Alignment.CenterEnd)
            ) {
                IconButton(

                    onClick = {
                        showDialog.value = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Lixeira"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogDeleteFarm(farm: Fazenda, deleteFarm: (String) -> Unit, getFarms: () -> Unit, setShowDialog: (Boolean) -> Unit = {}) {

    androidx.compose.material3.AlertDialog(
        onDismissRequest =  { setShowDialog(false) },//onClose,
        title = { Text("Excluir cliente") },
        text = { Text("Tem certeza de que deseja excluir este cliente?") },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = {
                    deleteFarm(farm.id)
                    getFarms()
                    setShowDialog(false)
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            androidx.compose.material3.Button(
                onClick = {
                    //showDialog = false
                    setShowDialog(false)
                }
            ) {
                Text("Cancelar")
            }
        }
    )
}