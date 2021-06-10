package com.tutoriales.simpleexecutor;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ExecutorService singleExecutor;
    private ExecutorService poolExecutor;
    private ScheduledExecutorService scheduleExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnSingle = findViewById(R.id.btnSingle);
        Button btnPool = findViewById(R.id.btnPool);
        Button btnSchedule = findViewById(R.id.btnSchedule);

        btnSingle.setOnClickListener(v -> { startSingleExecutorFutureCalleable(); });
        btnPool.setOnClickListener(v -> { startFixedThreadPoolExecutor(); });
        btnSchedule.setOnClickListener(v -> { startScheduleThreadPoolExecutorCalleable(); });
        
        singleExecutor = Executors.newSingleThreadExecutor(); //Ejecuta un solo thread a la vez
        poolExecutor = Executors.newFixedThreadPool(3); //Ejecuta máximo 3 thread a la vez, el siguiente lo pone en espera
        scheduleExecutor = Executors.newScheduledThreadPool(3); //Permite indicarle al sistema con que tiempo se ejecutan los threads
    }

    private void runInBackground(String tag, int time){
        Log.d(tag, "run: start");
        for(int i=0;i<time;i++){
            Log.d(tag, "step " + (i+1000));
            SystemClock.sleep(1000);
        }

        Log.d(tag, "run: end");
    }

    private void startSingleExecutor(){
        try {
            singleExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    runInBackground("SingleExecutor 1",2);
                }
            });
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void startSingleExecutorFutureRunneable(){
        try {
            Future future = singleExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    runInBackground("SingleFutureRunneable 1",2);
                }
            });

            future.get(); //obtiene el valor del executor, es sincrono

            singleExecutor.shutdown(); //utilizarlo para indicar que ya no se va a realizar mas tareas
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void startSingleExecutorFutureCalleable(){
        try {
            String result="";
            Future future = singleExecutor.submit(new Callable<String>() {
                public String call() throws Exception {
                    System.out.println("Asynchronous Callable");
                    return "Callable Result";
                }
            });

            //realizar otras operaciones
            //

            result = (String) future.get(); //obtiene el valor del executor cuando este listo, si llegas a este punto antes de terminar, genera bloqueos

            Log.d("SingleFutureCallback", result);

            singleExecutor.shutdown();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void startFixedThreadPoolExecutor(){
        try {
            poolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    runInBackground("ThreadPoolExecutor 1",3);
                }
            });
            poolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    runInBackground("ThreadPoolExecutor 2",3);
                }
            });
            poolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    runInBackground("ThreadPoolExecutor 3",3);
                }
            });
            poolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    runInBackground("ThreadPoolExecutor 4",3);
                }
            });
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void startScheduleExecutorFutureRunneable(){
        try {
            Future future = scheduleExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    runInBackground("ScheduleFutureRunneable 1",2);
                }
            });

            scheduleExecutor.schedule(new Runnable() {
                @Override
                public void run() {
                    future.cancel(true);
                } //no detiene el thread pero indica que no se usa el future o no devuelte datos
            },5, TimeUnit.SECONDS);

            future.get(); //obtiene el valor del executor, es sincrono

            scheduleExecutor.shutdown(); //utilizar al finalizar el executor, tambien sirve para hacer que los threads en cola no se ejecuten
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void startScheduleThreadPoolExecutorCalleable(){
        try {
            List<String> list=null;
            Future future = scheduleExecutor.submit(getCallableList("ScheduleCalleable",2));

            list = (List<String>) future.get(); //obtiene el valor del executor, si no esta listo, genera un bloqueo

            if(list!=null && list.size()>0){
               Log.d("ScheduleFutureCallback", list.get(0));
            }

            scheduleExecutor.shutdown();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private Callable<List<String>> getCallableList(String tag, int time){
        Callable<List<String>> listCallable = new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                List<String> list = new ArrayList<>();
                list.add("1 item");
                list.add("2 item");
                Log.d(tag, "getCallableList: start");
                SystemClock.sleep(time*1000);
                Log.d(tag, "getCallableList: end");
                return list;
            }
        };
        return listCallable;
    }
}