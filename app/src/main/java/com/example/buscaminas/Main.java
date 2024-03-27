package com.example.buscaminas;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.Image;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import androidx.gridlayout.widget.GridLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Random;

public class Main extends AppCompatActivity {
    TextView tv1;
    TextView tv2;
    static int dificultad = 1;
    static int personaje;
    private MenuItem menuItem;

    private Button[][] campo;
    private GridLayout gridLayout;

    private boolean primerClic = true;
    private Drawable[][] fondosOriginales;
    int[] personajes = new int[] { R.drawable.icono1, R.drawable.icono2, R.drawable.icono3, R.drawable.icono4 };
    int redFlag = R.drawable.redflag;

    // Método llamado cuando se crea la actividad
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridLayout = findViewById(R.id.gridLayout);
        // Crear el campo de juego al inicio
        crearCampoMinado(dificultad);
    }

    // Método para crear el campo de juego (buscaminas) dependiendo de la dificultad
    public void crearCampoMinado(int dificultad) {
        int minas = 0;
        int casillas = 0;

        // Determinar el número de minas y casillas según la dificultad
        if (dificultad == 1) {
            casillas = 8;
            minas = 10;
        } else if (dificultad == 2) {
            casillas = 12;
            minas = 20;
        } else if (dificultad == 3) {
            casillas = 16;
            minas = 60;
        }

        // Configurar el GridLayout para mostrar el campo minado
        gridLayout.setColumnCount(casillas);
        gridLayout.setRowCount(casillas);

        // Calcular el tamaño de cada casilla en función del tamaño de la pantalla
        int a = getResources().getDisplayMetrics().widthPixels;
        int b = getResources().getDisplayMetrics().heightPixels;
        int tamanyo = Math.min(a / casillas, b / casillas);

        // Inicializar cada botón y obtener la imagen del fondo de los botones
        campo = new Button[casillas][casillas];
        fondosOriginales = new Drawable[casillas][casillas];

        // Configurar cada casilla una por una
        for (int fila = 0; fila < campo.length; fila++) {
            for (int columna = 0; columna < campo.length; columna++) {
                campo[fila][columna] = new Button(this);
                fondosOriginales[fila][columna] = campo[fila][columna].getBackground();
                campo[fila][columna].setPadding(0, 0, 0, 0);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = tamanyo;
                params.height = tamanyo;
                campo[fila][columna].setLayoutParams(params);

                // Configurar el evento clic para descubrir casillas o colocar minas
                int finalMinas = minas;
                int finalFila = fila;
                int finalColumna = columna;
                campo[fila][columna].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // En el primer clic, colocar las minas después del clic
                        if (primerClic) {
                            colocarMinas(finalMinas, finalFila, finalColumna);
                            primerClic = false;
                        }
                        // Verificar si la casilla contiene una mina o no
                        if (campo[finalFila][finalColumna].getTag() != null
                                && campo[finalFila][finalColumna].getTag().equals("mina")) {
                            // Mostrar la imagen del personaje y perder el juego
                            campo[finalFila][finalColumna].setBackgroundResource(personajes[personaje]);
                            perder();
                        } else {
                            // Descubrir casillas adyacentes
                            descubrirCasillas(finalFila, finalColumna);
                        }
                    }
                });

                // Configurar el evento de clic largo para colocar o quitar una bandera
                campo[finalFila][finalColumna].setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (campo[finalFila][finalColumna].getTag() == null
                                || campo[finalFila][finalColumna].getTag().equals("mina")) {
                            // No hay etiqueta o la casilla ya ha sido descubierta, establecer la bandera
                            if ("mina".equals(campo[finalFila][finalColumna].getTag())) {
                                campo[finalFila][finalColumna].setTag("Marcada");
                                campo[finalFila][finalColumna].setBackgroundResource(redFlag);
                            } else {
                                campo[finalFila][finalColumna].setTag("Sin Mina");
                                campo[finalFila][finalColumna].setBackgroundResource(redFlag);
                            }
                        } else if ("Marcada".equals(campo[finalFila][finalColumna].getTag())) {
                            // La casilla estaba marcada y se desmarcó, volver a ser una mina
                            campo[finalFila][finalColumna].setTag("mina");
                            campo[finalFila][finalColumna].setBackground(fondosOriginales[finalFila][finalColumna]);
                        } else if ("Sin Mina".equals(campo[finalFila][finalColumna].getTag())) {
                            // La casilla estaba marcada como no mina, quitar la bandera
                            campo[finalFila][finalColumna].setBackground(fondosOriginales[finalFila][finalColumna]);
                            campo[finalFila][finalColumna].setTag(null);
                        }
                        // Verificar la victoria cada vez que se coloca o quita una bandera
                        ganar();

                        return true;
                    }
                });

                // Agregar la casilla al GridLayout
                gridLayout.addView(campo[fila][columna]);
            }
        }
    }

    // Método para colocar las minas en el campo
    public void colocarMinas(int cantidadMinas, int filaInicial, int columnaInicial) {
        Random random = new Random();
        int filas = campo.length;
        int columnas = campo[0].length;

        for (int i = 0; i < cantidadMinas; i++) {
            int fila, columna;

            do {
                fila = random.nextInt(filas);
                columna = random.nextInt(columnas);
            } while (campo[fila][columna].getTag() != null ||
                    (fila >= filaInicial - 1 && fila <= filaInicial + 1 &&
                            columna >= columnaInicial - 1 && columna <= columnaInicial + 1));

            // Si necesitas ver las minas para la corrección, descomenta la siguiente línea
            // campo[fila][columna].setBackgroundResource(personajes[personaje]);
            campo[fila][columna].setTag("mina");
        }
    }

    // Método para manejar la condición de derrota
    public void perder() {
        // Deshabilitar todos los botones y mostrar un cuadro de diálogo de pérdida
        for (int fila = 0; fila < campo.length; fila++) {
            for (int columna = 0; columna < campo.length; columna++) {
                campo[fila][columna].setEnabled(false);
            }
        }
        String texto = "Has Perdido";
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage(texto);
        alertBuilder.setPositiveButton("Aceptar", null);
        alertBuilder.setNegativeButton("Nueva partida", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Limpiar el GridLayout y comenzar una nueva partida
                gridLayout.removeAllViews();
                crearCampoMinado(dificultad);
                primerClic = true;
            }
        });
        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }

    // Método para manejar la condición de victoria
    public void ganar() {
        int totalMinas = obtenerTotalMinas();
        int minasMarcadasCorrectamente = 0;

        // Contar las minas marcadas correctamente con bandera
        for (int fila = 0; fila < campo.length; fila++) {
            for (int columna = 0; columna < campo[fila].length; columna++) {
                if ("mina".equals(campo[fila][columna].getTag()) && "Marcada".equals(campo[fila][columna].getTag())) {
                    minasMarcadasCorrectamente++;
                }
            }
        }

        // Mostrar un cuadro de diálogo de victoria si se han marcado correctamente todas las minas
        if (minasMarcadasCorrectamente == totalMinas) {
            String texto = "Has Ganado";
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setMessage(texto);
            alertBuilder.setPositiveButton("Aceptar", null);
            alertBuilder.setNegativeButton("Nueva partida", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Limpiar el GridLayout y comenzar una nueva partida
                    gridLayout.removeAllViews();
                    crearCampoMinado(dificultad);
                    primerClic = true;
                }
            });
            AlertDialog dialog = alertBuilder.create();
            dialog.show();
        }
    }

    // Método para obtener el total de minas en el campo
    private int obtenerTotalMinas() {
        int totalMinas = 0;

        // Contar el número total de minas en el campo
        for (int fila = 0; fila < campo.length; fila++) {
            for (int columna = 0; columna < campo[fila].length; columna++) {
                if ("mina".equals(campo[fila][columna].getTag())) {
                    totalMinas++;
                }
            }
        }

        return totalMinas;
    }

    // Método para contar las minas adyacentes a una casilla
    public int contarMinasAdyacentes(int fila, int columna) {
        int contadorMinas = 0;
        int filas = campo.length;
        int columnas = campo[0].length;

        // Coordenadas relativas de las 8 casillas adyacentes
        int[][] adyacentes = { { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, -1 }, { 0, 1 }, { 1, -1 }, { 1, 0 }, { 1, 1 } };

        // Descubrir casillas adyacentes
        for (int[] offset : adyacentes) {
            int nuevaFila = fila + offset[0];
            int nuevaColumna = columna + offset[1];

            // Verificar si la nueva posición está dentro de los límites del campo
            boolean dentroDeLimites = nuevaFila >= 0 && nuevaFila < filas && nuevaColumna >= 0 && nuevaColumna < columnas;

            if (dentroDeLimites && esMina(nuevaFila, nuevaColumna)) {
                contadorMinas++;
            }
        }

        return contadorMinas;
    }

    // Método que verifica si una casilla es una mina basándose en su tag
    private boolean esMina(int fila, int columna) {
        String tag = (String) campo[fila][columna].getTag();
        return tag != null && (tag.equals("mina") || tag.equals("Marcada"));
    }

    // Método para descubrir casillas
    private void descubrirCasillas(int fila, int columna) {
        if (campo[fila][columna].getTag() == null) {
            campo[fila][columna].setText(String.valueOf(contarMinasAdyacentes(fila, columna)));

            if (contarMinasAdyacentes(fila, columna) == 0) {
                campo[fila][columna].setText("");
                campo[fila][columna].setEnabled(false);
                campo[fila][columna].setBackgroundResource(R.drawable.cruz);
                campo[fila][columna].setTag("descubierta");

                // La casilla está vacía, descubrir casillas adyacentes
                descubrirCasillasAdyacentes(fila, columna);
            } else {
                campo[fila][columna].setTag("descubierta");
            }
        }
    }

    // Método para descubrir casillas vacías adyacentes si también están vacías
    private void descubrirCasillasAdyacentes(int fila, int columna) {
        int filas = campo.length;
        int columnas = campo[0].length;

        // Coordenadas relativas de las 8 casillas adyacentes
        int[][] adyacentes = { { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, -1 }, { 0, 1 }, { 1, -1 }, { 1, 0 }, { 1, 1 } };

        for (int[] offset : adyacentes) {
            int nuevaFila = fila + offset[0];
            int nuevaColumna = columna + offset[1];

            // Verificar si la nueva posición está dentro de los límites del campo
            if (nuevaFila >= 0 && nuevaFila < filas && nuevaColumna >= 0 && nuevaColumna < columnas) {
                descubrirCasillas(nuevaFila, nuevaColumna);
            }
        }
    }

    //A partir de aqui es donde esta toda la parte del menu
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu1, menu);
        menuItem = menu.findItem(R.id.icono);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {

        String texto = "";
        final String[] dificultades = { "" };
        //Primera opcion, las instrucciones
        if (menuItem.getItemId() == R.id.Instrucciones) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            texto = "El Buscaminas es un juego de lógica en el que el objetivo es descubrir todas las casillas vacías en un tablero sin detonar ninguna mina. Aquí tienes las reglas clave:\n"
                    + "\n" + "Haz clic en una casilla para descubrirla.\n"
                    + "Los números en las casillas reveladas indican cuántas minas hay en casillas adyacentes.\n"
                    + "Usa la información de los números para evitar las minas.\n"
                    + "Si seleccionas una mina, pierdes automáticamente.\n"
                    + "Marca casillas sospechosas de contener minas con banderas.\n"
                    + "Gana al descubrir todas las casillas seguras.\n"
                    + "¡Demuestra tu habilidad y lógica para ganar en el Buscaminas!";
            builder.setMessage(texto);
            builder.setPositiveButton("Aceptar", null);
            AlertDialog dialog = builder.create();
            dialog.show();

        } else if (menuItem.getItemId() == R.id.NuevoJuego) {
            //Segunda opcion, crear una partida nueva
            gridLayout.removeAllViews();
            crearCampoMinado(dificultad);
            primerClic = true;

        } else if (menuItem.getItemId() == R.id.Dificultad) {
            /*Tercera opcion, definir la dificultad, he puesto que cuando seleccionas la dificultad directamente te empiece una partida nueva
            , no lo he testeado del todo pero no deberia dar problema pero si los da comentar lineas 361-363*/


            View customView = getLayoutInflater().inflate(R.layout.dificultades, null);
            RadioButton radioOption1 = customView.findViewById(R.id.radioOption1);
            RadioButton radioOption2 = customView.findViewById(R.id.radioOption2);
            RadioButton radioOption3 = customView.findViewById(R.id.radioOption3);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(customView);
            builder.setTitle("Selecciona una opción:");
            builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (radioOption1.isChecked()) {
                        dificultades[0] = "Principiante";
                        dificultad = 1;
                    } else if (radioOption2.isChecked()) {
                        dificultades[0] = "Amateur";
                        dificultad = 2;
                    } else if (radioOption3.isChecked()) {
                        dificultades[0] = "Avanzado";
                        dificultad = 3;
                    }
                    gridLayout.removeAllViews();
                    crearCampoMinado(dificultad);
                    primerClic = true;
                    dialog.dismiss();
                }

            });
            builder.create().show();
        } else if (menuItem.getItemId() == R.id.CambiarPersonaje) {
             /*Cuarta opcion, definir la con que personaje quieres jugar, he puesto que cuando seleccionas un personaje directamente te empiece una partida nueva
            , no lo he testeado del todo pero no deberia dar problema pero si los da comentar lineas 392-394*/
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.personaje, null);
            final Spinner mySpinner = dialogView.findViewById(R.id.my_spinner);
            final ArrayList<String> personajes = new ArrayList<>();
            personajes.add("Personaje 1");
            personajes.add("Personaje 2");
            personajes.add("Personaje 3");
            personajes.add("Personaje 4");
            int[] iconos = { R.drawable.icono1, R.drawable.icono2, R.drawable.icono3, R.drawable.icono4 };
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                    personajes);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mySpinner.setAdapter(adapter);
            builder.setView(dialogView);
            builder.setTitle("Selecciona un personaje:");
            builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    personaje = mySpinner.getSelectedItemPosition();
                    menuItem.setIcon(iconos[personaje]);
                    gridLayout.removeAllViews();
                    crearCampoMinado(dificultad);
                    primerClic = true;
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }
        return true;
    }

}
