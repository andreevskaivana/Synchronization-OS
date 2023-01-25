package AV3ZADACI;

import java.util.concurrent.Semaphore;

public class SiO2 {
    public static int NUM_RUN = 50;
    static Semaphore si; //semafor za sicilium
    static Semaphore o; // semafor za kislorod
    static Semaphore siHere; //semafor koj najavuva prisustvo na sicilium
    static Semaphore oHere; //semafor koj najavuva priustvo na kislorod
    static Semaphore ready;

    public static void init() {
        //inicijalizacija na semaforite
        si = new Semaphore(1); //inicijalna vrednost ima 1 oti treba da zeme 1 atom od sicilium
        o = new Semaphore(2); //2 permisii oti ima 2 atomi kislorod
        siHere = new Semaphore(0);
        oHere = new Semaphore(0);
        ready = new Semaphore(0);
        //dr semafori nemaat permisii oti tie se aktiviraat vo dr scenario
    }

    public static class Si extends Thread {
        public void bond() {
            System.out.println("Si is bonding now");
        }

        public void run() {
            for (int i = 0; i < NUM_RUN; i++) {
                try {
                    execute();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void execute() throws InterruptedException {
            //koga se startuva klasa si mora da se dobie prvo notifikacija od semaforot si
            si.acquire();
            //sleden semafor e siHere
            //siHere.release();
            //oslobduvame i prakame poraka so siHere
            //siHere.release();
            //pravime 2 releasese za dvata posebni releasese na 2 atoma kislorod no moze da se skrati ova so
            siHere.release(2);
            oHere.release(2);
            //ohere ne izvestuva za dali e pristinat nekoj atom i go ktivirame
            //cekame 2 pati da dobieme poraka za 2 atoma
            ready.release(2);
            //2 poraki za 2 atoma na kislorod
            bond();
            si.release(); //sme zavrsile so implementacija na si i gi oslobudvame resusite
        }
    }

    public static class O extends Thread {
        public void execute() throws InterruptedException {
        //cekame prvo notifikacija za da se izvlece eden atom od kislirod
            o.acquire(1); //cekame 1 dozvola od si za da prodolzime natamu
            siHere.acquire(1); //precekuvame atom od si
            oHere.release();
            ready.acquire(); //signal od strana na koordinator na sicilium za da pocneme so povik bond
            bond();
            o.release();
        }

        public void bond() {
            System.out.println("O is bonding now");
        }

        public void run() {
            for (int i = 0; i < NUM_RUN; i++) {
                try {
                    execute();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
