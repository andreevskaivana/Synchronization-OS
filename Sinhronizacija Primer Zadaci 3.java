package AV3ZADACI;

import java.util.concurrent.Semaphore;

public class FinkiToilet {

    public static class Toilet {
        //spodelen resurs ni e toalet
        public void vlezi() {
            System.out.println("Vleguva...");
        }

        public void izlezi() {
            System.out.println("Izleguva..");
        }

        //inicijalizacija na semafori koi ke gi koristime
        static Semaphore toiletSem;
        static Semaphore mLock;//gi zaklucuva promenlivite za kolku vleguvaat vo redicite
        static Semaphore zLock; //zaklucuva za kolku moze da pristapat do promenlivata so znaci cekanje vo redic

        static int numM; //redica za mazi
        static int numZ; //redica za zeni

        public static void init() {
            toiletSem = new Semaphore(1); //samo 1 moze da pristapi do toaletot
            mLock = new Semaphore(1);
            zLock = new Semaphore(1); //locks imaat po 1 privlegija vo eden moment
            numM = 0;
            numZ = 0;
        }


        public static class Man extends Thread {
            private Toilet toilet; //spodelen resurs

            public Man(Toilet toilet) {
                this.toilet = toilet;
            }

            public void enter() throws InterruptedException {
                mLock.acquire(); //dali nekoj dr ima pristap do toaletot
                //mLock e za site mazi i se dodeka ovaa promenliva ne se oslobi nemoze dr da vleze vo toaletot i da napravi povik na metodot vlezi
                //treba da proverime dali nekoj ceka vo redica za toalet
                if (numM == 0) {
                    //proveruvame dali toaletot e tekovno prazen na vleguva nekoj dr slucajno i izvestuvame
                    toiletSem.acquire();
                }
                //stom vleguvame vo toaletot go zgolemnuvame br na mazi so cekaat vo redica
                numM++; //site ostnati koi doagaat ke znaat deka veke nekoj e vo toaletot
                this.toilet.vlezi(); //vleguvame vo toaletot + mora ova da e sinrhonizirano so dr oti ne e atomicna operacija
                mLock.release();
            }

            public void exit() {
                //se naogame vo toalet i sakame da izlzeme+sto se treba da napravime
                //mora prvo da se povika metodot izlezi
                this.toilet.izlezi();
                //promenlivata koja oznacuva cekanje vo red se namaluva
                numM--;
                //ako nemoj nikoj so ceka vo redot treba da gi izvestime zenite so cekaat od dr strana deka mozt da vlezat vo spodeleniot resurs
                //ova go pravime taka sto go oslobudvame zaednikot semafor
                if (numM == 0) {
                    //ako nema nikoj red e na zenite
                    toiletSem.release(); //go osloboduvame toaletot t.e zaednickot resurs za koristenje
                }
                mLock.release();
            }

            public void run() {
                super.run();
            }
        }

        public static class Woman extends Thread {
            private Toilet toilet;

            public Woman(Toilet toilet) {
                this.toilet = toilet;
            }

            public void enter() throws InterruptedException {
                zLock.acquire();
                if (numZ == 0) {
                    toiletSem.acquire();
                }
                numZ++;
                this.toilet.vlezi();
                zLock.release();
            }

            public void exit() {
                this.toilet.izlezi();
                numZ--;
                if (numZ == 0) {
                    toiletSem.release();
                }
                zLock.release();
            }

            public void run() {
                super.run();
            }
        }
    }
}