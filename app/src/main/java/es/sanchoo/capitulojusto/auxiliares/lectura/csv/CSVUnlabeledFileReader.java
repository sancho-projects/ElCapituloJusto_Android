package es.sanchoo.capitulojusto.auxiliares.lectura.csv;

import es.sanchoo.capitulojusto.R;
import es.sanchoo.capitulojusto.auxiliares.lectura.Table;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CSVUnlabeledFileReader extends ReaderTemplate {
    private Context context;
    private InputStream inputStream;
    private String lastLine;

    public CSVUnlabeledFileReader(Context context) {
        this.context = context;
    }

    @Override
    public void openSource(boolean isManga) {
        try {
//            int resourceId = context.getResources().getIdentifier(source, "raw", context.getPackageName());
//            inputStream = context.getResources().openRawResource(resourceId);
            inputStream = context.getResources().openRawResource((isManga)
                            ? R.raw.solution
                            : R.raw.solution_anime
                    );
            fichero = new BufferedReader(new InputStreamReader(inputStream));
        } catch (Exception e) {
            Log.e("Resource Error", "Recurso no encontrado: ");
        }
    }

    @Override
    public void processHeaders(String headers) {
        table = new Table(dividir(headers));
    }

    @Override
    public void processData(String data) {
        List<Integer> datos = new ArrayList<>();
        for (String dato : dividir(data)) {
            datos.add(Integer.parseInt(dato));
        }
        table.addRow(datos);
    }

    @Override
    public void closeSource() {
        try {
            if (fichero != null) {
                fichero.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e) {
            Log.e("Resource Error", "El archivo no se ha podido cerrar correctamente");
        }
    }

    @Override
    public boolean hasMoreData() {
        return lastLine != null;
    }

    @Override
    public String getNextData() {
        try {
            lastLine = fichero.readLine();
            return lastLine;
        } catch (Exception e) {
            Log.e("Resource Error", "No se ha podido leer la siguiente línea");
            return null;
        }
    }

    protected List<String> dividir(String linea) {
        if (linea != null) return Arrays.asList(linea.split(","));
        return null;
    }
}
