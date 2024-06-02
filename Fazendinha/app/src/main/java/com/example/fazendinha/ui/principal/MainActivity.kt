package com.example.fazendinha.ui.principal

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberImagePainter
import com.example.fabricasapatos.ui.activities.principal.ui.theme.md_theme_light_primary
import com.example.fazendinha.model.Fazenda
import com.example.fazendinha.ui.farm.CreateFarmsActivity
import com.example.fazendinha.ui.farm.GetFarmsActivity
import com.example.fazendinha.ui.search.id.SearchByIdActivity
import com.example.fazendinha.ui.search.name.SearchByNameActivity
import com.google.firebase.database.*
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var referencia : DatabaseReference

    private val farmList = mutableStateOf(emptyList<Fazenda>())
    var media = mutableStateOf(0.0)

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
                        // Calcula a média dos valores das propriedades
                        val average = farms.map { it.valorPropriedade }.average()
                        media.value = average
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
          Scaffold(
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

              Box(
                  modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 5.dp, vertical = 5.dp),
                  contentAlignment = Alignment.Center
              ) {
                  Column(
                      modifier = Modifier
                          .fillMaxSize()
                          .padding(horizontal = 5.dp, vertical = 5.dp),
                      verticalArrangement = Arrangement.Top
                  ) {
                      Card(
                          modifier = Modifier
                              .fillMaxWidth()
                              .padding(16.dp),
                          shape = RoundedCornerShape(8.dp),
                          border = BorderStroke(2.dp, md_theme_light_primary),
                          backgroundColor = Color.White
                      ) {
                          Column(
                              modifier = Modifier.padding(8.dp)
                          ) {
                              Row(
                                  modifier = Modifier.fillMaxWidth(),
                                  verticalAlignment = Alignment.CenterVertically
                              ) {
                                  Text(
                                      text = "Média do Valor das Propriedades",
                                      modifier = Modifier.weight(1f),
                                      textAlign = TextAlign.Center,
                                      style = MaterialTheme.typography.h5
                                  )
                                  Spacer(modifier = Modifier.width(8.dp))
                              }
                              Spacer(modifier = Modifier.height(8.dp))
                              Row(
                                  modifier = Modifier.fillMaxWidth(),
                                  verticalAlignment = Alignment.CenterVertically
                              ) {
                                  Text(
                                      text = DecimalFormat("#.##").format(media.value),
                                      textAlign = TextAlign.Center,
                                      style = MaterialTheme.typography.h4,
                                      modifier = Modifier.weight(1f)
                                  )

                              }
                          }
                      }
                  }


              }

          }
      }
  }
}


@Composable
fun NavigationDrawer (){
  val context = LocalContext.current
  DrawerBody(
    items = listOf(
      MenuItem(
        id = "fazenda",
        title = "Fazendas",
        contentDescription = "Tela de CRUD Fazendas",
        icon = Icons.Default.Agriculture
      ),
      MenuItem(
        id = "buscaNome",
        title = "Buscar por Nome",
        contentDescription = "Tela de Busca por Nome",
        icon = Icons.Default.Person
      ),
      MenuItem(
        id = "buscaId",
        title = "Buscar pelo Id",
        contentDescription = "Tela de Busca por Nome",
        icon = Icons.Default.LocalFlorist
      )
    ),
    onItemClick = {
      when(it.id){
        "fazenda" -> {context.startActivity(Intent(context, GetFarmsActivity::class.java))}
        "buscaNome" -> {context.startActivity(Intent(context, SearchByNameActivity::class.java))}
        "buscaId" -> {context.startActivity(Intent(context, SearchByIdActivity::class.java))}
      }
    }
  )
}

