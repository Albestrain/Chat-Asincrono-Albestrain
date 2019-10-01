
package server_messenger_aldo;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Handler;
import javax.swing.JOptionPane;

public class Server_Messenger_aldo {

    private static Set<String> names = new HashSet<>();
    private static Set<PrintWriter> writers = new HashSet<>();
    static HashMap<String, PrintWriter> conectados = new HashMap<String, PrintWriter>();
    //static HashMap<String, String> Bloqueados = new HashMap<String, String>();
    static ArrayList<String[]> Bloqueados = new ArrayList<>();
    
    public static void main(String[] args) throws Exception{
        
       System.out.println("The chat server is running... ");
        ExecutorService pool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(59001)) {
            while (true) {
                pool.execute(new Handler(listener.accept()) {});

            }
        }
        
    }
    
    private static class Handler implements Runnable{

        private String name, password;
        private Socket socket;
        private Scanner in;
        private PrintWriter out;
        

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    out.println("SUBMITNAME");
                    name = in.nextLine();
                    if (name == null) {
                        return;
                    }                 
                    synchronized (names) {
                        while(name.equals("") || name.startsWith(" ")){
                            out.println("ALERTASCampo vacio, vuelva a intentarlo");
                            out.println("SUBMITNAME");
                            name = in.nextLine();
                        }
                        
                        if(name.equals("null")){
                            out.println("CERRARCLIENTE");
                            break;
                        }
                        
                        BaseDeDatos tablas = new BaseDeDatos();
                        tablas.abrirConeccion();                       
                        if(tablas.UsuarioExistente(name)){
                            out.println("ALERTASBienvenido de nuevo "+name+", presione aceptar para continuar");
                            //Existe este usuario en la base de datos
                            out.println("SUBMITCONTRASENA");
                            password = in.nextLine();
                            if(password.equals("null")){
                                tablas.cerrarConeccion();
                                run();
                            }
                            while(!tablas.contraseñaCorrecta(name, password)){
                                out.println("ALERTASPASSWORD INCORRECTO, VUELVA A INTENTARLO");
                                out.println("SUBMITCONTRASENA");
                                password = in.nextLine();
                            }
                            if(tablas.contraseñaCorrecta(name, password)){
                                names.add(name);   
                                conectados.put(name, out);   
                                tablas.cerrarConeccion();
                                break;
                            }

                        }else{
                            out.println("ALERTASBienvenido a MessengerAldo, para registrarse presione aceptar y elija un password seguro (Más de 4 caracteres)");
                            //No existe en la base de datos<
                            out.println("SUBMITCONTRASENA");
                            password = in.nextLine();
                            System.out.println(password);
                            while(!password.equals("null") && password.length() < 5 || password.equals("") || password.startsWith(" ")){
                                out.println("ALERTASPassword muy débil, vuelva a intentarlo (Asegúrese que sea mayor a 4 caracteres y no meter una contraseña vacia)");
                                out.println("SUBMITCONTRASENA");
                                password = in.nextLine();
                            }
                            
                            if(password.equals("null")){
                                tablas.cerrarConeccion();
                                run();
                            }
                            
                            if(password.length() >= 5 && !password.equals("")){
                                tablas.insertarUsuario(name, password);
                                names.add(name);   
                                conectados.put(name, out); 
                                tablas.cerrarConeccion();
                                break;    
                            }
                                                       
                        }
                        tablas.cerrarConeccion();
                    }
                }
                //le digo a todos que se ha conectado un usuario
                out.println("NAMEACCEPTED " + name);
                for (PrintWriter writer : writers) {
                    writer.println("MESSAGE " + name + " has joined");
                }
                      
                writers.add(out);

                while (true) {
                    String input = in.nextLine();
                    if (input.toLowerCase().startsWith("/quit")) {
                        return;
                    }else if (input.toLowerCase().startsWith("/privado")) {
                        String[] user = input.split(" ");
                        String mensaje = "";
                        for(int i = 2; i < user.length; i++){
                            mensaje = mensaje + user[i] + " ";
                        }
                        if(user.length < 3){
                            conectados.get(name).println("MESSAGE Le faltan datos para poder acompletar la operación.");
                        }else{
                            boolean usuarioBloqueado = false;
                            if(user.length > 1){
                                if(conectados.containsKey(user[1])){
                                    BaseDeDatos tablas = new BaseDeDatos();
                                    tablas.abrirConeccion();
                                    if(!tablas.meTieneBloqueado(user[1], name)){
                                        conectados.get(user[1]).println("MESSAGE "+ name + ": " + mensaje);
                                        conectados.get(name).println("MESSAGE "+ name + ": " + mensaje); 
                                    }else{
                                        conectados.get(name).println("MESSAGE "+ name + ": " + mensaje + " -No le llegó-"); 
                                    }
                                    tablas.cerrarConeccion();
                                }else{
                                    System.out.println("El usuario no se encuentra conectado");
                                     conectados.get(name).println("MESSAGE " + user[1] + " no se encuentra conectado");
                                }
                            }else{
                                System.out.println("No se ha podido acompletar la operación, hacen falta datos");
                                conectados.get(name).println("MESSAGE "+"No se ha podido acompletar la operación, hacen falta datos");
                            }
                        }
                    }else if(input.toLowerCase().startsWith("/bloquear")){
                        String[] user = input.split(" ");
                        if(user.length >= 2){
                            if(!name.equals(user[1])){
                                BaseDeDatos tablas = new BaseDeDatos();
                                tablas.abrirConeccion();
                                if(tablas.UsuarioExistente(user[1])){
                                    System.out.println("existe");
                                    tablas.insertarBloqueados(name, user[1]);
                                    System.out.println("si insert");
                                    conectados.get(name).println("MESSAGE "+"Has bloqueado a " + user[1]); 
                                }else{
                                    conectados.get(name).println("MESSAGE " + user[1] + " no existe.");
                                }
                                tablas.cerrarConeccion();
                            }else{
                                conectados.get(name).println("MESSAGE "+"No intente bloquearse usted mismo :v");
                            }
                            
                        }else{
                            conectados.get(name).println("MESSAGE "+"No se ha podido acompletar la operación, hacen falta datos");
                            System.out.println("No se ha podido acompletar la operación, hacen falta datos");
                        }
                        
                        
                    }else if(input.toLowerCase().startsWith("/desbloquear")){
                         String[] user = input.split(" ");
                        if(user.length >= 2){
                            BaseDeDatos tablas = new BaseDeDatos();
                            tablas.abrirConeccion();
                            if(tablas.UsuarioExistente(user[1])){
                                if(tablas.meTieneBloqueado(name, user[1])){
                                    tablas.eliminarBloqueados(name, user[1]);
                                    conectados.get(name).println("MESSAGE "+"Has desbloqueado a " + user[1]); 
                                }else{
                                    conectados.get(name).println("MESSAGE "+"Usted no tiene bloqueado a "+user[1]);
                                }
                            }else{
                                conectados.get(name).println("MESSAGE " + user[1] + " no existe.");
                            }
                            tablas.cerrarConeccion();
                        }else{
                            System.out.println("No se ha podido acompletar la operación, hacen falta datos");
                            conectados.get(name).println("MESSAGE "+"No se ha podido acompletar la operación, hacen falta datos");
                        }
                        
                    }else{
                        if(input.startsWith(" ") || input.equals("")){
                            conectados.get(name).println("MESSAGE escriba algo");
                        }else{
                            ArrayList<String> clientes = new ArrayList<>();                      
                            for ( String key : conectados.keySet() ) {                           
                                clientes.add((String)key + "");                         
                            }

                            int lugar = -1;

                            for (PrintWriter writer : conectados.values()) {
                                lugar++;

                                BaseDeDatos tablas = new BaseDeDatos();
                                tablas.abrirConeccion();
                                if(!tablas.meTieneBloqueado(clientes.get(lugar), name)){
                                    writer.println("MESSAGE " + name + ": " + input);
                                }
                                tablas.cerrarConeccion();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                if (out != null) {
                    writers.remove(out);
                }
                if (name != null) {
                    System.out.println(" is leaving");
                    names.remove(name);
                    for(PrintWriter writer:writers){
                        writer.println("MESSAGE "+name+" has left");
                    }
                }
                try{
                    socket.close();
                } catch(IOException e){
                }
                }
            }

        }
    
}
