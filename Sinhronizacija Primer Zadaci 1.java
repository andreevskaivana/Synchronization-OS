package AV3ZADACI;

import javax.naming.ldap.Control;
import java.util.concurrent.Semaphore;

public class ProducerController {

    public static int NUM_RUN = 50;

    static Semaphore accessBuffer; //semafor koj go ogranicuva pristapot do baferot koj ni e spodelen resurs(kriticen domen)
    static Semaphore lock; //semafor koj go ogranicuva pristapot do promenilivata numChecks
    static Semaphore canCheck; //semafor koj proveruva dali samo 10 threada kako barano pravat proverka na odreden item,ni osigura deka 10 threda od klasata kontroler pravat proverka na baferot


    public static void init() {
        //da se inicijaliziraat site semafori
        accessBuffer = new Semaphore(1);
        //ima 1 dozvola na pocetokot bidejki vo 1 moment samo kontrolerot ili samo producerot moze da pristapi do baferot
        lock = new Semaphore(1);
        //bilo koj od kontrolerite moze samo vo 1 moment da pristapi do numCHecks i da pristapi do zastitentiot del t.e baferot
        canCheck = new Semaphore(10);
        //10 permisii oti 10 kontroleri moze da pristapuvaat
    }

    public static class Buffer {
        public int numChecks = 0;

        public void produce() {
            System.out.println("Producer is producing..");
        }

        public void check() {
            System.out.println("Controller is checking");
        }
    }

    public static class Prodcuer extends Thread {
        private final Buffer buffer;

        public Prodcuer(Buffer b) {
            this.buffer = b;
        }

        public void execute() throws InterruptedException {
            //sinhronizacija na metodot execute od producer koj go povikuva produce
            //spodeleniot resurs e zasitetn so semafo rkoj e zastiten so access buffer
            accessBuffer.acquire(); //cekame da dobieme poraka dali resursot e sloboden,koga ke doznaeme deka resursot e sloboden toa znaci deka producerot momentalno moze da go koristi baferot namesto kontroler
            //sega slobodno moze da povika metod koj proizveduva stavki(uloga na producer) bidejki se noaoga v baferot
            this.buffer.produce();
            //bufer e spodelena promenliva kade se naoga metodot produce zatoa prvo pristapuvame do baferot za preku nego da dojdeme do produce
            //otkako producerot zavrsuva so rabota toj treba da go oslobodi resursot za dr da rabotat so nego
            accessBuffer.release();
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

    public static class Controller extends Thread {
        private final Buffer buffer;

        public Controller(Buffer buffer) {
            this.buffer = buffer;
        }

        public void execute() throws InterruptedException {
            lock.acquire(); //ja stiteme num check
            //zavrsuvame so producer prodolzuvame so controller
            //numCHecks e vo buffer i zatoa preku buffer pristapuvame do nea
            //numchecks probveruva kolku se zainteresirani za baferot atm
            if (this.buffer.numChecks == 0) {
                //proveruvame za da se osigurame deka producerot ne planira/pravi nesto so baferot
                accessBuffer.acquire(); //cekame na access bufer da ne izvesti deka smeeme da vlezeme vo baferot
                this.buffer.check();
                //controler metod vo bafer kako produce sto e gore
            }
//            if(this.buffer.numChecks>=0){
//            //kontrolerite znaci rabotat sto znaci mozam da se vklucam u proverkata i nemora da proveruvam dali acccessbufer e otvoren
//            this.buffer.check();
//            }

            //ako e pogolemo od 0 direktno odi na check vaka e optimizran kodot
            //stom dobieme privilegija za baferot mora da ja zgolemime numCheck
            this.buffer.numChecks++; //da imame informacija kolku tekovni kontroler rabotat
            //mora da ja zastitime numcheck i ODI NAGORE mora da ja zaklucime / zastitieme so lock
            //zavrsivme so rabota ssega go osloboduvame lock semaforot
            lock.release();

            //proverka dali semaforot ni dozvoluva da vlezeme vo resursot oti treba da pazime dali preminuvame 10 vleguvanje

            canCheck.acquire();  //da sme sigurni dali e pogolem od 10 ili ne
            //otkako ke zavrsi ovaa proverka mora da napravime modifikacija na numcheck i da ja namalime vrednosta na kontroleri koi proveruvaat vo adden moment

            this.buffer.check();
            lock.acquire();
            this.buffer.numChecks--;

            //mora da proverume za what if situacija koga ke dojde posledniot kontroler
            if (this.buffer.numChecks == 0) {
                accessBuffer.release();
            }


            lock.release(); //stavame lock okolu numChecks oti taa ni e bitna promenliva koja mora da bide zastitena
            //canCheck.release(); //osloboduva resurs oti zavrsil so proverka
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
