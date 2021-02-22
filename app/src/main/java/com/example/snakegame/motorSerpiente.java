package com.example.snakegame;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.SensorEvent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.CountDownTimer;
import android.os.Looper;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;


import java.util.Random;

public class motorSerpiente extends SurfaceView implements Runnable {
    public static Thread hilo = null;
    private final int soundId;
    private final int soundFinish;
    private final int soundFail;
    private final int soundChoque;
    private final SoundPool soundPool;
    private MediaPlayer mediaPlayer;

    private AlertDialog dialog;


    public Context contexto;

    public SoundPool sonido;
    private CountDownTimer countDownTime;

    public int pantallaX;
    public int pantallaY;

    public int tamanoSerpiente;

    public int manzanaX;
    public int manzanaY;

    public int enemigoX;
    public int enemigoY;

    public int blockSize;

    public final int NUM_BLOCKS_WIDE = 40;
    public int numBlocksAltura;

    public long nextFrameTime;

    public final long FPS = 1;

    public final long MILLIS_PER_SECOND = 1;

    public int puntuacion;

    public int[] ejeSerpienteXs;
    public int[] ejeSerpienteXy;

    public volatile boolean jugando;

    public Canvas canvas;

    public SurfaceHolder surfaceHolder;

    public Paint paint;

    private CountDownTimer countDownTimer;
    private long timeLeft = 60000;
    private boolean timeRunning;


    public motorSerpiente(Context context, Point size) {
        super(context);
        tiempo();
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(getContext(), R.raw.snake_music);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();


        }


        soundPool = new SoundPool(0, AudioManager.STREAM_MUSIC, 0);
        soundId = soundPool.load(getContext(), R.raw.eat_bob, 1);
        soundFinish = soundPool.load(getContext(), R.raw.win, 1);
        soundFail = soundPool.load(getContext(), R.raw.game_over, 1);
        soundChoque = soundPool.load(getContext(), R.raw.choque, 1);

        contexto = context;


        pantallaY = size.y;
        pantallaX = size.x;


        blockSize = pantallaX / NUM_BLOCKS_WIDE;
        numBlocksAltura = pantallaY / blockSize;



        surfaceHolder = getHolder();
        paint = new Paint();

        ejeSerpienteXs = new int[50];
        ejeSerpienteXy = new int[50];

        jugar();

    }


    private void finalizacion(motorSerpiente ma) {
        jugando = false;
        mediaPlayer.pause();
        if (puntuacion == 9) {
            soundPool.play(soundFinish, 1, 1, 1, 0, 1);
        } else {
            soundPool.play(soundFail, 1, 1, 1, 0, 1);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Juego finalizado");
        builder.setMessage("Su puntuacion ha sido de " + puntuacion + " ¿Que desea hacer?");        // add the buttons
        builder.setPositiveButton("Reiniciar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                jugando = true;
                mediaPlayer.start();
                jugar();
                timeLeft = 60000;
                //tiempo();
                hilo = new Thread(ma);
                hilo.start();

            }
        });
        builder.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        });
        dialog = builder.create();


    }


    private void tiempo() {
        if (timeRunning) {
            stopTimer();
        } else {
            startTimer();
        }

    }


    private void stopTimer() {
        countDownTimer.cancel();
        timeRunning = false;
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;

            }

            @Override
            public void onFinish() {
                jugando = false;
            }

        }.start();
        timeRunning = true;
    }

    @Override
    public void run() {

        try {
            Looper.prepare();
            //tiempo();
            while (jugando && puntuacion <= 9) {
                if (actualizar()) {
                    draw();
                    update();
                }

            }

            timeRunning = false;

            finalizacion(this);
            dialog.show();
            Looper.loop();

        } catch (Exception e) {
            //e.printStackTrace();
        }
    }


    public void pause() {
        jugando = false;
        mediaPlayer.release();
        try {
            hilo.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {

        }

    }


    public void resume() {

        jugando = true;
        hilo = new Thread(this);
        hilo.start();
//        mediaPlayer.start();
    }

    public void jugar() {
        tamanoSerpiente = 1;
        ejeSerpienteXs[0] = NUM_BLOCKS_WIDE / 2;
        ejeSerpienteXy[0] = numBlocksAltura / 2;

        generarManzana();
        generarEnemigo();
        puntuacion = 0;
        nextFrameTime = System.currentTimeMillis();

    }

    private void generarEnemigo() {

        Random enemigoAleatorio = new Random();
        enemigoX = enemigoAleatorio.nextInt(NUM_BLOCKS_WIDE - 1)+1;
        enemigoY = enemigoAleatorio.nextInt(numBlocksAltura - 5) + 1;
    }

    public void generarManzana() {
        Random manzanaAleatoria = new Random();
        manzanaX = manzanaAleatoria.nextInt(NUM_BLOCKS_WIDE - 1) + 1;
        manzanaY = manzanaAleatoria.nextInt(numBlocksAltura - 5) + 1;
    }

    public void comerManzana() {
        tamanoSerpiente++;

        generarManzana();
        generarEnemigo();

        draw();
        puntuacion += 1;
        mediaPlayer.pause();
        soundPool.play(soundId, 1, 1, 1, 0, 1);
        mediaPlayer.start();
    }


    public boolean serpienteMuerta() {

        boolean muerta = false;

        if (ejeSerpienteXs[0] == 0) muerta = true;
        if (ejeSerpienteXs[0] >= NUM_BLOCKS_WIDE-1) muerta = true;
        if (ejeSerpienteXy[0] == 0) muerta = true;
        if (ejeSerpienteXy[0] == numBlocksAltura - 5) muerta = true;

        for (int i = tamanoSerpiente - 1; i > 0; i--) {
            if ((i > 4) && (ejeSerpienteXs[0] == ejeSerpienteXs[i]) && (ejeSerpienteXy[0] == ejeSerpienteXy[i])) {
                muerta = true;
                //dialog.show();
            }
        }
        return muerta;

    }

    public void update() {
        if (ejeSerpienteXs[0] == manzanaX && ejeSerpienteXy[0] == manzanaY) {
            comerManzana();
        }

        if ((enemigoX - 1) == manzanaX && (enemigoY - 1) == manzanaY) {
            jugando=false;
        }

        // moverSerpiente(event);

        if (serpienteMuerta()) {
            mediaPlayer.pause();
            soundPool.play(soundChoque, 1, 1, 0, 0, 1);
            jugando=false;
        }

    }

    public void draw() {
        if (surfaceHolder.getSurface().isValid()) {


            canvas = surfaceHolder.lockCanvas();

            //Color de fondo
            canvas.drawColor(Color.rgb(255, 250, 205));

            //Color Texto puntuacion
            paint.setColor(Color.rgb(75, 0, 130));

            paint.setTextSize(90);
            canvas.drawText("Puntuacion:" + puntuacion, 10, 70, paint);

            //Color serpiente
            paint.setColor(Color.rgb(0, 0, 255));

            for (int i = 0; i < tamanoSerpiente; i++) {
                canvas.drawRect(ejeSerpienteXs[i] * blockSize,
                        (ejeSerpienteXy[i] * blockSize),
                        (ejeSerpienteXs[i] * blockSize) + blockSize,
                        (ejeSerpienteXy[i] * blockSize) + blockSize,
                        paint);
            }


            paint.setColor(Color.argb(255, 255, 0, 0));


            canvas.drawRect(manzanaX * blockSize,
                    (manzanaY * blockSize),
                    (manzanaX * blockSize) + blockSize,
                    (manzanaY * blockSize) + blockSize,
                    paint);

            paint.setColor(Color.rgb(62, 39, 35));
            canvas.drawRect(enemigoX * blockSize,
                    (enemigoY * blockSize),
                    (enemigoX * blockSize) - blockSize,
                    (enemigoY * blockSize) - blockSize,
                    paint);
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public boolean actualizar() {
        if (nextFrameTime <= System.currentTimeMillis()) {
            nextFrameTime = System.currentTimeMillis() + MILLIS_PER_SECOND / FPS;
            return true;
        }
        return false;
    }

    public void moverSerpiente(SensorEvent event) {
        int x = Math.round(event.values[0]);
        int y = Math.round(event.values[1]);

        for (int i = tamanoSerpiente; i > 0; i--) {
            ejeSerpienteXs[i] = ejeSerpienteXs[i - 1];
            ejeSerpienteXy[i] = ejeSerpienteXy[i - 1];
        }
        //Toast.makeText(getContext()," "+pantallaY,Toast.LENGTH_SHORT).show();

        //Izquierda
        if (x < 1 && ejeSerpienteXs[0] <NUM_BLOCKS_WIDE-1/*- 1041*/) {
            ejeSerpienteXs[0] = (ejeSerpienteXs[0] + 1);
        }
        //Derecha
        if (x > -1 && ejeSerpienteXs[0] > 0) {
            ejeSerpienteXs[0] = (ejeSerpienteXs[0] - 1);

        }
        //Arriba
        if (y > 1 && ejeSerpienteXy[0] < numBlocksAltura-5/* - 2162*/) {
            ejeSerpienteXy[0] = (ejeSerpienteXy[0] + 1);

        }
        //Arriba
        if (y < -1 && ejeSerpienteXy[0] > 0) {
            ejeSerpienteXy[0] = (ejeSerpienteXy[0] - 1);

        }

        //enemigo
        //Izquierda
        if (x < 1 && enemigoX < NUM_BLOCKS_WIDE /*- 1041/*- 1041*/) {
            enemigoX = (enemigoX + 1);
        }
        //Derecha
        if (x > -1 && enemigoX > 1) {
            enemigoX = (enemigoX - 1);

        }
        //Arriba
        if (y < -1 && enemigoY > 1) {
            enemigoY = (enemigoY - 1);

        }
        //Abajo
        if (y > 1 && enemigoY < numBlocksAltura-4 /*- 2129/* - 2162*/) {
            enemigoY = (enemigoY + 1);
        }


        update();
    }
}
