import java.lang.Math;

class Monitor {
    private int leit, escr;  
    
    //Construtor
    Monitor() { 
       this.leit = 0; //leitores lendo (0 ou mais)
       this.escr = 0; //escritor escrevendo (0 ou 1)
    }
    
    //Entrada para leitores
    public synchronized void EntraLeitor (String tipo, int id) {

        try { 
            while (this.escr > 0) {
                System.out.println (tipo + " Bloqueado ("+id+")");
                wait();  //bloqueia pela condicao logica da aplicacao 
            }

            this.leit++;  //registra que ha mais um leitor lendo
            System.out.println (tipo + " Lendo ("+id+")");
        } catch (InterruptedException e) { }
    }
    
    //Saida para leitores
    public synchronized void SaiLeitor (String tipo, int id) {
       this.leit--; //registra que um leitor saiu
       if (this.leit == 0) 
            this.notify(); //libera escritor (caso exista escritor bloqueado)
       System.out.println (tipo + " Saindo ("+id+")");
    }
    
    //Entrada para escritores
    public synchronized void EntraEscritor (String tipo, int id) {
      try { 
        while ((this.leit > 0) || (this.escr > 0)) {
           System.out.println (tipo + " Bloqueado ("+id+")");
           wait();  //bloqueia pela condicao logica da aplicacao 
        }
        this.escr++; //registra que ha um escritor escrevendo
        System.out.println (tipo + " Escrevendo ("+id+")");
      } catch (InterruptedException e) { }
    }
    
    //Saida para escritores
    public synchronized void SaiEscritor (String tipo, int id) {
       this.escr--; //registra que o escritor saiu
       notifyAll(); //libera leitores e escritores (caso existam leitores ou escritores bloqueados)
       System.out.println (tipo + " Saindo ("+id+")");
    }
}
  

//Thread Leitor
class Leitor extends Thread {
    int id; //identificador da thread
    int delay; //atraso bobo
    Monitor monitor;//objeto monitor para coordenar a lógica de execução das threads
    String tipo = new String("Leitor"); //variável que salva o tipo da thread
    boolean printCheck = false; //check para verificar se já houve print
  
    //Construtor
    Leitor (int id, int delayTime, Monitor m) {
        this.id = id;
        this.delay = delayTime;
        this.monitor = m;
    }
  
    //Método executado pela thread
    public void run () {
        try {
            for (;;) {
                this.monitor.EntraLeitor(this.tipo, this.id);

                if (Main.var == 0 || Main.var == 1){
                    System.out.println("A variável não é um número primo!");
                    printCheck = true;         
                }

                if (!printCheck){
                    for (int i = 2; i < (Math.floor((Main.var)/2)); i++){
                        if (Main.var % i == 0){
                            System.out.println("A variável não é um número primo!");
                            printCheck = true;
                            break;
                        }
                    }
                }

                if(!printCheck) {
                    System.out.println("A variável é um número primo!");
                }

                this.monitor.SaiLeitor(this.tipo, this.id);
                sleep(this.delay); 
            }
        } catch (InterruptedException e) { return; }
    }
}

//Thread Escritor
class Escritor extends Thread {
    int id; //identificador da thread
    int delay; //atraso bobo...
    Monitor monitor; //objeto monitor para coordenar a lógica de execução das threads
    String tipo = new String("Escritor"); //variável que salva o tipo da thread
  
    //Construtor
    Escritor (int id, int delayTime, Monitor m) {
        this.id = id;
        this.delay = delayTime;
        this.monitor = m;
    }
  
    //Método executado pela thread
    public void run () {
        try {
            for (;;) {
                this.monitor.EntraEscritor(this.tipo, this.id);

                Main.var = this.id;

                System.out.println("Variável modificada para " + Main.var);

                this.monitor.SaiEscritor(this.tipo, this.id);
                sleep(this.delay); //atraso bobo...
            }

        } catch (InterruptedException e) { return; }
    }
}

//Thread Leitor/Escritor
class LeitorEscritor extends Thread {
    int id; //identificador da thread
    int delay; //atraso bobo
    Monitor monitor;//objeto monitor para coordenar a lógica de execução das threads
    String tipo = new String("Leitor/Escritor"); //variável que salva o tipo da thread
  
    //Construtor
    LeitorEscritor (int id, int delayTime, Monitor m) {
        this.id = id;
        this.delay = delayTime;
        this.monitor = m;
    }
  
    //Método executado pela thread
    public void run () {
        try {
            for (;;) {
                this.monitor.EntraLeitor(this.tipo, this.id);

                if (Main.var % 2 == 0){
                    System.out.println("Variável par! Valor: " + Main.var);
                } else {
                    System.out.println("Variável ímpar! Valor: " + Main.var);
                }

                this.monitor.SaiLeitor(this.tipo, this.id);

                this.monitor.EntraEscritor(this.tipo, this.id);

                Main.var = (Main.var * 2);

                System.out.println("Variável modificada para " + Main.var);

                this.monitor.SaiEscritor(this.tipo, this.id);
                sleep(this.delay); 
            }
        } catch (InterruptedException e) { return; }
    }
}
  

//Classe principal
class Main {
    static final int L = 4;     //Número de threads leitores
    static final int E = 3;     //Número de threads escritoras
    static final int LE = 2;    //Número de threads leitoras/escritoras

    static int var = 0; //Variável global que será manipulada
  
    public static void main (String[] args) {
        int i;
        Monitor monitor = new Monitor(); //Monitor (objeto compartilhado entre leitores e escritores)
        Leitor[] l = new Leitor[L]; //Threads leitores
        Escritor[] e = new Escritor[E]; //Threads escritores
        LeitorEscritor[] le = new LeitorEscritor[LE]; //Threads leitoras/escritores
      
        for (i=0; i<L; i++) {
            System.out.println("Leitor criado ("+i+")");
            l[i] = new Leitor(i+1, (i+1)*500, monitor);
            l[i].start(); 
        }

        for (i=0; i<E; i++) {
            System.out.println("Escritor criado ("+i+")");
            e[i] = new Escritor(i+1, (i+1)*500, monitor);
            e[i].start(); 
        }

        for (i=0; i<LE; i++) {
            System.out.println("Leitor/Escritor criado ("+i+")");
            le[i] = new LeitorEscritor(i+1, (i+1)*500, monitor);
            le[i].start(); 
       }
    }
}
  
